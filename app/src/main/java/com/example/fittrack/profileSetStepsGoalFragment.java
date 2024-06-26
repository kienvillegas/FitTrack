package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            dailyGoalInt = dailyGoal.isEmpty() ? 0 : Integer.parseInt(dailyGoal);
            weeklyGoalInt = weeklyGoal.isEmpty() ? dailyGoalInt * 7 : Integer.parseInt(weeklyGoal);

            if(dailyGoalInt == 0){
                pbSetStep.setVisibility(View.GONE);
                btnSetStepCancel.setVisibility(View.VISIBLE);
                btnSetStepGoal.setVisibility(View.VISIBLE);

                etSetStepDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetStepDaily.setError("Required");
                etSetStepDaily.requestFocus();
                return;
            }

            if(dailyGoalInt > 999999){
                pbSetStep.setVisibility(View.GONE);
                btnSetStepGoal.setVisibility(View.VISIBLE);
                btnSetStepCancel.setVisibility(View.VISIBLE);

                etSetStepDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetStepDaily.setError("Invalid");
                etSetStepDaily.requestFocus();
                return;
            }

            if(weeklyGoalInt > 999999){
                pbSetStep.setVisibility(View.GONE);
                btnSetStepGoal.setVisibility(View.VISIBLE);
                btnSetStepCancel.setVisibility(View.VISIBLE);

                etSetStepWeekly.setBackgroundResource(R.drawable.text_field_red);
                etSetStepWeekly.setError("Invalid");
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