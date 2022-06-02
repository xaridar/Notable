package com.xaridar.notable.auth;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.xaridar.notable.R;
import com.xaridar.notable.app.MainActivity;
import com.xaridar.notable.common.Utils;
import com.xaridar.notable.login.ChooseUsernameActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

import kotlin.jvm.functions.Function3;

public class GoogleAuth implements FirebaseInterface {
    public static void authenticate(String idToken, Activity ctx, Function3<FirebaseUser, Exception, Boolean, Void> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(ctx, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currUser = UserInfo.getUser();
                        DatabaseReference ref = db.getReference("users");
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                ref.removeEventListener(this);
                                boolean inUse = false;
                                if (!snapshot.hasChild(currUser.getUid())) {
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        if (child.hasChild("username")) {
                                            if (Objects.requireNonNull(child.child("username").getValue()).toString().equals(currUser.getDisplayName())) {
                                                inUse = true;
                                            }
                                        }
                                    }
                                    if (!inUse)
                                        ref.child(currUser.getUid()).child("username").setValue(currUser.getDisplayName());
                                    else {
                                        Toast.makeText(ctx, R.string.google_un_in_use, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                callback.invoke(currUser, null, inUse);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                ref.removeEventListener(this);
                                callback.invoke(null, new Exception(ctx.getString(R.string.an_error_occurred)), null);
                            }
                        });
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("GoogleAuth", "signInWithCredential:failure", task.getException());
                        callback.invoke(null, task.getException(), null);
                    }
                });
    }

    public static GoogleSignInOptions gso;
    public static Intent signInIntent;

    public static void create(Application app) {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(app.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(app, gso);
        signInIntent = mGoogleSignInClient.getSignInIntent();
    }

    public static ActivityResultLauncher<Void> getSignIn(AppCompatActivity a) {

        return a.registerForActivityResult(new ActivityResultContract<Void, Intent>() {
            @NonNull
            @NotNull
            @Override
            public Intent createIntent(@NonNull @NotNull Context context, Void input) {
                return signInIntent;
            }

            @Override
            public Intent parseResult(int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent intent) {
                return intent;
            }
        }, intent -> {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authenticate(account.getIdToken(), a, (user, e, inUse) -> {
                    if (user == null) {
                        Toast.makeText(a, a.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    Intent i;
                    if (inUse) {
                        i = new Intent(a, ChooseUsernameActivity.class);
                        i.putExtra("cancelable", false);
                        i.putExtra("back", false);
                    } else {
                        i = new Intent(a, MainActivity.class);
                    }
                    Utils.finishAll();
                    a.startActivity(i);
                    a.finish();
                    if (inUse) a.overridePendingTransition(R.anim.slide_in_right, R.anim.none);
                    return null;
                });
            } catch (ApiException e) {
                Log.w("GoogleAuth", "Google sign in failed", e);
            }
        });
    }
}