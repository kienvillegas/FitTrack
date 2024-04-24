package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

public class actWaterIntakeTracker extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView imBackBtn, imIncWater, imDecWater;
    Button btnAddDrink;
    TextView tvWaterTrackerTaken, tvWaterTrackerGoal, tvWaterTrackerPercent, tvWaterTrackerInputAmount;
    ProgressBar pbWaterTracker;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_intake_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        imBackBtn = findViewById(R.id.imWaterTrackerBack);
        imIncWater = findViewById(R.id.imIncWater);
        imDecWater = findViewById(R.id.imDecWater);
        btnAddDrink = findViewById(R.id.btnAddDrink);
        tvWaterTrackerTaken = findViewById(R.id.tvWaterTrackerTaken);
        tvWaterTrackerGoal = findViewById(R.id.tvWaterTrackerGoal);
        tvWaterTrackerPercent = findViewById(R.id.tvWaterTrackerPercent);
        tvWaterTrackerInputAmount = findViewById(R.id.tvWaterTrackerAmountInput);
        pbWaterTracker = findViewById(R.id.pbWaterTracker);

        fetchWaterData(userId);

        final int[] inputCounter = {1};
        final int[] totalWaterAmount = new int[1];
        int glassWaterMl = 250;
        imIncWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("users").document(userId);
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            int waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();
                            if (totalWaterAmount[0] < waterDailyGoal) {
                                inputCounter[0] += 1;
                                totalWaterAmount[0] = inputCounter[0] * glassWaterMl;
                                tvWaterTrackerInputAmount.setText(inputCounter[0] + "x Glass " + totalWaterAmount[0] + "ml");
                            } else {
                                Toast.makeText(actWaterIntakeTracker.this, "You've reached the maximum amount!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "Document does not exist");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting document", e);
                    }
                });
            }
        });

        imDecWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputCounter[0] > 1) {
                    inputCounter[0] -= 1;
                    totalWaterAmount[0] = inputCounter[0] * glassWaterMl;
                    tvWaterTrackerInputAmount.setText(inputCounter[0] + "x Glass " + totalWaterAmount[0] + "ml");
                }
            }
        });

        btnAddDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int glassWaterML = 250;
                try{
                    DocumentReference docRef = db.collection("users").document(userId);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                int waterPercent, dailyWaterTaken, weeklyWaterTaken, waterDailyGoal;
                                dailyWaterTaken = documentSnapshot.getLong("dailyWaterTaken").intValue();
                                weeklyWaterTaken = documentSnapshot.getLong("weeklyWaterTaken").intValue();
                                waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();

                                if(dailyWaterTaken <= waterDailyGoal){
                                    dailyWaterTaken += glassWaterML * inputCounter[0];
                                    weeklyWaterTaken += glassWaterML * inputCounter[0];

                                    checkGoalAchievement(dailyWaterTaken, waterDailyGoal);
                                    if (waterDailyGoal != 0) {
                                        waterPercent = Math.min((int) (((double) dailyWaterTaken / waterDailyGoal) * 100), 100);
                                    } else {
                                        waterPercent = 0;
                                    }

                                    inputCounter[0] = 1;
                                    totalWaterAmount[0] = glassWaterML;
                                    Map<String, Object> water = new HashMap<>();
                                    water.put("dailyWaterTaken", dailyWaterTaken);
                                    water.put("weeklyWaterTaken", weeklyWaterTaken);

                                    tvWaterTrackerTaken.setText(Integer.toString(dailyWaterTaken));
                                    tvWaterTrackerPercent.setText(Integer.toString(waterPercent) + "%");
                                    pbWaterTracker.setMax(100);
                                    pbWaterTracker.setProgress(waterPercent);

                                    docRef.update(water)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(actWaterIntakeTracker.this, "Successfully entered water taken", Toast.LENGTH_SHORT).show();
                                                    tvWaterTrackerInputAmount.setText("1x Glass 250ml");
                                                    Log.d(TAG, "Successfully added " + inputCounter);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Failed to add " + inputCounter);
                                                    Toast.makeText(actWaterIntakeTracker.this, "Failed to enter water taken", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }else{
                                    Toast.makeText(actWaterIntakeTracker.this, "You've already reached your daily goal.", Toast.LENGTH_SHORT).show();
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
                }catch(Exception e){
                    Toast.makeText(actWaterIntakeTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        imBackBtn.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void fetchWaterData(String userId){
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

                    tvWaterTrackerTaken.setText(Integer.toString(dailyStepTaken));
                    tvWaterTrackerGoal.setText(Integer.toString(stepDailyGoal));
                    tvWaterTrackerPercent.setText(String.valueOf(stepPercent) + "%");
                    pbWaterTracker.setMax(100);
                    pbWaterTracker.setProgress(stepPercent);
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
            Intent intent = new Intent(getApplicationContext(), bannerWaterGoalAchieved.class);
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