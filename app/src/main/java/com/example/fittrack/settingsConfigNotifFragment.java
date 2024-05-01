package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class settingsConfigNotifFragment extends Fragment {
    TextView tvConfiNotifUsername;
    ImageView imConfigNotifEditProfile, imConfigNotifChangePass, imConfigNotif, imConfigNotifDesign;
    Switch sConfigNotif;

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public settingsConfigNotifFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_config_notif, container, false);
        tvConfiNotifUsername = view.findViewById(R.id.tvConfigNotifUsername);
        imConfigNotifEditProfile = view.findViewById(R.id.imConfigNotifEditProfile);
        imConfigNotifChangePass = view.findViewById(R.id.imConfigNotifChangePass);
        imConfigNotif = view.findViewById(R.id.imConfigNotif);
        imConfigNotifDesign = view.findViewById(R.id.imConfigNotifDesign);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            String username = documentSnapshot.getString("name");
            tvConfiNotifUsername.setText(username);

        }).addOnFailureListener(e -> {
            Log.e(TAG, e.getMessage());
        });

        imConfigNotifEditProfile.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsEditProfileFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imConfigNotifChangePass.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsChangePasswordFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imConfigNotif.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imConfigNotifDesign.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsDesignFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }
}