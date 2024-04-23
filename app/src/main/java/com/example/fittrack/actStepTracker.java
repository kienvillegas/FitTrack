package com.example.fittrack;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class actStepTracker extends AppCompatActivity {
    private FirebaseAuth mAuth;

    ProgressBar pbStepTracker;
    ImageView imBackBtn;
    TextView tvStepsProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_tracker);
        mAuth = FirebaseAuth.getInstance();
        pbStepTracker = findViewById(R.id.pbWaterIntake);
        imBackBtn = findViewById(R.id.imBackBtn);

        imBackBtn.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.reload();
        }
    }
}
