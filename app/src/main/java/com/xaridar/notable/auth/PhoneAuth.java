package com.xaridar.notable.auth;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.xaridar.notable.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PhoneAuth implements FirebaseInterface {

    private static PhoneAuthOptions options;

    public static void authenticate(String phoneNum, Activity a, PhoneAuthProvider.OnVerificationStateChangedCallbacks cb) {
        options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNum)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(a)
                        .setCallbacks(cb)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public static void reVerify() {
        if (options == null) return;
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public static void signIn(Context ctx, PhoneAuthCredential cred, BiFunction<FirebaseUser, String, Void> callback) {
        mAuth.signInWithCredential(cred).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference ref = db.getReference("users/" + Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                ref.child("username").setValue("");
                callback.apply(mAuth.getCurrentUser(), null);
            } else {
                Log.w("OTP", "Failed to sign in user.");
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    callback.apply(null, ctx.getString(R.string.invalid_code));
                } else {
                    callback.apply(null, Objects.requireNonNull(task.getException()).getMessage());
                }
            }
        });
    }
}
