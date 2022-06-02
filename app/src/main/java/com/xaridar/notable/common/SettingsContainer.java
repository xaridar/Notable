package com.xaridar.notable.common;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.xaridar.notable.R;

import java.io.IOException;
import java.net.URL;

public class SettingsContainer {

    public static int DEFAULT_AVATAR_RESID = R.drawable.ic_default_avatar;
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    FirebaseUser user;
    public final MutableLiveData<Drawable> avatar = new MutableLiveData<>(null);
    public final MutableLiveData<String> name = new MutableLiveData<>("");

    public final MutableLiveData<Boolean> overrideAva = new MutableLiveData<>(false);

    private static SettingsContainer instance;

    private SettingsContainer(FirebaseUser user) {
        this.user = user;
    }

    public void refresh(Activity a) {
        if (this.avatar.getValue() == null)
            this.avatar.setValue(a.getDrawable(DEFAULT_AVATAR_RESID));

        if (overrideAva.getValue() != null && !overrideAva.getValue()) {
            if (user.getPhotoUrl() != null) {
                storage.getReference("avatars/" + user.getUid()).getDownloadUrl().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    Uri uri = task.getResult();

                    new Thread(() -> {
                        try {
                            Drawable d = BitmapDrawable.createFromStream(new URL(uri.toString()).openStream(), "bitmap");
                            a.runOnUiThread(() -> avatar.setValue(d));
                        } catch (IOException e) {
                            a.runOnUiThread(() -> avatar.setValue(a.getDrawable(DEFAULT_AVATAR_RESID)));
                            Log.e("SettingsCaching", "avatar error: " + e.toString());
                        }
                    }).start();
                });
            } else {
                this.avatar.setValue(a.getDrawable(DEFAULT_AVATAR_RESID));
            }
        }
        if (user.getDisplayName() != null)
            this.name.setValue(user.getDisplayName());
        else this.name.setValue("");
    }

    public static SettingsContainer getSettings(FirebaseUser user, Activity a) {
        if (instance == null || instance.user != user) {
            instance = new SettingsContainer(user);
        }
        instance.refresh(a);
        return instance;
    }

    public void setAvaFromLocalUri(Uri uri, Activity a) {
        if (uri != null) {
            new Thread(() -> {
                try {
                    Drawable drawable = Drawable.createFromStream(a.getContentResolver().openInputStream(uri), uri.toString());
                    a.runOnUiThread(() -> {
                        overrideAva.setValue(true);
                        this.avatar.setValue(drawable);
                    });
                } catch (IOException e) {
                    a.runOnUiThread(() -> this.avatar.setValue(a.getDrawable(DEFAULT_AVATAR_RESID)));
                    Log.e("SettingsCaching", "avatar error: " + e.toString());
                }
            }).start();
        } else {
            this.avatar.setValue(a.getDrawable(DEFAULT_AVATAR_RESID));
        }
    }
}
