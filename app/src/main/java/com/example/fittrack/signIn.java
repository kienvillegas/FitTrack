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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signIn extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button btnSignIn;
    EditText etSignInEmail, etSignInPasssword;
    TextView tvSignUp, tvForgotPassword;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        btnSignIn = findViewById(R.id.btnSignIn);
        etSignInEmail = findViewById(R.id.etSignInEmail);
        etSignInPasssword = findViewById(R.id.etSignInPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;

                email = String.valueOf(etSignInEmail.getText()).trim();
                password = String.valueOf(etSignInPasssword.getText()).trim();

                try{
                    if(email.isEmpty()){
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignInEmail.setBackground(drawable);                    }

                    if(password.isEmpty()){
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignInPasssword.setBackground(drawable);
                    }

                    if(email.isEmpty() || password.isEmpty()){
                        Toast.makeText(signIn.this, "Please fill in the missing fields", Toast.LENGTH_SHORT).show();
                    }else{
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "createUserWithEmail:success");

                                            Intent intent = new Intent(getApplicationContext(), dashboardPage.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(signIn.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }catch(Exception e){

                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), signUp.class);
                startActivity(intent);
                finish();
            }
        });
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etSignInEmail.getText().toString().trim();

                try{
                    if(!email.isEmpty()){
                        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(signIn.this, "Reset Password Link has been sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(signIn.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        int resourceId = R.drawable.text_field_red;
                        Drawable drawable = getResources().getDrawable(resourceId);
                        etSignInEmail.setBackground(drawable);
                    }
                }catch (Exception e){
                    Toast.makeText(signIn.this, "Error: " + email + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        etSignInEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSignInEmail.setBackground(drawable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSignInPasssword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int resourceId = R.drawable.text_field_bg_white;
                Drawable drawable = getResources().getDrawable(resourceId);
                etSignInPasssword.setBackground(drawable);
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
        if(currentUser != null){
            currentUser.reload();
        }
    }
}