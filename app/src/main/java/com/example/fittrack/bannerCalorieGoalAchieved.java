package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class bannerCalorieGoalAchieved extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvCalorieBannerTaken, tvCalorieBannerUsername, tvCalorieBannerPercent, tvCalorieBannerGoal;
    Button btnCalorieBannerShare, btnCalorieBannerCancel;
    ProgressBar pbCalorieBanner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_goal_achieved_banner);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvCalorieBannerTaken = findViewById(R.id.tvCalorieBannerTaken);
        tvCalorieBannerUsername = findViewById(R.id.tvCalorieBannerUsername);
        tvCalorieBannerPercent = findViewById(R.id.tvCalorieBannerPercent);
        tvCalorieBannerGoal = findViewById(R.id.tvCalorieBannerGoal);
        btnCalorieBannerShare = findViewById(R.id.btnCalorieBannerShare);
        btnCalorieBannerCancel = findViewById(R.id.btnCalorieBannerCancel);
        pbCalorieBanner = findViewById(R.id.pbCalorieBanner);
        imBackBtn = findViewById(R.id.imCalorieBannerBack);

            DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String username;
                int dailyCalorieTaken, calorieDailyGoal, caloriePercent;
                username = documentSnapshot.getString("name");
                dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();
                calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();

                if (calorieDailyGoal != 0) {
                    caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
                } else {
                    caloriePercent = 0;
                }

                String formattedCalorieTaken = NumberFormat.getInstance(Locale.US).format(dailyCalorieTaken);
                String formattedDailyGoal = NumberFormat.getInstance(Locale.US).format(calorieDailyGoal);

                tvCalorieBannerUsername.setText(username);
                tvCalorieBannerTaken.setText(formattedCalorieTaken + " Kcal");
                tvCalorieBannerPercent.setText(String.valueOf(caloriePercent) + "%");
                tvCalorieBannerGoal.setText(formattedDailyGoal);
                pbCalorieBanner.setMax(100);
                pbCalorieBanner.setProgress(caloriePercent);
            }else{
                Log.e(TAG, "Document does not exist");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "An error occurred: " + e.getMessage());
        });

        imBackBtn.setOnClickListener(view -> onBackPressed());

        btnCalorieBannerShare.setOnClickListener(v -> {
            String imagePath = "res/drawable/motivation.jpg";
            File imageFile = new File(imagePath);

            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

            startActivity(Intent.createChooser(shareIntent, "Share Image via"));
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}

