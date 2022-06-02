package com.xaridar.notable.models;

import android.net.Uri;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

public class SerializableNote implements Serializable {

    private final String content;
    private final String id;
    private final String username;
    private final String userid;
    private final URI userAvatar;
    private final double latitude;
    private final double longitude;
    private final LocalDateTime dateTime;

    public SerializableNote(Note note) {
        URI ua;
        this.content = note.getNoteContent();
        this.id = note.getNoteId();
        this.username = note.getUsername();
        this.userid = note.getUserid();
        try {
            ua = new URI(note.getUserAvatar().toString());
        } catch (URISyntaxException e) {
            ua = null;
        }
        this.userAvatar = ua;
        this.latitude = note.getLocation().latitude;
        this.longitude = note.getLocation().longitude;
        this.dateTime = note.getDateTime();
    }

    public Note getNote() {
        return new Note(id, content, username, userid, userAvatar == null ? Uri.EMPTY : Uri.parse(userAvatar.toString()), new LatLng(latitude, longitude), dateTime);
    }
}
