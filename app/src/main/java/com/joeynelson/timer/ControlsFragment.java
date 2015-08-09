package com.joeynelson.timer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.graphics.Color;
public class ControlsFragment extends Fragment {

    private boolean logging = true;
    private boolean twoPaneLayout;

    private Spinner mTimeSigNum;
    private Spinner mTimeSigDen;
    private EditText mMinutes;
    private EditText mSeconds;
    private EditText mBPM;
    private LinearLayout mLayout;

    public Spinner getSpinner1() { return mTimeSigNum; }
    public Spinner getSpinner2() { return mTimeSigDen; }
    public void setSpinner1(int x) { mTimeSigNum.setSelection(x); }
    public void setSpinner2(int x) { mTimeSigDen.setSelection(x); }
    public void setLayoutColor(int c) { mLayout.setBackgroundColor(c);}


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (logging) Log.d("ControlsFragment", "Start: onCreateView()");
        View view = inflater.inflate(R.layout.fragment_controls, container, false);

        // Set Spinner Default Values
        mTimeSigNum = (Spinner) view.findViewById(R.id.rythmspinner1);
    //Log.d("ControlsFragment", "sp =" + mTimeSigNum);
        mTimeSigDen = (Spinner) view.findViewById(R.id.rythmspinner2);
        this.setSpinner1(3);
        this.setSpinner2(3);

        mMinutes = (EditText) view.findViewById(R.id.editTextMinutes);
        mSeconds = (EditText) view.findViewById(R.id.editTextSeconds);
        mBPM = (EditText) view.findViewById(R.id.paceText);

        mLayout = (LinearLayout) view.findViewById(R.id.controlLayout);
        mLayout.setBackgroundColor(Color.LTGRAY);



        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (logging) Log.d("ControlsFragment", "Start: onActivityCreated()");
        super.onActivityCreated(savedInstanceState);


        /* ***************************************************************
        *                     Load Saved State Settings                  *
        * ****************************************************************/
        if (savedInstanceState != null) {// Load Player and Temporary round scores
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {  if (logging) Log.d("ControlsFragment", "Start: onSaveInstanceState()");
        /*****************************************************************
        *                     Save State Settings                        *
        *****************************************************************/
        super.onSaveInstanceState(outState);
    }



}