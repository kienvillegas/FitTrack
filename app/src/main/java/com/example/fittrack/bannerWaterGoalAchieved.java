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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class bannerWaterGoalAchieved extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    FirebaseAuth mAuth;
    TextView tvWaterBannerTaken, tvWaterBannerUsername, tvWaterBannerPercent, tvWaterBannerGoal;
    Button btnWaterBannerShare, btnWaterBannerCancel;
    ProgressBar pbWaterBanner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imBackBtn, imBannerWaterAchievement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_water_goal_achieved_banner);
        imBackBtn = findViewById(R.id.imWaterBannerBack);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

//        tvWaterBannerTaken = findViewById(R.id.tvWaterBannerTaken);
//        tvWaterBannerUsername = findViewById(R.id.tvWaterBannerUsername);
//        tvWaterBannerPercent = findViewById(R.id.tvWaterBannerPercent);
//        tvWaterBannerGoal = findViewById(R.id.tvWaterBannerGoal);
        btnWaterBannerShare = findViewById(R.id.btnWaterBannerShare);
        btnWaterBannerCancel = findViewById(R.id.btnWaterBannerCancel);
//        pbWaterBanner = findViewById(R.id.pbWaterBanner);
        imBackBtn = findViewById(R.id.imWaterBannerBack);
        imBannerWaterAchievement = findViewById(R.id.imBannerWaterAchievement);

//        DocumentReference docRef = db.collection("users").document(userId);
//        docRef.get().addOnSuccessListener(documentSnapshot -> {
//            if(documentSnapshot.exists()){
//                String username;
//                int dailyWaterTaken, waterDailyGoal, waterPercent;
//                username = documentSnapshot.getString("name");
//                dailyWaterTaken = documentSnapshot.getLong("dailyWaterTaken").intValue();
//                waterDailyGoal = documentSnapshot.getLong("waterDailyGoal").intValue();
//
//                if (waterDailyGoal != 0) {
//                    waterPercent = Math.min((int) (((double) dailyWaterTaken / waterDailyGoal) * 100), 100);
//                } else {
//                    waterPercent = 0;
//                }
//
//                String formattedWaterTaken = NumberFormat.getInstance(Locale.US).format(dailyWaterTaken);
//                String formattedDailyGoal = NumberFormat.getInstance(Locale.US).format(waterDailyGoal);
//
//                tvWaterBannerUsername.setText(username);
//                tvWaterBannerTaken.setText(formattedWaterTaken + " ml");
//                tvWaterBannerPercent.setText(String.valueOf(waterPercent) + "%");
//                tvWaterBannerGoal.setText(String.valueOf(formattedDailyGoal));
//                pbWaterBanner.setMax(100);
//                pbWaterBanner.setProgress(waterPercent);
//            }else{
//                Log.e(TAG, "Document does not exist");
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "An error occurred: " + e.getMessage());
//        });
        imBackBtn.setOnClickListener(view -> onBackPressed());
        btnWaterBannerShare.setOnClickListener(v -> {
            Drawable drawable = imBannerWaterAchievement.getDrawable();
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

        btnWaterBannerCancel.setOnClickListener(v -> {

            Intent intent = new Intent(getApplicationContext(), actWaterIntakeTracker.class);
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