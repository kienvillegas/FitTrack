package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class profilePage extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    BottomNavigationView bottomNav;
    ImageView imProfileSettings;
    TextView tvStepTab, tvWaterTab, tvSleepTab, tvCalorieTab, tvProfileDayMonDate;
    private boolean isSleepTab, isStepTab, isWaterTab, isCalorieTab = false;
    private StepSensorManager stepSensorManager;


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

        imProfileSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), settingsPage.class);
            startActivity(intent);
        });


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            initializeStepSensor();
        }
    }

    private void checkGoalAchievement(int dailyStepTaken, int steDailyGoal, String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isStepDailyGoal = documentSnapshot.getBoolean("isStepDailyGoal");

                    if (!isStepDailyGoal && dailyStepTaken >= steDailyGoal) {
                        updateStepGoalStatus(docRef, true);

                        Intent intent = new Intent(getApplicationContext(), bannerStepGoalAchieved.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching isStepDailyGoal: " + e.getMessage());
                });
    }

    private void updateStepGoalStatus(DocumentReference docRef, boolean isGoalAchieved) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("isStepDailyGoal", isGoalAchieved);

        docRef.update(goalData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully updated isStepDailyGoal");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update isStepDailyGoal: " + e.getMessage());
                });
    }
    private void initializeStepSensor() {
        Log.d(TAG, "initializeStepSensor");

        stepSensorManager = new StepSensorManager(this, stepCount -> {
            Log.d(TAG, "Listening...");

            DataManager dataManager = new DataManager(profilePage.this);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            mAuth = FirebaseAuth.getInstance();
            String userId = currentUser.getUid();

            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    int dailyStepTaken, weeklyStepTaken, stepDailyGoal;
                    String day = getCurrentDay();

                    dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
                    weeklyStepTaken = documentSnapshot.getLong("weeklyStepTaken").intValue();
                    stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();

                    dailyStepTaken += stepCount;
                    weeklyStepTaken += stepCount;
                    Log.d(TAG, "Daily Step Taken: " + dailyStepTaken);

                    saveWeekSteps(userId, day, dailyStepTaken);
                    checkGoalAchievement(dailyStepTaken, stepDailyGoal,userId);

                    Map<String, Object> steps = new HashMap<>();
                    steps.put("dailyStepTaken", dailyStepTaken);
                    steps.put("weeklyStepTaken", weeklyStepTaken);

                    docRef.update(steps).addOnSuccessListener(unused -> {
                        Log.d(TAG, "Successfully updated dailyStepTaken and weeklyStepTaken");
                        dataManager.saveCurrentDateTime();
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update dailyStepTaken and weeklyStepTaken");
                    });
                }else{
                    Log.e(TAG, "No such document");
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch document: " + e.getMessage());
            });
        });
        stepSensorManager.registerListener();
    }


    private String getCurrentDay(){
        Date date = new Date();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        return dayFormat.format(date);
    }

    private void saveWeekSteps(String userId, String day, int dailyStepTaken){
        DocumentReference weeklyStepRef = db.collection("weekly_step").document(userId);
        Map<String, Object> stepData = new HashMap<>();

        switch (day){
            case "Mon":
                stepData.put("mon", dailyStepTaken);
                break;
            case "Tue":
                stepData.put("tue", dailyStepTaken);
                break;
            case "Wed":
                stepData.put("wed", dailyStepTaken);
                break;
            case "Thu":
                stepData.put("thu", dailyStepTaken);
                break;
            case "Fri":
                stepData.put("fri", dailyStepTaken);
                break;
            case "Sat":
                stepData.put("sat", dailyStepTaken);
                break;
            case "Sun":
                stepData.put("sun", dailyStepTaken);
                break;
            default:
                Log.w(TAG, day + " is not available");
        }

        weeklyStepRef.update(stepData)
                .addOnSuccessListener(unused -> {
                    Log.i(TAG, "Successfully added " + stepData + " to Firestore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add " + stepData + " to Firestore");
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stepSensorManager != null) {
            stepSensorManager.unregisterListener();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize step sensor
                initializeStepSensor();
            } else {
                // Permission denied, show a message or take appropriate action
                Toast.makeText(this, "Permission denied. Step tracking won't work.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}