package com.example.fittrack;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class viewActivityFragment extends Fragment {
    ImageView imViewActRun, imViewActSwim, imViewActCycle, imViewActYoga, imViewActGym;

    public viewActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_activity, container, false);
        imViewActRun = view.findViewById(R.id.imViewActRun);
        imViewActSwim = view.findViewById(R.id.imViewActSwim);
        imViewActCycle = view.findViewById(R.id.imViewActCycle);
        imViewActYoga = view.findViewById(R.id.imViewActYoga);
        imViewActGym = view.findViewById(R.id.imViewActGym);

        imViewActRun.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("actName", "Running");
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new startActivityFragment();
            newFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imViewActCycle.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("actName", "Cycle");
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new startActivityFragment();
            newFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imViewActSwim.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("actName", "Swim");
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new startActivityFragment();
            newFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imViewActYoga.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("actName", "Yoga");
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new startActivityFragment();
            newFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        imViewActGym.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("actName", "Gym");
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment newFragment = new startActivityFragment();
            newFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragmentContainerView2, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }
}