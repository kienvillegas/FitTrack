package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
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
    EditText etSignUpEmail, etSignUpName, etSignUpPassword, etSignUpCPassowrd;
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
        etSignUpCPassowrd = findViewById(R.id.etSignUpCPassword);
        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, name, password, confirmPassword;
                email = String.valueOf(etSignUpEmail.getText());
                name = String.valueOf(etSignUpName.getText());
                password = String.valueOf(etSignUpPassword.getText());
                confirmPassword = String.valueOf(etSignUpCPassowrd.getText());
                String regexPattern = "\\b[A-Za-z0-9._%+-]+@gmail\\.com\\b";

                Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(email);
                try{
                    if(!email.isEmpty() || !name.isEmpty() || !password.isEmpty() || !confirmPassword.isEmpty()){
                        if(matcher.find()){
                            if(password.equals(confirmPassword)){
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(Task<AuthResult> task) {
                                                Log Log = null;
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "createUserWithEmail:success");

                                                    String userId = task.getResult().getUser().getUid();
                                                    Log.d(TAG, "User ID: " + userId);

                                                    addUserToFireStore(userId,email,name);
                                                    Toast.makeText(signUp.this, "Account Successfully Created.", Toast.LENGTH_SHORT).show();

                                                    etSignUpEmail.setText("");
                                                    etSignUpName.setText("");
                                                    etSignUpPassword.setText("");
                                                    etSignUpCPassowrd.setText("");
                                                } else {
                                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                    Toast.makeText(signUp.this, "Account Creation Failed.", Toast.LENGTH_SHORT).show();

                                                    if(task.getException() != null){
                                                        handleRegistrationError(task.getException());
                                                    }else{
                                                        Log.e(TAG, "Unknown registration error occurred");
                                                        Toast.makeText(signUp.this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(signUp.this, "Confirm Password does not match.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(signUp.this, "Email must contain \"@gmail.com\".", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(signUp.this, "Please fill in missing fields", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    Toast.makeText(signUp.this, "An Error Occured: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), signIn.class);
                startActivity(intent);
            }
        });
    }

    private void addUserToFireStore(String uid, String email, String name){
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("name", name);

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully added user to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add user to Firestore");
                });
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            Toast.makeText(signUp.this, "Weak password. Please use a stronger password.", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(signUp.this, "Invalid email format. Please enter a valid email.", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(signUp.this, "Email is already in use. Please use a different email.", Toast.LENGTH_SHORT).show();
        } else {
            String errorMessage = exception.getMessage();
            Log.e(TAG, "Registration Error: " + errorMessage);
            Toast.makeText(signUp.this, "An Error Occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSignInErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign In Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
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
