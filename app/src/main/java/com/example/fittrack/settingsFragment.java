package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class settingsFragment extends Fragment {
    ImageView imSettingsEditProfile, imSettingsChangePass, imSettingsNotifications, imSettingsDesign;
    TextView tvSettingsUsername;

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public settingsFragment() {
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
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        imSettingsEditProfile = view.findViewById(R.id.imSettingsEditProfile);
        imSettingsChangePass = view.findViewById(R.id.imSettingsChangePass);
        imSettingsDesign = view.findViewById(R.id.imSettingsDesign);
        tvSettingsUsername = view.findViewById(R.id.tvSettingsUsername);

        hideContentView(view);
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            showContentView(view);
            String username = documentSnapshot.getString("name");
            tvSettingsUsername.setText(username);
        }).addOnFailureListener(e -> {
            showContentView(view);
            Log.e(TAG, e.getMessage());
        });


        imSettingsEditProfile.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsEditProfileFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });
        imSettingsChangePass.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsChangePasswordFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        imSettingsDesign.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsDesignFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });
        return view;
    }

    private void hideContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imSettingsEditProfile, R.id.imSettingsChangePass, R.id.imSettingsDesign, R.id.imageView109};
            int[] textViewIds = {R.id.tvSettingsUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView162, R.id.textView185};

            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.GONE);
            }

            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.GONE);
            }
            ProgressBar pbSettingFragment = view.findViewById(R.id.pbSettingFragment);
            pbSettingFragment.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imSettingsEditProfile, R.id.imSettingsChangePass, R.id.imSettingsDesign, R.id.imageView109};
            int[] textViewIds = {R.id.tvSettingsUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView162, R.id.textView185};

            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.VISIBLE);
            }

            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.VISIBLE);
            }
            ProgressBar pbSettingFragment = view.findViewById(R.id.pbSettingFragment);
            pbSettingFragment.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}