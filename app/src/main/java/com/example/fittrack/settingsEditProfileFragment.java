package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.CollationElementIterator;
import java.util.HashMap;
import java.util.Map;

public class settingsEditProfileFragment extends Fragment {
    FirebaseAuth mAuth;
    TextView tvEditProfileUsername;
    ImageView imEditProfile;
    EditText etEditProfileUsername, etEditProfileEmail, etEditProfilePassword;
    Button btnEditProfileUsername, btnEditProfileEmail;
    ProgressBar pbEditProfileEmail, pbEditProfileUsername;

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    public settingsEditProfileFragment() {
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
            if (user == null && isAdded()) { // Check if the fragment is added
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
        View view = inflater.inflate(R.layout.fragment_settings_edit_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        if(view != null && isAdded()) {

            tvEditProfileUsername = view.findViewById(R.id.tvEditProfileUsername);
            etEditProfileUsername = view.findViewById(R.id.etEditProfileUsername);
            etEditProfileEmail = view.findViewById(R.id.etEditProfileEmail);
            imEditProfile = view.findViewById(R.id.imEditProfile);
            etEditProfilePassword = view.findViewById(R.id.etEditProfilePassword);
            btnEditProfileUsername = view.findViewById(R.id.btnEditProfileUsername);
            btnEditProfileEmail = view.findViewById(R.id.btnEditProfileEmail);
            pbEditProfileEmail = view.findViewById(R.id.pbEditProfileEmail);
            pbEditProfileUsername = view.findViewById(R.id.pbEditProfileUsername);

            pbEditProfileUsername.setVisibility(View.GONE);
            pbEditProfileEmail.setVisibility(View.GONE);
            btnEditProfileUsername.setVisibility(View.VISIBLE);
            btnEditProfileEmail.setVisibility(View.VISIBLE);

            CollectionReference collRef = db.collection("users");
            DocumentReference userRef = db.collection("users").document(userId);
            DocumentReference docRef = db.collection("users").document(userId);

            hideContentView(view);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                showContentView(view);
                String username = documentSnapshot.getString("name");
                tvEditProfileUsername.setText(username);
            }).addOnFailureListener(e -> {
                showContentView(view);
                Log.e(TAG, e.getMessage());
            });

            imEditProfile.setOnClickListener(v -> {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment newFragment = new settingsFragment();
                fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
                fragmentTransaction.commit();
            });

            btnEditProfileUsername.setOnClickListener(v -> {
                pbEditProfileUsername.setVisibility(View.VISIBLE);
                btnEditProfileUsername.setVisibility(View.GONE);
                etEditProfileUsername.setBackgroundResource(R.drawable.text_field_bg_grey);
                etEditProfileUsername.setError(null);

                String username = etEditProfileUsername.getText().toString().trim();

                if(username.isEmpty()){
                    pbEditProfileUsername.setVisibility(View.GONE);
                    btnEditProfileUsername.setVisibility(View.VISIBLE);
                    etEditProfileUsername.setBackgroundResource(R.drawable.text_field_red);
                    etEditProfileUsername.setError("Required");
                    etEditProfileUsername.requestFocus();
                    return;
                }

                if(username.length() < 5){
                    pbEditProfileUsername.setVisibility(View.GONE);
                    btnEditProfileUsername.setVisibility(View.VISIBLE);
                    etEditProfileUsername.setBackgroundResource(R.drawable.text_field_red);
                    etEditProfileUsername.setError("Minimum of 5 Characters");
                    etEditProfileUsername.requestFocus();
                    return;
                }

                try{
                    collRef.whereEqualTo("name", username).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                Log.d(TAG, "Successfully colleciton query");
                                if(!queryDocumentSnapshots.isEmpty()){
                                    Log.d(TAG, "Username Already Taken");

                                    pbEditProfileUsername.setVisibility(View.GONE);
                                    btnEditProfileUsername.setVisibility(View.VISIBLE);

                                    etEditProfileUsername.setBackgroundResource(R.drawable.text_field_red);
                                    etEditProfileUsername.setError("Username Already Exist");
                                    etEditProfileUsername.requestFocus();
                                }else{
                                    Log.d(TAG, "There's no account with the username: " + username);

                                    userRef.update("name", username)
                                            .addOnSuccessListener(unused -> {
                                                pbEditProfileUsername.setVisibility(View.GONE);
                                                btnEditProfileUsername.setVisibility(View.VISIBLE);

                                                Log.d(TAG, "Successfully updated username to " + username);
                                                tvEditProfileUsername.setText(username);
                                                etEditProfileUsername.setText("");
                                            }).addOnFailureListener(e -> {
                                                pbEditProfileUsername.setVisibility(View.GONE);
                                                btnEditProfileUsername.setVisibility(View.VISIBLE);

                                                Log.e(TAG, "Failed to update username to " + username);
                                            });
                                }
                            }).addOnFailureListener(e -> {
                                pbEditProfileUsername.setVisibility(View.GONE);
                                btnEditProfileUsername.setVisibility(View.VISIBLE);

                                Log.e(TAG,"Failed to find username: " +  e.getMessage());
                            });
                }catch(Exception e){
                    pbEditProfileUsername.setVisibility(View.GONE);
                    btnEditProfileUsername.setVisibility(View.VISIBLE);

                    Log.e(TAG, e.getMessage());
                }
            });

            btnEditProfileEmail.setOnClickListener(v -> {
                pbEditProfileEmail.setVisibility(View.VISIBLE);
                btnEditProfileEmail.setVisibility(View.GONE);

                etEditProfilePassword.setBackgroundResource(R.drawable.text_field_bg_grey);
                etEditProfileEmail.setBackgroundResource(R.drawable.text_field_bg_grey);
                etEditProfileEmail.setError(null);
                etEditProfilePassword.setError(null);

                String newEmail, password;
                newEmail = etEditProfileEmail.getText().toString().trim();
                password = etEditProfilePassword.getText().toString().trim();

                if(newEmail.isEmpty()){
                    pbEditProfileEmail.setVisibility(View.GONE);
                    btnEditProfileEmail.setVisibility(View.VISIBLE);

                    etEditProfileEmail.setBackgroundResource(R.drawable.text_field_red);
                    etEditProfileEmail.setError("Required");
                    etEditProfileEmail.requestFocus();
                    return;
                }

                if(password.isEmpty()){
                    pbEditProfileEmail.setVisibility(View.GONE);
                    btnEditProfileEmail.setVisibility(View.VISIBLE);

                    etEditProfilePassword.setBackgroundResource(R.drawable.text_field_red);
                    etEditProfilePassword.setError("Required");
                    etEditProfilePassword.requestFocus();
                    return;
                }

                if(!newEmail.endsWith("@gmail.com")){
                    pbEditProfileEmail.setVisibility(View.GONE);
                    btnEditProfileEmail.setVisibility(View.VISIBLE);

                    etEditProfileEmail.setBackgroundResource(R.drawable.text_field_red);
                    etEditProfileEmail.setError("Invalid Email Format");
                    etEditProfileEmail.requestFocus();
                    return;
                }

                try{
                    AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
                    collRef.whereEqualTo("email", newEmail).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if(!queryDocumentSnapshots.isEmpty()){
                                    Log.d(TAG, "Email Already In Use");

                                    pbEditProfileEmail.setVisibility(View.GONE);
                                    btnEditProfileEmail.setVisibility(View.VISIBLE);

                                    etEditProfileEmail.setBackgroundResource(R.drawable.text_field_red);
                                    etEditProfileEmail.setError("Email Already in Use");
                                    etEditProfileEmail.requestFocus();
                                }else{
                                    Log.d(TAG, "Email is not taken");

                                    currentUser.reauthenticate(credential)
                                            .addOnCompleteListener(task -> {
                                                if(task.isSuccessful()){
                                                    currentUser.verifyBeforeUpdateEmail(newEmail)
                                                            .addOnCompleteListener(verifyTask -> {
                                                                if(verifyTask.isSuccessful()){
                                                                    authStateListener = firebaseAuth -> {
                                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                        if(user != null && user.isEmailVerified()){
                                                                            Log.d(TAG, "Email has been successfully verified.");

                                                                            docRef.update("email", newEmail).addOnSuccessListener(unused -> {
                                                                                Log.d(TAG, "Email has been successfully updated: " + newEmail);
                                                                            }).addOnFailureListener(e -> {
                                                                                Log.e(TAG, "Failed to update email: " + e.getMessage());
                                                                            });

                                                                            pbEditProfileEmail.setVisibility(View.GONE);
                                                                            btnEditProfileEmail.setVisibility(View.VISIBLE);

                                                                            etEditProfileEmail.setText("");
                                                                            etEditProfilePassword.setText("");

                                                                            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
                                                                        }
                                                                    };
                                                                    FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
                                                                }else{
                                                                    pbEditProfileEmail.setVisibility(View.GONE);
                                                                    btnEditProfileEmail.setVisibility(View.VISIBLE);

                                                                    Log.e(TAG, "Error Updating Email: " + verifyTask.getException());
                                                                    Toast.makeText(requireContext(), "Failed to update email. Please try again later.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }else{
                                                    Log.e(TAG, "Error ReAuthentication: " + task.getException());

                                                    pbEditProfileEmail.setVisibility(View.GONE);
                                                    btnEditProfileEmail.setVisibility(View.VISIBLE);

                                                    etEditProfilePassword.setBackgroundResource(R.drawable.text_field_red);
                                                    etEditProfilePassword.setError("Incorrect Password");
                                                    etEditProfilePassword.requestFocus();
                                                }
                                            });
                                }
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, e.getMessage());
                            });
                }catch(Exception e){
                    pbEditProfileEmail.setVisibility(View.GONE);
                    btnEditProfileEmail.setVisibility(View.VISIBLE);

                    Log.e(TAG, "Unknown Error Occurred: " + e.getMessage());
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

            etEditProfilePassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    etEditProfilePassword.setBackgroundResource(R.drawable.text_field_bg_grey);
                    etEditProfilePassword.setError(null);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }else{
            Log.e(TAG, "View is null or Fragment is not attached");
        }
        return view;
    }
    private void hideContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imEditProfile};
            int[] textViewIds = {R.id.tvEditProfileUsername, R.id.textView158, R.id.textView159, R.id.textView10, R.id.textView14, R.id.textView39};
            int[] editTextIds = {R.id.etEditProfileUsername, R.id.etEditProfileEmail, R.id.etEditProfilePassword};
            int[] buttonIds = {R.id.btnEditProfileUsername, R.id.btnEditProfileEmail};
            int[] progressBarIds = {R.id.pbEditProfileUsername, R.id.pbEditProfileEmail};

            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.GONE);
            }

            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.GONE);
            }

            for (int id : editTextIds) {
                EditText editText = view.findViewById(id);
                editText.setVisibility(View.GONE);
            }

            for (int id : buttonIds) {
                Button button = view.findViewById(id);
                button.setVisibility(View.GONE);
            }

            for (int id : progressBarIds) {
                ProgressBar progressBar = view.findViewById(id);
                progressBar.setVisibility(View.GONE);
            }
            ProgressBar pbSettingEditProfile = view.findViewById(R.id.pbSettingEditProfile);
            pbSettingEditProfile.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imEditProfile};
            int[] textViewIds = {R.id.tvEditProfileUsername, R.id.textView158, R.id.textView159, R.id.textView10, R.id.textView14, R.id.textView39};
            int[] editTextIds = {R.id.etEditProfileUsername, R.id.etEditProfileEmail, R.id.etEditProfilePassword};
            int[] buttonIds = {R.id.btnEditProfileUsername, R.id.btnEditProfileEmail};
            int[] progressBarIds = {R.id.pbEditProfileUsername, R.id.pbEditProfileEmail};

            for (int id : imageViewIds) {
                ImageView imageView = view.findViewById(id);
                imageView.setVisibility(View.VISIBLE);
            }

            for (int id : textViewIds) {
                TextView textView = view.findViewById(id);
                textView.setVisibility(View.VISIBLE);
            }

            for (int id : editTextIds) {
                EditText editText = view.findViewById(id);
                editText.setVisibility(View.VISIBLE);
            }

            for (int id : buttonIds) {
                Button button = view.findViewById(id);
                button.setVisibility(View.VISIBLE);
            }

            for (int id : progressBarIds) {
                ProgressBar progressBar = view.findViewById(id);
                progressBar.setVisibility(View.VISIBLE);
            }

            ProgressBar pbSettingEditProfile = view.findViewById(R.id.pbSettingEditProfile);
            pbSettingEditProfile.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}