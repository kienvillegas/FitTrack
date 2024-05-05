package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

public class actFoodIntakeTracker extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;

    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvCalorieTrackerTaken, tvCalorieTrackerPercent, tvCalorieTrackerGoal;
    EditText etCalorieTrackerInput;
    ProgressBar pbCalorieTracker, pbAddCalories;
    Button btnAddCalories;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StepSensorManager stepSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_food_intake_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        DataManager dataManager = new DataManager(this);

        imBackBtn = findViewById(R.id.imCalorieTrackerBack);
        tvCalorieTrackerTaken = findViewById(R.id.tvCalorieTrackerTaken);
        tvCalorieTrackerPercent = findViewById(R.id.tvCalorieTrackerPercent);
        tvCalorieTrackerGoal = findViewById(R.id.tvCalorieTrackerGoal);
        etCalorieTrackerInput = findViewById(R.id.etCalorieTrackerInput);
        pbCalorieTracker = findViewById(R.id.pbCalorieTracker);
        btnAddCalories = findViewById(R.id.btnAddCalories);
        pbAddCalories = findViewById(R.id.pbAddCalories);

        pbAddCalories.setVisibility(View.GONE);
        btnAddCalories.setVisibility(View.VISIBLE);

        fetchCalorieData(userId);

        btnAddCalories.setOnClickListener(v -> {
            pbAddCalories.setVisibility(View.VISIBLE);
            btnAddCalories.setVisibility(View.GONE);

            String inputCalorie = etCalorieTrackerInput.getText().toString().trim();
            String day = getCurrentDay();
            try {
                DocumentReference docRef = db.collection("users").document(userId);
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int dailyCalorieTaken, weeklyCalorieTaken, calorieDailyGoal, calorieWeeklyGoal;
                        int diff = 0;

                        dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();
                        weeklyCalorieTaken = documentSnapshot.getLong("weeklyCalorieTaken").intValue();
                        calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();
                        calorieWeeklyGoal  = documentSnapshot.getLong("calorieWeeklyGoal").intValue();

                        dailyCalorieTaken += Integer.parseInt(inputCalorie);
                        weeklyCalorieTaken += Integer.parseInt(inputCalorie);

                        diff = calorieWeeklyGoal - weeklyCalorieTaken;

                        if(Integer.parseInt(inputCalorie) > diff){
                            pbAddCalories.setVisibility(View.GONE);
                            btnAddCalories.setVisibility(View.VISIBLE);
                            etCalorieTrackerInput.setBackgroundResource(R.drawable.text_field_red);
                            etCalorieTrackerInput.setError("Cannot be more than the weekly goal");
                            return;
                        }

                        if (inputCalorie.isEmpty()) {
                            pbAddCalories.setVisibility(View.GONE);
                            btnAddCalories.setVisibility(View.VISIBLE);

                            etCalorieTrackerInput.setBackgroundResource(R.drawable.text_field_red);
                            etCalorieTrackerInput.setError("Required");
                            return;
                        }

                        saveWeeklyCalorie(userId, day, dailyCalorieTaken);
                        checkGoalAchievement(dailyCalorieTaken, calorieDailyGoal, userId);

                        Map<String, Object> calories = new HashMap<>();
                        calories.put("dailyCalorieTaken", dailyCalorieTaken);
                        calories.put("weeklyCalorieTaken", weeklyCalorieTaken);

                        docRef.update(calories)
                                .addOnSuccessListener(unused -> {
                                    fetchCalorieData(userId);

                                    dataManager.saveCurrentDateTime();

                                    pbAddCalories.setVisibility(View.GONE);
                                    btnAddCalories.setVisibility(View.VISIBLE);

                                    Toast.makeText(actFoodIntakeTracker.this, "Successfully entered calories taken", Toast.LENGTH_SHORT).show();
                                    etCalorieTrackerInput.setText("");
                                    Log.d(TAG, "Successfully added " + inputCalorie);
                                }).addOnFailureListener(e -> {
                                    pbAddCalories.setVisibility(View.GONE);
                                    btnAddCalories.setVisibility(View.VISIBLE);

                                    Log.e(TAG, "Failed to add " + inputCalorie);
                                    Toast.makeText(actFoodIntakeTracker.this, "Failed to enter calories taken", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        pbAddCalories.setVisibility(View.GONE);
                        btnAddCalories.setVisibility(View.VISIBLE);

                        Log.e(TAG, "Document does not exist");
                    }
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));

            } catch (Exception e) {
                pbAddCalories.setVisibility(View.GONE);
                btnAddCalories.setVisibility(View.VISIBLE);

                Toast.makeText(actFoodIntakeTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        etCalorieTrackerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etCalorieTrackerInput.setText(filteredText);
                }

                etCalorieTrackerInput.setBackgroundResource(R.drawable.text_field_bg_grey);
                etCalorieTrackerInput.setError(null);
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

    private void fetchCalorieData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int calorieDailyGoal, dailyCalorieTaken, caloriePercent;
            calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();
            dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();

            if (calorieDailyGoal != 0) {
                caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
            }else{
                caloriePercent = 0;
            }

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(calorieDailyGoal);
            String formattedCalorieTaken = NumberFormat.getNumberInstance(Locale.US).format(dailyCalorieTaken);

            tvCalorieTrackerTaken.setText(formattedCalorieTaken + " Kcal");
            tvCalorieTrackerGoal.setText(formattedDailyGoal);
            tvCalorieTrackerPercent.setText(String.valueOf(caloriePercent) + "%");
            pbCalorieTracker.setMax(100);
            pbCalorieTracker.setProgress(caloriePercent);
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

    private void checkGoalAchievement(int dailyCalorieTaken, int calorieDailyGoal, String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isCalorieDailyGoal = documentSnapshot.getBoolean("isCalorieDailyGoal");

                    if (!isCalorieDailyGoal && dailyCalorieTaken >= calorieDailyGoal) {
                        updateCalorieGoalStatus(docRef, true);

                        Intent intent = new Intent(getApplicationContext(), bannerCalorieGoalAchieved.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching isCalorieDailyGoal: " + e.getMessage());
                });
    }

    private void updateCalorieGoalStatus(DocumentReference docRef, boolean isGoalAchieved) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("isCalorieDailyGoal", isGoalAchieved);

        docRef.update(goalData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully updated isCalorieDailyGoal");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update isCalorieDailyGoal: " + e.getMessage());
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

            DataManager dataManager = new DataManager(actFoodIntakeTracker.this);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userId = currentUser.getUid();
            String day = getCurrentDay();

            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    int dailyStepTaken, weeklyStepTaken, stepDailyGoal, stepWeeklyGoal;

                    dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
                    weeklyStepTaken = documentSnapshot.getLong("weeklyStepTaken").intValue();
                    stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();
                    stepWeeklyGoal = documentSnapshot.getLong("stepWeeklyGoal").intValue();


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

    private void saveWeeklyCalorie(String userId, String day, int dailyCalorieTaken){
        DocumentReference weeklyCalorieRef = db.collection("weekly_calorie").document(userId);
        Map<String, Object> calorieData = new HashMap<>();

        switch (day){
            case "Mon":
                calorieData.put("mon", dailyCalorieTaken);
                break;
            case "Tue":
                calorieData.put("tue", dailyCalorieTaken);
                break;
            case "Wed":
                calorieData.put("wed", dailyCalorieTaken);
                break;
            case "Thu":
                calorieData.put("thu", dailyCalorieTaken);
                break;
            case "Fri":
                calorieData.put("fri", dailyCalorieTaken);
                break;
            case "Sat":
                calorieData.put("sat", dailyCalorieTaken);
                break;
            case "Sun":
                calorieData.put("sun", dailyCalorieTaken);
                break;
            default:
                Log.w(TAG, day + " is not available");
        }

        weeklyCalorieRef.update(calorieData)
                .addOnSuccessListener(unused -> {
                    Log.i(TAG, "Successfully added " + calorieData + " to Firestore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add " + calorieData + " to Firestore");
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