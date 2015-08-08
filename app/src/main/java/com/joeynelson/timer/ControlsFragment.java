package com.joeynelson.timer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

public class ControlsFragment extends Fragment {

    private boolean logging = true;
    private boolean twoPaneLayout;

    private Spinner sp1;
    private Spinner sp2;
    public View view;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (logging) Log.d("FirstFragment", "Start: onCreateView()");
        View view = inflater.inflate(R.layout.fragment_controls, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (logging) Log.d("ClockFragment", "Start: onActivityCreated()");
        super.onActivityCreated(savedInstanceState);

    // Set Spinner Default Values
        sp1 = (Spinner)getActivity().findViewById(R.id.rythmspinner1);
        sp1.setSelection(3);
        sp2 = (Spinner)getActivity().findViewById(R.id.rythmspinner2);
        sp2.setSelection(3);

        /* ************************************************************** *
        *                     Load Saved Player Names                     *
        * *************************************************************** */
        if (savedInstanceState != null) {// Load Player and Temporary round scores
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {  if (logging) Log.d("FirstFragment", "Start: onSaveInstanceState()");
        /* ************************************************************** *
        *                     Save Player Names                           *
        * *************************************************************** */
        super.onSaveInstanceState(outState);
    }



}