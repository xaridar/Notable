package com.xaridar.notable.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.common.SignInButton;
import com.xaridar.notable.R;
import com.xaridar.notable.app.MainActivity;
import com.xaridar.notable.auth.GoogleAuth;
import com.xaridar.notable.auth.UserInfo;
import com.xaridar.notable.common.Utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Void> googleSignIn = GoogleAuth.getSignIn(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        GoogleAuth.create(getApplication());
        if (UserInfo.userExists()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.activity_home);
        SignInButton googleBtn = findViewById(R.id.googleSignIn);
        googleBtn.setOnClickListener(v -> googleClick());
        for (int i = 0; i < googleBtn.getChildCount(); i++) {
            if (googleBtn.getChildAt(i) instanceof TextView) {
                TextView e = (TextView) googleBtn.getChildAt(i);
                e.setTextSize(24);
            }
        }
        Button phoneBtn = findViewById(R.id.phoneSignIn);
        phoneBtn.setOnClickListener(v -> phoneClick());
        Button signIn = findViewById(R.id.signUpBtn);
        signIn.setOnClickListener(v -> signUp());
        Button login = findViewById(R.id.loginBtn);
        login.setOnClickListener(v -> login());
        Utils.addActivityToFinish(this);
    }

    public void signUp() {
        startActivity(new Intent(this, EmailSignupActivity.class));
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.none);
    }

    public void login() {
        startActivity(new Intent(this, EmailLoginActivity.class));
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.none);
    }

    public void googleClick() {
        googleSignIn.launch(null);
    }

    public void phoneClick() {
        startActivity(new Intent(this, PhoneAuthActivity.class));
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.none);
    }

    @Override
    public void finish() {
        Utils.finishActivity(this);
        super.finish();
    }
}