package com.xaridar.notable.models;

import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.xaridar.notable.R;
import com.xaridar.notable.events.QueryListener;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Note {

    private static final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private static final DatabaseReference notesRef = db.getReference("notes");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, uuuu hh:mm:ss a");

    private final String content;
    private final String id;
    private final String username;
    private final String userid;
    private final Uri userAvatar;
    private final LatLng loc;
    private final LocalDateTime dateTime;

    public Note(String id, String content, String username, String userid, Uri userAvatar, LatLng loc, LocalDateTime dateTime) {
        this.content = content;
        this.id = id;
        this.username = username;
        this.userid = userid;
        this.userAvatar = userAvatar;
        this.loc = loc;
        this.dateTime = dateTime;
    }

    public static void asyncNoteFromId(String id, QueryListener<Note> callback) {
        DatabaseReference childRef = notesRef.child(id);
        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                childRef.removeEventListener(this);
                if (!snapshot.exists()) {
                    callback.onQueryError(Resources.getSystem().getString(R.string.note_not_found));
                    return;
                }
                String userId = Objects.requireNonNull(snapshot.child("userid").getValue()).toString();
                String content = Objects.requireNonNull(snapshot.child("content").getValue()).toString();
                String location = Objects.requireNonNull(snapshot.child("location").getValue()).toString();
                LocalDateTime dateTime = LocalDateTime.parse(Objects.requireNonNull(snapshot.child("datetime").getValue()).toString(), dateTimeFormatter).atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                LatLng locLatLng = new LatLng(Double.parseDouble(location.split(",")[0]), Double.parseDouble(location.split(",")[1]));
                DatabaseReference userRef = db.getReference("users/" + userId);
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot userSnapshot) {
                        userRef.removeEventListener(this);
                        String username;
                        if (!userSnapshot.exists()) {
                            username = "[deleted user]";
                            Uri avatar = Uri.EMPTY;
                            callback.onQueryResult(new Note(id, content, username, userId, avatar, locLatLng, dateTime));
                        } else {
                            username = Objects.requireNonNull(userSnapshot.child("username").getValue()).toString();
                            FirebaseStorage.getInstance().getReference("avatars/" + userId).getDownloadUrl().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Uri avatar = task.getResult();
                                        callback.onQueryResult(new Note(id, content, username, userId, avatar, locLatLng, dateTime));
                                    }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        userRef.removeEventListener(this);
                        callback.onQueryError(Resources.getSystem().getString(R.string.query_cancelled));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                childRef.removeEventListener(this);
                callback.onQueryError(Resources.getSystem().getString(R.string.query_cancelled));
            }
        });
    }

    public String getUsername() {
        return username;
    }

    public Uri getUserAvatar() {
        return userAvatar;
    }

    public String getNoteId() {
        return id;
    }

    public LatLng getLocation() {
        return loc;
    }

    public String getNoteContent() {
        return content;
    }

    public String getUserid() {
        return userid;
    }

    public void addToDB() {
        DatabaseReference noteRef = notesRef.child(id);
        noteRef.child("content").setValue(content);
        noteRef.child("userid").setValue(userid);
        noteRef.child("location").setValue(loc.latitude + "," + loc.longitude);
        LocalDateTime nowInUtc = dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        noteRef.child("datetime").setValue(dateTimeFormatter.format(nowInUtc));
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
