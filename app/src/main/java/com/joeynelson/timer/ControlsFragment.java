package com.joeynelson.timer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.graphics.Color;
import android.widget.Switch;

public class ControlsFragment extends Fragment {

    private boolean logging = true;
    private boolean twoPaneLayout;

    private Switch mSwitch;
    private Spinner mTimeSigNum;
    private Spinner mTimeSigDen;
    private EditText mMinutes;
    private EditText mSeconds;
    private EditText mBPM;
    private LinearLayout mLayout;


    public boolean getSwitchState() {   return mSwitch.isChecked();}
    public int getMinutes() {   return Integer.parseInt(mMinutes.getText().toString());    }
    public int getSeconds() {   return Integer.parseInt(mSeconds.getText().toString());    }
    public int getBpm() {   return Integer.parseInt(mBPM.getText().toString());    }
    public int getTimeSignatureNumerator() {    return Integer.parseInt(mTimeSigNum.getSelectedItem().toString());    }
    public int getTimeSignatureDenominator() {    return Integer.parseInt(mTimeSigDen.getSelectedItem().toString());    }

    public void setSpinner1(int x) { mTimeSigNum.setSelection(x); }
    public void setSpinner2(int x) { mTimeSigDen.setSelection(x); }
    public void setLayoutColor(int c) { mLayout.setBackgroundColor(c);}


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (logging) Log.d("ControlsFragment", "Start: onCreateView()");
        View view = inflater.inflate(R.layout.fragment_controls, container, false);

        mSwitch = (Switch) view.findViewById(R.id.queueSwitch);


        mTimeSigNum = (Spinner) view.findViewById(R.id.rythmspinner1); //Log.d("ControlsFragment", "sp =" + mTimeSigNum);
        mTimeSigDen = (Spinner) view.findViewById(R.id.rythmspinner2);
        this.setSpinner1(2); // Set Spinner Default Values
        this.setSpinner2(0); // Set Spinner Default Values

        mMinutes = (EditText) view.findViewById(R.id.editTextMinutes);
        mSeconds = (EditText) view.findViewById(R.id.editTextSeconds);
        mBPM = (EditText) view.findViewById(R.id.paceText);
        //mBPM.setFilters(new InputFilter[]{ new InputFilter_MinMax(60, 240)}); /// broken

        mLayout = (LinearLayout) view.findViewById(R.id.controlLayout);
        mLayout.setBackgroundColor(Color.LTGRAY);

        mLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1.0f ));



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