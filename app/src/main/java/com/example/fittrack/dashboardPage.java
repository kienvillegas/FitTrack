package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.common.subtyping.qual.Bottom;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class dashboardPage extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;

    private FirebaseAuth mAuth;
    private StepSensorManager stepSensorManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView imAddWalk, imAddWater, imAddFood, imAddSleep;
    TextView tvDayMonDate, tvMonYr, tvDayOne, tvDayTwo, tvDayThree, tvDayFour, tvDayFive, tvDaySix, tvDaySeven;
    ImageView imDayOneBg, imDayTwoBg, imDayThreeBg, imDayFourBg, imDayFiveBg, imDaySixBg, imDaySevenBg;
    private TextView[] dayTextViews = new TextView[7];
    private TextView[] dateTextViews = new TextView[7];
    private ImageView[] bgImageView = new ImageView[7];

    TextView  tvStepPercent, tvCaloriePercent, tvSleepHours, tvWaterIntake, tvWeeklyPercent;
    ProgressBar pbDashboardStep, pbDashboardFood, pbDashboardWkProgress, pbDashboardContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_dashboard_page);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        DocumentReference docRef = db.collection("users").document(userId);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavDashboard);
        imAddWalk = findViewById(R.id.imAddWalk);
        imAddWater = findViewById(R.id.imAddWater);
        imAddFood = findViewById(R.id.imAddFood);
        imAddSleep= findViewById(R.id.imAddSleep);

        tvStepPercent = findViewById(R.id.tvDashboardStepPercent);
        tvCaloriePercent = findViewById(R.id.tvDashboardCaloriePercent);
        tvSleepHours = findViewById(R.id.tvDashboardSleep);
        tvWaterIntake = findViewById(R.id.tvDashboardWater);
        tvWeeklyPercent = findViewById(R.id.tvDashboardWkPercent);

        pbDashboardFood = findViewById(R.id.pbDashboardFood);
        pbDashboardStep = findViewById(R.id.pbDashboardStep);
        pbDashboardWkProgress = findViewById(R.id.pbDashboardWkProgress);

        SimpleDateFormat dayMonDate = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        Date date = new Date();
        String currentDate = dayMonDate.format(date);
        DataManager dataManager = new DataManager(dashboardPage.this);
        String currentDatetime = getCurrentDateTime();

        try{
            tvDayMonDate = findViewById(R.id.tvDayMonDate);
            tvMonYr = findViewById(R.id.tvMonYr);
            tvDayOne = findViewById(R.id.tvDayOne);
            tvDayTwo = findViewById(R.id.tvDayTwo);
            tvDayThree = findViewById(R.id.tvDayThree);
            tvDayFour = findViewById(R.id.tvDayFour);
            tvDayFive = findViewById(R.id.tvDayFive);
            tvDaySix = findViewById(R.id.tvDaySix);
            tvDaySeven = findViewById(R.id.tvDaySeven);

            imDayOneBg = findViewById(R.id.imDayOneBg);
            imDayTwoBg = findViewById(R.id.imDayTwoBg);
            imDayThreeBg = findViewById(R.id.imDayThreeBg);
            imDayFourBg = findViewById(R.id.imDayFourBg);
            imDayFiveBg = findViewById(R.id.imDayFiveBg);
            imDaySixBg = findViewById(R.id.imDaySixBg);
            imDaySevenBg = findViewById(R.id.imDaySevenBg);

            dayTextViews[0] = findViewById(R.id.textView164); // Sun
            dayTextViews[1] = findViewById(R.id.textView165); // Mon
            dayTextViews[2] = findViewById(R.id.textView166); // Tue
            dayTextViews[3] = findViewById(R.id.textView167); // Wed
            dayTextViews[4] = findViewById(R.id.textView168); // Thur
            dayTextViews[5] = findViewById(R.id.textView169); // Fri
            dayTextViews[6] = findViewById(R.id.textView170); // Sat

            dateTextViews[0] = findViewById(R.id.tvDayOne);
            dateTextViews[1] = findViewById(R.id.tvDayTwo);
            dateTextViews[2] = findViewById(R.id.tvDayThree);
            dateTextViews[3] = findViewById(R.id.tvDayFour);
            dateTextViews[4] = findViewById(R.id.tvDayFive);
            dateTextViews[5] = findViewById(R.id.tvDaySix);
            dateTextViews[6] = findViewById(R.id.tvDaySeven);

            bgImageView[0] = findViewById(R.id.imDayOneBg);
            bgImageView[1] = findViewById(R.id.imDayTwoBg);
            bgImageView[2] = findViewById(R.id.imDayThreeBg);
            bgImageView[3] = findViewById(R.id.imDayFourBg);
            bgImageView[4] = findViewById(R.id.imDayFiveBg);
            bgImageView[5] = findViewById(R.id.imDaySixBg);
            bgImageView[6] = findViewById(R.id.imDaySevenBg);

            tvDayMonDate.setText(currentDate);
            setWeeklyCalendar();
            bottomNav.setSelectedItemId(R.id.nav_dashboard);

            bottomNav.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_dashboard) {
                    return true;
                } else if (item.getItemId() == R.id.nav_activity) {
                    startActivity(new Intent(getApplicationContext(), activityPage.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    startActivity(new Intent(getApplicationContext(), profilePage.class));
                    finish();
                    return true;
                }
                return false;
            });
        }catch(Exception e){
            Toast.makeText(this, "An Error Occured: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        imAddWalk.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), actStepTracker.class);
            startActivity(intent);
        });

        imAddWater.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), actWaterIntakeTracker.class);
            startActivity(intent);
        });

        imAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), actFoodIntakeTracker.class);
            startActivity(intent);
        });
        imAddSleep.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), actSleepTracker.class);
            startActivity(intent);
        });

        if(currentDatetime.equals(dataManager.getStoredDate()) || dataManager.getStoredDate() == null){
            Log.d(TAG, "Current Date is equal, null or empty: " + dataManager.getStoredDate());

            fetchStepData(docRef);
            fetchWaterData(docRef);
            fetchCalorieData(docRef);
            fetchSleepData(docRef);
            fetchWeeklyData(docRef);
        }else{
            Map<String, Object> actData = new HashMap<>();
            actData.put("dailyStepTaken", 0);
            actData.put("isStepDailyGoal", false);
            actData.put("dailyWaterTaken", 0);
            actData.put("isWaterDailyGoal", false);
            actData.put("dailyCalorieTaken", 0);
            actData.put("isCalorieDailyGoal", false);
            actData.put("dailySleepTaken", 0);
            actData.put("isSleepDailyGoal", false);

            docRef.update(actData).addOnSuccessListener(unused -> {
                Log.d(TAG, "act data has been saved to firestore");
                dataManager.saveCurrentDateTime();

                fetchStepData(docRef);
                fetchWaterData(docRef);
                fetchCalorieData(docRef);
                fetchSleepData(docRef);
                fetchWeeklyData(docRef);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update on firestore");
            });
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            initializeStepSensor();
        }
    }

    private void setWeeklyCalendar(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayMonDate = new SimpleDateFormat("EEEE, MMMM, dd", Locale.getDefault());
        SimpleDateFormat monYr = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        for (int i = 0; i < 7; i++) {
            dayTextViews[i].setText(dayFormat.format(calendar.getTime()));
            dateTextViews[i].setText(dateFormat.format(calendar.getTime()));

            if (isCurrentDate(calendar)) {
                bgImageView[i].setImageResource(R.drawable.current_date_bg);
                dateTextViews[i].setTextColor(getResources().getColor(R.color.whiteText));
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        String currentDate = dayMonDate.format(calendar.getTime());
        String monthYear = monYr.format(calendar.getTime());

        tvDayMonDate.setText(currentDate);
        tvMonYr.setText(monthYear);
    }

    private boolean isCurrentDate(Calendar calendar) {
        Calendar currentDate = Calendar.getInstance();
        return currentDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void fetchStepData(DocumentReference docRef){
        hideContentView();
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int dailyStepTaken, stepDailyGoal, stepPercent;
            dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
            stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();

            if (stepDailyGoal != 0) {
                stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
            } else {
                stepPercent = 0;
            }
            showContentView();

            tvStepPercent.setText(String.valueOf(stepPercent) + "%");
            pbDashboardStep.setMax(100);
            pbDashboardStep.setProgress(stepPercent);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch Step Data: " + e.getMessage());
        });
    }

    private void fetchWaterData(DocumentReference docRef){
        hideContentView();

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int dailyWaterTaken;
            dailyWaterTaken = documentSnapshot.getLong("dailyWaterTaken").intValue();

            showContentView();

            String formattedDailyWater = NumberFormat.getNumberInstance(Locale.US).format(dailyWaterTaken);
            tvWaterIntake.setText(formattedDailyWater);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch Water Data: " + e.getMessage());
        });
    }

    private void fetchCalorieData(DocumentReference docRef){
        hideContentView();

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int dailyCalorieTaken, calorieDailyGoal, caloriePercent ;
            dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();
            calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();

            if (calorieDailyGoal != 0) {
                caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
            } else {
                caloriePercent = 0;
            }
            showContentView();

            tvCaloriePercent.setText(String.valueOf(caloriePercent) + "%");
            pbDashboardFood.setMax(100);
            pbDashboardFood.setProgress(caloriePercent);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch Calorie Data: " + e.getMessage());
        });
    }

    private void fetchSleepData(DocumentReference docRef){
        hideContentView();

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            int dailySleepTaken;
            dailySleepTaken = documentSnapshot.getLong("dailySleepTaken").intValue();
            showContentView();

            String formattedDailySleep = NumberFormat.getNumberInstance(Locale.US).format(dailySleepTaken);
            tvSleepHours.setText(formattedDailySleep);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch Sleep Data: " + e.getMessage());
        });
    }

    private void fetchWeeklyData(DocumentReference docRef){
        hideContentView();
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            Log.d(TAG, "Successfully fetched weekly data");

            int weeklyStepTaken, weeklyWaterTaken, weeklyCalorieTaken, weeklySleepTaken;
            int stepWeeklyGoal, waterWeeklyGoal, calorieWeeklyGoal, sleepWeeklyGoal;
            int weeklyProgressPercent;
            double stepProgress, waterProgress, sleepProgress, calorieProgress, averageProgress;

            weeklyStepTaken = documentSnapshot.getLong("weeklyStepTaken").intValue();
            weeklyWaterTaken = documentSnapshot.getLong("weeklyWaterTaken").intValue();
            weeklyCalorieTaken = documentSnapshot.getLong("weeklyCalorieTaken").intValue();
            weeklySleepTaken = documentSnapshot.getLong("weeklySleepTaken").intValue();

            stepWeeklyGoal = documentSnapshot.getLong("stepWeeklyGoal").intValue();
            waterWeeklyGoal = documentSnapshot.getLong("waterWeeklyGoal").intValue();
            calorieWeeklyGoal = documentSnapshot.getLong("calorieWeeklyGoal").intValue();
            sleepWeeklyGoal = documentSnapshot.getLong("sleepWeeklyGoal").intValue();

            stepProgress = (double) weeklyStepTaken / stepWeeklyGoal;
             waterProgress = (double) weeklyWaterTaken / waterWeeklyGoal;
             calorieProgress = (double) weeklyCalorieTaken / calorieWeeklyGoal;
             sleepProgress = (double) weeklySleepTaken / sleepWeeklyGoal;
             averageProgress = (stepProgress + waterProgress + calorieProgress + sleepProgress) / 4;
             weeklyProgressPercent = (int) (averageProgress * 100);
             weeklyProgressPercent = Math.min(weeklyProgressPercent, 100);

             Log.d(TAG, "Weekly Progress Percent" + String.valueOf(weeklyProgressPercent));

            showContentView();

            pbDashboardWkProgress.setMax(100);
             pbDashboardWkProgress.setProgress(weeklyProgressPercent);
             tvWeeklyPercent.setText(String.valueOf(weeklyProgressPercent) + "%");
        }).addOnFailureListener(e -> {
            Log.e(TAG, e.getMessage());
        });
    }

    private void hideContentView() {
        try {
            // Hide all ImageViews
            int[] imageViewIds = {R.id.imageView98, R.id.imageView99, R.id.imageView100, R.id.imageView101,
                    R.id.imageView102, R.id.imAddWalk, R.id.imageView104, R.id.imAddWater, R.id.imSleepIcon,
                    R.id.imAddFood, R.id.imageView110, R.id.imageView107};

            for (int id : imageViewIds) {
                View view = findViewById(id);
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    imageView.setVisibility(View.GONE);
                }
            }

            // Hide all TextViews
            int[] textViewIds = {R.id.textView178, R.id.textView181, R.id.textView184, R.id.textView153,
                    R.id.tvDashboardStepPercent, R.id.textView180, R.id.tvDashboardWater, R.id.textView183,
                    R.id.tvDashboardSleep, R.id.textView187, R.id.tvDashboardCaloriePercent, R.id.textView190,
                    R.id.tvDashboardWkPercent, R.id.textView188};

            for (int id : textViewIds) {
                View view = findViewById(id);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setVisibility(View.GONE);
                }
            }

            int[] progressBarIds = {R.id.pbDashboardWkProgress, R.id.pbDashboardFood, R.id.pbDashboardStep};

            for(int id : progressBarIds){
                View view = findViewById(id);
                if(view instanceof ProgressBar){
                    ProgressBar progressBar = (ProgressBar) view;
                    progressBar.setVisibility(View.GONE);
                }
            }

            ProgressBar progressBar = findViewById(R.id.pbDashboardContent);
            progressBar.setVisibility(View.VISIBLE);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private void showContentView() {
        try {
            int[] imageViewIds = {R.id.imageView98, R.id.imageView99, R.id.imageView100, R.id.imageView101,
                    R.id.imageView102, R.id.imAddWalk, R.id.imageView104, R.id.imAddWater, R.id.imSleepIcon,
                    R.id.imAddFood, R.id.imageView110, R.id.imageView107};

            for (int id : imageViewIds) {
                View view = findViewById(id);
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    imageView.setVisibility(View.VISIBLE);
                }
            }

            int[] textViewIds = {R.id.textView178, R.id.textView181, R.id.textView184, R.id.textView153,
                    R.id.tvDashboardStepPercent, R.id.textView180, R.id.tvDashboardWater, R.id.textView183,
                    R.id.tvDashboardSleep, R.id.textView187, R.id.tvDashboardCaloriePercent, R.id.textView190,
                    R.id.tvDashboardWkPercent, R.id.textView188};

            for (int id : textViewIds) {
                View view = findViewById(id);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setVisibility(View.VISIBLE);
                }
            }

            int[] progressBarIds = {R.id.pbDashboardWkProgress, R.id.pbDashboardFood, R.id.pbDashboardStep};

            for(int id : progressBarIds){
                View view = findViewById(id);
                if(view instanceof ProgressBar){
                    ProgressBar progressBar = (ProgressBar) view;
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            ProgressBar progressBar = findViewById(R.id.pbDashboardContent);
            progressBar.setVisibility(View.GONE);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private void applyTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = prefs.getInt("themePref", 0);

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

            DataManager dataManager = new DataManager(dashboardPage.this);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            mAuth = FirebaseAuth.getInstance();
            String userId = currentUser.getUid();
            final String[] storedDate = {dataManager.getStoredDate()};
            String currentDateTime = getCurrentDateTime();

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
                        fetchStepData(docRef);
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


    private String getCurrentDateTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return sdf.format(date);
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