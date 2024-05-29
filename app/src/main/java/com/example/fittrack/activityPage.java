package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
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
import androidx.fragment.app.FragmentContainerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class activityPage extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;


    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView tvActStepPercent, tvActStepTaken, tvActStepGoal, tvDateTimeRecentAct, tvActDayMonDate, tvActGreeting;
    ProgressBar pbActStep;
    ImageView imRecentActIcon;
    private PieChart pieChart;

    private StepSensorManager stepSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_acitivity_page);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvActStepTaken = findViewById(R.id.tvActStepTaken);
        tvActStepPercent = findViewById(R.id.tvActStepPercent);
        tvActStepGoal = findViewById(R.id.tvActStepGoal);
        pbActStep = findViewById(R.id.pbActStep);
        imRecentActIcon = findViewById(R.id.imRecentActIcon);
        tvDateTimeRecentAct = findViewById(R.id.tvDateTimeRecentAct);
        tvActDayMonDate = findViewById(R.id.tvActDayMonDate);
        tvActGreeting = findViewById(R.id.tvActGreeting);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavActivity);
        pieChart = findViewById(R.id.pieChart);
        bottomNav.setSelectedItemId(R.id.nav_activity);
        DataManager dataManager = new DataManager(this);
        final String[] storedDate = {dataManager.getStoredDate()};

        if(storedDate[0] == null || storedDate[0].isEmpty()){
            dataManager.saveCurrentDateTime();
            storedDate[0] = dataManager.getStoredDate();
        }

        SimpleDateFormat dayMonDate = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        Date date = new Date();
        String currentDate = dayMonDate.format(date);

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 12) {
            tvActGreeting.setText("Good Morning!");
        } else if (hour >= 12 && hour < 18) {
            tvActGreeting.setText("Good Afternoon!");
        } else {
            tvActGreeting.setText("Good Evening!");
        }

        tvActDayMonDate.setText(currentDate);

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
                startActivity(new Intent(getApplicationContext(), profilePage.class));
                finish();
                return true;
            }else if(item.getItemId() == R.id.nav_chat) {
                startActivity(new Intent(getApplicationContext(), chatPage.class));
                finish();
                return true;
            }
            return false;
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            initializeStepSensor();
        }

        hideContentView();
        DocumentReference dailyActRef = db.collection("activity_time_spent").document(userId);
        dailyActRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
              fetchPieChartData(documentSnapshot);
              showContentView();
            }else{
                showContentView();
                Log.e(TAG, "Document does not exist");
            }
            fetchRecentActs(userId);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch collection activity_time_spent: " + e.getMessage());
        });
    }

    private void fetchPieChartData(DocumentSnapshot documentSnapshot){
        int running, cycling, swimming, gym, yoga;
        running = documentSnapshot.getLong("Running").intValue();
        cycling = documentSnapshot.getLong("Cycle").intValue();
        swimming = documentSnapshot.getLong("Swim").intValue();
        yoga = documentSnapshot.getLong("Yoga").intValue();
        gym = documentSnapshot.getLong("Gym").intValue();

        ArrayList<PieEntry> entries = new ArrayList<>();
        if(running > 0)  entries.add(new PieEntry(running, "Running"));
        if(cycling > 0) entries.add(new PieEntry(cycling, "Cycling"));
        if(swimming > 0) entries.add(new PieEntry(swimming, "Swimming"));
        if(yoga > 0) entries.add(new PieEntry(yoga, "Yoga"));
        if(gym > 0) entries.add(new PieEntry(gym, "Gym"));

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 102, 0));   // Orange
        colors.add(Color.rgb(51, 153, 255));  // Blue
        colors.add(Color.rgb(255, 153, 0));   // Yellow
        colors.add(Color.rgb(0, 204, 0));     // Green
        colors.add(Color.rgb(255, 0, 0));     // Red

        float totalValue = 0f;
        for (PieEntry entry : entries) {
            totalValue += entry.getValue();
        }

        if (totalValue == 0f) {
            pieChart.setNoDataText("No chart data available");
        } else {
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setValueTextSize(14f);
            dataSet.setSliceSpace(3f);
            dataSet.setColors(colors);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter());

            pieChart.setUsePercentValues(true);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setTransparentCircleRadius(25f);
            pieChart.setHoleRadius(25f);
            pieChart.setRotationAngle(0);
            pieChart.setRotationEnabled(true);
            pieChart.animateY(1500);

            pieChart.setData(data);
            pieChart.setExtraOffsets(10f,10f,10f,10f);
            pieChart.getDescription().setEnabled(false);
            pieChart.getLegend().setEnabled(false);
            pieChart.invalidate();
        }
    }

    private void fetchRecentActs(String userId) {
        DocumentReference docRef = db.collection("recent_activities").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String actName, timeStarted, dateOfAct;
                int timeSpent;

                actName = documentSnapshot.getString("actName");
                timeStarted = documentSnapshot.getString("timeStarted");
                dateOfAct = documentSnapshot.getString("dateOfAct");
                timeSpent = documentSnapshot.getLong("timeSpent").intValue();
                String formattedTimeSpent = formatTimeSpent(timeSpent);

                if(actName != null){
                    switch (actName) {
                        case "Running":
                            imRecentActIcon.setImageResource(R.drawable.running_icon);
                            break;
                        case "Cycle":
                            imRecentActIcon.setImageResource(R.drawable.cycling_icon);
                            break;
                        case "Swim":
                            imRecentActIcon.setImageResource(R.drawable.swimming_icon);
                            break;
                        case "Yoga":
                            imRecentActIcon.setImageResource(R.drawable.yoga_icon);
                            break;
                        case "Gym":
                            imRecentActIcon.setImageResource(R.drawable.weights_icon);
                            break;
                        default:
                            Log.w(TAG, actName + " is not available!");
                    }
                    tvDateTimeRecentAct.setText(dateOfAct + " - " + timeStarted + " - " + formattedTimeSpent);
                }else{
                    Log.e(TAG, "actName is null");
                }
            } else {
                Log.e(TAG, "Document does not exist");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));
    }
    private String formatTimeSpent(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + " sec";
        } else {
            long minutes = seconds / 60;
            if (minutes < 60) {
                return minutes + " min";
            } else {
                long hours = minutes / 60;
                return hours + " hr";
            }
        }
    }

    private void hideContentView() {
        try {
            int[] imageViewIds = {R.id.imageView12, R.id.imageView74, R.id.imageView22};
            int[] textViewIds = {R.id.textView5, R.id.textView8, R.id.tvActStepTaken, R.id.textView11, R.id.textView12, R.id.textView16, R.id.textView17, R.id.textView25, R.id.tvDateTimeRecentAct, R.id.tvActStepGoal, R.id.tvActStepPercent};
            int[] progressBarIds = {R.id.pbActStep};
            int[] fragmentIds = {R.id.fragmentContainerView2};
            int[] chart = {R.id.pieChart};

            for (int id : imageViewIds) {
                ImageView imageView = findViewById(id);
                imageView.setVisibility(View.GONE);
            }

            for (int id : textViewIds) {
                TextView textView = findViewById(id);
                textView.setVisibility(View.GONE);
            }

            for (int id : progressBarIds) {
                ProgressBar progressBar = findViewById(id);
                progressBar.setVisibility(View.GONE);
            }

            for (int id : fragmentIds) {
                FragmentContainerView fragmentContainerView = findViewById(id);
                fragmentContainerView.setVisibility(View.GONE);
            }

            for (int id : chart){
                PieChart pieChart = findViewById(id);
                pieChart.setVisibility(View.GONE);
            }

            ProgressBar pbActivityContent = findViewById(R.id.pbActivityContent);
            pbActivityContent.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showContentView() {
        try {
            int[] imageViewIds = {R.id.imageView12, R.id.imageView74, R.id.imageView22};
            int[] textViewIds = {R.id.textView5, R.id.textView8, R.id.tvActStepTaken, R.id.textView11, R.id.textView12, R.id.textView16, R.id.textView17, R.id.textView25, R.id.tvDateTimeRecentAct, R.id.tvActStepGoal, R.id.tvActStepPercent};
            int[] progressBarIds = {R.id.pbActStep};
            int[] fragmentIds = {R.id.fragmentContainerView2};
            int[] chart = {R.id.pieChart};

            for (int id : imageViewIds) {
                ImageView imageView = findViewById(id);
                imageView.setVisibility(View.VISIBLE);
            }

            for (int id : textViewIds) {
                TextView textView = findViewById(id);
                textView.setVisibility(View.VISIBLE);
            }

            for (int id : progressBarIds) {
                ProgressBar progressBar = findViewById(id);
                progressBar.setVisibility(View.VISIBLE);
            }

            for (int id : fragmentIds) {
                FragmentContainerView fragmentContainerView = findViewById(id);
                fragmentContainerView.setVisibility(View.VISIBLE);
            }
            for (int id : chart){
                PieChart pieChart = findViewById(id);
                pieChart.setVisibility(View.VISIBLE);
            }

            ProgressBar pbActivityContent = findViewById(R.id.pbActivityContent);
            pbActivityContent.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
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

            DataManager dataManager = new DataManager(activityPage.this);
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