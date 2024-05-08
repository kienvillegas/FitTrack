package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class actSleepTracker extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;


    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvSleepTrackerTaken, tvSleepTrackerPercent, tvSleepTrackerGoal;
    EditText etSleepTrackerInput;
    ProgressBar pbSleepTracker, pbAddHours;
    Button btnAddHours;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StepSensorManager stepSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_act_sleep_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        DataManager dataManager = new DataManager(this);

        imBackBtn = findViewById(R.id.imSleepTrackerBack);
        tvSleepTrackerTaken = findViewById(R.id.tvSleepTrackerTaken);
        tvSleepTrackerPercent = findViewById(R.id.tvSleepTrackerPercent);
        tvSleepTrackerGoal = findViewById(R.id.tvSleepTrackerGoal);
        etSleepTrackerInput = findViewById(R.id.etSleepTrackerInput);
        pbSleepTracker = findViewById(R.id.pbSleepTracker);
        btnAddHours = findViewById(R.id.btnAddHours);
        pbAddHours = findViewById(R.id.pbAddHours);

        etSleepTrackerInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        pbAddHours.setVisibility(View.GONE);
        btnAddHours.setVisibility(View.VISIBLE);

        fetchSleepData(userId);

        btnAddHours.setOnClickListener(v -> {
            pbAddHours.setVisibility(View.VISIBLE);
            btnAddHours.setVisibility(View.GONE);
            String inputSleep = etSleepTrackerInput.getText().toString().trim();
            String day = getCurrentDay();

            try {
                DocumentReference docRef = db.collection("users").document(userId);
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int dailySleepTaken, weeklySleepTaken, sleepDailyGoal;
                        int diff;
                        dailySleepTaken = documentSnapshot.getLong("dailySleepTaken").intValue();
                        weeklySleepTaken = documentSnapshot.getLong("weeklySleepTaken").intValue();
                        sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal").intValue();

                        diff = sleepDailyGoal - dailySleepTaken;

                        if (inputSleep.isEmpty()) {
                            pbAddHours.setVisibility(View.GONE);
                            btnAddHours.setVisibility(View.VISIBLE);

                            etSleepTrackerInput.setBackgroundResource(R.drawable.text_field_red);
                            etSleepTrackerInput.setError("Please enter hours of sleep");
                            return;
                        }

                        if(Integer.parseInt(inputSleep) > diff){
                            pbAddHours.setVisibility(View.GONE);
                            btnAddHours.setVisibility(View.VISIBLE);
                            etSleepTrackerInput.setError("Cannot be more than daily goal");
                            return;
                        }

                        dailySleepTaken += Integer.parseInt(inputSleep);
                        weeklySleepTaken += Integer.parseInt(inputSleep);

                        saveWeeklySleep(userId, day, dailySleepTaken);
                        checkGoalAchievement(dailySleepTaken, sleepDailyGoal, userId);

                        Map<String, Object> sleep = new HashMap<>();
                        sleep.put("dailySleepTaken", dailySleepTaken);
                        sleep.put("weeklySleepTaken", weeklySleepTaken);

                        docRef.update(sleep)
                                .addOnSuccessListener(unused -> {
                                    fetchSleepData(userId);
                                    Toast.makeText(actSleepTracker.this, "Successfully entered hours of sleep taken", Toast.LENGTH_SHORT).show();
                                    dataManager.saveCurrentDateTime();
                                    pbAddHours.setVisibility(View.GONE);
                                    btnAddHours.setVisibility(View.VISIBLE);
                                    etSleepTrackerInput.setText("");
                                    Log.d(TAG, "Successfully added " + inputSleep);
                                }).addOnFailureListener(e -> {
                                    pbAddHours.setVisibility(View.GONE);
                                    btnAddHours.setVisibility(View.VISIBLE);

                                    Log.e(TAG, "Failed to add " + inputSleep);
                                    Toast.makeText(actSleepTracker.this, "Failed to enter hours of sleep taken", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        pbAddHours.setVisibility(View.GONE);
                        btnAddHours.setVisibility(View.VISIBLE);

                        Log.e(TAG, "Document does not exist");
                    }
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));

            } catch (Exception e) {
                pbAddHours.setVisibility(View.GONE);
                btnAddHours.setVisibility(View.VISIBLE);

                Toast.makeText(actSleepTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        etSleepTrackerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etSleepTrackerInput.setText(filteredText);
                }

                int resourceId = R.drawable.text_field_bg_grey;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSleepTrackerInput.setBackground(drawable);
                etSleepTrackerInput.setError(null);
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
    private void fetchSleepData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int sleepDailyGoal, dailySleepTaken, sleepPercent;
            sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal").intValue();
            dailySleepTaken = documentSnapshot.getLong("dailySleepTaken").intValue();

            if (sleepDailyGoal != 0) {
                sleepPercent = Math.min((int) (((double) dailySleepTaken / sleepDailyGoal) * 100), 100);
            }else{
                sleepPercent = 0;
            }

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(sleepDailyGoal);
            String formattedSleepTaken = NumberFormat.getNumberInstance(Locale.US).format(dailySleepTaken);

            tvSleepTrackerTaken.setText(formattedSleepTaken + " Hours");
            tvSleepTrackerGoal.setText(formattedDailyGoal);
            tvSleepTrackerPercent.setText(String.valueOf(sleepPercent) + "%");
            pbSleepTracker.setMax(100);
            pbSleepTracker.setProgress(sleepPercent);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch sleep data");
        });
    }

    private void checkStepGoalAchievement(int dailyStepTaken, int steDailyGoal, String userId) {
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

    private void checkGoalAchievement(int dailySleepTaken, int sleepDailyGoal, String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isSleepDailyGoal = documentSnapshot.getBoolean("isSleepDailyGoal");

                    if (!isSleepDailyGoal && dailySleepTaken >= sleepDailyGoal) {
                        updateSleepGoalStatus(docRef, true);

                        Intent intent = new Intent(getApplicationContext(), bannerSleepGoalAchieved.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching isSleepDailyGoal: " + e.getMessage());
                });
    }

    private void updateSleepGoalStatus(DocumentReference docRef, boolean isGoalAchieved) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("isSleepDailyGoal", isGoalAchieved);

        docRef.update(goalData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully updated isSleepDailyGoal");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update isSleepDailyGoal: " + e.getMessage());
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


    private void initializeStepSensor() {
        Log.d(TAG, "initializeStepSensor");

        stepSensorManager = new StepSensorManager(this, stepCount -> {
            Log.d(TAG, "Listening...");

            DataManager dataManager = new DataManager(actSleepTracker.this);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userId = currentUser.getUid();
            String day = getCurrentDay();

            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    int dailyStepTaken, weeklyStepTaken, stepDailyGoal;

                    dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
                    weeklyStepTaken = documentSnapshot.getLong("weeklyStepTaken").intValue();
                    stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();

                    dailyStepTaken += stepCount;
                    weeklyStepTaken += stepCount;
                    Log.d(TAG, "Daily Step Taken: " + dailyStepTaken);

                    saveWeekSteps(userId, day, dailyStepTaken);
                    checkStepGoalAchievement(dailyStepTaken, stepDailyGoal, userId);

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


    private void saveWeeklySleep(String userId, String day, int dailySleepTaken){
        DocumentReference weeklySleepRef = db.collection("weekly_sleep").document(userId);
        Map<String, Object> sleepData = new HashMap<>();

        switch (day){
            case "Mon":
                sleepData.put("mon", dailySleepTaken);
                break;
            case "Tue":
                sleepData.put("tue", dailySleepTaken);
                break;
            case "Wed":
                sleepData.put("wed", dailySleepTaken);
                break;
            case "Thu":
                sleepData.put("thu", dailySleepTaken);
                break;
            case "Fri":
                sleepData.put("fri", dailySleepTaken);
                break;
            case "Sat":
                sleepData.put("sat", dailySleepTaken);
                break;
            case "Sun":
                sleepData.put("sun", dailySleepTaken);
                break;
            default:
                Log.w(TAG, day + " is not available");
        }

        weeklySleepRef.update(sleepData)
                .addOnSuccessListener(unused -> {
                    Log.i(TAG, "Successfully added " + sleepData + " to Firestore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add " + sleepData + " to Firestore");
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