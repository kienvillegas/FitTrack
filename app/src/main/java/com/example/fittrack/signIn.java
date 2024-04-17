package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
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
    TextView tvSignIn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        btnSignIn = findViewById(R.id.btnSignIn);
        etSignInEmail = findViewById(R.id.etSignInEmail);
        etSignInPasssword = findViewById(R.id.etSignInPassword);
        tvSignIn = findViewById(R.id.tvSignUp);
        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;

                email = String.valueOf(etSignInEmail.getText());
                password = String.valueOf(etSignInPasssword.getText());

                try{
                    if(!email.isEmpty() && !password.isEmpty()){
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "signInWithEmail:success");
                                            Toast.makeText(signIn.this, "Signed In Successfully", Toast.LENGTH_SHORT).show();
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            if(currentUser != null){
                                                String uid = currentUser.getUid();
                                                handleUserStatus(uid);
                                            }
                                        } else {
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                            Toast.makeText(signIn.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(signIn.this, "Please fill in missing fields.", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    Toast.makeText(signIn.this, "An Error Occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleUserStatus(String uid){
        Map<String, Object> data = new HashMap<>();
        data.put("isOnline", true);

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Data is added to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add data to Firestore");
                });
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
        if(currentUser != null){
            currentUser.reload();
        }
    }
}