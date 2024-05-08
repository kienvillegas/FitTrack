package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class profileSetWaterGoalFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvSetWaterDaily, tvSetWaterWeekly;
    EditText etSetWaterDaily, etSetWaterWeekly;
    Button btnSetWaterGoal, btnSetWaterCancel;
    ProgressBar pbSetWater;
    public profileSetWaterGoalFragment() {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_profile_set_water_goal, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvSetWaterDaily = view.findViewById(R.id.tvSetWaterDaily);
        tvSetWaterWeekly = view.findViewById(R.id.tvSetWaterWeekly);
        etSetWaterDaily = view.findViewById(R.id.etSetWaterDaily);
        etSetWaterWeekly = view.findViewById(R.id.etSetWaterWeekly);
        btnSetWaterGoal = view.findViewById(R.id.btnSetWaterGoal);
        btnSetWaterCancel = view.findViewById(R.id.btnSetWaterCancel);
        pbSetWater = view.findViewById(R.id.pbSetWater);

        etSetWaterWeekly.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        etSetWaterDaily.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        pbSetWater.setVisibility(View.GONE);
        btnSetWaterGoal.setVisibility(View.VISIBLE);
        btnSetWaterCancel.setVisibility(View.VISIBLE);

        DocumentReference docRef = db.collection("users").document(userId);

        int waterDailyGoal, waterWeeklyGoal;
        Bundle bundle = getArguments();

        if(bundle != null){
            waterDailyGoal = bundle.getInt("dailyGoal");
            waterWeeklyGoal = bundle.getInt("weeklyGoal");
            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(waterDailyGoal);
            String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(waterWeeklyGoal);
            tvSetWaterDaily.setText(formattedDailyGoal);
            tvSetWaterWeekly.setText(formattedWeeklyGoal);
        }else{
            Log.e(TAG, "Bundle is null");
        }

        btnSetWaterGoal.setOnClickListener(v -> {
            pbSetWater.setVisibility(View.VISIBLE);
            btnSetWaterGoal.setVisibility(View.GONE);
            btnSetWaterCancel.setVisibility(View.GONE);
            etSetWaterDaily.setError(null);
            etSetWaterWeekly.setError(null);

            String dailyGoal, weeklyGoal;
            int dailyGoalInt, weeklyGoalInt;
            List<Integer> dailyGoalList = new ArrayList<>();
            List<Integer> weeklyGoalList = new ArrayList<>();

            dailyGoal = etSetWaterDaily.getText().toString().trim();
            weeklyGoal = etSetWaterWeekly.getText().toString().trim();

            if(dailyGoal.isEmpty()){
                pbSetWater.setVisibility(View.GONE);
                btnSetWaterGoal.setVisibility(View.VISIBLE);
                btnSetWaterCancel.setVisibility(View.VISIBLE);

                etSetWaterDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetWaterDaily.setError("Required");
                etSetWaterDaily.requestFocus();
                return;
            }

            dailyGoalInt = Integer.parseInt(dailyGoal);

            if(dailyGoalInt > 4000){
                pbSetWater.setVisibility(View.GONE);
                btnSetWaterGoal.setVisibility(View.VISIBLE);
                btnSetWaterCancel.setVisibility(View.VISIBLE);

                etSetWaterDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetWaterDaily.setError("Cannot be more than 4,000 mL");
                etSetWaterDaily.requestFocus();
                return;
            }

            weeklyGoalInt = weeklyGoal.isEmpty() ? dailyGoalInt * 7 : Integer.parseInt(weeklyGoal);

            if(weeklyGoalInt > 28000){
                pbSetWater.setVisibility(View.GONE);
                btnSetWaterGoal.setVisibility(View.VISIBLE);
                btnSetWaterCancel.setVisibility(View.VISIBLE);

                etSetWaterWeekly.setBackgroundResource(R.drawable.text_field_red);
                etSetWaterWeekly.setError("Cannot be more than 28,000 mL");
                etSetWaterWeekly.requestFocus();
                return;
            }

            dailyGoalList.add(dailyGoalInt);
            weeklyGoalList.add(weeklyGoalInt);

            try{
                String formattedDailyGoal, formattedWeeklyGoal;

                if(weeklyGoal.isEmpty()){
                    dailyGoalList.add(Integer.parseInt(dailyGoal));
                    weeklyGoalList.add(Integer.parseInt(dailyGoal) * 7);
                }

                if (!weeklyGoal.isEmpty()) {
                    dailyGoalList.add(Integer.parseInt(dailyGoal));
                    weeklyGoalList.add(Integer.parseInt(weeklyGoal));
                }

                formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(dailyGoalList.get(0));
                formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(weeklyGoalList.get(0));

                Map<String, Object> waterData = new HashMap<>();
                waterData.put("waterDailyGoal", dailyGoalList.get(0));
                waterData.put("waterWeeklyGoal", weeklyGoalList.get(0));

                docRef.update(waterData)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Successfully updated water daily and weekly goal");
                            tvSetWaterDaily.setText(formattedDailyGoal);
                            tvSetWaterWeekly.setText(formattedWeeklyGoal);
                            etSetWaterDaily.setText("");
                            etSetWaterWeekly.setText("");

                            pbSetWater.setVisibility(View.GONE);
                            btnSetWaterCancel.setVisibility(View.VISIBLE);
                            btnSetWaterGoal.setVisibility(View.VISIBLE);

                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            Fragment newFragment = new profileWaterFragment();
                            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update water daily and weekly goal");
                            pbSetWater.setVisibility(View.GONE);
                            btnSetWaterCancel.setVisibility(View.VISIBLE);
                            btnSetWaterGoal.setVisibility(View.VISIBLE);
                        });
            }catch (Exception e){
                Log.e(TAG, "An error occurred: " + e.getMessage());
                pbSetWater.setVisibility(View.GONE);
                btnSetWaterGoal.setVisibility(View.VISIBLE);
                btnSetWaterCancel.setVisibility(View.VISIBLE);
            }
        });

        btnSetWaterCancel.setOnClickListener(v1 -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new profileWaterFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        etSetWaterDaily.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etSetWaterDaily.setText(filteredText);
                }

                etSetWaterDaily.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetWaterDaily.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSetWaterWeekly.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etSetWaterWeekly.setText(filteredText);
                }
                etSetWaterWeekly.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetWaterWeekly.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;    }
}