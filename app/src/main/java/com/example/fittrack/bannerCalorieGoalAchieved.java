package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class bannerCalorieGoalAchieved extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView imBackBtn, imBannerCalorieAchievement;
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

//        tvCalorieBannerTaken = findViewById(R.id.tvCalorieBannerTaken);
//        tvCalorieBannerUsername = findViewById(R.id.tvCalorieBannerUsername);
//        tvCalorieBannerPercent = findViewById(R.id.tvCalorieBannerPercent);
//        tvCalorieBannerGoal = findViewById(R.id.tvCalorieBannerGoal);
        btnCalorieBannerShare = findViewById(R.id.btnCalorieBannerShare);
        btnCalorieBannerCancel = findViewById(R.id.btnCalorieBannerCancel);
//        pbCalorieBanner = findViewById(R.id.pbCalorieBanner);
        imBackBtn = findViewById(R.id.imCalorieBannerBack);
        imBannerCalorieAchievement = findViewById(R.id.imBannerCalorieAchievement);
//            DocumentReference docRef = db.collection("users").document(userId);
//        docRef.get().addOnSuccessListener(documentSnapshot -> {
//            if(documentSnapshot.exists()){
//                String username;
//                int dailyCalorieTaken, calorieDailyGoal, caloriePercent;
//                username = documentSnapshot.getString("name");
//                dailyCalorieTaken = documentSnapshot.getLong("dailyCalorieTaken").intValue();
//                calorieDailyGoal = documentSnapshot.getLong("calorieDailyGoal").intValue();
//
//                if (calorieDailyGoal != 0) {
//                    caloriePercent = Math.min((int) (((double) dailyCalorieTaken / calorieDailyGoal) * 100), 100);
//                } else {
//                    caloriePercent = 0;
//                }
//
//                String formattedCalorieTaken = NumberFormat.getInstance(Locale.US).format(dailyCalorieTaken);
//                String formattedDailyGoal = NumberFormat.getInstance(Locale.US).format(calorieDailyGoal);
//
//                tvCalorieBannerUsername.setText(username);
//                tvCalorieBannerTaken.setText(formattedCalorieTaken + " Kcal");
//                tvCalorieBannerPercent.setText(String.valueOf(caloriePercent) + "%");
//                tvCalorieBannerGoal.setText(formattedDailyGoal);
//                pbCalorieBanner.setMax(100);
//                pbCalorieBanner.setProgress(caloriePercent);
//            }else{
//                Log.e(TAG, "Document does not exist");
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "An error occurred: " + e.getMessage());
//        });

        imBackBtn.setOnClickListener(view -> onBackPressed());

        btnCalorieBannerShare.setOnClickListener(v -> {
            Drawable drawable = imBannerCalorieAchievement.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                Uri imageUri = Uri.parse(path);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                startActivity(Intent.createChooser(shareIntent, "Share Image"));
            }
        });

        btnCalorieBannerCancel.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), actFoodIntakeTracker.class);
            startActivity(intent);
            finish();
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}

