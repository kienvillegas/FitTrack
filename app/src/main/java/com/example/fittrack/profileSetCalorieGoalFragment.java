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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            dailyGoalInt = dailyGoal.isEmpty() ? 0 : Integer.parseInt(dailyGoal);
            weeklyGoalInt = weeklyGoal.isEmpty() ? dailyGoalInt * 7 : Integer.parseInt(weeklyGoal);

            if(dailyGoalInt == 0){
                pbSetCalorie.setVisibility(View.GONE);
                btnSetCalorieCancel.setVisibility(View.VISIBLE);
                btnSetCalorieGoal.setVisibility(View.VISIBLE);

                etSetCalorieDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetCalorieDaily.setError("Required");
                return;
            }

            if(dailyGoalInt > 999999){
                pbSetCalorie.setVisibility(View.GONE);
                btnSetCalorieGoal.setVisibility(View.VISIBLE);
                btnSetCalorieCancel.setVisibility(View.VISIBLE);

                etSetCalorieDaily.setBackgroundResource(R.drawable.text_field_red);
                etSetCalorieDaily.setError("Invalid");
                etSetCalorieDaily.requestFocus();
                return;
            }

            if(weeklyGoalInt > 999999){
                pbSetCalorie.setVisibility(View.GONE);
                btnSetCalorieGoal.setVisibility(View.VISIBLE);
                btnSetCalorieCancel.setVisibility(View.VISIBLE);

                etSetCalorieWeekly.setBackgroundResource(R.drawable.text_field_red);
                etSetCalorieWeekly.setError("Invalid");
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