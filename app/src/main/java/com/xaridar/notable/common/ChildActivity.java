package com.xaridar.notable.common;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.xaridar.notable.R;

import java.util.Objects;

public abstract class ChildActivity extends AppCompatActivity {

    public enum ButtonEnum {
        UP, EXIT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContentView() != 0) {
            setContentView(getContentView());
            ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(getBackType() == ButtonEnum.UP ? R.drawable.ic_back : getBackType() == ButtonEnum.EXIT ? R.drawable.ic_close : R.drawable.ic_back);
        }
    }

    protected abstract int getContentView();

    protected abstract ButtonEnum getBackType();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
