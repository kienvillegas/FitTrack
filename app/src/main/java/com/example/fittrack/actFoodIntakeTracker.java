package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class actFoodIntakeTracker extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvCalorieTrackerTaken, tvCalorieTrackerPercent, tvCalorieTrackerGoal;
    EditText etCalorieTrackerInput;
    ProgressBar pbCalorieTracker, pbAddCalories;
    Button btnAddCalories;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_intake_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        DataManager dataManager = new DataManager(this);
        final String[]
                storedDate = {dataManager.getStoredDate()};
        String currentDate = getCurrentDateTime();

        if(storedDate[0] == null || storedDate[0].isEmpty()){
            dataManager.saveCurrentDateTime();
            storedDate[0] = dataManager.getStoredDate();
        }

        imBackBtn = findViewById(R.id.imCalorieTrackerBack);
        tvCalorieTrackerTaken = findViewById(R.id.tvCalorieTrackerTaken);
        tvCalorieTrackerPercent = findViewById(R.id.tvCalorieTrackerPercent);
        tvCalorieTrackerGoal = findViewById(R.id.tvCalorieTrackerGoal);
        etCalorieTrackerInput = findViewById(R.id.etCalorieTrackerInput);
        pbCalorieTracker = findViewById(R.id.pbCalorieTracker);
        btnAddCalories = findViewById(R.id.btnAddCalories);
        pbAddCalories = findViewById(R.id.pbAddCalories);

        pbAddCalories.setVisibility(View.GONE);
        btnAddCalories.setVisibility(View.VISIBLE);

        fetchCalorieData(userId,storedDate[0]);
        btnAddCalories.setOnClickListener(v -> {
            pbAddCalories.setVisibility(View.VISIBLE);
            btnAddCalories.setVisibility(View.GONE);

            String inputCalorie = etCalorieTrackerInput.getText().toString().trim();
            String day = getCurrentDay();
            try {
                if (inputCalorie.isEmpty()) {
                    pbAddCalories.setVisibility(View.GONE);
                    btnAddCalories.setVisibility(View.VISIBLE);

                    etCalorieTrackerInput.setBackgroundResource(R.drawable.text_field_red);
                    etCalorieTrackerInput.setError("Please enter calories");
                } else {
                    DocumentReference docRef = db.collection("users").document(userId);
                    docRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            int caloriePercent, dailyCalorieTaken, weeklyCalorieTaken, calorieDailyGoal;

                             dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();
                             weeklyCalorieTaken = documentSnapshot.getLong("weeklyCalorieTaken").intValue();
                             calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();

                             dailyCalorieTaken += Integer.parseInt(inputCalorie);
                             weeklyCalorieTaken += Integer.parseInt(inputCalorie);

                             saveWeeklyCalorie(userId, day, dailyCalorieTaken);
                             checkGoalAchievement(dailyCalorieTaken, calorieDailyGoal, userId, storedDate[0]);

                             if (calorieDailyGoal != 0) {
                                    caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
                                } else {
                                    caloriePercent = 0;
                                }

                                Map<String, Object> calories = new HashMap<>();
                                calories.put("dailyCalorieTaken", dailyCalorieTaken);
                                calories.put("weeklyCalorieTaken", weeklyCalorieTaken);

                                if(storedDate[0].equals(currentDate) || storedDate[0] == null || storedDate[0].isEmpty()){
                                    Log.d(TAG, storedDate[0] + " is equal to " + currentDate);

                                    tvCalorieTrackerTaken.setText(Integer.toString(dailyCalorieTaken) + " Kcal");
                                    tvCalorieTrackerPercent.setText(Integer.toString(caloriePercent) + "%");
                                    pbCalorieTracker.setMax(100);
                                    pbCalorieTracker.setProgress(caloriePercent);

                                    docRef.update(calories)
                                            .addOnSuccessListener(unused -> {
                                                dataManager.saveCurrentDateTime();

                                                pbAddCalories.setVisibility(View.GONE);
                                                btnAddCalories.setVisibility(View.VISIBLE);

                                                Toast.makeText(actFoodIntakeTracker.this, "Successfully entered calories taken", Toast.LENGTH_SHORT).show();
                                                etCalorieTrackerInput.setText("");
                                                Log.d(TAG, "Successfully added " + inputCalorie);
                                            }).addOnFailureListener(e -> {
                                                pbAddCalories.setVisibility(View.GONE);
                                                btnAddCalories.setVisibility(View.VISIBLE);

                                                Log.e(TAG, "Failed to add " + inputCalorie);
                                                Toast.makeText(actFoodIntakeTracker.this, "Failed to enter calories taken", Toast.LENGTH_SHORT).show();
                                            });
                                }else{
                                    Log.d(TAG, storedDate[0] + " is not equal to " + currentDate);

                                    dailyCalorieTaken = Integer.parseInt(inputCalorie);
                                    Map<String, Object> calorieTaken = new HashMap<>();
                                    calorieTaken.put("dailyCalorieTaken", dailyCalorieTaken);

                                    docRef.update(calorieTaken)
                                            .addOnSuccessListener(unused -> {
                                                pbAddCalories.setVisibility(View.GONE);
                                                btnAddCalories.setVisibility(View.VISIBLE);

                                                Log.d(TAG, "Successfully updated daily calorie taken");
                                            }).addOnFailureListener(e -> {
                                                pbAddCalories.setVisibility(View.GONE);
                                                btnAddCalories.setVisibility(View.VISIBLE);

                                                Log.e(TAG, "Failed to update daily calorie taken");
                                            });
                                }
                        } else {
                            pbAddCalories.setVisibility(View.GONE);
                            btnAddCalories.setVisibility(View.VISIBLE);

                            Log.e(TAG, "Document does not exist");
                        }
                    }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));
                }
            } catch (Exception e) {
                pbAddCalories.setVisibility(View.GONE);
                btnAddCalories.setVisibility(View.VISIBLE);

                Toast.makeText(actFoodIntakeTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        etCalorieTrackerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etCalorieTrackerInput.setBackgroundResource(R.drawable.text_field_bg_grey);
                etCalorieTrackerInput.setError(null);
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

    private void fetchCalorieData(String userId, String storedDateTime) {
        String currentDate = getCurrentDateTime();
        DocumentReference docRef = db.collection("users").document(userId);

        if (storedDateTime.equals(currentDate)) {
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    updateCalorieUI(documentSnapshot);
                } else {
                    Log.e(TAG, "Document does not exist");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));
        } else {
            Map<String, Object> calorieData = new HashMap<>();
            calorieData.put("dailyCalorieTaken", 0);
            docRef.update(calorieData)
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "Successfully updated dailyCalorieTaken to zero");
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating dailyCalorieTaken to zero");
                    });

            updateCalorieUI(null);
        }
    }

    private void updateCalorieUI(@Nullable DocumentSnapshot documentSnapshot) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            int calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal") != null ? documentSnapshot.getLong("calorieDailyGoal").intValue() : 0;
            int dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken") != null ? documentSnapshot.getLong("dailyCalorieTaken").intValue() : 0;

            int caloriePercent = 0;
            if (calorieDailyGoal != 0) {
                caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
            }

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(calorieDailyGoal);
            String formattedCalorieTaken = NumberFormat.getNumberInstance(Locale.US).format(dailyCalorieTaken);

            tvCalorieTrackerTaken.setText(formattedCalorieTaken + " Kcal");
            tvCalorieTrackerGoal.setText(formattedDailyGoal);
            tvCalorieTrackerPercent.setText(String.valueOf(caloriePercent) + "%");
            pbCalorieTracker.setMax(100);
            pbCalorieTracker.setProgress(caloriePercent);
        } else {
            Log.e(TAG, "Document snapshot is null or does not exist");
        }
    }

    private void checkGoalAchievement(int dailyCalorieTaken, int calorieDailyGoal, String userId, String storedDateTime) {
        String currentDateTime = getCurrentDateTime();

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isCalorieDailyGoal = documentSnapshot.getBoolean("isCalorieDailyGoal");

                    if (!storedDateTime.equals(currentDateTime)) {
                        updateCalorieGoalStatus(docRef, false);
                    }

                    if (!isCalorieDailyGoal && dailyCalorieTaken >= calorieDailyGoal) {
                        updateCalorieGoalStatus(docRef, true);

                        Intent intent = new Intent(getApplicationContext(), bannerCalorieGoalAchieved.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching isCalorieDailyGoal: " + e.getMessage());
                });
    }

    private void updateCalorieGoalStatus(DocumentReference docRef, boolean isGoalAchieved) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("isCalorieDailyGoal", isGoalAchieved);

        docRef.update(goalData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully updated isCalorieDailyGoal");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update isCalorieDailyGoal: " + e.getMessage());
                });
    }

    private String getCurrentDay(){
        Date date = new Date();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        return dayFormat.format(date);
    }

    private String getCurrentDateTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return sdf.format(date);
    }


    private void saveWeeklyCalorie(String userId, String day, int dailyCalorieTaken){
        DocumentReference weeklyCalorieRef = db.collection("weekly_calorie").document(userId);
        Map<String, Object> calorieData = new HashMap<>();

        switch (day){
            case "Mon":
                calorieData.put("mon", dailyCalorieTaken);
                break;
            case "Tue":
                calorieData.put("tue", dailyCalorieTaken);
                break;
            case "Wed":
                calorieData.put("wed", dailyCalorieTaken);
                break;
            case "Thu":
                calorieData.put("thu", dailyCalorieTaken);
                break;
            case "Fri":
                calorieData.put("fri", dailyCalorieTaken);
                break;
            case "Sat":
                calorieData.put("sat", dailyCalorieTaken);
                break;
            case "Sun":
                calorieData.put("sun", dailyCalorieTaken);
                break;
            default:
                Log.w(TAG, day + " is not available");
        }

        weeklyCalorieRef.update(calorieData)
                .addOnSuccessListener(unused -> {
                    Log.i(TAG, "Successfully added " + calorieData + " to Firestore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add " + calorieData + " to Firestore");
                });
    }

    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.reload();
        }
    }
}