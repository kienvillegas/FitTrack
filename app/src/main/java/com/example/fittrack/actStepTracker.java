package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class actStepTracker extends AppCompatActivity{
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;
    private FirebaseAuth mAuth;

    ProgressBar pbStepTracker, pbAddSteps;
    ImageView imBackBtn;
    TextView tvStepTrackerTaken, tvStepTrackerPercent, tvStepTrackerGoal;
    EditText etStepTracker;
    Button btnAddSteps;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;
    private StepSensorManager stepSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_step_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        DataManager dataManager = new DataManager(actStepTracker.this);

        final String[] storedDate = {dataManager.getStoredDate()};

        DocumentReference docRef = db.collection("users").document(userId);

        if (storedDate[0] == null || storedDate[0].isEmpty()) {
            dataManager.saveCurrentDateTime();
            storedDate[0] = dataManager.getStoredDate();
        }

        pbStepTracker = findViewById(R.id.pbStepTracker);
        imBackBtn = findViewById(R.id.imStepTrackerBack);

        tvStepTrackerTaken = findViewById(R.id.tvStepTackerTaken);
        tvStepTrackerPercent = findViewById(R.id.tvStepsTrackerPercent);
        tvStepTrackerGoal = findViewById(R.id.tvStepsTrackerGoal);
        etStepTracker = findViewById(R.id.etStepTrackerInput);
        btnAddSteps = findViewById(R.id.btnAddSteps);
        pbAddSteps = findViewById(R.id.pbAddSteps);

        etStepTracker.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        pbAddSteps.setVisibility(View.GONE);
        btnAddSteps.setVisibility(View.VISIBLE);

        fetchStepsData(userId);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            initializeStepSensor();
        }

        btnAddSteps.setOnClickListener(v -> {
            pbAddSteps.setVisibility(View.VISIBLE);
            btnAddSteps.setVisibility(View.GONE);
            String inputStep = etStepTracker.getText().toString().trim();
            String day = getCurrentDay();

            try {
                if (inputStep.isEmpty()) {
                    pbAddSteps.setVisibility(View.GONE);
                    btnAddSteps.setVisibility(View.VISIBLE);

                    int resourceId = R.drawable.text_field_red;
                    Drawable drawable = getResources().getDrawable(resourceId);
                    etStepTracker.setBackground(drawable);
                    etStepTracker.setError("Please enter steps");
                    return;
                }

                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int stepPercent, dailyStepTaken, weeklyStepTaken, stepDailyGoal, stepWeeklyGoal;
                        int diff;

                        dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
                        weeklyStepTaken = documentSnapshot.getLong("weeklyStepTaken").intValue();
                        stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();
                        stepWeeklyGoal = documentSnapshot.getLong("stepWeeklyGoal").intValue();

                        dailyStepTaken += Integer.parseInt(inputStep);
                        weeklyStepTaken += Integer.parseInt(inputStep);

                        diff = stepWeeklyGoal - weeklyStepTaken;

                        if(Integer.parseInt(inputStep) > diff){
                            pbAddSteps.setVisibility(View.GONE);
                            btnAddSteps.setVisibility(View.VISIBLE);

                            etStepTracker.setBackgroundResource(R.drawable.text_field_red);
                            etStepTracker.setError("Cannot be more than the weekly goal");
                            return; 
                        }

                        saveWeekSteps(userId, day, dailyStepTaken);
                        checkGoalAchievement(dailyStepTaken, stepDailyGoal,userId);
                        if (stepDailyGoal != 0) {
                            stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
                        } else {
                            stepPercent = 0;
                        }

                        Map<String, Object> steps = new HashMap<>();
                        steps.put("dailyStepTaken", dailyStepTaken);
                        steps.put("weeklyStepTaken", weeklyStepTaken);

                        String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(stepDailyGoal);
                        String formattedSteptaken = NumberFormat.getNumberInstance(Locale.US).format(dailyStepTaken);

                        tvStepTrackerTaken.setText(formattedSteptaken);
                        tvStepTrackerGoal.setText(formattedDailyGoal);
                        tvStepTrackerPercent.setText(String.valueOf(stepPercent) + "%");
                        pbStepTracker.setMax(100);
                        pbStepTracker.setProgress(stepPercent);

                        docRef.update(steps)
                                .addOnSuccessListener(unused -> {
                                    dataManager.saveCurrentDateTime();
                                    pbAddSteps.setVisibility(View.GONE);
                                    btnAddSteps.setVisibility(View.VISIBLE);

                                    Toast.makeText(actStepTracker.this, "Successfully entered steps taken", Toast.LENGTH_SHORT).show();
                                    etStepTracker.setText("");
                                    Log.d(TAG, "Successfully added " + inputStep);
                                }).addOnFailureListener(e -> {
                                    pbAddSteps.setVisibility(View.GONE);
                                    btnAddSteps.setVisibility(View.VISIBLE);

                                    Log.e(TAG, "Failed to add " + inputStep);
                                    Toast.makeText(actStepTracker.this, "Failed to enter steps taken", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        pbAddSteps.setVisibility(View.GONE);
                        btnAddSteps.setVisibility(View.VISIBLE);

                        Log.e(TAG, "Document does not exist");
                    }
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));

            } catch (Exception e) {
                pbAddSteps.setVisibility(View.GONE);
                btnAddSteps.setVisibility(View.VISIBLE);

                Toast.makeText(actStepTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        etStepTracker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etStepTracker.setText(filteredText);
                }

                int resourceId = R.drawable.text_field_bg_grey;
                Drawable drawable = getResources().getDrawable(resourceId);
                etStepTracker.setBackground(drawable);
                etStepTracker.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        imBackBtn.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void fetchStepsData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int stepDailyGoal, dailyStepTaken, stepPercent;
            stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();
            dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();

            if (stepDailyGoal != 0) {
                stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
            }else{
                stepPercent = 0;
            }

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(stepDailyGoal);
            String formattedStepTaken = NumberFormat.getNumberInstance(Locale.US).format(dailyStepTaken);

            tvStepTrackerTaken.setText(formattedStepTaken);
            tvStepTrackerGoal.setText(formattedDailyGoal);
            tvStepTrackerPercent.setText(String.valueOf(stepPercent) + "%");
            pbStepTracker.setMax(100);
            pbStepTracker.setProgress(stepPercent);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch step data");
        });
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


    private String getCurrentDay(){
        Date date = new Date();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        return dayFormat.format(date);
    }

    private String getCurrentDateTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return sdf.format(date);
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

    private void initializeStepSensor() {
        Log.d(TAG, "initializeStepSensor");

        stepSensorManager = new StepSensorManager(this, stepCount -> {
            Log.d(TAG, "Listening...");

            DataManager dataManager = new DataManager(actStepTracker.this);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userId = currentUser.getUid();
            String day = getCurrentDay();
            final String[] storedDate = {dataManager.getStoredDate()};

            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    int stepPercent, dailyStepTaken, weeklyStepTaken, stepDailyGoal;

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
                        fetchStepsData(userId);
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
