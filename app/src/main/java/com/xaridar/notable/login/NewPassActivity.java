package com.xaridar.notable.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.xaridar.notable.R;
import com.xaridar.notable.app.MainActivity;
import com.xaridar.notable.common.PasswordToggleLayout;

import org.jetbrains.annotations.NotNull;

public class NewPassActivity extends AppCompatActivity {

    String code;
    private boolean abort = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        validateUser(data);
    }

    private void validateUser(Uri data) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (data == null) {
            Log.w("resetPass", "No URI specified");
            finish();
            abort = true;
            return;
        }
        Log.i("resetPass", data.getPath() + ", " + data.getEncodedQuery());
        if (data.getQueryParameter("oobCode") == null || data.getQueryParameter("mode") == null) {
            Log.w("resetPass", "Incomplete Url; aborting");
            finish();
            abort = true;
            return;
        }
        String method = data.getQueryParameter("mode");
        if (!method.equals("resetPassword")) {
            Log.w("resetPass", "Bad method; aborting");
            finish();
            abort = true;
            return;
        }
        code = data.getQueryParameter("oobCode");
        auth.verifyPasswordResetCode(data.getQueryParameter("oobCode")).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("resetPass", "Error validating code: " + (task.getException() != null ? task.getException().getMessage() : "canceled") + ", " + task.getException().getClass());
                if (task.getException() != null) {
                    String msg = "";
                    if(task.getException() instanceof FirebaseAuthActionCodeException) {
                        msg = "Validation code is either invalid or expired. Please send a new reset password email.";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                abort = true;
                finish();
            } else {
                Log.i("resetPass", "Valid link");
                showUI();
            }
        });
    }

    private void showUI() {
        setContentView(R.layout.activity_new_pass);
        TextView error = findViewById(R.id.resetPasswordError);
        PasswordToggleLayout ptl = findViewById(R.id.resetPasswordTxt);
        Button btn = findViewById(R.id.resetPasswordBtn);

        ptl.setOnKeyListener((view, i, keyEvent) -> {
            error.setText("");
            return false;
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        btn.setOnClickListener(v -> auth.confirmPasswordReset(code, ptl.getText().toString())
                .addOnFailureListener(e -> error.setText(e.getMessage()))
                .addOnSuccessListener(unused -> {
                    startActivity(new Intent(this, EmailLoginActivity.class));
                    finish();
                })
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!abort) return;
//        startActivity(new Intent(this, HomeActivity.class));
//        finish();
    }
}