package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class profileCalorieFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvProfileCalorieTaken, tvProfileCalorieDailyGoal, tvProfileCalorieWeeklyGoal;
    Button btnProfileCalorieSetGoal;
    BarChart barChart;
    public profileCalorieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile_calorie, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        if(view != null && isAdded()){
            Log.d(TAG, "View is not null");
            tvProfileCalorieTaken = view.findViewById(R.id.tvProfileCalorieTaken);
            tvProfileCalorieDailyGoal = view.findViewById(R.id.tvProfileCaloreiDailyGoal);
            tvProfileCalorieWeeklyGoal = view.findViewById(R.id.tvProfileCalorieWeeklyGoal);
            btnProfileCalorieSetGoal = view.findViewById(R.id.btnProfileCalorieSetGoal);
            barChart = view.findViewById(R.id.calorieBarChart);
            if(userId != null){
                Log.d(TAG, "Fragment is attached");
                fetchWaterData(userId);
                DocumentReference weeklyCalorieRef = db.collection("weekly_calorie").document(userId);
                weeklyCalorieRef.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if(isAdded()){
                                if(documentSnapshot.exists()){
                                    int monCalorie, tueCalorie, wedCalorie, thuCalorie, friCalorie, satCalorie, sunCalorie;
                                    monCalorie = documentSnapshot.getLong("mon").intValue();
                                    tueCalorie = documentSnapshot.getLong("tue").intValue();
                                    wedCalorie = documentSnapshot.getLong("wed").intValue();
                                    thuCalorie = documentSnapshot.getLong("thu").intValue();
                                    friCalorie = documentSnapshot.getLong("fri").intValue();
                                    satCalorie = documentSnapshot.getLong("sat").intValue();
                                    sunCalorie = documentSnapshot.getLong("sun").intValue();

                                    List<BarEntry> entries = new ArrayList<>();
                                    entries.add(new BarEntry(0, monCalorie));
                                    entries.add(new BarEntry(1, tueCalorie));
                                    entries.add(new BarEntry(2, wedCalorie));
                                    entries.add(new BarEntry(3, thuCalorie));
                                    entries.add(new BarEntry(4, friCalorie));
                                    entries.add(new BarEntry(5, satCalorie));
                                    entries.add(new BarEntry(6, sunCalorie));

                                    BarDataSet dataSet = new BarDataSet(entries, "Bar Data");
                                    int primary = ContextCompat.getColor(requireContext(), R.color.primary);
                                    int tertiary = ContextCompat.getColor(requireContext(), R.color.tertiaryDark);
                                    dataSet.setColor(primary);
                                    dataSet.setDrawValues(false);
                                    BarData barData = new BarData(dataSet);
                                    barData.setBarWidth(0.5f);
                                    XAxis xAxis = barChart.getXAxis();
                                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                    xAxis.setDrawLabels(true);
                                    xAxis.setDrawGridLines(true);
                                    xAxis.setDrawAxisLine(true);
                                    xAxis.setGridColor(tertiary);
                                    xAxis.setGranularity(1f);
                                    xAxis.setTextSize(12f);
                                    xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"}));

                                    YAxis yAxisRight = barChart.getAxisRight();
                                    YAxis yAxisLeft = barChart.getAxisLeft();
                                    yAxisLeft.setDrawGridLines(false);
                                    yAxisRight.setDrawGridLines(false);
                                    yAxisLeft.setTextSize(12f);
                                    yAxisRight.setTextSize(12f);
                                    yAxisRight.setEnabled(false);

                                    barChart.setExtraOffsets(20f, 20f, 20f, 20f);
                                    barChart.getDescription().setEnabled(false);
                                    barChart.getLegend().setEnabled(false);
                                    barChart.setData(barData);
                                    barChart.invalidate();
                                }else{
                                    Log.d(TAG, "Document does not exist");
                                }
                            }else{
                                Log.e(TAG, "Fragment is not attached");
                            }
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "An error occurred: " + e.getMessage());
                        });

                btnProfileCalorieSetGoal.setOnClickListener(v -> {

                    DocumentReference docRef = db.collection("users").document(userId);
                    docRef.get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if(isAdded()){
                                    if(documentSnapshot.exists()){
                                        int calorieDailyGoal, calorieWeeklyGoal;

                                        calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();
                                        calorieWeeklyGoal = documentSnapshot.getLong("calorieWeeklyGoal").intValue();

                                        Bundle bundle = new Bundle();
                                        bundle.putInt("dailyGoal", calorieDailyGoal);
                                        bundle.putInt("weeklyGoal", calorieWeeklyGoal);

                                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        Fragment newFragment = new profileSetCalorieGoalFragment();
                                        newFragment.setArguments(bundle);
                                        fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                                        fragmentTransaction.addToBackStack(null);
                                        fragmentTransaction.commit();
                                    }else{
                                        Log.e(TAG, "Document does not exist");
                                    }
                                }else{
                                    Log.e(TAG, "Fragment is not attached");
                                }
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "An error occurred: " + e.getMessage());
                            });
                });
            }else{
                Log.e(TAG, "User ID is null");
            }
        }else{
            Log.e(TAG, "View is null or Fragment is not attached");
        }
        return view;
    }
    private void fetchWaterData(String userId){
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        int weeklyCalorieTaken, calorieDailyGoal, calorieWeeklyGoal;
                        weeklyCalorieTaken = documentSnapshot.getLong("weeklyCalorieTaken").intValue();
                        calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();
                        calorieWeeklyGoal = documentSnapshot.getLong("calorieWeeklyGoal").intValue();

                        String formattedCalorie = NumberFormat.getNumberInstance(Locale.US).format(weeklyCalorieTaken);
                        String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(calorieDailyGoal);
                        String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(calorieWeeklyGoal);

                        tvProfileCalorieTaken.setText(formattedCalorie);
                        tvProfileCalorieDailyGoal.setText(formattedDailyGoal);
                        tvProfileCalorieWeeklyGoal.setText(formattedWeeklyGoal);
                    }else{
                        Log.d(TAG, "Document does not exist");
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "An error occurred: " + e.getMessage());
                });
    }
}