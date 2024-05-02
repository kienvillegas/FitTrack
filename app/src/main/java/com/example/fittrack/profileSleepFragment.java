package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class  profileSleepFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvProfileSleepTaken, tvProfileSleepDailyGoal, tvProfileSleepWeeklyGoal;
    BarChart barChart;
    Button btnProfileSleepSetGoal;

    public profileSleepFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_sleep, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        if(view != null && isAdded()){
            Log.d(TAG, "View is not null");

            tvProfileSleepTaken = view.findViewById(R.id.tvProfileSleepTaken);
            tvProfileSleepDailyGoal = view.findViewById(R.id.tvProfileSleepDailyGoal);
            tvProfileSleepWeeklyGoal = view.findViewById(R.id.tvProfileSleepWeeklyGoal);
            btnProfileSleepSetGoal = view.findViewById(R.id.btnProfileSleepSetGoal);
            barChart = view.findViewById(R.id.sleepBarChart);
            if(userId != null){
                Log.d(TAG, "Fragment is attached");
                hideContentView();
                fetchSleepData(userId);

                DocumentReference weeklySleepRef = db.collection("weekly_sleep").document(userId);
                weeklySleepRef.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if(isAdded()){
                                if(documentSnapshot.exists()){
                                    showContentView();

                                    int monSleep, tueSleep, wedSleep, thuSleep, friSleep, satSleep, sunSleep;

                                    monSleep = documentSnapshot.getLong("mon").intValue();
                                    tueSleep = documentSnapshot.getLong("tue").intValue();
                                    wedSleep = documentSnapshot.getLong("wed").intValue();
                                    thuSleep = documentSnapshot.getLong("thu").intValue();
                                    friSleep = documentSnapshot.getLong("fri").intValue();
                                    satSleep = documentSnapshot.getLong("sat").intValue();
                                    sunSleep = documentSnapshot.getLong("sun").intValue();

                                    List<BarEntry> entries = new ArrayList<>();
                                    entries.add(new BarEntry(0, monSleep));
                                    entries.add(new BarEntry(1, tueSleep));
                                    entries.add(new BarEntry(2, wedSleep));
                                    entries.add(new BarEntry(3, thuSleep));
                                    entries.add(new BarEntry(4, friSleep));
                                    entries.add(new BarEntry(5, satSleep));
                                    entries.add(new BarEntry(6, sunSleep));

                                    BarDataSet dataSet = new BarDataSet(entries, "Bar Data");
                                    TypedValue typedValue = new TypedValue();
                                    requireContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
                                    int primary = typedValue.data;
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

                btnProfileSleepSetGoal.setOnClickListener(v -> {

                    DocumentReference docRef = db.collection("users").document(userId);
                    docRef.get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if(isAdded()){
                                    if(documentSnapshot.exists()){
                                        int sleepDailyGoal, sleepWeeklyGoal;

                                        sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal").intValue();
                                        sleepWeeklyGoal = documentSnapshot.getLong("sleepWeeklyGoal").intValue();

                                        Bundle bundle = new Bundle();
                                        bundle.putInt("dailyGoal", sleepDailyGoal);
                                        bundle.putInt("weeklyGoal", sleepWeeklyGoal);

                                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        Fragment newFragment = new profileSetSleepGoalFragment();
                                        newFragment.setArguments(bundle);
                                        fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                                        fragmentTransaction.addToBackStack(null);
                                        fragmentTransaction.commit();
                                    }else{
                                        Log.e(TAG, "Document does not exist");
                                    }
                                }else{
                                    Log.d(TAG, "Fragment is not attached");
                                }
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "An error occurred: " + e.getMessage());
                            });
                });
            }else{
                Log.e(TAG, "User Id is null");
            }
        }else{
            Log.e(TAG, "View is null or Fragment is not attached");
        }
        return view;
    }

    private void fetchSleepData(String userId){
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        int weeklySleepTaken, sleepDailyGoal, sleepWeeklyGoal;

                        weeklySleepTaken = documentSnapshot.getLong("weeklySleepTaken").intValue();
                        sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal").intValue();
                        sleepWeeklyGoal = documentSnapshot.getLong("sleepWeeklyGoal").intValue();

                        String formattedHours = NumberFormat.getNumberInstance(Locale.US).format(weeklySleepTaken);
                        String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(sleepDailyGoal);
                        String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(sleepWeeklyGoal);

                        tvProfileSleepTaken.setText(formattedHours);
                        tvProfileSleepDailyGoal.setText(formattedDailyGoal);
                        tvProfileSleepWeeklyGoal.setText(formattedWeeklyGoal);
                    }else{
                        Log.d(TAG, "Document does not exist");
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "An error occurred: " + e.getMessage());
                });
    }

    private void hideContentView() {
        try {
            if (getView() == null) return; // Ensure fragment is attached

            int[] textViewIds = {R.id.textView55, R.id.tvProfileSleepTaken, R.id.textView59, R.id.textView60, R.id.textView61, R.id.tvProfileSleepDailyGoal, R.id.textView63, R.id.tvProfileSleepWeeklyGoal};
            int[] imageViewIds = {R.id.imageView34, R.id.imageView36, R.id.imageView37};
            int[] chartIds = {R.id.sleepBarChart};

            View fragmentView = getView();

            for (int id : textViewIds) {
                TextView textView = fragmentView.findViewById(id);
                textView.setVisibility(View.GONE);
            }

            for (int id : imageViewIds) {
                ImageView imageView = fragmentView.findViewById(id);
                imageView.setVisibility(View.GONE);
            }

            for (int id : chartIds) {
                BarChart barChart = fragmentView.findViewById(id);
                barChart.setVisibility(View.GONE);
            }

            Button button = fragmentView.findViewById(R.id.btnProfileSleepSetGoal);
            button.setVisibility(View.GONE);

            ProgressBar pbProfileSleep = fragmentView.findViewById(R.id.pbProfileSleep);
            pbProfileSleep.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContentView() {
        try {
            if (getView() == null) return; // Ensure fragment is attached

            int[] textViewIds = {R.id.textView55, R.id.tvProfileSleepTaken, R.id.textView59, R.id.textView60, R.id.textView61, R.id.tvProfileSleepDailyGoal, R.id.textView63, R.id.tvProfileSleepWeeklyGoal};
            int[] imageViewIds = {R.id.imageView34, R.id.imageView36, R.id.imageView37};
            int[] chartIds = {R.id.sleepBarChart};

            View fragmentView = getView();

            for (int id : textViewIds) {
                TextView textView = fragmentView.findViewById(id);
                textView.setVisibility(View.VISIBLE);
            }

            for (int id : imageViewIds) {
                ImageView imageView = fragmentView.findViewById(id);
                imageView.setVisibility(View.VISIBLE);
            }

            for (int id : chartIds) {
                BarChart barChart = fragmentView.findViewById(id);
                barChart.setVisibility(View.VISIBLE);
            }

            Button button = fragmentView.findViewById(R.id.btnProfileSleepSetGoal);
            button.setVisibility(View.VISIBLE);

            ProgressBar pbProfileSleep = fragmentView.findViewById(R.id.pbProfileSleep);
            pbProfileSleep.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}