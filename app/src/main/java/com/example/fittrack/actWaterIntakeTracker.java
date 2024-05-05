package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class actWaterIntakeTracker extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;

    FirebaseAuth mAuth;
    ImageView imBackBtn, imIncWater, imDecWater;
    Button btnAddDrink;
     TextView tvWaterTrackerTaken, tvWaterTrackerGoal, tvWaterTrackerPercent, tvWaterTrackerInputAmount;
    ProgressBar pbWaterTracker, pbAddDrink;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StepSensorManager stepSensorManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_water_intake_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        DataManager dataManager = new DataManager(this);
        final String[] storedDate = {dataManager.getStoredDate()};

        if(storedDate[0] == null || storedDate[0].isEmpty()){
            dataManager.saveCurrentDateTime();
            storedDate[0] = dataManager.getStoredDate();
        }

        imBackBtn = findViewById(R.id.imWaterTrackerBack);
        imIncWater = findViewById(R.id.imIncWater);
        imDecWater = findViewById(R.id.imDecWater);
        btnAddDrink = findViewById(R.id.btnAddDrink);
        tvWaterTrackerTaken = findViewById(R.id.tvWaterTrackerTaken);
        tvWaterTrackerGoal = findViewById(R.id.tvWaterTrackerGoal);
        tvWaterTrackerPercent = findViewById(R.id.tvWaterTrackerPercent);
        tvWaterTrackerInputAmount = findViewById(R.id.tvWaterTrackerAmountInput);
        pbWaterTracker = findViewById(R.id.pbWaterTracker);
        pbAddDrink = findViewById(R.id.pbAddDrink);

        pbAddDrink.setVisibility(View.GONE);
        btnAddDrink.setVisibility(View.VISIBLE);

        fetchWaterData(userId);

        final int[] inputCounter = {1};
        final int[] totalWaterAmount = new int[1];
        int glassWaterMl = 250;
        imIncWater.setOnClickListener(v -> {
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    int waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();
                    if (totalWaterAmount[0] < waterDailyGoal) {
                        inputCounter[0] += 1;
                        totalWaterAmount[0] = inputCounter[0] * glassWaterMl;
                        tvWaterTrackerInputAmount.setText(inputCounter[0] + "x Glass " + totalWaterAmount[0] + "ml");
                    } else {
                        Toast.makeText(actWaterIntakeTracker.this, "You've reached the maximum amount!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Document does not exist");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Error getting document", e));
        });

        imDecWater.setOnClickListener(v -> {
            if (inputCounter[0] > 1) {
                inputCounter[0] -= 1;
                totalWaterAmount[0] = inputCounter[0] * glassWaterMl;
                tvWaterTrackerInputAmount.setText(inputCounter[0] + "x Glass " + totalWaterAmount[0] + "ml");
            }
        });

        btnAddDrink.setOnClickListener(v -> {
            pbAddDrink.setVisibility(View.VISIBLE);
            btnAddDrink.setVisibility(View.GONE);

            int glassWaterML = 250;
            String day = getCurrentDay();

            try{
                DocumentReference docRef = db.collection("users").document(userId);
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int dailyWaterTaken, weeklyWaterTaken, waterDailyGoal;
                        dailyWaterTaken = documentSnapshot.getLong("dailyWaterTaken").intValue();
                        weeklyWaterTaken = documentSnapshot.getLong("weeklyWaterTaken").intValue();
                        waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();

                            dailyWaterTaken += glassWaterML * inputCounter[0];
                            weeklyWaterTaken += glassWaterML * inputCounter[0];

                            saveWeeklyWaterIntake(userId, day, dailyWaterTaken);
                            checkGoalAchievement(dailyWaterTaken, waterDailyGoal, userId);

                            inputCounter[0] = 1;
                            totalWaterAmount[0] = glassWaterML;

                            Map<String, Object> water = new HashMap<>();
                            water.put("dailyWaterTaken", dailyWaterTaken);
                            water.put("weeklyWaterTaken", weeklyWaterTaken);

                            docRef.update(water)
                                .addOnSuccessListener(unused -> {
                                    fetchWaterData(userId);
                                    dataManager.saveCurrentDateTime();
                                    pbAddDrink.setVisibility(View.GONE);
                                    btnAddDrink.setVisibility(View.VISIBLE);

                                    Toast.makeText(actWaterIntakeTracker.this, "Successfully entered water taken", Toast.LENGTH_SHORT).show();
                                    tvWaterTrackerInputAmount.setText("1x Glass 250ml");
                                    Log.d(TAG, "Successfully added " + inputCounter);
                                }).addOnFailureListener(e -> {
                                    pbAddDrink.setVisibility(View.GONE);
                                    btnAddDrink.setVisibility(View.VISIBLE);

                                    Log.e(TAG, "Failed to add " + inputCounter);
                                    Toast.makeText(actWaterIntakeTracker.this, "Failed to enter water taken", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        pbAddDrink.setVisibility(View.GONE);
                        btnAddDrink.setVisibility(View.VISIBLE);

                        Log.e(TAG, "Document does not exist");
                    }
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));
            }catch(Exception e){
                pbAddDrink.setVisibility(View.GONE);
                btnAddDrink.setVisibility(View.VISIBLE);

                Toast.makeText(actWaterIntakeTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imBackBtn.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void fetchWaterData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int waterDailyGoal, dailyWaterTaken, waterPercent;
            waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();
            dailyWaterTaken = documentSnapshot.getLong("dailyWaterTaken").intValue();

            if (waterDailyGoal != 0) {
                waterPercent = Math.min((int) (((double) dailyWaterTaken / waterDailyGoal) * 100), 100);
            }else{
                waterPercent = 0;
            }

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(waterDailyGoal);
            String formattedWaterTaken = NumberFormat.getNumberInstance(Locale.US).format(dailyWaterTaken);

            tvWaterTrackerTaken.setText(formattedWaterTaken + " mL");
            tvWaterTrackerGoal.setText(formattedDailyGoal);
            tvWaterTrackerPercent.setText(String.valueOf(waterPercent) + "%");
            pbWaterTracker.setMax(100);
            pbWaterTracker.setProgress(waterPercent);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch calorie data");
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

    private void checkGoalAchievement(int dailyWaterTaken, int waterDailyGoal, String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isWaterDailyGoal = documentSnapshot.getBoolean("isWaterDailyGoal");

                    if (!isWaterDailyGoal && dailyWaterTaken >= waterDailyGoal) {
                        updateWaterGoalStatus(docRef, true);

                        Intent intent = new Intent(getApplicationContext(), bannerWaterGoalAchieved.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching isWaterDailyGoal: " + e.getMessage());
                });
    }

    private void updateWaterGoalStatus(DocumentReference docRef, boolean isGoalAchieved) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("isWaterDailyGoal", isGoalAchieved);

        docRef.update(goalData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully updated isWaterDailyGoal");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update isWaterDailyGoal: " + e.getMessage());
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

    private void initializeStepSensor() {
        Log.d(TAG, "initializeStepSensor");

        stepSensorManager = new StepSensorManager(this, stepCount -> {
            Log.d(TAG, "Listening...");

            DataManager dataManager = new DataManager(actWaterIntakeTracker.this);
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


    private void saveWeeklyWaterIntake(String userId, String day, int dailyWaterTaken){
        DocumentReference weeklyWaterRef = db.collection("weekly_water").document(userId);
        Map<String, Object> waterData = new HashMap<>();

        switch (day){
            case "Mon":
                waterData.put("mon", dailyWaterTaken);
                break;
            case "Tue":
                waterData.put("tue", dailyWaterTaken);
                break;
            case "Wed":
                waterData.put("wed", dailyWaterTaken);
                break;
            case "Thu":
                waterData.put("thu", dailyWaterTaken);
                break;
            case "Fri":
                waterData.put("fri", dailyWaterTaken);
                break;
            case "Sat":
                waterData.put("sat", dailyWaterTaken);
                break;
            case "Sun":
                waterData.put("sun", dailyWaterTaken);
                break;
            default:
                Log.w(TAG, day + " is not available");
        }

        weeklyWaterRef.update(waterData)
                .addOnSuccessListener(unused -> {
                    Log.i(TAG, "Successfully added " + waterData + " to Firestore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add " + waterData + " to Firestore");
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