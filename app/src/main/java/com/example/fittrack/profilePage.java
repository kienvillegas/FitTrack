package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class profilePage extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    private FirebaseAuth mAuth;
    BottomNavigationView bottomNav;
    ImageView imProfileSettings;
    TextView tvStepTab, tvWaterTab, tvSleepTab, tvCalorieTab, tvProfileDayMonDate;
    private boolean isSleepTab, isStepTab, isWaterTab, isCalorieTab = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();


        setContentView(R.layout.activity_profile_page);
        mAuth = FirebaseAuth.getInstance();

        bottomNav = findViewById(R.id.bottomNavProfile);
        imProfileSettings = findViewById(R.id.imProfileSettings);
        tvStepTab = findViewById(R.id.stepTab);
        tvWaterTab = findViewById(R.id.waterTab);
        tvCalorieTab = findViewById(R.id.calorieTab);
        tvSleepTab = findViewById(R.id.sleepTab);
        tvProfileDayMonDate = findViewById(R.id.tvProfileDayMonDate);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        SimpleDateFormat dayMonDate = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        Date date = new Date();
        String currentDate = dayMonDate.format(date);
        tvProfileDayMonDate.setText(currentDate);

        tvStepTab.setOnClickListener(v -> {
            isStepTab = true;
            isWaterTab = false;
            isCalorieTab = false;
            isSleepTab = false;

            if(isStepTab){
                tvStepTab.setEnabled(false);
                tvWaterTab.setEnabled(true);
                tvCalorieTab.setEnabled(true);
                tvSleepTab.setEnabled(true);

                tvStepTab.setBackgroundResource(R.drawable.back_select);
                tvWaterTab.setBackgroundResource(0);
                tvCalorieTab.setBackgroundResource(0);
                tvSleepTab.setBackgroundResource(0);
                tvStepTab.setTextColor(getResources().getColor(R.color.whiteText)) ;
                tvWaterTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvCalorieTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvSleepTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));

                Fragment newFragment = new profileStepsFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                fragmentTransaction.commit();
            }
        });

        tvWaterTab.setOnClickListener(v -> {
            isStepTab = false;
            isWaterTab = true;
            isCalorieTab = false;
            isSleepTab = false;

            if(isWaterTab) {
                tvStepTab.setEnabled(true);
                tvWaterTab.setEnabled(false);
                tvCalorieTab.setEnabled(true);
                tvSleepTab.setEnabled(true);

                tvStepTab.setBackgroundResource(0);
                tvWaterTab.setBackgroundResource(R.drawable.back_select);
                tvCalorieTab.setBackgroundResource(0);
                tvSleepTab.setBackgroundResource(0);
                tvStepTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvWaterTab.setTextColor(getResources().getColor(R.color.whiteText));
                tvCalorieTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvSleepTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));

                Fragment newFragment = new profileWaterFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                fragmentTransaction.commit();
            }
        });

        tvCalorieTab.setOnClickListener(v -> {
            isStepTab = false;
            isWaterTab = false;
            isCalorieTab = true;
            isSleepTab = false;

            if(isCalorieTab) {
                tvStepTab.setEnabled(true);
                tvWaterTab.setEnabled(true);
                tvCalorieTab.setEnabled(false);
                tvSleepTab.setEnabled(true);
                tvStepTab.setBackgroundResource(0);
                tvWaterTab.setBackgroundResource(0);
                tvCalorieTab.setBackgroundResource(R.drawable.back_select);
                tvSleepTab.setBackgroundResource(0);
                tvStepTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvWaterTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvCalorieTab.setTextColor(getResources().getColor(R.color.whiteText));
                tvSleepTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));

                Fragment newFragment = new profileCalorieFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                fragmentTransaction.commit();
            }
        });

        tvSleepTab.setOnClickListener(v -> {
            isStepTab = false;
            isWaterTab = false;
            isCalorieTab = false;
            isSleepTab = true;

            if(isSleepTab) {
                tvStepTab.setEnabled(true);
                tvWaterTab.setEnabled(true);
                tvCalorieTab.setEnabled(true);
                tvSleepTab.setEnabled(false);
                tvStepTab.setBackgroundResource(0);
                tvWaterTab.setBackgroundResource(0);
                tvCalorieTab.setBackgroundResource(0);
                tvSleepTab.setBackgroundResource(R.drawable.back_select);
                tvStepTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvWaterTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvCalorieTab.setTextColor(getResources().getColor(R.color.tertiaryDarkText));
                tvSleepTab.setTextColor(getResources().getColor(R.color.whiteText));

                Fragment newFragment = new profileSleepFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                fragmentTransaction.commit();
            }
        });


        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), dashboardPage.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_activity) {
                startActivity(new Intent(getApplicationContext(), activityPage.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        imProfileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), settingsPage.class);
                startActivity(intent);
            }
        });
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

    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.reload();
        }
    }
}