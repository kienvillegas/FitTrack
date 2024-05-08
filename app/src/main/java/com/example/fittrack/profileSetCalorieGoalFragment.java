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

public class profileSetCalorieGoalFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvSetCalorieDaily , tvSetCalorieWeekly;
    EditText etSetCalorieDaily, etSetCalorieWeekly;
    Button btnSetCalorieGoal, btnSetCalorieCancel;
    ProgressBar pbSetCalorie;

    public profileSetCalorieGoalFragment() {
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
        View view = inflater.inflate(R.layout.fragment_profile_set_calorie_goal, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userId = currentUser.getUid();

        tvSetCalorieDaily = view.findViewById(R.id.tvSetCalorieDaily);
        tvSetCalorieWeekly = view.findViewById(R.id.tvSetCalorieWeekly);
        etSetCalorieDaily = view.findViewById(R.id.etSetCalorieDaily);
        etSetCalorieWeekly = view.findViewById(R.id.etSetCalorieWeekly);
        btnSetCalorieCancel = view.findViewById(R.id.btnSetCalorieCancel);
        btnSetCalorieGoal = view.findViewById(R.id.btnSetCalorieGoal);
        pbSetCalorie = view.findViewById(R.id.pbSetCalorie);

        etSetCalorieDaily.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        etSetCalorieWeekly.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        pbSetCalorie.setVisibility(View.GONE);
        btnSetCalorieCancel.setVisibility(View.VISIBLE);
        btnSetCalorieGoal.setVisibility(View.VISIBLE);

        DocumentReference docRef = db.collection("users").document(userId);

        int calorieDailyGoal, calorieWeeklyGoal;
        Bundle bundle = getArguments();

        if(bundle != null){
            calorieDailyGoal = bundle.getInt("dailyGoal");
            calorieWeeklyGoal = bundle.getInt("weeklyGoal");

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(calorieDailyGoal);
            String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(calorieWeeklyGoal);

            Log.d(TAG, calorieDailyGoal + " " + " " + calorieWeeklyGoal);
            tvSetCalorieDaily.setText(formattedDailyGoal);
            tvSetCalorieWeekly.setText(formattedWeeklyGoal);
        }else{
            Log.e(TAG, "Bundle is null");
        }

        btnSetCalorieGoal.setOnClickListener(v -> {
            pbSetCalorie.setVisibility(View.VISIBLE);
            btnSetCalorieCancel.setVisibility(View.GONE);
            btnSetCalorieGoal.setVisibility(View.GONE);

            etSetCalorieDaily.setError(null);
            etSetCalorieWeekly.setError(null);

            String dailyGoal, weeklyGoal;
            int dailyGoalInt, weeklyGoalInt;
            List<Integer> dailyGoalList = new ArrayList<Integer>();
            List<Integer> weeklyGoalList = new ArrayList<Integer>();

            dailyGoal = etSetCalorieDaily.getText().toString().trim();
            weeklyGoal = etSetCalorieWeekly.getText().toString().trim();


            if(dailyGoal.isEmpty()){
                pbSetCalorie.setVisibility(View.GONE);
                btnSetCalorieCancel.setVisibility(View.VISIBLE);
                btnSetCalorieGoal.setVisibility(View.VISIBLE);

                etSetCalorieDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetCalorieDaily.setError("Required");
                return;
            }

            dailyGoalInt = Integer.parseInt(dailyGoal);

            if(dailyGoalInt > 3000){
                pbSetCalorie.setVisibility(View.GONE);
                btnSetCalorieGoal.setVisibility(View.VISIBLE);
                btnSetCalorieCancel.setVisibility(View.VISIBLE);

                etSetCalorieDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetCalorieDaily.setError("Cannot be more than 3,000 Kcal");
                etSetCalorieDaily.requestFocus();
                return;
            }

            weeklyGoalInt = weeklyGoal.isEmpty() ? dailyGoalInt * 7 : Integer.parseInt(weeklyGoal);

            if(weeklyGoalInt > 21000){
                pbSetCalorie.setVisibility(View.GONE);
                btnSetCalorieGoal.setVisibility(View.VISIBLE);
                btnSetCalorieCancel.setVisibility(View.VISIBLE);

                etSetCalorieWeekly.setBackgroundResource(R.drawable.text_field_red);
                etSetCalorieWeekly.setError("Cannot be more than 21,000 Kcal");
                etSetCalorieWeekly.requestFocus();
                return;
            }

            dailyGoalList.add(dailyGoalInt);
            weeklyGoalList.add(weeklyGoalInt);

            try{
                String formattedDailyGoal, formattedWeeklyGoal;

                formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(dailyGoalList.get(0));
                formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(weeklyGoalList.get(0));

                Map<String, Object> calorieData = new HashMap<>();
                calorieData.put("calorieDailyGoal", dailyGoalList.get(0));
                calorieData.put("calorieWeeklyGoal", weeklyGoalList.get(0));

                docRef.update(calorieData)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Successfully updated calorie daily and weekly goal");

                            tvSetCalorieDaily.setText(formattedDailyGoal);
                            tvSetCalorieWeekly.setText(formattedWeeklyGoal);
                            etSetCalorieDaily.setText("");
                            etSetCalorieWeekly.setText("");
                            pbSetCalorie.setVisibility(View.GONE);
                            btnSetCalorieCancel.setVisibility(View.VISIBLE);
                            btnSetCalorieGoal.setVisibility(View.VISIBLE);

                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            Fragment newFragment = new profileCalorieFragment();
                            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update calorie daily and weekly goal");
                            pbSetCalorie.setVisibility(View.GONE);
                            btnSetCalorieCancel.setVisibility(View.VISIBLE);
                            btnSetCalorieGoal.setVisibility(View.VISIBLE);
                        });
            }catch (Exception e){
                Log.e(TAG, "An error occurred: " + e.getMessage());
                pbSetCalorie.setVisibility(View.GONE);
                btnSetCalorieCancel.setVisibility(View.VISIBLE);
                btnSetCalorieGoal.setVisibility(View.VISIBLE);
            }
        });

        btnSetCalorieCancel.setOnClickListener(v1 -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new profileCalorieFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        etSetCalorieDaily.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etSetCalorieDaily.setText(filteredText);
                }

                etSetCalorieDaily.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetCalorieDaily.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSetCalorieWeekly.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etSetCalorieWeekly.setText(filteredText);
                }

                etSetCalorieWeekly.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetCalorieWeekly.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }
}