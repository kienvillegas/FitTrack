package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signUp extends AppCompatActivity {
    private FirebaseAuth mAuth;

    Button btnSignUp;
    TextView tvSignIn;
    EditText etSignUpEmail, etSignUpName, etSignUpPassword, etSignUpCPassword;
    ProgressBar pbSignUp;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);
        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpName = findViewById(R.id.etSignUpName);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etSignUpCPassword = findViewById(R.id.etSignUpCPassword);
        pbSignUp = findViewById(R.id.pbSignUp);
        mAuth = FirebaseAuth.getInstance();

        pbSignUp.setVisibility(View.GONE);
        btnSignUp .setVisibility(View.VISIBLE);

        Map<String, Object> stepData = new HashMap<>();
        stepData.put("mon", 0);
        stepData.put("tue", 0);
        stepData.put("wed", 0);
        stepData.put("thu", 0);
        stepData.put("fri", 0);
        stepData.put("sat", 0);
        stepData.put("sun", 0);

        Map<String, Object> waterData = new HashMap<>();
        waterData.put("mon", 0);
        waterData.put("tue", 0);
        waterData.put("wed", 0);
        waterData.put("thu", 0);
        waterData.put("fri", 0);
        waterData.put("sat", 0);
        waterData.put("sun", 0);

        Map<String, Object> calorieData = new HashMap<>();
        calorieData.put("mon", 0);
        calorieData.put("tue", 0);
        calorieData.put("wed", 0);
        calorieData.put("thu", 0);
        calorieData.put("fri", 0);
        calorieData.put("sat", 0);
        calorieData.put("sun", 0);

        Map<String, Object> sleepData = new HashMap<>();
        sleepData.put("mon", 0);
        sleepData.put("tue", 0);
        sleepData.put("wed", 0);
        sleepData.put("thu", 0);
        sleepData.put("fri", 0);
        sleepData.put("sat", 0);
        sleepData.put("sun", 0);

        btnSignUp.setOnClickListener(v -> {
            pbSignUp.setVisibility(View.VISIBLE);
            btnSignUp .setVisibility(View.GONE);

            String email, name, password, confirmPassword;
            email = etSignUpEmail.getText().toString().trim();
            name = etSignUpName.getText().toString().trim();
            password = etSignUpPassword.getText().toString().trim();
            confirmPassword = etSignUpCPassword.getText().toString().trim();

            try{
                    if(email.isEmpty()){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpEmail.setBackgroundResource(R.drawable.text_field_red);
                        etSignUpEmail.setError("Required");
                        etSignUpEmail.requestFocus();
                        return;
                    }

                    if(name.isEmpty()){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpName.setBackgroundResource(R.drawable.text_field_red);
                        etSignUpName.setError("Required");
                        etSignUpName.requestFocus();
                        return;
                    }

                    if(password.isEmpty()){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpPassword.setBackgroundResource(R.drawable.text_field_red);
                        etSignUpPassword.setError("Required");
                        etSignUpPassword.requestFocus();
                        return;
                    }

                    if(confirmPassword.isEmpty()){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpCPassword.setBackgroundResource(R.drawable.text_field_red);
                        etSignUpCPassword.setError("Required");
                        etSignUpPassword.requestFocus();
                        return;
                    }

                    if(name.length() < 5){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpName.setError("Minimum of 5 Characters");
                        etSignUpName.requestFocus();
                        return;
                    }

                    if(password.length() < 8){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpPassword.setBackgroundResource(R.drawable.text_field_red);
                        etSignUpPassword.setError("Atleast 8 Characters");
                        etSignUpPassword.requestFocus();
                        return;
                    }

                    if(!email.endsWith("@gmail.com")){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpEmail.setBackgroundResource(R.drawable.text_field_red);
                        etSignUpEmail.setError("Invalid Email Format");
                        etSignUpEmail.requestFocus();
                        return;
                    }

                    if(!password.equals(confirmPassword)){
                        pbSignUp.setVisibility(View.GONE);
                        btnSignUp .setVisibility(View.VISIBLE);

                        etSignUpCPassword.setBackgroundResource(R.drawable.text_field_red);
                        etSignUpCPassword.setError("Confirm Password Do Not Match");
                        etSignUpPassword.requestFocus();
                        return;
                    }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = task.getResult().getUser().getUid();
                                Log.d(TAG, "User ID: " + userId);

                                DocumentReference docRef = db.collection("users").document(userId);
                                DocumentReference weeklyStepRef = db.collection("weekly_step").document(userId);
                                DocumentReference weeklyWaterRef = db.collection("weekly_water").document(userId);
                                DocumentReference weeklyCalorieRef = db.collection("weekly_calorie").document(userId);
                                DocumentReference weeklySleepRef = db.collection("weekly_sleep").document(userId);

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("name", name);
                                userData.put("stepDailyGoal", 4000);
                                userData.put("stepWeeklyGoal", 28000);
                                userData.put("dailyStepTaken", 0);
                                userData.put("weeklyStepTaken", 0);
                                userData.put("waterDailyGoal", 2000);
                                userData.put("waterWeeklyGoal", 140000);
                                userData.put("dailyWaterTaken", 0);
                                userData.put("weeklyWaterTaken", 0);
                                userData.put("calorieDailyGoal", 2000);
                                userData.put("calorieWeeklyGoal", 14000);
                                userData.put("dailyCalorieTaken", 0);
                                userData.put("weeklyCalorieTaken", 0);
                                userData.put("sleepDailyGoal", 10);
                                userData.put("sleepWeeklyGoal", 56);
                                userData.put("dailySleepTaken", 0);
                                userData.put("weeklySleepTaken", 0);
                                userData.put("isStepDailyGoal", false);
                                userData.put("isWaterDailyGoal", false);
                                userData.put("isCalorieDailyGoal", false);
                                userData.put("isSleepDailyGoal", false);

                                docRef.set(userData)
                                        .addOnSuccessListener(unused -> Log.d(TAG, "Successfully added " + name + " " + email + " to Firestore Database")).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Failed to added " + name + " " + email + " to Firestore Database");
                                                Log.e(TAG, e.getMessage());
                                            }
                                        });
                                weeklyStepRef.set(stepData);
                                weeklyWaterRef.set(waterData);
                                weeklyCalorieRef.set(calorieData);
                                weeklySleepRef.set(sleepData);

                                pbSignUp.setVisibility(View.GONE);
                                btnSignUp .setVisibility(View.VISIBLE);

                                Intent intent = new Intent(getApplicationContext(), signIn.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(signUp.this, "Account Successfully Registered", Toast.LENGTH_SHORT).show();
                            } else {
                                pbSignUp.setVisibility(View.GONE);
                                btnSignUp .setVisibility(View.VISIBLE);

                                Exception exception = task.getException();
                                if(exception instanceof  FirebaseAuthException){
                                    FirebaseAuthException firebaseAuthException = (FirebaseAuthException) exception;
                                    String errorCode = firebaseAuthException.getErrorCode();

                                    switch (errorCode) {
                                        case "ERROR_INVALID_EMAIL":
                                            etSignUpEmail.setBackgroundResource(R.drawable.text_field_red);
                                            etSignUpEmail.setError("Invalid Email Format");
                                            etSignUpEmail.requestFocus();
                                            break;
                                        case "ERROR_EMAIL_ALREADY_IN_USE":
                                            etSignUpEmail.setBackgroundResource(R.drawable.text_field_red);
                                            etSignUpEmail.setError("Email is already in use");
                                            etSignUpEmail.requestFocus();
                                            break;
                                        case "ERROR_WEAK_PASSWORD":
                                            etSignUpPassword.setBackgroundResource(R.drawable.text_field_red);
                                            etSignUpPassword.setError("Alteast 6 Characters");
                                            etSignUpPassword.requestFocus();
                                            break;
                                        default:
                                            Toast.makeText(signUp.this,  "Authentication failed: " + firebaseAuthException.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Log.e(TAG, "Sign-up failed: " + exception.getMessage(), exception);
                                    Toast.makeText(signUp.this, "Sign-up failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        });

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), signIn.class);
            startActivity(intent);
            finish();
        });

        etSignUpEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSignUpEmail.setBackgroundResource(R.drawable.text_field_bg_white);
                etSignUpEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSignUpName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSignUpName.setBackgroundResource(R.drawable.text_field_bg_white);
                etSignUpName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSignUpPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSignUpPassword.setBackgroundResource(R.drawable.text_field_bg_white);
                etSignUpPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSignUpCPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSignUpCPassword.setBackgroundResource(R.drawable.text_field_bg_white);
                etSignUpCPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
     }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.reload();
        }
    }
}
