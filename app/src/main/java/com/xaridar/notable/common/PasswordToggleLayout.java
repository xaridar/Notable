package com.xaridar.notable.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xaridar.notable.R;

import org.jetbrains.annotations.NotNull;

public class PasswordToggleLayout extends LinearLayout {

    private Context ctx;
    private EditText passwordField;
    private CheckBox checkBox;
    private View holder;


    public PasswordToggleLayout(@NonNull @NotNull Context context) {
        super(context);
        this.ctx = context;
        init(null);
    }

    public PasswordToggleLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        init(attrs);
    }

    public PasswordToggleLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.ctx = context;
        init(attrs);
    }

    @SuppressLint("ResourceType")
    private void init(AttributeSet attrs) {
        TypedValue a = new TypedValue();
        if (!ctx.getTheme().resolveAttribute(android.R.attr.colorControlNormal, a, true)) {
            throw new RuntimeException("android.R.attr.colorControlNormal is not a color resource.");
        }
        LayoutInflater mInflater = LayoutInflater.from(ctx);
        removeAllViewsInLayout();

        holder = mInflater.inflate(R.layout.layout_toggle, this, true);

        checkBox = holder.findViewById(R.id.toggleCheck);
        checkBox.setButtonTintList(ColorStateList.valueOf(ctx.getColor(a.resourceId)));
        passwordField = holder.findViewById(R.id.password);

        if (attrs != null) {
            TypedArray arr = ctx.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.PasswordToggleLayout, 0, 0);

            CharSequence hintText = arr.getText(R.styleable.PasswordToggleLayout_customHint);
            if (hintText != null) passwordField.setHint(hintText);

            arr.recycle();
        }

        checkBox.setOnClickListener(v -> {
            int selStart = passwordField.getSelectionStart();
            int selEnd = passwordField.getSelectionEnd();

            passwordField.setTransformationMethod(!checkBox.isChecked()
                    ? PasswordTransformationMethod.getInstance()
                    : null);
            passwordField.setSelection(selStart, selEnd);
        });
    }

    public Editable getText() {
        return passwordField.getText();
    }
}
