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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class startActivityFragment extends Fragment {
    ImageView imStartActIcon, imStartActIncTime, imStartActDecTime;
    TextView tvStartActName, tvStartActCurrentTime, tvStartActTimeGoal;
    Button btnStartActStart, btnStartActCancel;

    public startActivityFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_activity, container, false);
        imStartActIcon = view.findViewById(R.id.imStartActIcon);
        imStartActIncTime = view.findViewById(R.id.imStartActIncTime);
        imStartActDecTime = view.findViewById(R.id.imStartActDecTime);
        tvStartActName = view.findViewById(R.id.tvStartActName);
        tvStartActCurrentTime = view.findViewById(R.id.tvStartActCurrentTime);
        tvStartActTimeGoal = view.findViewById(R.id.tvStartActTimeGoal);
        btnStartActStart = view.findViewById(R.id.btnStartActStart);
        btnStartActCancel = view.findViewById(R.id.btnStartActCancel);


        Bundle bundle = getArguments();
        final String[] actName = {""};
        final int[] timeGoal = {1};
        final String[] currentTime = {getCurrentTime()};

        if(bundle != null){

            actName[0] = bundle.getString("actName");
            if(actName != null){
                switch (actName[0]){
                    case "Running":
                        imStartActIcon.setImageResource(R.drawable.running_icon);
                        tvStartActName.setText(actName[0]);
                        break;
                    case "Cycle":
                        imStartActIcon.setImageResource(R.drawable.cycling_icon);
                        tvStartActName.setText(actName[0]);
                        break;
                    case "Swim":
                        imStartActIcon.setImageResource(R.drawable.swimming_icon);
                        tvStartActName.setText(actName[0]);
                        break;
                    case "Yoga":
                        imStartActIcon.setImageResource(R.drawable.yoga_icon);
                        tvStartActName.setText(actName[0]);
                        break;
                    case "Gym":
                        imStartActIcon.setImageResource(R.drawable.weights_icon);
                        tvStartActName.setText(actName[0]);
                        break;
                    default:
                        Log.w(TAG, actName[0] + " is not available!");
                }
            }else{
                Log.e(TAG,"Act Name is " + actName[0]);
            }

            tvStartActTimeGoal.setText(String.valueOf(timeGoal[0]));
            tvStartActCurrentTime.setText(currentTime[0]);
        }

        imStartActIncTime.setOnClickListener(v -> {
            if(timeGoal[0] < 5){
                timeGoal[0] += 1;
                tvStartActTimeGoal.setText(String.valueOf(timeGoal[0]));
            }
        });

        imStartActDecTime.setOnClickListener(v -> {
            if(timeGoal[0] > 1){
                timeGoal[0] -= 1;
                tvStartActTimeGoal.setText(String.valueOf(timeGoal[0]));
            }
        });

        btnStartActStart.setOnClickListener(v -> {
            bundle.putInt("timeGoal", timeGoal[0]);
            bundle.putString("actName", actName[0]);
            bundle.putString("currentTime", currentTime[0]);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new inProgressActivityFragment();
            newFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        btnStartActCancel.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new viewActivityFragment();
            newFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        return view;
    }

    private String getCurrentTime(){
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date currentTime = new Date();
        return timeFormat.format(currentTime);
    }
}