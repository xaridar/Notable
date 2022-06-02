package com.xaridar.notable.app;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xaridar.notable.R;
import com.xaridar.notable.common.ChildActivity;
import com.xaridar.notable.common.Utils;
import com.xaridar.notable.events.QueryListener;
import com.xaridar.notable.models.Note;
import com.xaridar.notable.models.SerializableNote;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class NoteActivity extends ChildActivity {
    @Override
    protected int getContentView() {
        return R.layout.fragment_note;
    }

    @Override
    protected ButtonEnum getBackType() {
        return ButtonEnum.EXIT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none, R.anim.slide_out_bottom);
    }
}