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
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class settingsChangePasswordFragment extends Fragment {
    ImageView imChangePassBack, imChangePassEditProfile;
    ProgressBar pbChangePassSave;
    TextView tvChangePassUsername;
    EditText etChangePassOldPass, etChangePassNewPass, etChangePassConfirmPass;
    Button btnChangePassSave;

    FirebaseAuth mAuth;
    FirebaseFirestore db= FirebaseFirestore.getInstance();

    public settingsChangePasswordFragment() {
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
                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
        View view = inflater.inflate(R.layout.fragment_settings_change_password, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        imChangePassBack = view.findViewById(R.id.imChangePassBack);
        imChangePassEditProfile = view.findViewById(R.id.imChangePassEditProfile);
        etChangePassOldPass = view.findViewById(R.id.etChangePassOldPass);
        etChangePassNewPass = view.findViewById(R.id.etChangePassNewPass);
        etChangePassConfirmPass = view.findViewById(R.id.etChangePassConfirmPass);
        btnChangePassSave = view.findViewById(R.id.btnChangePassSave);
        tvChangePassUsername = view.findViewById(R.id.tvChangePassUsername);
        pbChangePassSave = view.findViewById(R.id.pbChangePassSave);

        pbChangePassSave.setVisibility(View.GONE);
        btnChangePassSave.setVisibility(View.VISIBLE);

        hideContentView(view);
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            showContentView(view);
            Log.d(TAG, "Successfully fetched username");

            String username = documentSnapshot.getString("name");
            tvChangePassUsername.setText(username);
        }).addOnFailureListener(e -> {
            showContentView(view);

            Log.e(TAG, "Failed to get username: " + e.getMessage());
        });

        imChangePassBack.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        imChangePassEditProfile.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new settingsEditProfileFragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, newFragment);
            fragmentTransaction.commit();
        });

        btnChangePassSave.setOnClickListener(v -> {
            pbChangePassSave.setVisibility(View.VISIBLE);
            btnChangePassSave.setVisibility(View.GONE);

            etChangePassOldPass.setBackgroundResource(R.drawable.text_field_bg_grey);
            etChangePassNewPass.setBackgroundResource(R.drawable.text_field_bg_grey);
            etChangePassConfirmPass.setBackgroundResource(R.drawable.text_field_bg_grey);
            etChangePassOldPass.setError(null);
            etChangePassNewPass.setError(null);
            etChangePassConfirmPass.setError(null);

            String oldPass, newPass, confirmPass;
            oldPass = etChangePassOldPass.getText().toString().trim();
            newPass = etChangePassNewPass.getText().toString().trim();
            confirmPass = etChangePassConfirmPass.getText().toString().trim();

            if(oldPass.isEmpty()){
                pbChangePassSave.setVisibility(View.GONE);
                btnChangePassSave.setVisibility(View.VISIBLE);
                etChangePassOldPass.setBackgroundResource(R.drawable.text_field_red);
                etChangePassOldPass.setError("Required");
                etChangePassOldPass.requestFocus();

                return;
            }

            if(newPass.isEmpty()){
                pbChangePassSave.setVisibility(View.GONE);
                btnChangePassSave.setVisibility(View.VISIBLE);
                etChangePassNewPass.setBackgroundResource(R.drawable.text_field_red);
                etChangePassNewPass.setError("Required");
                etChangePassNewPass.requestFocus();

                return;
            }

            if(confirmPass.isEmpty()){
                pbChangePassSave.setVisibility(View.GONE);
                btnChangePassSave.setVisibility(View.VISIBLE);
                etChangePassConfirmPass.setBackgroundResource(R.drawable.text_field_red);
                etChangePassConfirmPass.setError("Required");
                etChangePassConfirmPass.requestFocus();

                return;
            }

            if(newPass.length() < 8){
                pbChangePassSave.setVisibility(View.GONE);
                btnChangePassSave.setVisibility(View.VISIBLE);
                etChangePassNewPass.setBackgroundResource(R.drawable.text_field_red);
                etChangePassNewPass.setError("Atleast 8 Characters");
                etChangePassNewPass.requestFocus();

                return;
            }

            if(!newPass.equals(confirmPass)){
                pbChangePassSave.setVisibility(View.GONE);
                btnChangePassSave.setVisibility(View.VISIBLE);
                etChangePassConfirmPass.setBackgroundResource(R.drawable.text_field_red);
                etChangePassConfirmPass.setError("Password Do Not Match");
                etChangePassConfirmPass.requestFocus();

                return;
            }

            try{
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPass);
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                currentUser.updatePassword(newPass)
                                        .addOnCompleteListener(updateTask -> {
                                            if(updateTask.isSuccessful()){
                                                Log.d(TAG, "Successfully Updated Password");

                                                etChangePassOldPass.setText("");
                                                etChangePassNewPass.setText("");
                                                etChangePassConfirmPass.setText("");

                                                Toast.makeText(requireContext(), "Successfully Updated Password.", Toast.LENGTH_SHORT).show();

                                                pbChangePassSave.setVisibility(View.GONE);
                                                btnChangePassSave.setVisibility(View.VISIBLE);
                                            }else{
                                                Log.e(TAG, "Failed to update password: " + updateTask.getException());
                                                Toast.makeText(requireContext(), "Failed to Update Password. Please Try Again.", Toast.LENGTH_SHORT).show();

                                                pbChangePassSave.setVisibility(View.GONE);
                                                btnChangePassSave.setVisibility(View.VISIBLE);
                                            }
                                        });
                            }else{
                                Log.e(TAG, "Failed to Reauthenticate: " + task.getException());

                                etChangePassOldPass.setBackgroundResource(R.drawable.text_field_red);
                                etChangePassOldPass.setError("Incorrect Password");
                                etChangePassOldPass.requestFocus();

                                pbChangePassSave.setVisibility(View.GONE);
                                btnChangePassSave.setVisibility(View.VISIBLE);
                            }
                        });
            }catch(Exception e){
                Log.e(TAG, "Unkown Error Occurred: " + e.getMessage());

                pbChangePassSave.setVisibility(View.GONE);
                btnChangePassSave.setVisibility(View.VISIBLE);
            }
        });

        etChangePassOldPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etChangePassOldPass.setBackgroundResource(R.drawable.text_field_bg_grey);
                etChangePassOldPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etChangePassNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etChangePassNewPass.setBackgroundResource(R.drawable.text_field_bg_grey);
                etChangePassNewPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etChangePassConfirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etChangePassConfirmPass.setBackgroundResource(R.drawable.text_field_bg_grey);
                etChangePassConfirmPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }
    private void hideContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imChangePassEditProfile, R.id.imChangePassBack};
            int[] textViewIds = {R.id.tvChangePassUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView26, R.id.textView30, R.id.textView34};
            int[] editTextIds = {R.id.etChangePassOldPass, R.id.etChangePassNewPass, R.id.etChangePassConfirmPass};
            int[] buttonIds = {R.id.btnChangePassSave};
            int[] progressBarIds = {R.id.pbChangePassSave};

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

            ProgressBar pbSettingChangePass = view.findViewById(R.id.pbSettingChangePass);
            pbSettingChangePass.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContentView(View view) {
        try {
            int[] imageViewIds = {R.id.imageView85, R.id.imageView86, R.id.imChangePassEditProfile, R.id.imChangePassBack};
            int[] textViewIds = {R.id.tvChangePassUsername, R.id.textView158, R.id.textView159, R.id.textView160, R.id.textView26, R.id.textView30, R.id.textView34};
            int[] editTextIds = {R.id.etChangePassOldPass, R.id.etChangePassNewPass, R.id.etChangePassConfirmPass};
            int[] buttonIds = {R.id.btnChangePassSave};
            int[] progressBarIds = {R.id.pbChangePassSave};

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

            ProgressBar pbSettingChangePass = view.findViewById(R.id.pbSettingChangePass);
            pbSettingChangePass.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}