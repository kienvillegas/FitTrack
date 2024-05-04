package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class settingsPage extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    Button btnSignOut;
    Dialog dialog;
    Button btnSignOutCancel, btnSignOutConfirm;
    ImageView imSettingsBack;
    private StepSensorManager stepSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_settings_page);
        mAuth = FirebaseAuth.getInstance();
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

            DataManager dataManager = new DataManager(settingsPage.this);
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