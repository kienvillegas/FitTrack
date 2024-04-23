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
                email = etSignUpEmail.getText().toString().trim();
                name = etSignUpName.getText().toString().trim();
                password = etSignUpPassword.getText().toString().trim();
                confirmPassword = etSignUpCPassowrd.getText().toString().trim();

                try{
                    if(email.isEmpty()){
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignUpEmail.setBackground(drawable);
                    }

                    if(name.isEmpty()){
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignUpName.setBackground(drawable);
                    }

                    if(password.isEmpty()){
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignUpPassword.setBackground(drawable);
                    }

                    if(confirmPassword.isEmpty()){
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignUpCPassowrd.setBackground(drawable);
                    }

                    if(email.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
                        Toast.makeText(signUp.this, "Please fill in the missing fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(password.equals(confirmPassword)){
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String userId = task.getResult().getUser().getUid();
                                            Log.d(TAG, "User ID: " + userId);

                                            Intent intent = new Intent(getApplicationContext(), signIn.class);
                                            startActivity(intent);
                                            finish();

                                            Toast.makeText(signUp.this, "Account Successfully Registered", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                                            if (task.getException() != null) {
                                            } else {
                                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                Toast.makeText(signUp.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                    }else{
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignUpCPassowrd.setBackground(drawable);
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
                finish();
            }
        });

        etSignUpEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSignUpEmail.setBackground(drawable);
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
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSignUpName.setBackground(drawable);
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
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSignUpPassword.setBackground(drawable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSignUpCPassowrd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSignUpCPassowrd.setBackground(drawable);
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
