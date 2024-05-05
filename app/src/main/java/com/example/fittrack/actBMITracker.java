package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class actBMITracker extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView imBackBtn;
    EditText etBMITrackeWeight, etBMITrackeHeight;
    Button btnEnterData;
    ProgressBar pbEnterData;
    LineChart lineChart;
    private StepSensorManager stepSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_act_bmitracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        imBackBtn = findViewById(R.id.imBMITrackerBack);
        etBMITrackeWeight = findViewById(R.id.etBMITrackerWeight);
        etBMITrackeHeight = findViewById(R.id.etBMITrackerHeight);
        btnEnterData = findViewById(R.id.btnEnterData);
        pbEnterData = findViewById(R.id.pbEnterData);
        lineChart = findViewById(R.id.lineChart);

        DocumentReference docRef = db.collection("users").document(userId);
        DocumentReference bmiRef = db.collection("bmi").document(userId);
        fetchBMIData(bmiRef);

        pbEnterData.setVisibility(View.GONE);
        btnEnterData.setVisibility(View.VISIBLE);

        btnEnterData.setOnClickListener(v -> {
            pbEnterData.setVisibility(View.VISIBLE);
            btnEnterData.setVisibility(View.GONE);

            String weight, height;
            weight = etBMITrackeWeight.getText().toString().trim();
            height = etBMITrackeHeight.getText().toString().trim();

            if(weight.isEmpty()){
                pbEnterData.setVisibility(View.GONE);
                btnEnterData.setVisibility(View.VISIBLE);
                etBMITrackeWeight.setBackgroundResource(R.drawable.text_field_red);
                etBMITrackeWeight.setError("Required");
                return;
            }

            if(height.isEmpty()){
                pbEnterData.setVisibility(View.GONE);
                btnEnterData.setVisibility(View.VISIBLE);
                etBMITrackeHeight.setBackgroundResource(R.drawable.text_field_red);
                etBMITrackeHeight.setError("Required");
                return;
            }

            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    String day = getCurrentDay();

                    boolean isDailyBMITaken;
                    double heightDbl, weightDbl, bmi;

                    heightDbl = Double.parseDouble(height);
                    weightDbl = Double.parseDouble(weight);
                    bmi = weightDbl / Math.pow(heightDbl, 2);

                    Map<String,Object> bmiRecords = new HashMap<>();
                    switch(day.toLowerCase()){
                        case "mon":
                            bmiRecords.put("mon", bmi);
                            break;
                        case "tue":
                            bmiRecords.put("tue",bmi);
                            break;
                        case "wed":
                            bmiRecords.put("wed", bmi);
                            break;
                        case "thu":
                            bmiRecords.put("thu", bmi);
                            break;
                        case "fri":
                            bmiRecords.put("fri", bmi);
                            break;
                        case "sat":
                            bmiRecords.put("sat", bmi);
                            break;
                        case "sun":
                            bmiRecords.put("sun", bmi);
                            break;
                    }
                    bmiRef.update(bmiRecords)
                            .addOnSuccessListener(unused -> {
                                pbEnterData.setVisibility(View.GONE);
                                btnEnterData.setVisibility(View.VISIBLE);
                                etBMITrackeWeight.setText("");
                                etBMITrackeHeight.setText("");

                                fetchBMIData(bmiRef);

                                Log.d(TAG, "Successfully saved bmi: " + bmiRecords);
                            }).addOnFailureListener(e -> {
                                pbEnterData.setVisibility(View.GONE);
                                btnEnterData.setVisibility(View.VISIBLE);
                                Log.e(TAG, "Failed to save bmi: " + e.getMessage());
                            });
                }else{
                    pbEnterData.setVisibility(View.GONE);
                    btnEnterData.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Document does not exist");
                }
            }).addOnFailureListener(e -> {
                pbEnterData.setVisibility(View.GONE);
                btnEnterData.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error Occured fetching bmi data: " + e.getMessage());
            });
        });
        imBackBtn.setOnClickListener(view -> onBackPressed());
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    private String getCurrentDay(){
        Date date = new Date();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        return dayFormat.format(date);
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

            DataManager dataManager = new DataManager(actBMITracker.this);
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

    private void fetchBMIData(DocumentReference bmiRef){
        bmiRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                Double[] bmiData = new Double[7];
                bmiData[0] = documentSnapshot.getDouble("mon");
                bmiData[1] = documentSnapshot.getDouble("tue");
                bmiData[2] = documentSnapshot.getDouble("wed");
                bmiData[3] = documentSnapshot.getDouble("thu");
                bmiData[4] = documentSnapshot.getDouble("fri");
                bmiData[5] = documentSnapshot.getDouble("sat");
                bmiData[6] = documentSnapshot.getDouble("sun");

                displayChart(bmiData);
            }else{
                Log.e(TAG, "Document does not exist");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch bmi data: " + e.getMessage());
        });
    }

    private void displayChart(Double [] bmiData){
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < bmiData.length; i++) {
            if(bmiData[i] != null){
                entries.add(new Entry(i, bmiData[i].floatValue()));
            }else{
                entries.add(new Entry(i, 0f));
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "BMI Data");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextSize(12f);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(bmiData.length, true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextSize(12f);

        lineChart.getAxisLeft().setDrawAxisLine(true);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setExtraOffsets(20f, 20f, 20f, 20f);
        lineChart.getLegend().setEnabled(false);
        lineChart.animateY(1000);

        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setHighlightPerDragEnabled(true);
        lineChart.setHighlightPerTapEnabled(true);

        lineChart.invalidate();
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
