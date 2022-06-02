package com.xaridar.notable.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.xaridar.notable.R;
import com.xaridar.notable.app.MainActivity;
import com.xaridar.notable.auth.PhoneAuth;
import com.xaridar.notable.common.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class PhoneAuthActivity extends AppCompatActivity {

    private ConstraintLayout sendLayout;
    private ConstraintLayout confLayout;
    private Button sendSMS;
    private CountryCodePicker ccp;
    private EditText phoneNum;
    private TextView sendError;

    private TextView sentOTP;
    private EditText[] digits;
    private TextView confError;
    private Button resendSMSButton;

    private int currDigit = 0;
    private boolean verifying = false;
    private boolean pressed = false;
    private String verId;
    private boolean pasted = false;
    private boolean activityStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        sendLayout = findViewById(R.id.sendLayout);
        confLayout = findViewById(R.id.confLayout);
        sendSMS = findViewById(R.id.confPhoneBtn);
        ccp = findViewById(R.id.ccp);
        phoneNum = findViewById(R.id.phoneNumField);
        sendError = findViewById(R.id.phoneNumError);

        sentOTP = findViewById(R.id.sentOTP);
        digits = new EditText[]{
                findViewById(R.id.digit1),
                findViewById(R.id.digit2),
                findViewById(R.id.digit3),
                findViewById(R.id.digit4),
                findViewById(R.id.digit5),
                findViewById(R.id.digit6)
        };
        resendSMSButton = findViewById(R.id.resendSMSButton);
        confError = findViewById(R.id.phoneNumError2);

        resendSMSButton.setOnClickListener(v -> showSendSMS());

        Arrays.stream(digits).forEach(digit -> {
            digit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                    if (pasted) return;
                    confError.setText("");
                    if (after == 1) {
                        singleCharacterInp(charSequence);
                    } else {
                        multiCharacterInp(charSequence);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            digit.setOnFocusChangeListener((v, b) -> {
                if (!b || digit == digits[currDigit]) return;
                currDigit = getFirstEmptyDigit();
                if (currDigit == -1) submitCode();
                else digits[currDigit].requestFocus();
            });
            digit.setOnKeyListener((view, i, keyEvent) -> {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (currDigit > 0) {
                        currDigit--;
                        digits[currDigit].setText("");
                        digits[currDigit].requestFocus();
                    }
                    return true;
                }
                return false;
            });
        });

        showSendSMS();

        ccp.registerCarrierNumberEditText(phoneNum);

        ccp.setAutoDetectedCountry(true);
        phoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!ccp.isValidFullNumber()) {
                    sendError.setText(R.string.invalid_number);
                } else {
                    sendError.setText("");
                }
            }
        });
        sendSMS.setOnClickListener(v -> {
            if (pressed) return;
            if (!ccp.isValidFullNumber()) return;
            String num = ccp.getFullNumberWithPlus();
            pressed = true;
            PhoneAuth.authenticate(num, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
                    verifying = false;
                    Log.i("PhoneAuth", "onVerificationCompleted");
                    PhoneAuth.signIn(PhoneAuthActivity.this, phoneAuthCredential, PhoneAuthActivity.this::signedIn);
                }

                @Override
                public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                    verifying = false;
                    Log.i("PhoneAuth", "onVerificationFailed");
                }

                @Override
                public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    verifying = false;
                    verId = s;
                    Log.i("PhoneAuth", "onCodeSent");
                    showCodeConfirm();
                }
            });
            verifying = true;
        });
    }

    private void submitCode() {
        Arrays.stream(digits).forEach(digit -> digit.setEnabled(false));
        Optional<String> submitted = Arrays.stream(digits).map(digit -> digit.getText().toString()).reduce((left, right) -> left + right);
        if (!submitted.isPresent()) {
            Log.w("OTP", "Something went wrong.");
            showSendSMS();
            return;
        }
        String submittedCode = submitted.get();
        PhoneAuthCredential cred = PhoneAuthProvider.getCredential(verId, submittedCode);
        PhoneAuth.signIn(this, cred, this::signedIn);
    }

    private Void signedIn(FirebaseUser u, String e) {
        if (u == null) {
//            showSendSMS();
            confError.setText(e);
            pasted = true;
            Arrays.stream(digits).forEach(digit -> {
                digit.setEnabled(true);
                digit.setText("");
            });
            pasted = false;
            currDigit = 0;
            digits[currDigit].requestFocus();
        } else if (u.getDisplayName() != null) {
            Intent i = new Intent(PhoneAuthActivity.this, MainActivity.class);
            Utils.finishAll();
            startActivity(i);
            activityStarted = true;
            finish();
        } else {

            Intent i = new Intent(PhoneAuthActivity.this, ChooseUsernameActivity.class);
            Utils.finishAll();
            i.putExtra("cancelable", false);
            i.putExtra("back", false);
            startActivity(i);
            activityStarted = true;
            overridePendingTransition(R.anim.slide_in_right, R.anim.none);
            finish();
        }
        return null;
    }

    private void showSendSMS() {
        phoneNum.setText("");
        sendError.setText("");
        sendLayout.setVisibility(View.VISIBLE);
        confLayout.setVisibility(View.GONE);
        phoneNum.requestFocus();
    }

    private void showCodeConfirm() {
        confError.setText("");
        Arrays.stream(digits).forEach(digit -> digit.setText(""));
        Arrays.stream(digits).forEach(digit -> digit.setEnabled(true));
        currDigit = 0;
        sentOTP.setText(getString(R.string.sent_OTP, ccp.getFormattedFullNumber()));
        sendLayout.setVisibility(View.GONE);
        confLayout.setVisibility(View.VISIBLE);
        digits[0].requestFocus();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putBoolean("verifying", verifying);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verifying = savedInstanceState.getBoolean("verifying");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (verifying) PhoneAuth.reVerify();
    }

    private void singleCharacterInp(CharSequence charSequence) {
        char chara = charSequence.charAt(0);
        if (Character.isDigit(chara)) {
            currDigit++;
            if (currDigit >= 6) {
                submitCode();
                return;
            }
            digits[currDigit].requestFocus();
        } else {
            digits[currDigit].setText("");
        }
    }

    private void multiCharacterInp(CharSequence charSequence) {
        if (charSequence.toString().matches("^\\d+$")) {
            if (charSequence.length() <= 6 - currDigit) {
                pasted = true;
                for (int i = 0; i < charSequence.length(); i++) {
                    digits[currDigit + i].setText(String.valueOf(charSequence.charAt(i)));
                }
                pasted = false;

                currDigit = getFirstEmptyDigit();
                if (currDigit == -1) submitCode();
                else digits[currDigit].requestFocus();
            }
        }
    }

    private int getFirstEmptyDigit() {
        for (int i = 0; i < 6; i++) {
            if (digits[i].getText().toString().equals("")) return i;
        }
        return -1;
    }

    @Override
    public void finish() {
        super.finish();
        if (!activityStarted)
            overridePendingTransition(R.anim.none, R.anim.slide_out_bottom);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}