package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
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

public class profileWaterFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvProfileWaterTaken, tvProfileWaterDailyGoal, tvProfileWaterWeeklyGoal;
    Button btnPofileWaterSetGoal;
    BarChart barChart;


    public profileWaterFragment() {
    }

    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    public void onStart() {
        super.onStart();
        // Add auth state listener when the fragment starts
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove auth state listener when the fragment stops
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the authStateListener when the fragment is destroyed
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize AuthStateListener
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Log.d(TAG, "onAuthStateChanged:signed_out");
                // Example: Redirect to sign-in fragment
                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Sign In Required")
                        .setMessage("Please sign in to access this feature.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Handle sign-in action or any other action
                            // If currentUser is null, navigate to the sign-in activity
                            Intent intent = new Intent(requireContext(), signIn.class);
                            startActivity(intent);
                            requireActivity().finish(); // Finish the current activity
                        })
                        .setCancelable(false) // Set dialog non-cancelable
                        .show();
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove the authStateListener to prevent memory leaks
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the authStateListener when the fragment is resumed
        if (authStateListener != null) {
            mAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile_water, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        if(view != null && isAdded()){
            Log.d(TAG, "View is not null");

            tvProfileWaterTaken = view.findViewById(R.id.tvProfileWaterTaken);
            tvProfileWaterDailyGoal = view.findViewById(R.id.tvProfileWaterDailyGoal);
            tvProfileWaterWeeklyGoal = view.findViewById(R.id.tvProfileWaterWeeklyGoal);
            btnPofileWaterSetGoal = view.findViewById(R.id.btnProfileWaterSetGoal);
            barChart = view.findViewById(R.id.waterBarChart);

            authStateListener = firebaseAuth -> {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    // Example: Redirect to sign-in fragment
                    androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Sign In Required")
                            .setMessage("Please sign in to access this feature.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Handle sign-in action or any other action
                                // If currentUser is null, navigate to the sign-in activity
                                Intent intent = new Intent(requireContext(), signIn.class);
                                startActivity(intent);
                                requireActivity().finish(); // Finish the current activity
                            })
                            .setCancelable(false) // Set dialog non-cancelable
                            .show();
                    return;
                }
                DocumentReference weeklyWaterRef = db.collection("weekly_water").document(userId);
                DocumentReference docRef = db.collection("users").document(userId);

                if(userId != null){
                    Log.d(TAG, "Fragment is attached");
                    hideContentView();
                    fetchWaterData(userId);
                    displayBarChart(weeklyWaterRef);

                    btnPofileWaterSetGoal.setOnClickListener(v -> {
                        docRef.get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if(isAdded()){
                                        if(documentSnapshot.exists()){
                                            int waterDailyGoal, waterWeeklyGoal;

                                            waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();
                                            waterWeeklyGoal = documentSnapshot.getLong("waterWeeklyGoal").intValue();

                                            Bundle bundle = new Bundle();
                                            bundle.putInt("dailyGoal", waterDailyGoal);
                                            bundle.putInt("weeklyGoal", waterWeeklyGoal);

                                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            Fragment newFragment = new profileSetWaterGoalFragment();
                                            newFragment.setArguments(bundle);
                                            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
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
                    Log.e(TAG, "User Id is null");
                }
            };
        }else{
            Log.e(TAG, "View is null or Fragment is not attached");
        }

        return view;
    }

    private void displayBarChart(DocumentReference weeklyWaterRef){
        weeklyWaterRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(isAdded()){
                        if(documentSnapshot.exists()){
                            showContentView();

                            int monWater, tueWater, wedWater, thuWater, friWater, satWater, sunWater;
                            monWater = documentSnapshot.getLong("mon").intValue();
                            tueWater = documentSnapshot.getLong("tue").intValue();
                            wedWater = documentSnapshot.getLong("wed").intValue();
                            thuWater = documentSnapshot.getLong("thu").intValue();
                            friWater = documentSnapshot.getLong("fri").intValue();
                            satWater = documentSnapshot.getLong("sat").intValue();
                            sunWater = documentSnapshot.getLong("sun").intValue();

                            List<BarEntry> entries = new ArrayList<>();
                            entries.add(new BarEntry(0, monWater));
                            entries.add(new BarEntry(1, tueWater));
                            entries.add(new BarEntry(2, wedWater));
                            entries.add(new BarEntry(3, thuWater));
                            entries.add(new BarEntry(4, friWater));
                            entries.add(new BarEntry(5, satWater));
                            entries.add(new BarEntry(6, sunWater));

                            BarDataSet dataSet = new BarDataSet(entries, "Bar Data");
                            TypedValue typedValue = new TypedValue();
                            requireContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
                            int primary = typedValue.data;                                    int tertiary = ContextCompat.getColor(requireContext(), R.color.tertiaryDark);
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
                            barChart.animateY(1000);
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
    }
    private void fetchWaterData(String userId){
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        int weeklyWaterTaken, waterDailyGoal, waterWeeklyGoal;
                        weeklyWaterTaken = documentSnapshot.getLong("weeklyWaterTaken").intValue();
                        waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();
                        waterWeeklyGoal = documentSnapshot.getLong("waterWeeklyGoal").intValue();

                        String formattedWater = NumberFormat.getNumberInstance(Locale.US).format(weeklyWaterTaken);
                        String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(waterDailyGoal);
                        String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(waterWeeklyGoal);

                        tvProfileWaterTaken.setText(formattedWater);
                        tvProfileWaterDailyGoal.setText(formattedDailyGoal);
                        tvProfileWaterWeeklyGoal.setText(formattedWeeklyGoal);
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

            int[] textViewIds = {R.id.textView38, R.id.tvProfileWaterTaken, R.id.textView40, R.id.textView41, R.id.textView42, R.id.tvProfileWaterDailyGoal, R.id.textView44, R.id.tvProfileWaterWeeklyGoal};
            int[] imageViewIds = {R.id.imageView25, R.id.imageView26, R.id.imageView28};
            int[] chartIds = {R.id.waterBarChart};

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

            Button button = fragmentView.findViewById(R.id.btnProfileWaterSetGoal);
            button.setVisibility(View.GONE);

            ProgressBar pbProfileWater = fragmentView.findViewById(R.id.pbProfileWater);
            pbProfileWater.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContentView() {
        try {
            if (getView() == null) return; // Ensure fragment is attached

            int[] textViewIds = {R.id.textView38, R.id.tvProfileWaterTaken, R.id.textView40, R.id.textView41, R.id.textView42, R.id.tvProfileWaterDailyGoal, R.id.textView44, R.id.tvProfileWaterWeeklyGoal};
            int[] imageViewIds = {R.id.imageView25, R.id.imageView26, R.id.imageView28};
            int[] chartIds = {R.id.waterBarChart};

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

            Button button = fragmentView.findViewById(R.id.btnProfileWaterSetGoal);
            button.setVisibility(View.VISIBLE);

            ProgressBar pbProfileWater = fragmentView.findViewById(R.id.pbProfileWater);
            pbProfileWater.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}