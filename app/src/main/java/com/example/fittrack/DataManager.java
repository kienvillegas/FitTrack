package com.example.fittrack;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataManager {
    private SharedPreferences sharedPreferences;
    private static final String DATE_KEY = "stored_date";

    public DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences("MyDataPrefs", Context.MODE_PRIVATE);
    }

    public void saveCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        sharedPreferences.edit().putString(DATE_KEY, currentDateTime).apply();
    }

    public String getStoredDate() {
        return sharedPreferences.getString(DATE_KEY, "");
    }
}