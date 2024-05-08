package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class settingsDeleteFragment extends Fragment {
    private static final String THEME_PREF_KEY = "themePref";
    private static final int THEME_DEFAULT = 0;
    private static final int THEME_ORANGE = 1;
    private static final int THEME_GREEN = 2;

    private int currentTheme;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView imDeleteEditProfile,imDeleteChangePass, imDeleteDesign, imDelete;
    TextView tvDeleteUsername;
    Button btnDeleteAccount;

    SharedPreferences prefs;

    public settingsDeleteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_delete, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        tvDeleteUsername = view.findViewById(R.id.tvDesignUsername);
        imDeleteEditProfile = view.findViewById(R.id.imDesignEditProfile);
        imDeleteChangePass = view.findViewById(R.id.imDesignChangePass);
        imDeleteDesign = view.findViewById(R.id.imDesign);
        imDelete = view.findViewById(R.id.imDeleteBack);
        btnDeleteAccount = view.findViewById(R.id.btnSettingsDelete);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        hideContentView(view);
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    showContentView(view);
                    String username = documentSnapshot.getString("name");
                    tvDeleteUsername.setText(username);
                })
                .addOnFailureListener(e -> {
                    showContentView(view);
                    Log.e(TAG, "Error fetching data from Firestore: " + e.getMessage());
                });

        btnDeleteAccount.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View  saveChangesConfirmation =  LayoutInflater.from(getActivity()).inflate(R.layout.save_changes_confirmation, null);
            saveChangesConfirmation.setBackgroundResource(R.drawable.confirmation_bg);
            builder.setView(saveChangesConfirmation);
            AlertDialog dialog = builder.create();

            Button btnCancel, btnConfirm;
            btnCancel = saveChangesConfirmation.findViewById(R.id.btnSaveChangesNo);
            btnConfirm = saveChangesConfirmation.findViewById(R.id.btnSaveChangesYes);

            btnConfirm.setOnClickListener(v1 -> {
                try {
                    if (currentUser != null) {

                        // Delete user's Firestore documents from multiple collections
                        deleteFirestoreDocuments(userId, "users");
                        deleteFirestoreDocuments(userId, "activity_time_spent");
                        deleteFirestoreDocuments(userId, "bmi");
                        deleteFirestoreDocuments(userId, "recent_activities");
                        deleteFirestoreDocuments(userId, "weekly_calorie");
                        deleteFirestoreDocuments(userId, "weekly_step");
                        deleteFirestoreDocuments(userId, "weekly_water");
                        deleteFirestoreDocuments(userId, "weekly_sleep");
                        // Delete the user account
                        currentUser.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Account deleted successfully
                                        // Redirect to login screen or perform any other action
                                        Intent intent = new Intent(getContext(), SplashScreen.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        // Failed to delete account
                                        Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "User is not signed in", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            btnCancel.setOnClickListener(v12 -> {
                dialog.dismiss();
            });
            dialog.show();
        });

        imDeleteEditProfile.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsEditProfileFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        imDeleteChangePass.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsChangePasswordFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        imDeleteDesign.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsDesignFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        imDelete.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });
        return view;
    }

    private void hideContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imDeleteEditProfile, R.id.imDeleteChangePass, R.id.imDeleteDesign, R.id.imageView109, R.id.imDeleteBack};
            int[] textViewIds = {R.id.tvDeleteUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView162, R.id.textView185, R.id.textView49};
            int[] buttonIds = {R.id.btnSettingsDelete};
            int[] progressBarIds = {R.id.pbDelete};

            // Hide ImageViews
            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.GONE);
            }

            // Hide TextViews
            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.GONE);
            }

            // Hide Buttons
            for (int id : buttonIds) {
                Button button = view.findViewById(id);
                button.setVisibility(View.GONE);
            }

            // Show ProgressBars
            for (int id : progressBarIds) {
                ProgressBar progressBar = view.findViewById(id);
                progressBar.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imDeleteEditProfile, R.id.imDeleteChangePass, R.id.imDeleteDesign, R.id.imageView109, R.id.imDeleteBack};
            int[] textViewIds = {R.id.tvDeleteUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView162, R.id.textView185, R.id.textView49};
            int[] buttonIds = {R.id.btnSettingsDelete};
            int[] progressBarIds = {R.id.pbDelete};

            // Show ImageViews
            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.VISIBLE);
            }

            // Show TextViews
            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.VISIBLE);
            }

            // Show Buttons
            for (int id : buttonIds) {
                Button button = view.findViewById(id);
                button.setVisibility(View.VISIBLE);
            }

            // Hide ProgressBars
            for (int id : progressBarIds) {
                ProgressBar progressBar = view.findViewById(id);
                progressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteFirestoreDocuments(String userId, String collectionName) {
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection(collectionName);
        collectionRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete each document
                            collectionRef.document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Document deleted successfully
                                        Toast.makeText(getContext(), "Documents are deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to delete document
                                        Toast.makeText(getContext(), "Failed to delete documents", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Failed to query documents
                        Toast.makeText(getContext(), "Failed to query documents", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}