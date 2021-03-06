package com.jamespfluger.devicefinder.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jamespfluger.devicefinder.R;

public class AboutFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container.clearDisappearingChildren();
        getActivity().getFragmentManager().popBackStack();
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        return root;
    }
}
