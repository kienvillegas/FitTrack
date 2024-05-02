package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class bannerStepGoalAchieved extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    FirebaseAuth mAuth;
    TextView tvStepBannerTaken, tvStepBannerUsername, tvStepBannerPercent, tvStepBannerGoal;
    Button btnStepBannerShare, btnStepBannerCancel;
    ProgressBar pbStepBanner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imBackBtn, imBannerStepAchievement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_step_goal_achieved_banner);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

//        tvStepBannerTaken = findViewById(R.id.tvStepBannerTaken);
//        tvStepBannerUsername = findViewById(R.id.tvStepBannerUsername);
//        tvStepBannerPercent = findViewById(R.id.tvStepBannerPercent);
//        tvStepBannerGoal = findViewById(R.id.tvStepBannerGoal);
        btnStepBannerShare = findViewById(R.id.btnStepBannerShare);
        btnStepBannerCancel = findViewById(R.id.btnStepBannerCancel);
//        pbStepBanner = findViewById(R.id.pbStepBanner);
        imBackBtn = findViewById(R.id.imStepBannerBack);
        imBannerStepAchievement = findViewById(R.id.imBannerStepAchievement);

//        DocumentReference docRef = db.collection("users").document(userId);
//        docRef.get().addOnSuccessListener(documentSnapshot -> {
//            if(documentSnapshot.exists()){
//                String username;
//                int dailyStepTaken, stepDailyGoal, stepPercent;
//                username = documentSnapshot.getString("name");
//                dailyStepTaken = documentSnapshot.getLong("dailyStepTaken").intValue();
//                stepDailyGoal = documentSnapshot.getLong("stepDailyGoal").intValue();
//
//                if (stepDailyGoal != 0) {
//                    stepPercent = Math.min((int) (((double) dailyStepTaken / stepDailyGoal) * 100), 100);
//                } else {
//                    stepPercent = 0;
//                }
//
//                String formattedStepTaken = NumberFormat.getInstance(Locale.US).format(dailyStepTaken);
//                String formattedDailyGoal = NumberFormat.getInstance(Locale.US).format(stepDailyGoal);
//
//                tvStepBannerUsername.setText(username);
//                tvStepBannerTaken.setText(formattedStepTaken);
//                tvStepBannerPercent.setText(String.valueOf(stepPercent) + "%");
//                tvStepBannerGoal.setText(String.valueOf(formattedDailyGoal));
//                pbStepBanner.setMax(100);
//                pbStepBanner.setProgress(stepPercent);
//            }else{
//                Log.e(TAG, "Document does not exist");
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "An error occurred: " + e.getMessage());
//        });


        imBackBtn.setOnClickListener(view -> onBackPressed());
        btnStepBannerShare.setOnClickListener(v -> {
            Drawable drawable = imBannerStepAchievement.getDrawable();
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

        btnStepBannerCancel.setOnClickListener(v -> {

            Intent intent = new Intent(getApplicationContext(), actStepTracker.class);
            startActivity(intent);
            finish();
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}