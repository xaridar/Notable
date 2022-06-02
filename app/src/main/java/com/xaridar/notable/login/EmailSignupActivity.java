package com.xaridar.notable.login;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionValues;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xaridar.notable.R;
import com.xaridar.notable.auth.EmailAuth;
import com.xaridar.notable.common.PasswordToggleLayout;
import com.xaridar.notable.common.Utils;

import java.util.regex.Pattern;


public class EmailSignupActivity extends AppCompatActivity {

    private TextView error;
    private boolean pressed = false;
    private boolean activityStarted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_signup);
        error = findViewById(R.id.email_error);
        Button btn = findViewById(R.id.signup_btn);
        btn.setOnClickListener(v -> signUp());
    }

    public void signUp() {
        if (pressed) return;
        error.setText("");
        EditText usernameTxt = findViewById(R.id.signup_username);
        EditText emailTxt = findViewById(R.id.signup_email);
        PasswordToggleLayout passwordTxt = findViewById(R.id.signup_password);
        CharSequence username = usernameTxt.getText();
        CharSequence email = emailTxt.getText();
        CharSequence password = passwordTxt.getText();

        if (username.toString().equals("")) {
            error.setText(R.string.username_blank_error);
        }
        if (email.toString().equals("")) {
            if (error.getText() == "") {
                error.setText(R.string.email_blank_error);
            }
        }
        if (password.toString().equals("")) {
            if (error.getText() == "") {
                error.setText(R.string.password_blank_error);
            }
        }
        if (!Pattern.compile("^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$").matcher(email).find()) {
            if (error.getText() == "") {
                error.setText(R.string.invalid_email);
            }
        }

        if (error.getText() != "") return;

        EmailAuth.signUp(email.toString(), password.toString(), username.toString(), this, (user, e) -> {
            if (pressed) return null;
            if (user == null) error.setText(e);
            else {
                Log.i("signUp", String.format("Signed up! Username: %s", user.getDisplayName()));

                pressed = true;
                Intent i = new Intent(this, ChooseAvatarActivity.class);
                i.putExtra("skippable", true);
                i.putExtra("cancelable", false);
                i.putExtra("back", false);
                Utils.finishAll();
                startActivity(i);
                activityStarted = true;
                overridePendingTransition(R.anim.slide_in_right, R.anim.none);
                finish();
            }
            return null;
        });
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
