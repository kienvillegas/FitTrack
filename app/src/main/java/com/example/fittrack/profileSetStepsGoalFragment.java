package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.Inet4Address;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class profileSetStepsGoalFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvSetStepDaily, tvSetStepWeekly;
    EditText etSetStepDaily, etSetStepWeekly;
    Button btnSetStepGoal, btnSetStepCancel;
    ProgressBar pbSetStep;

    public profileSetStepsGoalFragment() {
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
        View view = inflater.inflate(R.layout.fragment_profile_set_steps_goal, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvSetStepDaily = view.findViewById(R.id.tvSetStepDaily);
        tvSetStepWeekly = view.findViewById(R.id.tvSetStepWeekly);
        etSetStepDaily = view.findViewById(R.id.etSetStepDaily);
        etSetStepWeekly = view.findViewById(R.id.etSetStepWeekly);
        btnSetStepCancel = view.findViewById(R.id.btnSetStepCancel);
        btnSetStepGoal = view.findViewById(R.id.btnSetStepGoal);
        pbSetStep = view.findViewById(R.id.pbSetStep);

        etSetStepDaily.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        etSetStepWeekly.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        pbSetStep.setVisibility(View.GONE);
        btnSetStepCancel.setVisibility(View.VISIBLE);
        btnSetStepGoal.setVisibility(View.VISIBLE);

        DocumentReference docRef = db.collection("users").document(userId);

        int stepDailyGoal, stepWeeklyGoal;
        Bundle bundle = getArguments();

        if(bundle != null){
            stepDailyGoal = bundle.getInt("dailyGoal");
            stepWeeklyGoal = bundle.getInt("weeklyGoal");
            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(stepDailyGoal);
            String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(stepWeeklyGoal);
            tvSetStepDaily.setText(formattedDailyGoal);
            tvSetStepWeekly.setText(formattedWeeklyGoal);
        }else{
            Log.e(TAG, "Bundle is null");
        }

        btnSetStepGoal.setOnClickListener(v -> {
            pbSetStep.setVisibility(View.VISIBLE);
            btnSetStepCancel.setVisibility(View.GONE);
            btnSetStepGoal.setVisibility(View.GONE);
            etSetStepDaily.setError(null);
            etSetStepWeekly.setError(null);


            String dailyGoal, weeklyGoal;
            int dailyGoalInt, weeklyGoalInt;
            List<Integer> dailyGoalList = new ArrayList<>();
            List<Integer> weeklyGoalList = new ArrayList<>();

            dailyGoal = etSetStepDaily.getText().toString().trim();
            weeklyGoal = etSetStepWeekly.getText().toString().trim();
            Log.d(TAG, "" + weeklyGoal);

            if(dailyGoal.isEmpty()){
                pbSetStep.setVisibility(View.GONE);
                btnSetStepCancel.setVisibility(View.VISIBLE);
                btnSetStepGoal.setVisibility(View.VISIBLE);

                etSetStepDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetStepDaily.setError("Required");
                etSetStepDaily.requestFocus();
                return;
            }

            dailyGoalInt = Integer.parseInt(dailyGoal);

            if(dailyGoalInt > 10000){
                pbSetStep.setVisibility(View.GONE);
                btnSetStepGoal.setVisibility(View.VISIBLE);
                btnSetStepCancel.setVisibility(View.VISIBLE);

                etSetStepDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetStepDaily.setError("Cannot be more than 10,000 steps");
                etSetStepDaily.requestFocus();
                return;
            }

            weeklyGoalInt = weeklyGoal.isEmpty() ? dailyGoalInt * 7 : Integer.parseInt(weeklyGoal);

            if(weeklyGoalInt > 70000){
                pbSetStep.setVisibility(View.GONE);
                btnSetStepGoal.setVisibility(View.VISIBLE);
                btnSetStepCancel.setVisibility(View.VISIBLE);

                etSetStepWeekly.setBackgroundResource(R.drawable.text_field_red);
                etSetStepWeekly.setError("Cannot be more than 70,000 steps");
                etSetStepWeekly.requestFocus();
                return;
            }

            dailyGoalList.add(dailyGoalInt);
            weeklyGoalList.add(weeklyGoalInt);

            try{
                String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(dailyGoalList.get(0));
                String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(weeklyGoalList.get(0));

                Map<String, Object> stepData = new HashMap<>();
                stepData.put("stepDailyGoal", dailyGoalList.get(0));
                stepData.put("stepWeeklyGoal", weeklyGoalList.get(0));

                docRef.update(stepData)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Successfully updated step daily and weekly goal");
                            tvSetStepDaily.setText(formattedDailyGoal);
                            tvSetStepWeekly.setText(formattedWeeklyGoal);
                            etSetStepDaily.setText("");
                            etSetStepWeekly.setText("");

                            pbSetStep.setVisibility(View.GONE);
                            btnSetStepCancel.setVisibility(View.VISIBLE);
                            btnSetStepGoal.setVisibility(View.VISIBLE);

                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            Fragment newFragment = new profileStepsFragment();
                            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();

                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update step daily and weekly goal");
                            pbSetStep.setVisibility(View.GONE);
                            btnSetStepCancel.setVisibility(View.VISIBLE);
                            btnSetStepGoal.setVisibility(View.VISIBLE);
                        });

            }catch (Exception e){
                Log.e(TAG, "An error occurred: " + e.getMessage());
                pbSetStep.setVisibility(View.GONE);
                btnSetStepCancel.setVisibility(View.VISIBLE);
                btnSetStepGoal.setVisibility(View.VISIBLE);
            }
        });

        btnSetStepCancel.setOnClickListener(v1 -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new profileStepsFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });


        etSetStepDaily.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etSetStepDaily.setText(filteredText);
                }

                etSetStepDaily.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetStepDaily.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSetStepWeekly.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == '0') {
                    String filteredText = s.toString().substring(1);
                    etSetStepWeekly.setText(filteredText);
                }

                etSetStepWeekly.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetStepWeekly.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }
}