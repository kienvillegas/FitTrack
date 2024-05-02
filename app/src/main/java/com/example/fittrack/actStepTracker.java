package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
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
import androidx.appcompat.app.AppCompatActivity;

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

    private int stepCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_step_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        DataManager dataManager = new DataManager(this);
        final String[] storedDate = {dataManager.getStoredDate()};
        String currentDate = getCurrentDateTime();

        DocumentReference docRef = db.collection("users").document(userId);

        if(storedDate[0] == null || storedDate[0].isEmpty()){
            dataManager.saveCurrentDateTime();
            storedDate[0] = dataManager.getStoredDate();
        }

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        pbStepTracker = findViewById(R.id.pbStepTracker);
        imBackBtn = findViewById(R.id.imStepTrackerBack);

        tvStepTrackerTaken = findViewById(R.id.tvStepTackerTaken);
        tvStepTrackerPercent = findViewById(R.id.tvStepsTrackerPercent);
        tvStepTrackerGoal = findViewById(R.id.tvStepsTrackerGoal);
        etStepTracker = findViewById(R.id.etStepTrackerInput);
        btnAddSteps = findViewById(R.id.btnAddSteps);
        pbAddSteps = findViewById(R.id.pbAddSteps);

        pbAddSteps.setVisibility(View.GONE);
        btnAddSteps.setVisibility(View.VISIBLE);

        fetchStepsData(userId,storedDate[0]);

        btnAddSteps.setOnClickListener(v -> {
            pbAddSteps.setVisibility(View.VISIBLE);
            btnAddSteps.setVisibility(View.GONE);
            String inputStep = etStepTracker.getText().toString().trim();
            String day = getCurrentDay();

            try{
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        final int[] stepPercent = new int[1];
                        final int[] dailyStepTaken = new int[1];
                        final int[] weeklyStepTaken = new int[1];
                        int stepDailyGoal;
                        int stepWeeklyGoal;
                        dailyStepTaken[0] = documentSnapshot.getLong("dailyStepTaken").intValue();
                        weeklyStepTaken[0] = documentSnapshot.getLong("weeklyStepTaken").intValue();
                        stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();
                        stepWeeklyGoal = documentSnapshot.getLong("stepWeeklyGoal").intValue();

                        if(stepCounterSensor == null){
                            Log.d(TAG, "StepCounterSensor is null");
                            return;
                        }
                    }else{
                        Log.e(TAG, "No such document");
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Fail to fetch data: " + e.getMessage());
                });
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
//            try {
//                if (inputStep.isEmpty()) {
//                    pbAddSteps.setVisibility(View.GONE);
//                    btnAddSteps.setVisibility(View.VISIBLE);
//
//                    int resourceId = R.drawable.text_field_red;
//                    Drawable drawable = getResources().getDrawable(resourceId);
//                    etStepTracker.setBackground(drawable);
//                    etStepTracker.setError("Please enter steps");
//                } else {
//
//                    docRef.get().addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            int stepPercent, dailyStepTaken, weeklyStepTaken, stepDailyGoal;
//                             dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
//                             weeklyStepTaken = documentSnapshot.getLong("weeklyStepTaken").intValue();
//                             stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();
//
//                                dailyStepTaken += Integer.parseInt(inputStep);
//                                weeklyStepTaken += Integer.parseInt(inputStep);
//
//                                saveWeekSteps(userId, day, dailyStepTaken);
//                                checkGoalAchievement(dailyStepTaken, stepDailyGoal,userId, storedDate[0]);
//                                if (stepDailyGoal != 0) {
//                                    stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
//                                } else {
//                                    stepPercent = 0;
//                                }
//
//                                Map<String, Object> steps = new HashMap<>();
//                                steps.put("dailyStepTaken", dailyStepTaken);
//                                steps.put("weeklyStepTaken", weeklyStepTaken);
//
//                                String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(stepDailyGoal);
//                                String formattedSteptaken = NumberFormat.getNumberInstance(Locale.US).format(dailyStepTaken);
//
//                                tvStepTrackerTaken.setText(formattedSteptaken);
//                                tvStepTrackerGoal.setText(formattedDailyGoal);
//                                tvStepTrackerPercent.setText(String.valueOf(stepPercent) + "%");
//                                pbStepTracker.setMax(100);
//                                pbStepTracker.setProgress(stepPercent);
//
//                                if(storedDate[0].equals(currentDate) || storedDate[0] == null || storedDate[0].isEmpty()){
//                                    Log.d(TAG, storedDate[0] + " is equal to " + currentDate);
//
//                                    docRef.update(steps)
//                                            .addOnSuccessListener(unused -> {
//                                                dataManager.saveCurrentDateTime();
//                                                pbAddSteps.setVisibility(View.GONE);
//                                                btnAddSteps.setVisibility(View.VISIBLE);
//
//                                                Toast.makeText(actStepTracker.this, "Successfully entered steps taken", Toast.LENGTH_SHORT).show();
//                                                etStepTracker.setText("");
//                                                Log.d(TAG, "Successfully added " + inputStep);
//                                            }).addOnFailureListener(e -> {
//                                                pbAddSteps.setVisibility(View.GONE);
//                                                btnAddSteps.setVisibility(View.VISIBLE);
//
//                                                Log.e(TAG, "Failed to add " + inputStep);
//                                                Toast.makeText(actStepTracker.this, "Failed to enter steps taken", Toast.LENGTH_SHORT).show();
//                                            });
//                                }else{
//                                    Log.e(TAG, storedDate[0] + " is not equal to " + currentDate);
//                                    dataManager.saveCurrentDateTime();
//
//                                    dailyStepTaken = Integer.parseInt(inputStep);
//                                    Map<String, Object> stepTaken = new HashMap<>();
//                                    stepTaken.put("dailyStepTaken", dailyStepTaken);
//
//                                    docRef.update(stepTaken)
//                                            .addOnSuccessListener(unused -> {
//                                                pbAddSteps.setVisibility(View.GONE);
//                                                btnAddSteps.setVisibility(View.VISIBLE);
//
//                                                Log.d(TAG, "Successfully updated daily step taken");
//                                            }).addOnFailureListener(e -> {
//                                                pbAddSteps.setVisibility(View.GONE);
//                                                btnAddSteps.setVisibility(View.VISIBLE);
//
//                                                Log.e(TAG, "Failed to update daily step taken");
//                                            });
//                                }
//                        } else {
//                            pbAddSteps.setVisibility(View.GONE);
//                            btnAddSteps.setVisibility(View.VISIBLE);
//
//                            Log.e(TAG, "Document does not exist");
//                        }
//                    }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));
//                }
//            } catch (Exception e) {
//                pbAddSteps.setVisibility(View.GONE);
//                btnAddSteps.setVisibility(View.VISIBLE);
//
//                Toast.makeText(actStepTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
        });

        etStepTracker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

    private void fetchStepsData(String userId, String storedDateTime) {
        String currentDate = getCurrentDateTime();
        DocumentReference docRef = db.collection("users").document(userId);

        if (storedDateTime.equals(currentDate)) {
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    updateStepUI(documentSnapshot);
                } else {
                    Log.e(TAG, "Document does not exist");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));
        } else {
            Map<String, Object> stepData = new HashMap<>();
            stepData.put("dailyStepTaken", 0);
            docRef.update(stepData)
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "Successfully updated dailyStepTaken to zero");
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating dailyStepTaken to zero");
                    });

            updateStepUI(null);
        }
    }

    private void updateStepUI(@Nullable DocumentSnapshot documentSnapshot) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            int stepDailyGoal = documentSnapshot.getLong("stepDailyGoal") != null ? documentSnapshot.getLong("stepDailyGoal").intValue() : 0;
            int dailyStepTaken = documentSnapshot.getLong("dailyStepTaken") != null ? documentSnapshot.getLong("dailyStepTaken").intValue() : 0;

            int stepPercent = 0;
            if (stepDailyGoal != 0) {
                stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
            }

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(stepDailyGoal);
            String formattedStepTaken = NumberFormat.getNumberInstance(Locale.US).format(dailyStepTaken);

            tvStepTrackerTaken.setText(formattedStepTaken);
            tvStepTrackerGoal.setText(formattedDailyGoal);
            tvStepTrackerPercent.setText(String.valueOf(stepPercent) + "%");
            pbStepTracker.setMax(100);
            pbStepTracker.setProgress(stepPercent);
        } else {
            Log.e(TAG, "Document snapshot is null or does not exist");
        }
    }

    private void checkGoalAchievement(int dailyStepTaken, int steDailyGoal, String userId, String storedDateTime) {
        String currentDateTime = getCurrentDateTime();

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isStepDailyGoal = documentSnapshot.getBoolean("isStepDailyGoal");

                    if (!storedDateTime.equals(currentDateTime)) {
                        updateStepGoalStatus(docRef, false);
                    }

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

    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.reload();
        }
    }
}
