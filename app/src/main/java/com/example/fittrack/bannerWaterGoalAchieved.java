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

public class bannerWaterGoalAchieved extends AppCompatActivity {
    FirebaseAuth mAuth;
    TextView tvWaterBannerTaken, tvWaterBannerUsername, tvWaterBannerPercent, tvWaterBannerGoal;
    Button btnWaterBannerShare, btnWaterBannerCancel;
    ProgressBar pbWaterBanner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imBackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_goal_achieved_banner);
        imBackBtn = findViewById(R.id.imWaterBannerBack);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvWaterBannerTaken = findViewById(R.id.tvWaterBannerTaken);
        tvWaterBannerUsername = findViewById(R.id.tvWaterBannerUsername);
        tvWaterBannerPercent = findViewById(R.id.tvWaterBannerPercent);
        tvWaterBannerGoal = findViewById(R.id.tvWaterBannerGoal);
        btnWaterBannerShare = findViewById(R.id.btnWaterBannerShare);
        btnWaterBannerCancel = findViewById(R.id.btnWaterBannerCancel);
        pbWaterBanner = findViewById(R.id.pbWaterBanner);
        imBackBtn = findViewById(R.id.imWaterBannerBack);

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String username;
                int dailyWaterTaken, waterDailyGoal, waterPercent;
                username = documentSnapshot.getString("name");
                dailyWaterTaken = documentSnapshot.getLong("dailyWaterTaken").intValue();
                waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();

                if (waterDailyGoal != 0) {
                    waterPercent = Math.min((int) (((double) dailyWaterTaken / waterDailyGoal) * 100), 100);
                } else {
                    waterPercent = 0;
                }

                String formattedWaterTaken = NumberFormat.getInstance(Locale.US).format(dailyWaterTaken);
                String formattedDailyGoal = NumberFormat.getInstance(Locale.US).format(waterDailyGoal);

                tvWaterBannerUsername.setText(username);
                tvWaterBannerTaken.setText(formattedWaterTaken + " ml");
                tvWaterBannerPercent.setText(String.valueOf(waterPercent) + "%");
                tvWaterBannerGoal.setText(String.valueOf(formattedDailyGoal));
                pbWaterBanner.setMax(100);
                pbWaterBanner.setProgress(waterPercent);
            }else{
                Log.e(TAG, "Document does not exist");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "An error occurred: " + e.getMessage());
        });
        imBackBtn.setOnClickListener(view -> onBackPressed());
        btnWaterBannerShare.setOnClickListener(v -> {
            Uri imageUri = Uri.parse("res/image/penguin.gif");

            Intent shareIntent = new Intent(Intent.ACTION_SENDTO);
            shareIntent.setType("image/gif");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            startActivity(Intent.createChooser(shareIntent, "Share GIF via"));
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}