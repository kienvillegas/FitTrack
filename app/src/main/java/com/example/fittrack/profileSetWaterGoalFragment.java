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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        pbSetWater.setVisibility(View.GONE);
        btnSetWaterGoal.setVisibility(View.VISIBLE);
        btnSetWaterCancel.setVisibility(View.VISIBLE);

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
            dailyGoal = etSetWaterDaily.getText().toString().trim();
            weeklyGoal = etSetWaterWeekly.getText().toString().trim();

            try{
                if(dailyGoal.isEmpty()){
                    pbSetWater.setVisibility(View.GONE);
                    btnSetWaterGoal.setVisibility(View.VISIBLE);
                    btnSetWaterCancel.setVisibility(View.VISIBLE);

                    etSetWaterDaily.setBackgroundResource(R.drawable.text_field_red);
                    etSetWaterDaily.setError("Required");
                    etSetWaterDaily.requestFocus();
                    return;
                }

                if(weeklyGoal.isEmpty()){
                    pbSetWater.setVisibility(View.GONE);
                    btnSetWaterGoal.setVisibility(View.VISIBLE);
                    btnSetWaterCancel.setVisibility(View.VISIBLE);
                    etSetWaterWeekly.setBackgroundResource(R.drawable.text_field_red);
                    etSetWaterWeekly.setError("Required");
                    etSetWaterWeekly.requestFocus();
                    return;
                }

                if(!dailyGoal.isEmpty() && !dailyGoal.isEmpty()){
                    int dailyGoalInt = Integer.parseInt(dailyGoal);
                    int weeklyGoalInt = Integer.parseInt(weeklyGoal);

                    DocumentReference docRef = db.collection("users").document(userId);
                    Map<String, Object> waterData = new HashMap<>();
                    waterData.put("waterDailyGoal", Integer.parseInt(dailyGoal));
                    waterData.put("waterWeeklyGoal", Integer.parseInt(weeklyGoal));

                    docRef.update(waterData)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Successfully updated step daily and weekly goal");
                                String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(dailyGoalInt);
                                String formattedWeeklyGoal = NumberFormat.getNumberInstance(Locale.US).format(weeklyGoalInt);
                                tvSetWaterDaily.setText(formattedDailyGoal);
                                tvSetWaterWeekly.setText(formattedWeeklyGoal);
                                etSetWaterDaily.setText("");
                                etSetWaterWeekly.setText("");

                                pbSetWater.setVisibility(View.GONE);
                                btnSetWaterGoal.setVisibility(View.VISIBLE);
                                btnSetWaterCancel.setVisibility(View.VISIBLE);

                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                Fragment newFragment = new profileWaterFragment();
                                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update step daily and weekly goal");
                                pbSetWater.setVisibility(View.GONE);
                                btnSetWaterGoal.setVisibility(View.VISIBLE);
                                btnSetWaterCancel.setVisibility(View.VISIBLE);
                            });
                }else{
                    Toast.makeText(requireContext(), "Please fill in the missing fields", Toast.LENGTH_SHORT).show();
                    pbSetWater.setVisibility(View.GONE);
                    btnSetWaterGoal.setVisibility(View.VISIBLE);
                    btnSetWaterCancel.setVisibility(View.VISIBLE);
                }
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
                etSetWaterWeekly.setBackgroundResource(R.drawable.text_field_primary_border);
                etSetWaterWeekly.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;    }
}