package com.xaridar.notable.auth;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;
import java.util.function.Function;

public class UserInfo implements FirebaseInterface {
    public static void updateCurrPfp(Uri uri, Function<Boolean, Void> callback, OnCompleteListener<Void> updateCallback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.apply(false);
            return;
        }
        StorageReference avatarsRef = storage.getReference("avatars");
        StorageReference userRef = avatarsRef.child(user.getUid());
        if (uri != null) {
            UploadTask task = userRef.putFile(uri);
            task.continueWithTask(task1 -> {
                if (!task1.isSuccessful()) {
                    throw Objects.requireNonNull(task1.getException());
                }

                return userRef.getDownloadUrl();
            }).addOnFailureListener(e -> {
                Log.w("Storage", "Upload failed.");
                callback.apply(true);
            }).addOnSuccessListener(taskSnapshot -> {
                Log.i("Storage", "Success: " + taskSnapshot);
                user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(taskSnapshot).build()).addOnCompleteListener(updateCallback);
//                db.getReference("users/" + user.getUid()).child("pfp").setValue(taskSnapshot.toString());
                callback.apply(true);
            });
        } else {
            Task<Void> task = userRef.delete();
            task.addOnFailureListener(e -> {
                Log.w("DelStorage", "Deletion failed.");
                callback.apply(true);
            }).addOnSuccessListener(taskSnapshot -> {
                Log.i("DelStorage", "Success: " + taskSnapshot);
                user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(null).build());
//                db.getReference("users/" + user.getUid()).child("pfp").setValue("");
                callback.apply(true);
            });
        }
    }

    public static void logout() {
        mAuth.signOut();
    }

    public static boolean userExists() {
        return mAuth.getCurrentUser() != null;
    }

    public static FirebaseUser getUser() {
        return mAuth.getCurrentUser();
    }

    public static void deleteUser() {
        FirebaseUser user = getUser();
        Log.d("DeleteUser", "succeeded!");
        db.getReference("users/" + user.getUid()).removeValue();
        storage.getReference("avatars/" + user.getUid()).delete();
        user.delete();
    }
}
