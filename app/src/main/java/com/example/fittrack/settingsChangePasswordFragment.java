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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            Log.d(TAG, "Successfully fetched username");

            String username = documentSnapshot.getString("name");
            tvChangePassUsername.setText(username);
        }).addOnFailureListener(e -> {
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
}