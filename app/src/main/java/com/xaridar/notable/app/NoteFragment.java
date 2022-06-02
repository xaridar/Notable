package com.xaridar.notable.app;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xaridar.notable.R;
import com.xaridar.notable.common.Utils;
import com.xaridar.notable.events.QueryListener;
import com.xaridar.notable.models.Note;
import com.xaridar.notable.models.SerializableNote;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class NoteFragment extends Fragment {
    private TextView usernameText;
    private ImageView avatarImg;
    private TextView noteText;
    private TextView locText;
    private TextView dateText;
    private TextView timeText;

    private String sendingAct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendingAct = requireActivity().getIntent().getStringExtra("activity");
        usernameText = view.findViewById(R.id.noteUsername);
        avatarImg = view.findViewById(R.id.noteAvatar);
        noteText = view.findViewById(R.id.noteContent);
        locText = view.findViewById(R.id.noteLocation);
        dateText = view.findViewById(R.id.noteDate);
        timeText = view.findViewById(R.id.noteTime);
        Intent intent = requireActivity().getIntent();
        if (intent.hasExtra("NoteID")) {
            String noteId = intent.getStringExtra("NoteID");
            Note.asyncNoteFromId(noteId, new QueryListener<Note>() {
                @Override
                public void onQueryResult(@NonNull @NotNull Note result) {
                    usernameText.setText(result.getUsername());
                    dateText.setText(Utils.dateFormatter.format(result.getDateTime()));
                    timeText.setText(Utils.timeFormatter.format(result.getDateTime()));
                    try {
                        List<Address> addresses = new Geocoder(getContext()).getFromLocation(result.getLocation().latitude, result.getLocation().longitude, 1);
                        if (addresses == null || addresses.size() == 0) {
                            locText.setText(R.string.loc_unavailable);
                        } else {
                            Address address = addresses.get(0);
                            String location = Utils.formatAddr(address);
                            locText.setText(location);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    noteText.setText(result.getNoteContent());
                }

                @Override
                public void onQueryError(@NonNull @NotNull String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
            });
        } else if (intent.hasExtra("noteSerializable")) {
            Note note = ((SerializableNote) intent.getSerializableExtra("noteSerializable")).getNote();
            if (!intent.hasExtra("serializedAva"))
                new Thread(() -> {
                    try {
                        Drawable bmp = BitmapDrawable.createFromStream(new URL(note.getUserAvatar().toString()).openStream(), "bitmap");
                        requireActivity().runOnUiThread(() -> avatarImg.setBackground(bmp));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            else {
                byte[] b = intent.getByteArrayExtra("serializedAva");

                Drawable bmp = BitmapDrawable.createFromStream(new ByteArrayInputStream(b), "bitmap");
                avatarImg.setBackground(bmp);
            }
            usernameText.setText(note.getUsername());
            dateText.setText(Utils.dateFormatter.format(note.getDateTime()));
            timeText.setText(Utils.timeFormatter.format(note.getDateTime()));
            try {
                List<Address> addresses = new Geocoder(getContext()).getFromLocation(note.getLocation().latitude, note.getLocation().longitude, 1);
                if (addresses == null || addresses.size() == 0) {
                    locText.setText(R.string.loc_unavailable);
                } else {
                    Address address = addresses.get(0);
                    String location = Utils.formatAddr(address);
                    locText.setText(location);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            noteText.setText(note.getNoteContent());
        } else {
            requireActivity().finish();
            return;
        }


        Button leaveNoteBtn = view.findViewById(R.id.leaveNoteBtn);
        Button viewNotesBtn = view.findViewById(R.id.allNotesBtn);
        leaveNoteBtn.setOnClickListener(v -> requireActivity().finish());
        viewNotesBtn.setOnClickListener(v -> viewNoteList());
    }

    public void viewNoteList() {
        Intent listIntent = new Intent(getContext(), NoteListActivity.class);
        startActivity(listIntent);
    }
}