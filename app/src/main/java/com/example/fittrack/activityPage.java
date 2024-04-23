package com.example.fittrack;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class activityPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acitivity_page);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavActivity);
        bottomNav.setSelectedItemId(R.id.nav_activity);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), dashboardPage.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_activity) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), profilePage.class));
                finish();
                return true;
            }
            return false;
        });
    }
}