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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class settingsDesignFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView imDesignEditProfile, imDesignChangePass, imDesignNotification, imDesign;

    TextView tvDesignUsername;
    public settingsDesignFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_design, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvDesignUsername = view.findViewById(R.id.tvDesignUsername);
        imDesignEditProfile = view.findViewById(R.id.imDesignEditProfile);
        imDesignChangePass = view.findViewById(R.id.imDesignChangePass);
        imDesignNotification = view.findViewById(R.id.imDesignNotification);
        imDesign = view.findViewById(R.id.imDesign);

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            String username = documentSnapshot.getString("name");
            tvDesignUsername.setText(username);

        }).addOnFailureListener(e -> {
            Log.e(TAG, e.getMessage());
        });

        imDesignEditProfile.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsEditProfileFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imDesignChangePass.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsChangePasswordFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imDesignNotification.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsConfigNotifFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imDesign.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }
}