package com.joeynelson.timer;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentManager;
import android.widget.LinearLayout;

public class ClockFragment extends Fragment
{
    private boolean logging = false;
    private boolean twoPaneLayout;




    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (logging) Log.d("ClockFragment", "Start: onCreateView()");
        View view = inflater.inflate(R.layout.fragment_clock, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (logging) Log.d("ClockFragment", "Start: onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
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