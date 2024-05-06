package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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