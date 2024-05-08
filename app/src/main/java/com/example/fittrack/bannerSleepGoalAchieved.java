package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class bannerSleepGoalAchieved extends AppCompatActivity {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    FirebaseAuth mAuth;
    ImageView imBackBtn;
    TextView tvSleepBannerTaken, tvSleepBannerUsername, tvSleepBannerPercent, tvSleepBannerGoal;
    Button btnSleepBannerShare, btnSleepBannerCancel;
    ProgressBar pbSleepBanner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imBannerSleepAchievement;
    Drawable drawable;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();

        setContentView(R.layout.activity_banner_sleep_goal_achieved);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String userId = currentUser.getUid();
//
//        tvSleepBannerTaken = findViewById(R.id.tvSleepBannerTaken);
//        tvSleepBannerUsername = findViewById(R.id.tvSleepBannerUsername);
//        tvSleepBannerPercent = findViewById(R.id.tvSleepBannerPercent);
//        tvSleepBannerGoal = findViewById(R.id.tvSleepBannerGoal);
        btnSleepBannerShare = findViewById(R.id.btnSleepBannerShare);
        btnSleepBannerCancel = findViewById(R.id.btnSleepBannerCancel);
//        pbSleepBanner = findViewById(R.id.pbCalorieBanner);
        imBackBtn = findViewById(R.id.imCalorieBannerBack);
        imBannerSleepAchievement = findViewById(R.id.imBannerSleepAchievement);

//        DocumentReference docRef = db.collection("users").document(userId);
//        docRef.get().addOnSuccessListener(documentSnapshot -> {
//            if(documentSnapshot.exists()){
//                String username;
//                int dailySleepTaken, sleepDailyGoal, sleepPercent;
//                username = documentSnapshot.getString("name");
//                dailySleepTaken = documentSnapshot.getLong("dailySleepTaken").intValue();
//                sleepDailyGoal = documentSnapshot.getLong("sleepDailyGoal").intValue();
//
//                if (sleepDailyGoal != 0) {
//                    sleepPercent = Math.min((int) (((double) dailySleepTaken / sleepDailyGoal) * 100), 100);
//                } else {
//                    sleepPercent = 0;
//                }
//
//                String formattedSleepTaken = NumberFormat.getInstance(Locale.US).format(dailySleepTaken);
//                String formattedDailyGoal = NumberFormat.getInstance(Locale.US).format(sleepDailyGoal);
//
//                tvSleepBannerUsername.setText(username);
//                tvSleepBannerTaken.setText(formattedSleepTaken + " Hours");
//                tvSleepBannerPercent.setText(String.valueOf(sleepPercent) + "%");
//                tvSleepBannerGoal.setText(String.valueOf(formattedDailyGoal));
//                pbSleepBanner.setMax(100);
//                pbSleepBanner.setProgress(sleepPercent);
//            }else{
//                Log.e(TAG, "Document does not exist");
//            }
//        }).addOnFailureListener(e -> {
//            Log.e(TAG, "An error occurred: " + e.getMessage());
//        });
        imBackBtn.setOnClickListener(view -> onBackPressed());

        btnSleepBannerShare.setOnClickListener(v -> {
            Drawable drawable = imBannerSleepAchievement.getDrawable();
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

        btnSleepBannerCancel.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), actSleepTracker.class);
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

    public void onBackPressed() {
        super.onBackPressed();
    }
}