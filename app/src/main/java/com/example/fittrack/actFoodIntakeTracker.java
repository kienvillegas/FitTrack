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

public class actFoodIntakeTracker extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvCalorieTrackerTaken, tvCalorieTrackerPercent, tvCalorieTrackerGoal;
    EditText etCalorieTrackerInput;
    ProgressBar pbCalorieTracker;
    Button btnAddCalories;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_intake_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        imBackBtn = findViewById(R.id.imCalorieTrackerBack);
        tvCalorieTrackerTaken = findViewById(R.id.tvCalorieTrackerTaken);
        tvCalorieTrackerPercent = findViewById(R.id.tvCalorieTrackerPercent);
        tvCalorieTrackerGoal = findViewById(R.id.tvCalorieTrackerGoal);
        etCalorieTrackerInput = findViewById(R.id.etCalorieTrackerInput);
        pbCalorieTracker = findViewById(R.id.pbCalorieTracker);
        btnAddCalories = findViewById(R.id.btnAddCalories);

        fetchCalorieData(userId);
        btnAddCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCalorie = etCalorieTrackerInput.getText().toString().trim();
                try {
                    if (inputCalorie.isEmpty()) {
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etCalorieTrackerInput.setBackground(drawable);
                    } else {
                        DocumentReference docRef = db.collection("users").document(userId);
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    int caloriePercent, dailyCalorieTaken, weeklyCalorieTaken, calorieDailyGoal;
                                     dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();
                                     weeklyCalorieTaken = documentSnapshot.getLong("weeklyCalorieTaken").intValue();
                                     calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();

                                    if(dailyCalorieTaken <= calorieDailyGoal){
                                        dailyCalorieTaken += Integer.parseInt(inputCalorie);
                                        weeklyCalorieTaken += Integer.parseInt(inputCalorie);

                                        checkGoalAchievement(dailyCalorieTaken, calorieDailyGoal);
                                        if (calorieDailyGoal != 0) {
                                            caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
                                        } else {
                                            caloriePercent = 0;
                                        }

                                        Map<String, Object> calories = new HashMap<>();
                                        calories.put("dailyCalorieTaken", dailyCalorieTaken);
                                        calories.put("weeklyCalorieTaken", weeklyCalorieTaken);

                                        tvCalorieTrackerTaken.setText(Integer.toString(dailyCalorieTaken) + " Kcal");
                                        tvCalorieTrackerPercent.setText(Integer.toString(caloriePercent) + "%");
                                        pbCalorieTracker.setMax(100);
                                        pbCalorieTracker.setProgress(caloriePercent);

                                        docRef.update(calories)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(actFoodIntakeTracker.this, "Successfully entered calories taken", Toast.LENGTH_SHORT).show();
                                                        etCalorieTrackerInput.setText("");
                                                        Log.d(TAG, "Successfully added " + inputCalorie);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e(TAG, "Failed to add " + inputCalorie);
                                                        Toast.makeText(actFoodIntakeTracker.this, "Failed to enter calories taken", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }else{
                                        Toast.makeText(actFoodIntakeTracker.this, "You've already reached your daily goal.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(actFoodIntakeTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        etCalorieTrackerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etCalorieTrackerInput.setBackground(drawable);
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

    private void fetchCalorieData(String userId){
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int dailyCalorieTaken,caloriePercent ,calorieDailyGoal;

                    dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();
                    calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();

                    if (calorieDailyGoal != 0) {
                        caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
                    } else {
                        caloriePercent = 0;
                    }


                    tvCalorieTrackerTaken.setText(Integer.toString(dailyCalorieTaken) + " Kcal");
                    tvCalorieTrackerGoal.setText(Integer.toString(calorieDailyGoal));
                    tvCalorieTrackerPercent.setText(String.valueOf(caloriePercent) + "%");
                    pbCalorieTracker.setMax(100);
                    pbCalorieTracker.setProgress(caloriePercent);
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

    private void checkGoalAchievement(int dailyCalorieTaken, int calorieDailyGoal){
        if(dailyCalorieTaken == calorieDailyGoal){
            Intent intent = new Intent(getApplicationContext(), bannerCalorieGoalAchieved.class);
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