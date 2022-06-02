package com.xaridar.notable.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.xaridar.notable.R;
import com.xaridar.notable.auth.UserInfo;
import com.xaridar.notable.common.ChildActivity;
import com.xaridar.notable.common.SettingsContainer;
import com.xaridar.notable.login.ChooseAvatarActivity;
import com.xaridar.notable.login.ChooseAvatarFragment;
import com.xaridar.notable.login.ChooseUsernameActivity;
import com.xaridar.notable.login.ChooseUsernameFragment;
import com.xaridar.notable.login.HomeActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SettingsActivity extends ChildActivity {

    boolean large = false;

    private List<CardView> tabs = new ArrayList<>();
    private int currTab;

    private SettingsFragment lastFrag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button logoutButton = findViewById(R.id.logoutBtn);
        if (UserInfo.getUser() == null) {
            logout();
            return;
        }
        SettingsContainer.getSettings(UserInfo.getUser(), this);

        if (logoutButton != null) {
            Button changeAvatarButton = findViewById(R.id.changeAvatar);
            Button changeUsernameButton = findViewById(R.id.changeUsername);
            Button deleteUserButton = findViewById(R.id.delAcctBtn);
            logoutButton.setOnClickListener(v -> logout());
            changeAvatarButton.setOnClickListener(v -> {
                Intent i = new Intent(this, ChooseAvatarActivity.class);
                i.putExtra("skippable", false);
                i.putExtra("cancelable", true);
                i.putExtra("back", true);
                startActivity(i);
            });
            changeUsernameButton.setOnClickListener(v -> {
                Intent i = new Intent(this, ChooseUsernameActivity.class);
                i.putExtra("cancelable", true);
                i.putExtra("back", true);
                startActivity(i);
            });
            deleteUserButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.NotableAlert)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.are_you_sure_long)
                        .setPositiveButton(R.string.yes_im_sure, (di, i) -> {
                            reauthUserForDeletion();
                        })
                        .setNegativeButton(R.string.never_mind, null);
                builder.show();
            });
        } else {
            large = true;
            tabSelect(null);
            tabs = Arrays.asList(
                    findViewById(R.id.changeAvatarTab),
                    findViewById(R.id.changeUsernameTab)
            );
            CardView delTab = findViewById(R.id.delAcctBtnTab);
            delTab.setOnClickListener(v -> {
                tabSelect(v);
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.NotableAlert)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.are_you_sure_long)
                        .setPositiveButton(R.string.yes_im_sure, (di, i) -> {
                            reauthUserForDeletion();
                        })
                        .setNegativeButton(R.string.never_mind, null);
                builder.show();
            });
            tabs.forEach(tab -> tab.setOnClickListener(this::tabSelect));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!large) return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_toolbar_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.logoutAction) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void tabSelect(View v) {
        if (!tabs.contains(v)) {
            currTab = -1;
        } else {
            currTab = tabs.indexOf(v);
        }
        tabs.forEach(tab -> {
            ((TextView) tab.findViewWithTag("tabText")).setTextColor(getColor(R.color.text_main));
        });
        if (currTab > -1)
            ((TextView) tabs.get(currTab).findViewWithTag("tabText")).setTextColor(getColor(R.color.text_secondary));
        SettingsFragment frag;
        switch (currTab) {
            case 0:
                frag = ChooseAvatarFragment.newInstance(true);
                break;
            case 1:
                frag = ChooseUsernameFragment.newInstance(true);
                break;
            default:
                frag = new SettingsDefaultFragment();
        }
        if (lastFrag != null) {
            lastFrag.scheduleFinish((unused) -> {
                getSupportFragmentManager().beginTransaction().replace(R.id.settingsContent, (Fragment) frag).commit();
                lastFrag = frag;
                return null;
            });
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.settingsContent, (Fragment) frag).commit();
            lastFrag = frag;
        }
    }

    private void reauthUserForDeletion() {
        List<String> ids = UserInfo.getUser().getProviderData().stream().map(com.google.firebase.auth.UserInfo::getProviderId).collect(Collectors.toList());
        if (ids.contains(EmailAuthProvider.PROVIDER_ID)) {
            RelativeLayout rl = new RelativeLayout(this);
            EditText et = new EditText(this);
            rl.addView(et);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    r.getDisplayMetrics()
            );
            lp.setMarginStart(px);
            lp.setMarginEnd(px);
            et.setLayoutParams(lp);
            new AlertDialog.Builder(this, R.style.NotableAlert)
                    .setTitle(R.string.reenter_password)
                    .setView(rl)
                    .setPositiveButton(R.string.del_my_acct, (di, i) -> {
                        if (et.getText().toString().equals("")) {
                            Toast.makeText(this, R.string.no_password, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        UserInfo.getUser().reauthenticate(EmailAuthProvider.getCredential(Objects.requireNonNull(UserInfo.getUser().getEmail()), et.getText().toString())).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                deleteUser();
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(this, R.string.incorrect_password, Toast.LENGTH_SHORT).show();
                                }
                                Log.w("DeleteUser", Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });
                    }).setNegativeButton(android.R.string.cancel, null).show();
        } else if (ids.contains(GoogleAuthProvider.PROVIDER_ID)) {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                UserInfo.getUser().reauthenticate(GoogleAuthProvider.getCredential(acct.getIdToken(), null)).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deleteUser();
                    }
                });
            }
        } else if (ids.contains(PhoneAuthProvider.PROVIDER_ID)) {
//                user.reauthenticate(Phone);
            deleteUser();
        }
    }

    private void deleteUser() {
        UserInfo.deleteUser();
        logout();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_settings;
    }

    @Override
    protected ButtonEnum getBackType() {
        return ButtonEnum.UP;
    }

    public void logout() {
        UserInfo.logout();
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}