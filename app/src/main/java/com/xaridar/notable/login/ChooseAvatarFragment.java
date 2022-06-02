package com.xaridar.notable.login;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xaridar.notable.R;
import com.xaridar.notable.app.MainActivity;
import com.xaridar.notable.app.SettingsFragment;
import com.xaridar.notable.auth.UserInfo;
import com.xaridar.notable.common.SettingsContainer;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.function.Function;

public class ChooseAvatarFragment extends Fragment implements SettingsFragment {

    public ImageButton avatarBtn;
    private FloatingActionButton remAva;
    private Uri ava;
    private boolean finish;
    private boolean noFinish = false;
    private boolean updated = false;
    private MutableLiveData<Boolean> pending = new MutableLiveData<>(false);

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        updated = true;
        if (uri != null) {
            try {
                avatarBtn.setBackground(Drawable.createFromStream(requireActivity().getContentResolver().openInputStream(uri), uri.toString()));
                ava = uri;
                remAva.setEnabled(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            avatarBtn.setBackground(requireActivity().getDrawable(R.drawable.ic_default_avatar));
            remAva.setEnabled(false);
            ava = null;
        }
    });
    private SettingsContainer cont;

    public static ChooseAvatarFragment newInstance(boolean asFragment) {

        Bundle args = new Bundle();
        args.putBoolean("fragment", asFragment);

        ChooseAvatarFragment fragment = new ChooseAvatarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_avatar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatarBtn = view.findViewById(R.id.avatar_select);
        Button goAvatarBtn = view.findViewById(R.id.goAvatarBtn);
        Button skipAvatarBtn = view.findViewById(R.id.skipAvatarBtn);
        Button cancelBtn = view.findViewById(R.id.cancelChangeAvatarBtn);
        skipAvatarBtn.setVisibility(requireActivity().getIntent().getBooleanExtra("skippable", false) ? View.VISIBLE : View.GONE);
        cancelBtn.setVisibility(requireActivity().getIntent().getBooleanExtra("cancelable", true) ? View.VISIBLE : View.GONE);
        finish = requireActivity().getIntent().getBooleanExtra("back", false);
        remAva = view.findViewById(R.id.removeAvatar);
        if (getArguments() != null && getArguments().getBoolean("fragment")) {
            skipAvatarBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
            noFinish = true;
        }

        avatarBtn.setOnClickListener(v -> chooseAvatar());
        assert UserInfo.getUser() != null;
        cont = SettingsContainer.getSettings(UserInfo.getUser(), getActivity());
        cont.avatar.observe(getViewLifecycleOwner(), drawable -> avatarBtn.setBackground(drawable));
//        new Thread(() -> {
//            try {
//                if (UserInfo.getUser().getPhotoUrl() != null) {
//                    Drawable bmp = BitmapDrawable.createFromStream(new URL(UserInfo.getUser().getPhotoUrl().toString()).openStream(), "bitmap");
//                    if (isAdded())
//                        requireActivity().runOnUiThread(() -> avatarBtn.setBackground(bmp));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();

        remAva.setEnabled(UserInfo.getUser().getPhotoUrl() != null);
        remAva.setOnClickListener(v -> {
            avatarBtn.setBackground(requireActivity().getDrawable(R.drawable.ic_default_avatar));
            remAva.setEnabled(false);
            ava = null;
        });
        goAvatarBtn.setOnClickListener(v -> confirm());
        skipAvatarBtn.setOnClickListener(v -> skip());
        cancelBtn.setOnClickListener(v -> requireActivity().finish());
    }

    public void chooseAvatar() {
        mGetContent.launch("image/*");
    }

    public void confirm() {
        pending.setValue(true);
        if (updated) UserInfo.updateCurrPfp(ava, bool -> {
            if (!isAdded()) return null;
            cont.setAvaFromLocalUri(ava, getActivity());
            pending.setValue(false);
            if (!bool) {
                startActivity(new Intent(getContext(), HomeActivity.class));
                requireActivity().finish();
                return null;
            }
            if (!noFinish) {
                if (!finish) startActivity(new Intent(getContext(), MainActivity.class));
                requireActivity().finish();
            }
            return null;
        }, task -> {
            if (task.isSuccessful()) cont.overrideAva.setValue(false);
        });
        else {
            if (!noFinish) {
                if (!finish) startActivity(new Intent(getContext(), MainActivity.class));
                requireActivity().finish();
            }
        }
    }


    @Override
    public void scheduleFinish(Function<Void, Void> whenCanFinish) {
        if (pending.getValue() != null && pending.getValue()) {
            pending.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean val) {
                    if (!val) {
                        pending.removeObserver(this);
                        whenCanFinish.apply(null);
                    }
                }
            });
        } else whenCanFinish.apply(null);
    }

    public void skip() {
        startActivity(new Intent(getContext(), MainActivity.class));
        requireActivity().finish();
    }
}