package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.AsyncTaskLoader;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class settingsDesignFragment extends Fragment {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    private int currentTheme;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView imDesignEditProfile, imDesignChangePass, imDesignNotification, imDesign;
    Button btnChangeThemeSave,btnChangeThemeDefault, btnChangeThemeSecond, btnChangeThemeThird;

    TextView tvDesignUsername;

    SharedPreferences prefs;

    public settingsDesignFragment() {
    }
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    public void onStart() {
        super.onStart();
        // Add auth state listener when the fragment starts
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove auth state listener when the fragment stops
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the authStateListener when the fragment is destroyed
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize AuthStateListener
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Log.d(TAG, "onAuthStateChanged:signed_out");
                // Example: Redirect to sign-in fragment
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
                builder.setTitle("Sign In Required")
                        .setMessage("Please sign in to access this feature.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Handle sign-in action or any other action
                            // If currentUser is null, navigate to the sign-in activity
                            Intent intent = new Intent(requireContext(), signIn.class);
                            startActivity(intent);
                            requireActivity().finish(); // Finish the current activity
                        })
                        .setCancelable(false) // Set dialog non-cancelable
                        .show();
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove the authStateListener to prevent memory leaks
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the authStateListener when the fragment is resumed
        if (authStateListener != null) {
            mAuth.addAuthStateListener(authStateListener);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_design, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        tvDesignUsername = view.findViewById(R.id.tvDesignUsername);
        imDesignEditProfile = view.findViewById(R.id.imDesignEditProfile);
        imDesignChangePass = view.findViewById(R.id.imDesignChangePass);
//        imDesignNotification = view.findViewById(R.id.imDesignNotification);
        imDesign = view.findViewById(R.id.imDesign);

        btnChangeThemeDefault = view.findViewById(R.id.btnChangeThemeDefault);
        btnChangeThemeSecond = view.findViewById(R.id.btnChangeThemeSecond);
        btnChangeThemeThird = view.findViewById(R.id.btnChangeThemeThird);
        btnChangeThemeSave = view.findViewById(R.id.btnChangeThemeSave);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        hideContentView(view);

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    showContentView(view);
                    String username = documentSnapshot.getString("name");
                    tvDesignUsername.setText(username);
                })
                .addOnFailureListener(e -> {
                    showContentView(view);
                    Log.e(TAG, "Error fetching data from Firestore: " + e.getMessage());
                });

        Drawable drawable = getResources().getDrawable(R.drawable.icon_check);
        btnChangeThemeDefault.setOnClickListener(v -> {
            currentTheme = 0;
            changeTheme(currentTheme);
            btnChangeThemeDefault.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable);
            btnChangeThemeSecond.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            btnChangeThemeThird.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        });

        btnChangeThemeSecond.setOnClickListener(v -> {
            currentTheme = 1;
            changeTheme(currentTheme);
            btnChangeThemeDefault.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            btnChangeThemeSecond.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable);
            btnChangeThemeThird.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        });

        btnChangeThemeThird.setOnClickListener(v -> {
            currentTheme = 2;
            changeTheme(currentTheme);
            btnChangeThemeDefault.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            btnChangeThemeSecond.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            btnChangeThemeThird.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable);
        });

        btnChangeThemeSave.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View  saveChangesConfirmation =  LayoutInflater.from(getActivity()).inflate(R.layout.save_changes_confirmation, null);
            saveChangesConfirmation.setBackgroundResource(R.drawable.confirmation_bg);
            builder.setView(saveChangesConfirmation);
            AlertDialog dialog = builder.create();

            Button btnCancel, btnConfirm;
            btnCancel = saveChangesConfirmation.findViewById(R.id.btnSaveChangesNo);
            btnConfirm = saveChangesConfirmation.findViewById(R.id.btnSaveChangesYes);

            btnConfirm.setOnClickListener(v1 -> {
                try{
                    dialog.dismiss();
                    saveThemePreference(currentTheme);

                    Intent intent = new Intent(getContext(), dashboardPage.class);
                    startActivity(intent);
                    getActivity().finish();
                }catch(Exception e){
                    e.printStackTrace();
                }
            });

            btnCancel.setOnClickListener(v12 -> {
                dialog.dismiss();
                changeTheme(THEME_DEFAULT);
            });
            dialog.show();
        });

        imDesignEditProfile.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsEditProfileFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        imDesignChangePass.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsChangePasswordFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

//        imDesignNotification.setOnClickListener(v -> {
//            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            Fragment newFragment = new settingsConfigNotifFragment();
//            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
//            fragmentTransaction.commit();
//        });

        imDesign.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        return view;
    }

    private void applyTheme(int theme) {
        Log.d(TAG, "Applying theme: " + theme);
        switch (theme) {
            case THEME_ORANGE:
                requireActivity().setTheme(R.style.AppOrangeTheme);
                break;
            case THEME_GREEN:
                requireActivity().setTheme(R.style.AppGreenTheme);
                break;
            default:
                requireActivity().setTheme(R.style.AppDefaultTheme);
        }
    }

    private void changeTheme(int theme) {
        try{
            Log.d(TAG, "Changing theme to: " + theme);
            applyTheme(theme);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void saveThemePreference(int theme) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(THEME_PREF_KEY, theme);
        editor.apply();
    }
    private void hideContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imDesignEditProfile, R.id.imDesignChangePass, R.id.imDesign, R.id.imageView109};
            int[] textViewIds = {R.id.tvDesignUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView162, R.id.textView185, R.id.textView36};
            int[] buttonIds = {R.id.btnChangeThemeDefault, R.id.btnChangeThemeSecond, R.id.btnChangeThemeThird, R.id.btnChangeThemeSave};
            int[] progressBarIds = {R.id.pbSettingDesign};

            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.GONE);
            }

            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.GONE);
            }

            for (int id : buttonIds) {
                Button button = view.findViewById(id);
                button.setVisibility(View.GONE);
            }

            for (int id : progressBarIds) {
                ProgressBar progressBar = view.findViewById(id);
                progressBar.setVisibility(View.VISIBLE); // Show the progress bar
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imDesignEditProfile, R.id.imDesignChangePass, R.id.imDesign, R.id.imageView109};
            int[] textViewIds = {R.id.tvDesignUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView162, R.id.textView185, R.id.textView36};
            int[] buttonIds = {R.id.btnChangeThemeDefault, R.id.btnChangeThemeSecond, R.id.btnChangeThemeThird, R.id.btnChangeThemeSave};
            int[] progressBarIds = {R.id.pbSettingDesign};

            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.VISIBLE);
            }

            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.VISIBLE);
            }

            for (int id : buttonIds) {
                Button button = view.findViewById(id);
                button.setVisibility(View.VISIBLE);
            }

            for (int id : progressBarIds) {
                ProgressBar progressBar = view.findViewById(id);
                progressBar.setVisibility(View.GONE); // Hide the progress bar
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

