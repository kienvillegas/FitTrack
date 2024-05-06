package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class SplashScreen extends AppCompatActivity {
    private static final long SPLASH_TIMEOUT = 10000;
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_splash_screen);
        ImageView logo = findViewById(R.id.imSplashLogo);

        ObjectAnimator animator = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, signIn.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIMEOUT);
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

}