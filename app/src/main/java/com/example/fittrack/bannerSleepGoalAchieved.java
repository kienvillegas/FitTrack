package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class bannerSleepGoalAchieved extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvSleepBannerTaken, tvSleepBannerUsername, tvSleepBannerPercent, tvSleepBannerGoal;
    Button btnSleepBannerShare, btnSleepBannerCancel;
    ProgressBar pbSleepBanner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_sleep_goal_achieved);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvSleepBannerTaken = findViewById(R.id.tvSleepBannerTaken);
        tvSleepBannerUsername = findViewById(R.id.tvSleepBannerUsername);
        tvSleepBannerPercent = findViewById(R.id.tvSleepBannerPercent);
        tvSleepBannerGoal = findViewById(R.id.tvSleepBannerGoal);
        btnSleepBannerShare = findViewById(R.id.btnSleepBannerShare);
        btnSleepBannerCancel = findViewById(R.id.btnSleepBannerCancel);
        pbSleepBanner = findViewById(R.id.pbCalorieBanner);
        imBackBtn = findViewById(R.id.imCalorieBannerBack);

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String username;
                int dailySleepTaken, sleepDailyGoal, sleepPercent;
                username = documentSnapshot.getString("name");
                dailySleepTaken = documentSnapshot.getLong("dailySleepTaken").intValue();
                sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal").intValue();

                if (sleepDailyGoal != 0) {
                    sleepPercent = Math.min((int) (((double) dailySleepTaken / sleepDailyGoal) * 100), 100);
                } else {
                    sleepPercent = 0;
                }

                String formattedSleepTaken = NumberFormat.getInstance(Locale.US).format(dailySleepTaken);
                String formattedDailyGoal = NumberFormat.getInstance(Locale.US).format(sleepDailyGoal);

                tvSleepBannerUsername.setText(username);
                tvSleepBannerTaken.setText(formattedSleepTaken + " Hours");
                tvSleepBannerPercent.setText(String.valueOf(sleepPercent) + "%");
                tvSleepBannerGoal.setText(String.valueOf(formattedDailyGoal));
                pbSleepBanner.setMax(100);
                pbSleepBanner.setProgress(sleepPercent);
            }else{
                Log.e(TAG, "Document does not exist");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "An error occurred: " + e.getMessage());
        });
        imBackBtn.setOnClickListener(view -> onBackPressed());

        btnSleepBannerShare.setOnClickListener(v -> {
            Uri imageUri = Uri.parse("res/image/penguin.gif");

            Intent shareIntent = new Intent(Intent.ACTION_SENDTO);
            shareIntent.setType("image/gif");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            startActivity(Intent.createChooser(shareIntent, "Share GIF via"));
        });
    }
    public void onBackPressed() {
        super.onBackPressed();
    }
}