package com.xaridar.notable.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xaridar.notable.R;
import com.xaridar.notable.app.MainActivity;
import com.xaridar.notable.auth.EmailAuth;
import com.xaridar.notable.common.PasswordToggleLayout;
import com.xaridar.notable.common.Utils;

public class EmailLoginActivity extends AppCompatActivity {

    private TextView error;
    private boolean pressed = false;
    private boolean activityStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        error = findViewById(R.id.login_email_error);
        Button btn = findViewById(R.id.login_btn);
        btn.setOnClickListener(v -> login());
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.none);
        });
        forgotPassword.setFocusable(true);
    }

    public void login() {
        if (pressed) return;
        error.setText("");
        EditText emailTxt = findViewById(R.id.login_email);
        PasswordToggleLayout passwordTxt = findViewById(R.id.login_password);
        CharSequence email = emailTxt.getText();
        CharSequence password = passwordTxt.getText();

        if (email.toString().equals("")) {
            error.setText(R.string.email_blank_error);
        }
        if (password.toString().equals("")) {
            if (error.getText() == "") {
                error.setText(R.string.password_blank_error);
            }
        }

        if (error.getText() != "") return;

        EmailAuth.logIn(email.toString(), password.toString(), this, (user, e) -> {
            if (pressed) return null;
            if (user == null) error.setText(e);
            else {
                Log.i("login", String.format("Logged in! Username: %s", user.getDisplayName()));
                pressed = true;

                Intent i = new Intent(this, MainActivity.class);
                Utils.finishAll();
                startActivity(i);
                activityStarted = true;
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