package com.example.fittrack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class settingsPage extends AppCompatActivity {
    Button btnSignOut;
    Dialog dialog;
    Button btnSignOutCancel, btnSignOutConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        btnSignOut = findViewById(R.id.btnSignOut);

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

    }
}