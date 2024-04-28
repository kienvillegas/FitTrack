package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class profileSetSleepGoalFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView tvSetSleepDaily, tvSetSleepWeekly;
    EditText etSetSleepDaily, etSetSleepWeekly;
    Button btnSetSleepGoal, btnSetSleepCancel;
    ProgressBar pbSetSleep;

    public profileSetSleepGoalFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_set_sleep_goal, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvSetSleepDaily = view.findViewById(R.id.tvSetSleepDaily);
        tvSetSleepWeekly = view.findViewById(R.id.tvSetSleepWeekly);
        etSetSleepDaily = view.findViewById(R.id.etSetSleepDaily);
        etSetSleepWeekly = view.findViewById(R.id.etSetSleepWeekly);
        btnSetSleepCancel = view.findViewById(R.id.btnSetSleepCancel);
        btnSetSleepGoal = view.findViewById(R.id.btnSetSleepGoal);
        pbSetSleep = view.findViewById(R.id.pbSetSleep);

        pbSetSleep.setVisibility(View.GONE);
        btnSetSleepGoal.setVisibility(View.VISIBLE);
        btnSetSleepCancel.setVisibility(View.VISIBLE);

        int sleepDailyGoal, sleepWeeklyGoal;
        Bundle bundle = getArguments();

        if (bundle != null) {
            sleepDailyGoal = bundle.getInt("dailyGoal");
            sleepWeeklyGoal = bundle.getInt("weeklyGoal");
            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(sleepDailyGoal);
            String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(sleepWeeklyGoal);
            tvSetSleepDaily.setText(formattedDailyGoal);
            tvSetSleepWeekly.setText(formattedWeeklyGoal);
        } else {
            Log.e(TAG, "Bundle is null");
        }

        btnSetSleepGoal.setOnClickListener(v -> {
            pbSetSleep.setVisibility(View.VISIBLE);
            btnSetSleepGoal.setVisibility(View.GONE);
            btnSetSleepCancel.setVisibility(View.GONE);
            etSetSleepDaily.setError(null);
            etSetSleepWeekly.setError(null);

            String dailyGoal, weeklyGoal;
            dailyGoal = etSetSleepDaily.getText().toString().trim();
            weeklyGoal = etSetSleepWeekly.getText().toString().trim();

            try {
                if (dailyGoal.isEmpty()) {
                    pbSetSleep.setVisibility(View.GONE);
                    btnSetSleepGoal.setVisibility(View.VISIBLE);
                    btnSetSleepCancel.setVisibility(View.VISIBLE);

                    etSetSleepDaily.setBackgroundResource(R.drawable.text_field_red);
                    etSetSleepDaily.setError("Required");
                    etSetSleepDaily.requestFocus();
                    return;
                }

                if (weeklyGoal.isEmpty()) {
                    pbSetSleep.setVisibility(View.GONE);
                    btnSetSleepGoal.setVisibility(View.VISIBLE);
                    btnSetSleepCancel.setVisibility(View.VISIBLE);

                    etSetSleepWeekly.setBackgroundResource(R.drawable.text_field_red);
                    etSetSleepWeekly.setError("Required");
                    etSetSleepWeekly.requestFocus();
                    return;
                }

                if (!dailyGoal.isEmpty() && !dailyGoal.isEmpty()) {
                    int dailyGoalInt = Integer.parseInt(dailyGoal);
                    int weeklyGoalInt = Integer.parseInt(weeklyGoal);

                    DocumentReference docRef = db.collection("users").document(userId);
                    Map<String, Object> calorieData = new HashMap<>();
                    calorieData.put("sleepDailyGoal", Integer.parseInt(dailyGoal));
                    calorieData.put("sleepWeeklyGoal", Integer.parseInt(weeklyGoal));

                    docRef.update(calorieData)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Successfully updated sleep daily and weekly goal");
                                String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(dailyGoalInt);
                                String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(weeklyGoalInt);
                                tvSetSleepDaily.setText(formattedDailyGoal);
                                tvSetSleepWeekly.setText(formattedWeeklyGoal);
                                etSetSleepDaily.setText("");
                                etSetSleepWeekly.setText("");

                                pbSetSleep.setVisibility(View.GONE);
                                btnSetSleepGoal.setVisibility(View.VISIBLE);
                                btnSetSleepCancel.setVisibility(View.VISIBLE);

                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                Fragment newFragment = new profileSleepFragment();
                                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update sleep daily and weekly goal");
                            });
                } else {
                    Toast.makeText(requireContext(), "Please fill in the missing fields", Toast.LENGTH_SHORT).show();
                    pbSetSleep.setVisibility(View.GONE);
                    btnSetSleepGoal.setVisibility(View.VISIBLE);
                    btnSetSleepCancel.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                Log.e(TAG, "An error occurred: " + e.getMessage());
                pbSetSleep.setVisibility(View.GONE);
                btnSetSleepGoal.setVisibility(View.VISIBLE);
                btnSetSleepCancel.setVisibility(View.VISIBLE);
            }
        });

        btnSetSleepCancel.setOnClickListener(v1 -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new profileSleepFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        etSetSleepDaily.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSetSleepDaily.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetSleepDaily.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSetSleepWeekly.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSetSleepWeekly.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetSleepWeekly.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }
}