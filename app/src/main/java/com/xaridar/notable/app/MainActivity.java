package com.xaridar.notable.app;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.xaridar.notable.R;
import com.xaridar.notable.auth.UserInfo;
import com.xaridar.notable.login.ChooseUsernameActivity;
import com.xaridar.notable.login.NewPassActivity;


public class MainActivity extends AppCompatActivity {

    private FirebaseUser user;
    private MapFragment mapFrag;
    ActivityResultLauncher<Intent> contentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
        if (res.getResultCode() == Activity.RESULT_OK) {
            Intent intent = res.getData();
            if (intent != null && intent.hasExtra("NoteContent")) {
                mapFrag.addNote(intent.getStringExtra("NoteContent"), user);
            }
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = UserInfo.getUser();
        setTitle(getString(R.string.greeting, user.getDisplayName()));
        mapFrag = MapFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer, mapFrag).commit();
        FloatingActionButton addFab = findViewById(R.id.addFab);
        addFab.setOnClickListener(view -> {
            contentLauncher.launch(new Intent(this, CreateNoteActivity.class));
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.none);
        });
        ExtendedFloatingActionButton notesFab = findViewById(R.id.notesFab);
        notesFab.setOnClickListener(view -> {
            startActivity(new Intent(this, NoteListActivity.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dash_toolbar, menu);
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(new PorterDuffColorFilter(getColor(android.R.color.white), PorterDuff.Mode.SRC_IN));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if (UserInfo.getUser().getDisplayName() == null || UserInfo.getUser().getDisplayName().equals("")) {
            Intent i = new Intent(this, ChooseUsernameActivity.class);
            startActivity(i);
            finish();
        }
        else {
            setTitle(getString(R.string.greeting, user.getDisplayName()));
        }
        super.onResume();
    }

    public void test(View view) {
        startActivity(new Intent(this, NewPassActivity.class));
    }
}