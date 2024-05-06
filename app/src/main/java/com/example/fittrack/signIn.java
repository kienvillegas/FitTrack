package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class signIn extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;
    private FirebaseAuth mAuth;

    Button btnSignIn;
    EditText etSignInEmail, etSignInPasssword;
    TextView tvSignUp, tvForgotPassword;
    ProgressBar pbSignIn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_sign_in);
        btnSignIn = findViewById(R.id.btnSignIn);
        etSignInEmail = findViewById(R.id.etSignInEmail);
        etSignInPasssword = findViewById(R.id.etSignInPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        pbSignIn = findViewById(R.id.pbSignIn);
        mAuth = FirebaseAuth.getInstance();

        CollectionReference userRef = db.collection("users");

        pbSignIn.setVisibility(View.GONE);
        btnSignIn.setVisibility(View.VISIBLE);
        tvForgotPassword.setVisibility(View.VISIBLE);


        btnSignIn.setOnClickListener(v -> {
            pbSignIn.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);
            etSignInEmail.setError(null);
            etSignInPasssword.setError(null);
            etSignInEmail.setBackgroundResource(R.drawable.text_field_bg_white);
            etSignInPasssword.setBackgroundResource(R.drawable.text_field_bg_white);


            String email, password;

            email = String.valueOf(etSignInEmail.getText()).trim();
            password = String.valueOf(etSignInPasssword.getText()).trim();

            try{
                if (email.isEmpty()) {
                        pbSignIn.setVisibility(View.GONE);
                        btnSignIn.setVisibility(View.VISIBLE);

                        etSignInEmail.setBackgroundResource(R.drawable.text_field_red);
                        etSignInEmail.setError("Required");
                        etSignInEmail.requestFocus();
                        return;
                     }

                    if (password.isEmpty()) {
                        pbSignIn.setVisibility(View.GONE);
                        btnSignIn.setVisibility(View.VISIBLE);

                        etSignInPasssword.setBackgroundResource(R.drawable.text_field_red);
                        etSignInPasssword.setError("Required");
                        etSignInPasssword.requestFocus();
                        return;
                    }

                    if (!email.endsWith("@gmail.com")) {
                        pbSignIn.setVisibility(View.GONE);
                        btnSignIn.setVisibility(View.VISIBLE);

                        etSignInEmail.setError("Invalid Email Format");
                        etSignInEmail.requestFocus();
                        return;
                    }

                    userRef.whereEqualTo("email", email).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                Log.d(TAG, "Successful colleciton query");
                                if(!queryDocumentSnapshots.isEmpty()) {
                                    Log.d(TAG, "Email is found in the collection");
                                    mAuth.signInWithEmailAndPassword(email, password)
                                            .addOnSuccessListener(authResult -> {
                                                pbSignIn.setVisibility(View.GONE);
                                                btnSignIn.setVisibility(View.VISIBLE);
                                                etSignInEmail.setText("");
                                                etSignInPasssword.setText("");

                                                Intent intent = new Intent(getApplicationContext(), dashboardPage.class);
                                                startActivity(intent);
                                                finish();
                                            }).addOnFailureListener(e -> {
                                                pbSignIn.setVisibility(View.GONE);
                                                btnSignIn.setVisibility(View.VISIBLE);

                                                if(e instanceof  FirebaseAuthException){
                                                    FirebaseAuthException firebaseAuthException = (FirebaseAuthException) e;
                                                    String errorCode = firebaseAuthException.getErrorCode();

                                                    switch (errorCode){
                                                        case "ERROR_INVALID_CREDENTIAL":
                                                            etSignInPasssword.setBackgroundResource(R.drawable.text_field_red);
                                                            etSignInPasssword.setError("Incorrect Password");
                                                            etSignInPasssword.requestFocus();
                                                            break;
                                                        case "ERROR_WRONG_PASSWORD":
                                                            etSignInPasssword.setBackgroundResource(R.drawable.text_field_red);
                                                            etSignInPasssword.setError("Incorrect Password");
                                                            etSignInPasssword.requestFocus();
                                                            break;
                                                        case "ERROR_INVALID_EMAIL":
                                                            etSignInEmail.setBackgroundResource(R.drawable.text_field_red);
                                                            etSignInEmail.setError("Incorrect Email");
                                                            etSignInEmail.requestFocus();
                                                            break;
                                                        case "ERROR_USER_NOT_FOUND":
                                                            etSignInEmail.setBackgroundResource(R.drawable.text_field_red);
                                                            etSignInEmail.setError("User Not Found");
                                                            etSignInEmail.requestFocus();
                                                            break;
                                                        default:
                                                            Log.e(TAG, "Error Code: "  + errorCode);
                                                    }
                                                }
                                            });
                                }else {
                                    pbSignIn.setVisibility(View.GONE);
                                    btnSignIn.setVisibility(View.VISIBLE);

                                    etSignInEmail.setBackgroundResource(R.drawable.text_field_red);
                                    etSignInEmail.setError("User Not Found");
                                    etSignInEmail.requestFocus();
                                    Log.e(TAG, "No Documents found linked to that email, " + email);
                                }
                            }).addOnFailureListener(e -> {
                                pbSignIn.setVisibility(View.GONE);
                                btnSignIn.setVisibility(View.VISIBLE);

                                Log.e(TAG, "Error Occurred Searching Through Collection: " + e.getMessage());
                            });
            }catch(Exception e){
                pbSignIn.setVisibility(View.GONE);
                btnSignIn.setVisibility(View.VISIBLE);
                etSignInEmail.setText("");
                etSignInPasssword.setText("");
                Toast.makeText(signIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), signUp.class);
            startActivity(intent);
            finish();
        });
        tvForgotPassword.setOnClickListener(v -> {
            pbSignIn.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.GONE);
            etSignInEmail.setError(null);

            String email = etSignInEmail.getText().toString().trim();
            Log.d(TAG, "Email entered:" + email);

            try{
                if (email.isEmpty()) {
                    pbSignIn.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.VISIBLE);
                    tvForgotPassword.setVisibility(View.VISIBLE);

                    etSignInEmail.setBackgroundResource(R.drawable.text_field_red);
                    etSignInEmail.setError("Required");
                    etSignInEmail.requestFocus();
                    return;
                }

                if (!email.endsWith("@gmail.com")) {
                    pbSignIn.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.VISIBLE);
                    tvForgotPassword.setVisibility(View.VISIBLE);

                    etSignInEmail.setError("Invalid Email Format");
                    etSignInEmail.requestFocus();
                    return;
                }

                userRef.whereEqualTo("email", email).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if(!queryDocumentSnapshots.isEmpty()){
                                Log.e(TAG, "Document found linked to the email");
                                mAuth.sendPasswordResetEmail(email)
                                        .addOnSuccessListener(unused -> {
                                            Log.d(TAG, "Password reset email sent successfully to: " + email);

                                            pbSignIn.setVisibility(View.GONE);
                                            btnSignIn.setVisibility(View.VISIBLE);
                                            tvForgotPassword.setVisibility(View.VISIBLE);
                                            etSignInEmail.setText("");
                                            etSignInPasssword.setText("");

                                            Toast.makeText(signIn.this, "Reset Password Link has been sent to your email", Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(e -> {
                                            Log.d(TAG, "Query successful. Retrieved documents: " + queryDocumentSnapshots.size());
                                            Log.e(TAG, "Failed to send password reset email: " + e.getMessage(), e);

                                            pbSignIn.setVisibility(View.GONE);
                                            btnSignIn.setVisibility(View.VISIBLE);
                                            tvForgotPassword.setVisibility(View.VISIBLE);
                                            etSignInPasssword.setText("");

                                            if(e instanceof FirebaseAuthException) {
                                                FirebaseAuthException firebaseAuthException = (FirebaseAuthException) e;
                                                String errorCode = firebaseAuthException.getErrorCode();

                                                Log.e(TAG, "FirebaseAuthException ErrorCode: " + errorCode);


                                                int resourceId = R.drawable.text_field_red;
                                                Drawable drawable = getResources().getDrawable(resourceId);

                                                switch (errorCode) {
                                                    case "ERROR_INVALID_EMAIL":
                                                        etSignInEmail.setBackground(drawable);
                                                        etSignInEmail.setError("Invalid Email Format");
                                                        etSignInEmail.requestFocus();
                                                        break;
                                                    case "ERROR_USER_NOT_FOUND":
                                                        etSignInEmail.setBackground(drawable);
                                                        etSignInEmail.setError("User Not Found");
                                                        etSignInEmail.requestFocus();
                                                        break;
                                                    default:
                                                        Log.e(TAG, "Unknown error code: " + errorCode);
                                                }
                                            }
                                        });
                            }else{
                                pbSignIn.setVisibility(View.GONE);
                                btnSignIn.setVisibility(View.VISIBLE);
                                tvForgotPassword.setVisibility(View.VISIBLE);

                                etSignInEmail.setBackgroundResource(R.drawable.text_field_red);
                                etSignInEmail.setError("User Not Found");
                                etSignInEmail.requestFocus();

                                Log.e(TAG, "No Documents found linked to that email");
                            }
                        }).addOnFailureListener(e -> {
                            pbSignIn.setVisibility(View.GONE);
                            btnSignIn.setVisibility(View.VISIBLE);
                            tvForgotPassword.setVisibility(View.VISIBLE);

                            Log.e(TAG, "Error executing Firestore query: " + e.getMessage(), e);
                        });
            }catch (Exception e){
                pbSignIn.setVisibility(View.GONE);
                btnSignIn.setVisibility(View.VISIBLE);
                tvForgotPassword.setVisibility(View.VISIBLE);

                Toast.makeText(signIn.this, "An Error Occured: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        etSignInEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSignInEmail.setBackgroundResource(R.drawable.text_field_bg_white);
                etSignInEmail.setError(null);
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
                etSignInPasssword.setBackgroundResource(R.drawable.text_field_bg_white);
                etSignInPasssword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
}