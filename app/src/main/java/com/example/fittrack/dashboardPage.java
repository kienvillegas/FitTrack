package com.example.fittrack;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.common.subtyping.qual.Bottom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class dashboardPage extends AppCompatActivity {
    private FirebaseAuth mAuth;

    ImageView imAddWalk, imAddWater, imAddFood, imAddSleep;
    TextView tvDayMonDate, tvMonYr, tvDayOne, tvDayTwo, tvDayThree, tvDayFour, tvDayFive, tvDaySix, tvDaySeven;
    ImageView imDayOneBg, imDayTwoBg, imDayThreeBg, imDayFourBg, imDayFiveBg, imDaySixBg, imDaySevenBg;
    private TextView[] dayTextViews = new TextView[7];
    private TextView[] dateTextViews = new TextView[7];
    private ImageView[] bgImageView = new ImageView[7];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_page);
        mAuth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavDashboard);
        imAddWalk = findViewById(R.id.imAddWalk);
        imAddWater = findViewById(R.id.imAddWater);
        imAddFood = findViewById(R.id.imAddFood);

        tvDayMonDate = findViewById(R.id.tvDayMonDate);
        tvMonYr = findViewById(R.id.tvMonYr);
        tvDayOne = findViewById(R.id.tvDayOne);
        tvDayTwo = findViewById(R.id.tvDayTwo);
        tvDayThree = findViewById(R.id.tvDayThree);
        tvDayFour = findViewById(R.id.tvDayFour);
        tvDayFive = findViewById(R.id.tvDayFive);
        tvDaySix = findViewById(R.id.tvDaySix);
        tvDaySeven = findViewById(R.id.tvDaySeven);

        imDayOneBg = findViewById(R.id.imDayOneBg);
        imDayTwoBg = findViewById(R.id.imDayTwoBg);
        imDayThreeBg = findViewById(R.id.imDayThreeBg);
        imDayFourBg = findViewById(R.id.imDayFourBg);
        imDayFiveBg = findViewById(R.id.imDayFiveBg);
        imDaySixBg = findViewById(R.id.imDaySixBg);
        imDaySevenBg = findViewById(R.id.imDaySevenBg);

        dayTextViews[0] = findViewById(R.id.textView164); // Sun
        dayTextViews[1] = findViewById(R.id.textView165); // Mon
        dayTextViews[2] = findViewById(R.id.textView166); // Tue
        dayTextViews[3] = findViewById(R.id.textView167); // Wed
        dayTextViews[4] = findViewById(R.id.textView168); // Thur
        dayTextViews[5] = findViewById(R.id.textView169); // Fri
        dayTextViews[6] = findViewById(R.id.textView170); // Sat

        dateTextViews[0] = findViewById(R.id.tvDayOne);
        dateTextViews[1] = findViewById(R.id.tvDayTwo);
        dateTextViews[2] = findViewById(R.id.tvDayThree);
        dateTextViews[3] = findViewById(R.id.tvDayFour);
        dateTextViews[4] = findViewById(R.id.tvDayFive);
        dateTextViews[5] = findViewById(R.id.tvDaySix);
        dateTextViews[6] = findViewById(R.id.tvDaySeven);

        bgImageView[0] = findViewById(R.id.imDayOneBg);
        bgImageView[1] = findViewById(R.id.imDayTwoBg);
        bgImageView[2] = findViewById(R.id.imDayThreeBg);
        bgImageView[3] = findViewById(R.id.imDayFourBg);
        bgImageView[4] = findViewById(R.id.imDayFiveBg);
        bgImageView[5] = findViewById(R.id.imDaySixBg);
        bgImageView[6] = findViewById(R.id.imDaySevenBg);

        setWeeklyCalendar();
        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                return true;
            } else if (item.getItemId() == R.id.nav_activity) {
                startActivity(new Intent(getApplicationContext(), activityPage.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), profilePage.class));
                finish();
                return true;
            }
            return false;
        });

        imAddWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), actStepTracker.class);
                startActivity(intent);
            }
        });

        imAddWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), actWaterIntakeTracker.class);
                startActivity(intent);
            }
        });

        imAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), actFoodIntakeTracker.class);
                startActivity(intent);
            }
        });
    }

    private void setWeeklyCalendar(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayMonDate = new SimpleDateFormat("EEEE, MMMM, dd", Locale.getDefault());
        SimpleDateFormat monYr = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        for (int i = 0; i < 7; i++) {
            dayTextViews[i].setText(dayFormat.format(calendar.getTime()));
            dateTextViews[i].setText(dateFormat.format(calendar.getTime()));

            if (isCurrentDate(calendar)) {
                bgImageView[i].setImageResource(R.drawable.current_date_bg);
                dateTextViews[i].setTextColor(getResources().getColor(R.color.whiteText));
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        String currentDate = dayMonDate.format(calendar.getTime());
        String monthYear = monYr.format(calendar.getTime());

        tvDayMonDate.setText(currentDate);
        tvMonYr.setText(monthYear);
    }

    private boolean isCurrentDate(Calendar calendar) {
        Calendar currentDate = Calendar.getInstance();
        return currentDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.reload();
        }
    }
}