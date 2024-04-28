package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class settingsEditProfileFragment extends Fragment {
    FirebaseAuth mAuth;
    TextView tvSettingsUsername;
    ImageView imEditUsername, imEditEmail, imEditProfile;
    EditText etEditProfileUsername, etEditProfileEmail;
    Button btnEditProfileSave;

    private boolean isEditUsernameEnabled, isEditEmailEnabled = false;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public settingsEditProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_edit_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        tvSettingsUsername = view.findViewById(R.id.tvSettingsUsername);
        imEditUsername = view.findViewById(R.id.imEditUsername);
        imEditEmail = view.findViewById(R.id.imEditEmail);
        etEditProfileUsername = view.findViewById(R.id.etEditProfileUsername);
        etEditProfileEmail = view.findViewById(R.id.etEditProfileEmail);
        btnEditProfileSave = view.findViewById(R.id.btnEditProfileSave);


        if(isEditUsernameEnabled){
            etEditProfileUsername.setEnabled(true);
        }else{
            etEditProfileUsername.setEnabled(false);
        }

        if(isEditEmailEnabled){
            etEditProfileEmail.setEnabled(true);
        }else{
            etEditProfileEmail.setEnabled(false);
        }

        imEditProfile.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imEditUsername.setOnClickListener(v -> {
            isEditUsernameEnabled = !isEditUsernameEnabled;
            if (isEditUsernameEnabled) {
                imEditUsername.setImageResource(R.drawable.done_edit_icon);
            } else {
                imEditUsername.setImageResource(R.drawable.edit_icon);
            }
        });

        imEditEmail.setOnClickListener(v -> {
            isEditEmailEnabled = !isEditEmailEnabled;
            if (isEditEmailEnabled) {
                imEditEmail.setImageResource(R.drawable.done_edit_icon);
            } else {
                imEditEmail.setImageResource(R.drawable.edit_icon);
            }
        });

        btnEditProfileSave.setOnClickListener(v -> {
            String newUsername, newEmail;
            newUsername = etEditProfileUsername.getText().toString().trim();
            newEmail = etEditProfileEmail.getText().toString().trim();

            if(newUsername.isEmpty() && newEmail.isEmpty()){
                return;
            }

            if(newUsername.length() < 5){
                etEditProfileUsername.setError("Minimum of 5 Characters");
            }else{
                checkUsernameUniqueness(newUsername, userId);
            }

            if(Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()){
                checkForExistingEmail(newEmail, userId);
            }else{
                etEditProfileEmail.setError("Invalid Email Format");
            }
        });

        etEditProfileUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etEditProfileUsername.setBackgroundResource(R.drawable.text_field_bg_grey);
                etEditProfileUsername.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etEditProfileEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etEditProfileEmail.setBackgroundResource(R.drawable.text_field_bg_grey);
                etEditProfileEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void checkUsernameUniqueness(String newUsername, String userId) {
        CollectionReference usersRef = db.collection("users");
        DocumentReference docRef = db.collection("users").document(userId);

        usersRef.whereEqualTo("name", newUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Map<String, Object> nameData = new HashMap<>();
                            nameData.put("name", newUsername);

                            docRef.update(nameData)
                                    .addOnSuccessListener(unused -> {
                                        Log.e(TAG, "Successfully Changed Username");
                                    }).addOnFailureListener(e -> {
                                        Log.e(TAG, e.getMessage());

                                    });
                        } else {
                            etEditProfileUsername.setError("Username is already in use");
                        }
                    } else {
                        Log.e(TAG, "An error ocurred: " + task.getException());
                    }
                });
    }

    private void checkForExistingEmail(String newEmail, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        DocumentReference docRef = db.collection("users").document(userId);

        usersRef.whereEqualTo("email", newEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Map<String, Object> emailData = new HashMap<>();
                            emailData.put("email", newEmail);

                            docRef.update(emailData)
                                    .addOnSuccessListener(unused -> {
                                        Log.e(TAG, "Successfully Changed Email");
                                    }).addOnFailureListener(e -> {
                                        Log.e(TAG, e.getMessage());
                                    });
                        } else {
                            etEditProfileUsername.setError("email is already in use");
                        }
                    } else {
                        Log.e(TAG, "An error ocurred: " + task.getException());
                    }
                });
    }

}