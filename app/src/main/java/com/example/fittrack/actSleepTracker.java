package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class actSleepTracker extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvSleepTrackerTaken, tvSleepTrackerPercent, tvSleepTrackerGoal;
    EditText etSleepTrackerInput;
    ProgressBar pbSleepTracker, pbAddHours;
    Button btnAddHours;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_act_sleep_tracker);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        DataManager dataManager = new DataManager(this);
        final String[] storedDate = {dataManager.getStoredDate()};
        String currentDate = getCurrentDateTime();

        if(storedDate[0] == null || storedDate[0].isEmpty()){
            dataManager.saveCurrentDateTime();
            storedDate[0] = dataManager.getStoredDate();
        }

        imBackBtn = findViewById(R.id.imSleepTrackerBack);
        tvSleepTrackerTaken = findViewById(R.id.tvSleepTrackerTaken);
        tvSleepTrackerPercent = findViewById(R.id.tvSleepTrackerPercent);
        tvSleepTrackerGoal = findViewById(R.id.tvSleepTrackerGoal);
        etSleepTrackerInput = findViewById(R.id.etSleepTrackerInput);
        pbSleepTracker = findViewById(R.id.pbSleepTracker);
        btnAddHours = findViewById(R.id.btnAddHours);
        pbAddHours = findViewById(R.id.pbAddHours);

        pbAddHours.setVisibility(View.GONE);
        btnAddHours.setVisibility(View.VISIBLE);

        fetchSleepData(userId,storedDate[0]);

        btnAddHours.setOnClickListener(v -> {
            pbAddHours.setVisibility(View.VISIBLE);
            btnAddHours.setVisibility(View.GONE);
            String inputSleep = etSleepTrackerInput.getText().toString().trim();
            String day = getCurrentDay();
            try {
                if (inputSleep.isEmpty()) {
                    pbAddHours.setVisibility(View.GONE);
                    btnAddHours.setVisibility(View.VISIBLE);

                    etSleepTrackerInput.setBackgroundResource(R.drawable.text_field_red);
                    etSleepTrackerInput.setError("Please enter hours of sleep");
                    return;
                }

                if(Integer.parseInt(inputSleep) > 8){
                    pbAddHours.setVisibility(View.GONE);
                    btnAddHours.setVisibility(View.VISIBLE);

                    etSleepTrackerInput.setBackgroundResource(R.drawable.text_field_red);
                    etSleepTrackerInput.setError("Maximum of 8 Hours sleep");
                    return;
                }

                DocumentReference docRef = db.collection("users").document(userId);
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int sleepPercent, dailySleepTaken, weeklySleepTaken, sleepDailyGoal;
                        dailySleepTaken = documentSnapshot.getLong("dailySleepTaken").intValue();
                        weeklySleepTaken = documentSnapshot.getLong("weeklySleepTaken").intValue();
                        sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal").intValue();

                        if(dailySleepTaken >= sleepDailyGoal){
                            pbAddHours.setVisibility(View.GONE);
                            btnAddHours.setVisibility(View.VISIBLE);
                            etSleepTrackerInput.setError("Maximum limit reached");
                            return;
                        }

                        dailySleepTaken += Integer.parseInt(inputSleep);
                        weeklySleepTaken += Integer.parseInt(inputSleep);

                        saveWeeklySleep(userId, day, dailySleepTaken);
                        checkGoalAchievement(dailySleepTaken, sleepDailyGoal, userId, storedDate[0]);
                        if (sleepDailyGoal != 0) {
                            sleepPercent = Math.min((int) (((double) dailySleepTaken / sleepDailyGoal) * 100), 100);
                        } else {
                            sleepPercent = 0;
                        }

                        Map<String, Object> sleep = new HashMap<>();
                        sleep.put("dailySleepTaken", dailySleepTaken);
                        sleep.put("weeklySleepTaken", weeklySleepTaken);

                        tvSleepTrackerTaken.setText(Integer.toString(dailySleepTaken) + " Hours");
                        tvSleepTrackerPercent.setText(Integer.toString(sleepPercent) + "%");
                        pbSleepTracker.setMax(100);
                        pbSleepTracker.setProgress(sleepPercent);

                        if(storedDate[0].equals(currentDate) || storedDate[0] == null || storedDate[0].isEmpty()) {
                            docRef.update(sleep)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(actSleepTracker.this, "Successfully entered hours of sleep taken", Toast.LENGTH_SHORT).show();
                                        dataManager.saveCurrentDateTime();
                                        pbAddHours.setVisibility(View.GONE);
                                        btnAddHours.setVisibility(View.VISIBLE);
                                        etSleepTrackerInput.setText("");
                                        Log.d(TAG, "Successfully added " + inputSleep);
                                    }).addOnFailureListener(e -> {
                                        pbAddHours.setVisibility(View.GONE);
                                        btnAddHours.setVisibility(View.VISIBLE);

                                        Log.e(TAG, "Failed to add " + inputSleep);
                                        Toast.makeText(actSleepTracker.this, "Failed to enter hours of sleep taken", Toast.LENGTH_SHORT).show();
                                    });
                        }else{
                            Log.d(TAG, storedDate[0] + " is not equal to " + currentDate);
                            dataManager.saveCurrentDateTime();

                            dailySleepTaken = Integer.parseInt(inputSleep);
                            Map<String, Object> sleepTaken = new HashMap<>();

                            sleepTaken.put("dailySleepTaken", dailySleepTaken);

                            docRef.update(sleepTaken)
                                    .addOnSuccessListener(unused -> {
                                        pbAddHours.setVisibility(View.GONE);
                                        btnAddHours.setVisibility(View.VISIBLE);

                                        Log.d(TAG, "Successfully updated daily sleep taken");
                                    }).addOnFailureListener(e -> {
                                        pbAddHours.setVisibility(View.GONE);
                                        btnAddHours.setVisibility(View.VISIBLE);

                                        Log.e(TAG, "Failed to update daily sleep taken");
                                    });
                        }
                    } else {
                        pbAddHours.setVisibility(View.GONE);
                        btnAddHours.setVisibility(View.VISIBLE);

                        Log.e(TAG, "Document does not exist");
                    }
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));

            } catch (Exception e) {
                pbAddHours.setVisibility(View.GONE);
                btnAddHours.setVisibility(View.VISIBLE);

                Toast.makeText(actSleepTracker.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        etSleepTrackerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int resourceId = R.drawable.text_field_bg_grey;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSleepTrackerInput.setBackground(drawable);
                etSleepTrackerInput.setError(null);
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
    private void fetchSleepData(String userId, String storedDateTime) {
        String currentDate = getCurrentDateTime();
        DocumentReference docRef = db.collection("users").document(userId);

        if (storedDateTime.equals(currentDate)) {
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    updateSleepUI(documentSnapshot);
                } else {
                    Log.e(TAG, "Document does not exist");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to get document", e));
        } else {
            Map<String, Object> sleepData = new HashMap<>();
            sleepData.put("dailySleepTaken", 0);
            docRef.update(sleepData)
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "Successfully updated dailySleepTaken to zero");
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating dailySleepTaken to zero");
                    });

            updateSleepUI(null);
        }
    }

    private void updateSleepUI(@Nullable DocumentSnapshot documentSnapshot) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            int sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal") != null ? documentSnapshot.getLong("sleepDailyGoal").intValue() : 0;
            int dailySleepTaken = documentSnapshot.getLong("dailySleepTaken") != null ? documentSnapshot.getLong("dailySleepTaken").intValue() : 0;

            int sleepPercent = 0;
            if (sleepDailyGoal != 0) {
                sleepPercent = Math.min((int) (((double) dailySleepTaken / sleepDailyGoal) * 100), 100);
            }

            String formattedDailyGoal = NumberFormat.getNumberInstance(Locale.US).format(sleepDailyGoal);
            String formattedSleepTaken = NumberFormat.getNumberInstance(Locale.US).format(dailySleepTaken);

            tvSleepTrackerTaken.setText(formattedSleepTaken + " Hours");
            tvSleepTrackerGoal.setText(formattedDailyGoal);
            tvSleepTrackerPercent.setText(String.valueOf(sleepPercent) + "%");
            pbSleepTracker.setMax(100);
            pbSleepTracker.setProgress(sleepPercent);
        } else {
            Log.e(TAG, "Document snapshot is null or does not exist");
        }
    }
    private void checkGoalAchievement(int dailySleepTaken, int sleepDailyGoal, String userId, String storedDateTime) {
        String currentDateTime = getCurrentDateTime();

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isSleepDailyGoal = documentSnapshot.getBoolean("isSleepDailyGoal");

                    if (!storedDateTime.equals(currentDateTime)) {
                        updateSleepGoalStatus(docRef, false);
                    }

                    if (!isSleepDailyGoal && dailySleepTaken >= sleepDailyGoal) {
                        updateSleepGoalStatus(docRef, true);

                        Intent intent = new Intent(getApplicationContext(), bannerSleepGoalAchieved.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching isSleepDailyGoal: " + e.getMessage());
                });
    }

    private void updateSleepGoalStatus(DocumentReference docRef, boolean isGoalAchieved) {
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("isSleepDailyGoal", isGoalAchieved);

        docRef.update(goalData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Successfully updated isSleepDailyGoal");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update isSleepDailyGoal: " + e.getMessage());
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

    private void saveWeeklySleep(String userId, String day, int dailySleepTaken){
        DocumentReference weeklySleepRef = db.collection("weekly_sleep").document(userId);
        Map<String, Object> sleepData = new HashMap<>();

        switch (day){
            case "Mon":
                sleepData.put("mon", dailySleepTaken);
                break;
            case "Tue":
                sleepData.put("tue", dailySleepTaken);
                break;
            case "Wed":
                sleepData.put("wed", dailySleepTaken);
                break;
            case "Thu":
                sleepData.put("thu", dailySleepTaken);
                break;
            case "Fri":
                sleepData.put("fri", dailySleepTaken);
                break;
            case "Sat":
                sleepData.put("sat", dailySleepTaken);
                break;
            case "Sun":
                sleepData.put("sun", dailySleepTaken);
                break;
            default:
                Log.w(TAG, day + " is not available");
        }

        weeklySleepRef.update(sleepData)
                .addOnSuccessListener(unused -> {
                    Log.i(TAG, "Successfully added " + sleepData + " to Firestore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add " + sleepData + " to Firestore");
                });
    }
    private void applyTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = prefs.getInt(THEME_PREF_KEY, 0);

        Log.d(TAG, "Applying theme: " + theme);
        switch (theme) {
            case THEME_ORANGE:
                setTheme(R.style.AppOrangeTheme);
                break;
            case THEME_GREEN:
                setTheme(R.style.AppGreenTheme);
                break;
            default:
                setTheme(R.style.AppDefaultTheme);
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