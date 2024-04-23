package com.example.fittrack;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class actStepTracker extends AppCompatActivity {
    ProgressBar pbStepTracker;
    TextView tvStepsProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_tracker);
        pbStepTracker = findViewById(R.id.pbWaterIntake);

        pbStepTracker.setProgress(50);
    }
}