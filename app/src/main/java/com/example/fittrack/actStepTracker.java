package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class actStepTracker extends AppCompatActivity {
    private FirebaseAuth mAuth;

    ProgressBar pbStepTracker;
    ImageView imBackBtn;
    TextView tvStepTrackerTaken, tvStepTrackerPercent, tvStepTrackerGoal;
    EditText etStepTracker;
    Button btnAddSteps;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        pbStepTracker = findViewById(R.id.pbCalorieTracker);
        imBackBtn = findViewById(R.id.imStepTrackerBack);

        tvStepTrackerTaken = findViewById(R.id.tvStepTackerTaken);
        tvStepTrackerPercent = findViewById(R.id.tvStepsTrackerPercent);
        tvStepTrackerGoal = findViewById(R.id.tvStepsTrackerGoal);
        etStepTracker = findViewById(R.id.etStepTrackerInput);
        btnAddSteps = findViewById(R.id.btnAddSteps);

        fetchStepsData(userId);
        btnAddSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputStep = etStepTracker.getText().toString().trim();

                try {
                    if (inputStep.isEmpty()) {
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etStepTracker.setBackground(drawable);
                    } else {
                        DocumentReference docRef = db.collection("users").document(userId);
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    int stepPercent, dailyStepTaken, weeklyStepTaken, stepDailyGoal;
                                     dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
                                     weeklyStepTaken = documentSnapshot.getLong("weeklyStepTaken").intValue();
                                     stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();

                                    if(dailyStepTaken <= stepDailyGoal){
                                        dailyStepTaken += Integer.parseInt(inputStep);
                                        weeklyStepTaken += Integer.parseInt(inputStep);
                                        
                                        checkGoalAchievement(dailyStepTaken, stepDailyGoal);
                                        if (stepDailyGoal != 0) {
                                            stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
                                        } else {
                                            stepPercent = 0;
                                        }

                                        Map<String, Object> steps = new HashMap<>();
                                        steps.put("dailyStepTaken", dailyStepTaken);
                                        steps.put("weeklyStepTaken", weeklyStepTaken);

                                        tvStepTrackerTaken.setText(Integer.toString(dailyStepTaken));
                                        tvStepTrackerPercent.setText(Integer.toString(stepPercent) + "%");
                                        pbStepTracker.setMax(100);
                                        pbStepTracker.setProgress(stepPercent);

                                        docRef.update(steps)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(actStepTracker.this, "Successfully entered steps taken", Toast.LENGTH_SHORT).show();
                                                        etStepTracker.setText("");
                                                        Log.d(TAG, "Successfully added " + inputStep);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e(TAG, "Failed to add " + inputStep);
                                                        Toast.makeText(actStepTracker.this, "Failed to enter steps taken", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }else{
                                        Toast.makeText(actStepTracker.this, "You've already reached your daily goal.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(TAG, "Document does not exist");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to get document", e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Toast.makeText(actStepTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        etStepTracker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etStepTracker.setBackground(drawable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        imBackBtn.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void fetchStepsData(String userId){
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int dailyStepTaken,stepPercent ,stepDailyGoal;

                    dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
                    stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();

                    if (stepDailyGoal != 0) {
                        stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
                    } else {
                        stepPercent = 0;
                    }

                    tvStepTrackerTaken.setText(Integer.toString(dailyStepTaken));
                    tvStepTrackerGoal.setText(Integer.toString(stepDailyGoal));
                    tvStepTrackerPercent.setText(String.valueOf(stepPercent) + "%");
                    pbStepTracker.setMax(100);
                    pbStepTracker.setProgress(stepPercent);
                } else {
                    Log.e(TAG, "Document does not exist");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to get document", e);
            }
        });
    }

    private void checkGoalAchievement(int dailyStepsTaken, int stepDailyGoal){
       if(dailyStepsTaken == stepDailyGoal){
           Intent intent = new Intent(getApplicationContext(), bannerStepGoalAchieved.class);
           startActivity(intent);
       }
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.reload();
        }
    }
}
