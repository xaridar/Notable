package com.xaridar.notable.login;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xaridar.notable.R;
import com.xaridar.notable.app.SettingsFragment;
import com.xaridar.notable.auth.UserInfo;
import com.xaridar.notable.common.SettingsContainer;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChooseUsernameFragment extends Fragment implements SettingsFragment {

    private boolean finish;
    private boolean noFinish = false;
    private EditText usernameTxt;
    private TextView error;
    private Button goButton;
    private Button cancelButton;

    FirebaseUser user;
    private SettingsContainer cont;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_username, container, false);
    }

    public static ChooseUsernameFragment newInstance(boolean asFragment) {

        Bundle args = new Bundle();
        args.putBoolean("fragment", asFragment);

        ChooseUsernameFragment fragment = new ChooseUsernameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = UserInfo.getUser();
        if (user == null) {
            Intent i = new Intent(getContext(), HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
            return;
        }

        usernameTxt = view.findViewById(R.id.setUsernameTxt);
        error = view.findViewById(R.id.setUsernameError);
        goButton = view.findViewById(R.id.setUsernameConf);
        cancelButton = view.findViewById(R.id.setUsernameCancel);
        cont = SettingsContainer.getSettings(user, getActivity());

        cont.name.observe(getViewLifecycleOwner(), s -> usernameTxt.setText(s));

        cancelButton.setVisibility(requireActivity().getIntent().getBooleanExtra("cancelable", false) ? View.VISIBLE : View.GONE);
        finish = requireActivity().getIntent().getBooleanExtra("back", false);

        if (getArguments() != null && getArguments().getBoolean("fragment")) {
            cancelButton.setVisibility(View.GONE);
            noFinish = true;
        }

        cancelButton.setOnClickListener(v -> requireActivity().finish());
        goButton.setOnClickListener(v -> setName());
        usernameTxt.setOnKeyListener((v, i, keyEvent) -> {
            error.setText("");
            return false;
        });
    }

    private void setName() {
        String name = usernameTxt.getText().toString();
        if (name.equals("")) {
            error.setText(R.string.no_empty_username);
            return;
        }
        if (name.equals(user.getDisplayName())) {
            if (!noFinish) {
                if (!finish) {
                    Intent i = new Intent(getContext(), ChooseAvatarActivity.class);
                    i.putExtra("skippable", true);
                    i.putExtra("cancelable", false);
                    i.putExtra("back", false);
                    startActivity(i);
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.none);
                }
                requireActivity().finish();
            }
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference userRef = usersRef.child(user.getUid());
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                usersRef.removeEventListener(this);
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.hasChild("username")) {
                        if (Objects.requireNonNull(child.child("username").getValue()).toString().equals(name)) {
                            error.setText(R.string.username_in_use);
                            return;
                        }
                    }
                }
                userRef.child("username").setValue(name);
                user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());

                if (!isAdded()) return;
                cont.name.setValue(name);
                if (!noFinish) {
                    if (!finish) {
                        Intent i = new Intent(getContext(), ChooseAvatarActivity.class);
                        i.putExtra("skippable", true);
                        i.putExtra("cancelable", false);
                        i.putExtra("back", false);
                        startActivity(i);
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.none);
                    }
                    requireActivity().finish();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                usersRef.removeEventListener(this);
                ChooseUsernameFragment.this.error.setText(R.string.an_error_occurred);
            }
        });
    }
}