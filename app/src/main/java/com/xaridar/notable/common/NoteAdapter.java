package com.xaridar.notable.common;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.xaridar.notable.R;
import com.xaridar.notable.app.NoteActivity;
import com.xaridar.notable.app.NoteListActivity;
import com.xaridar.notable.events.QueryListener;
import com.xaridar.notable.models.Note;
import com.xaridar.notable.models.SerializableNote;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<Note> currNotes = new ArrayList<>();
    private final Activity a;

    public NoteAdapter(Activity a) {
        this.a = a;
    }

    @NonNull
    @NotNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NoteViewHolder holder, int position) {
        Note note = currNotes.get(position);
        holder.username.setText(note.getUsername());
        new Thread(() -> {
            try {
                if (!note.getUserAvatar().toString().equals("")) {
                    Drawable bmp = BitmapDrawable.createFromStream(new URL(note.getUserAvatar().toString()).openStream(), "bitmap");
                    a.runOnUiThread(() -> holder.avatar.setBackground(bmp));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            List<Address> addresses = new Geocoder(a).getFromLocation(note.getLocation().latitude, note.getLocation().longitude, 1);
            if (addresses == null || addresses.size() == 0) {
                holder.location.setText(R.string.loc_unavailable);
            } else {
                Address address = addresses.get(0);
                String location = Utils.formatAddr(address);
                holder.location.setText(location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.content.setText(note.getNoteContent());
        holder.date.setText(Utils.dateFormatter.format(note.getDateTime()));

        holder.itemView.setOnClickListener(view -> new Thread(() -> {
            Intent viewNoteIntent = new Intent(a, NoteActivity.class);
            viewNoteIntent.putExtra("noteSerializable", new SerializableNote(note));
            viewNoteIntent.putExtra("activity", NoteListActivity.class.getName());
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new URL(note.getUserAvatar().toString()).openStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                viewNoteIntent.putExtra("serializedAva", b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            a.runOnUiThread(() -> {
                ActivityOptionsCompat opts = ActivityOptionsCompat.makeSceneTransitionAnimation(a,
                        Pair.create(holder.avatar, "noteAvatar"),
                        Pair.create(holder.content, "noteContent"),
                        Pair.create(holder.username, "noteUsername"),
                        Pair.create(holder.location, "noteLocation"),
                        Pair.create(holder.date, "noteDate")
                );
                a.startActivity(viewNoteIntent, opts.toBundle());
            });
        }).start());
    }

    public void addNoteIds(List<String> ids) {
        ids.forEach(id -> Note.asyncNoteFromId(id, new QueryListener<Note>() {
            @Override
            public void onQueryResult(@NonNull @NotNull Note result) {
                currNotes.add(result);
                notifyDataSetChanged();
            }

            @Override
            public void onQueryError(@NonNull @NotNull String error) {
                Log.w("NoteAdapter", "Error: " + error);
            }
        }));
    }

    public void addNoteId(String id) {
        Note.asyncNoteFromId(id, new QueryListener<Note>() {
            @Override
            public void onQueryResult(@NonNull @NotNull Note result) {
                currNotes.add(result);
                notifyDataSetChanged();
            }

            @Override
            public void onQueryError(@NonNull @NotNull String error) {
                Log.w("NoteAdapter", "Error: " + error);
            }
        });
    }

    public void removeNoteId(String id) {
        currNotes = currNotes.stream().filter(note -> !note.getNoteId().equals(id)).collect(Collectors.toList());
        notifyDataSetChanged();
    }

    public void clearNoteIds() {
        currNotes = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return currNotes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatar;
        private final TextView username;
        private final TextView content;
        private final TextView location;
        private final TextView date;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.indivNoteAvatar);
            username = itemView.findViewById(R.id.indivNoteUsername);
            content = itemView.findViewById(R.id.indivNoteContent);
            location = itemView.findViewById(R.id.indivNoteLoc);
            date = itemView.findViewById(R.id.indivNoteDate);
        }
    }
}
