package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class settingsPage extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;


    Button btnSignOut;
    Dialog dialog;
    Button btnSignOutCancel, btnSignOutConfirm;
    ImageView imSettingsBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_settings_page);
        btnSignOut = findViewById(R.id.btnSignOut);
        imSettingsBack = findViewById(R.id.imSettingsBack);

        dialog = new Dialog(settingsPage.this);
        dialog.setContentView(R.layout.sign_out_confirmation);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.confirmation_bg);
        dialog.setCancelable(false);

        btnSignOutCancel = dialog.findViewById(R.id.btnSignOutNo);
        btnSignOutConfirm = dialog.findViewById(R.id.btnSignOutYes);

        btnSignOut.setOnClickListener(v -> {
            dialog.show();
        });

        btnSignOutCancel.setOnClickListener(v -> {
                dialog.dismiss();
        });

        btnSignOutConfirm.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            dialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), signIn.class);
            startActivity(intent);
            finish();
        });

        imSettingsBack.setOnClickListener(view -> onBackPressed());
    }

    private void applyTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = prefs.getInt(THEME_PREF_KEY, 0);

        Log.d(TAG, "Applying theme: " + theme);
        switch (theme) {
            case THEME_ORANGE:
                setTheme(R.style.AppOrangeTheme);
                break;
            case THEME_GREEN:
                setTheme(R.style.AppGreenTheme);
                break;
            default:
                setTheme(R.style.AppDefaultTheme);
        }
    }
}