package com.xaridar.notable.auth;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.xaridar.notable.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

public class EmailAuth implements FirebaseInterface {

    public static void signUp(String email, String password, String username, Activity ctx, BiFunction<FirebaseUser, String, Void> callback) {
        DatabaseReference usersRef = db.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                usersRef.removeEventListener(this);
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.hasChild("username")) {
                        if (Objects.requireNonNull(child.child("username").getValue()).toString().equals(username)) {
                            callback.apply(null, ctx.getString(R.string.username_in_use));
                            return;
                        }
                    }
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ctx, task -> {
                    if (task.isSuccessful()) {
                        // Add user
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null)
                            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build()).addOnCompleteListener(ctx, updateTask -> {
                                if (task.isSuccessful()) {
                                    DatabaseReference ref = db.getReference("users/" + mAuth.getCurrentUser().getUid());
                                    ref.child("username").setValue(username);
                                    callback.apply(mAuth.getCurrentUser(), null);
                                } else {
                                    callback.apply(null, Objects.requireNonNull(task.getException()).getMessage());
                                }
                            });
                        callback.apply(mAuth.getCurrentUser(), null);
                    } else {

                        callback.apply(null, Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                usersRef.removeEventListener(this);
                callback.apply(null, error.getMessage());
            }
        });
    }

    public static void logIn(String email, String password, Activity ctx, BiFunction<FirebaseUser, String, Void> callback) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(ctx, task -> {
            if (task.isSuccessful()) {
                callback.apply(mAuth.getCurrentUser(), null);
            } else {
                callback.apply(null, Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }
}
