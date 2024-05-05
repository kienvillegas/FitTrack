package com.example.fittrack;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class inProgressActivityFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;

    ImageView imInProgressIcon;
    TextView tvInProgressActName, tvInProgressCurrentTime, tvInProgressTimeGoal;
    Button btnInProgressDone, btnInProgressCancel;
    CountDownTimer countDownTimer;

    private long startTimeInMillis;
    private long totalTimeInMillis;

    public inProgressActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_in_progress_activity, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        imInProgressIcon = view.findViewById(R.id.imInProgressIcon);
        tvInProgressActName = view.findViewById(R.id.tvInProgressActName);
        tvInProgressCurrentTime = view.findViewById(R.id.tvInProgressCurrentTime);
        tvInProgressTimeGoal = view.findViewById(R.id.tvInProgressTimeGoal);
        btnInProgressDone = view.findViewById(R.id.btnInProgressDone);
        btnInProgressCancel = view.findViewById(R.id.btnInProgressCancel);

        Bundle bundle = getArguments();
        final String[] actName = {""};
        String currentTime;
        int timeGoal;

        actName[0] = bundle.getString("actName");
        currentTime = bundle.getString("currentTime");
        timeGoal = bundle.getInt("timeGoal");

        if (bundle != null) {
            actName[0] = bundle.getString("actName");
            if (actName[0] != null) {
                switch (actName[0]) {
                    case "running":
                        imInProgressIcon.setImageResource(R.drawable.running_icon);
                        break;
                    case "cycle":

                        imInProgressIcon.setImageResource(R.drawable.cycling_icon);
                        break;
                    case "swim":

                        imInProgressIcon.setImageResource(R.drawable.swimming_icon);
                        break;
                    case "yoga":

                        imInProgressIcon.setImageResource(R.drawable.yoga_icon);
                        break;
                    case "gym":

                        imInProgressIcon.setImageResource(R.drawable.weights_icon);
                        break;
                    default:
                        Log.w(TAG, actName + " is not available!");
                }
                tvInProgressActName.setText(actName[0]);
            } else {
                Log.e(TAG, "Act Name is " + actName[0]);
            }

            tvInProgressCurrentTime.setText(currentTime);
            Log.d(TAG, "Current Time: " + currentTime);
            tvInProgressTimeGoal.setText(String.valueOf(timeGoal));
            startCountdown(timeGoal);
        }

        btnInProgressDone.setOnClickListener(v -> {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            String currentDate = dateFormat.format(date);
            try{
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    long timeSpentInMillis = System.currentTimeMillis() - startTimeInMillis;
                    Log.d(TAG, "On Finish: " + timeSpentInMillis);

                    Map<String, Object> recentAct = new HashMap<>();
                    recentAct.put("actName", String.valueOf(actName[0]));
                    recentAct.put("dateOfAct", currentDate);
                    recentAct.put("timeStarted", currentTime);
                    recentAct.put("timeSpent", timeSpentInMillis);

                    DocumentReference docRef = db.collection("recent_activities").document(userId);
                    DocumentReference activityRef = db.collection("activity_time_spent").document(userId);

                    docRef.set(recentAct).addOnSuccessListener(unused -> {
                        Log.i(TAG, "Successfully added recent activity to Firestore");
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to added recent activity to Firestore: " + e.getMessage());
                    });

                    activityRef.get().addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            long actTimeSpent = documentSnapshot.getLong(actName[0]);
                            long timeSpentInSeconds = timeSpentInMillis / 1000; // Convert milliseconds to seconds

                            actTimeSpent += timeSpentInSeconds;
                            Log.d(TAG, "Activity Time Spent: " + timeSpentInSeconds);

                            activityRef.update(actName[0], actTimeSpent).addOnSuccessListener(unused -> {
                                Log.d(TAG, "Successfully updated activity time sptent");
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to updated activity time spent: " + e.getMessage());
                            });
                        }else{
                            Log.e(TAG, "Document does not exist");
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch data from collection: " + e.getMessage());
                    });

                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment newFragment = new viewActivityFragment();
                    fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }catch(Exception e){
                Log.e(TAG, "An Error Occurred: " + e.getMessage());
            }
        });

        btnInProgressCancel.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View  cancelActConfirmation =  LayoutInflater.from(getActivity()).inflate(R.layout.cancel_activity_confirmation, null);
            cancelActConfirmation.setBackgroundResource(R.drawable.confirmation_bg);
            builder.setView(cancelActConfirmation);
            AlertDialog dialog = builder.create();

            Button btnCancel, btnConfirm;
            btnCancel = cancelActConfirmation.findViewById(R.id.btnCancelActNo);
            btnConfirm = cancelActConfirmation.findViewById(R.id.btnCancelActYes);

            btnCancel.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            btnConfirm.setOnClickListener(v1 -> {
                dialog.dismiss();
                countDownTimer.cancel();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment newFragment = new viewActivityFragment();
                fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            });
            dialog.show();
        });
        return view;
    }

    private void startCountdown(int timeGoal) {
        startTimeInMillis = System.currentTimeMillis(); // Store the start time
        long totalTimeInMillis = timeGoal * 60 * 60 * 1000; // Convert hours to milliseconds
        countDownTimer = new CountDownTimer(totalTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (1000 * 60 * 60);
                long minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60);
                long seconds = (millisUntilFinished % (1000 * 60)) / 1000;
                String timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                tvInProgressTimeGoal.setText(timeLeftFormatted);
            }

            @Override
            public void onFinish() {

                long timeSpentInMillis = totalTimeInMillis;
                Log.d(TAG, "On Finish: " + timeSpentInMillis);
                tvInProgressTimeGoal.setText("00:00:00");
            }
        }.start();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

