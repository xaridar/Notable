package com.xaridar.notable.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.xaridar.notable.R;

import org.jetbrains.annotations.NotNull;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailTxt;
    Button sendBtn;
    TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emailTxt = findViewById(R.id.forgotPasswordEmail);
        sendBtn = findViewById(R.id.forgotPasswordSend);
        error = findViewById(R.id.forgotPasswordError);

        emailTxt.setOnKeyListener((view, i, keyEvent) -> {
            error.setText("");
            return false;
        });
        sendBtn.setOnClickListener(v -> sendLink());
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void sendLink() {
        String email = emailTxt.getText().toString();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnFailureListener(e -> error.setText(e.getMessage()))
                .addOnSuccessListener(unused -> Toast.makeText(this, getString(R.string.reset_sent, email), Toast.LENGTH_SHORT).show());
    }
}