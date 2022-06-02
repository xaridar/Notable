package com.xaridar.notable.app;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xaridar.notable.R;
import com.xaridar.notable.common.ChildActivity;

public class CreateNoteActivity extends ChildActivity {

    private EditText contentField;
    private TextView errorText;
    private boolean pressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentField = findViewById(R.id.createNoteMsg);
        errorText = findViewById(R.id.createNoteError);
        Button createNoteBtn = findViewById(R.id.createNoteBtn);
        createNoteBtn.setOnClickListener(v -> submitNote());
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_create_note;
    }

    @Override
    protected ButtonEnum getBackType() {
        return ButtonEnum.EXIT;
    }

    public void submitNote() {
        if (pressed) return;
        String msg = contentField.getText().toString();
        if (msg.equals("")) {
            errorText.setText(R.string.no_empty_note);
            return;
        }
        pressed = true;
        Intent intent = new Intent();
        intent.putExtra("NoteContent", msg);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.none, R.anim.slide_out_bottom);
    }
}