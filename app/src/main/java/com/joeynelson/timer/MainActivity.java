package com.joeynelson.timer;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;


public class MainActivity extends FragmentActivity implements View.OnClickListener
{   private boolean logging = true;

    private static final String FORMAT = "%02d:%02d:%02d";

    private static int mNumberOfControls;

    //~// Widgets //~//
    private Button mStartButton;
    private String mTimerState = "STOPPED";
    private TextView mTime;


    // Timer Variables //
    private CountDownTimer mTimer;
    protected int mActiveTimerIndex=0;
    private ControlsFragment[] mControlsFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    if (logging) Log.d("MainActivity", "Start: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Widgets //
        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(this);

        mTime = (TextView) findViewById(R.id.clock_display);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            mNumberOfControls = 4;
            String tag = "";
            ControlsFragment ctrlfrag;

            FragmentManager fragMan = getSupportFragmentManager();
            FragmentTransaction fragTran = fragMan.beginTransaction();
            for (int i = 0; i < mNumberOfControls; i++) {
                tag = "cfrag"+i;
                ctrlfrag = new ControlsFragment();
                fragTran.add(R.id.fragment_container, ctrlfrag, tag);
            }
            fragTran.commit(); // commit additions
            fragMan.executePendingTransactions();

            // Populate Fragments Array
            mControlsFragments = new ControlsFragment[mNumberOfControls];
            for (int b = 0; b < mNumberOfControls; b++) {
                tag = "cfrag"+b;
                mControlsFragments[b] = (ControlsFragment) getSupportFragmentManager().findFragmentByTag(tag); //if (logging) Log.d("ClockFragment", "OnClick = " + cFrag);
            }

        }
    }
    @Override
    public void onClick(View view)
    { if (logging) Log.d("onClick", "Start");
        switch(view.getId())
        {
        case R.id.start_button: Log.d("onClick", "case R.id.start_button");
            if (mTimerState.equals("STOPPED") && mActiveTimerIndex < mNumberOfControls)   // If a timer hasn't already started then start one, If a timer has started, dont start another one!
            { Log.d("onClick", "if (mTimerAlreadyStarted = false)");
                mTimerState = "STARTED";
                //highlightTimer(mActiveTimerIndex);
                if (mControlsFragments[mActiveTimerIndex].getSwitchState()) {
                    int hrs = 0;
                    int mins = mControlsFragments[mActiveTimerIndex].getMinutes();
                    int secs = mControlsFragments[mActiveTimerIndex].getSeconds();
                    // Create CountDownTimer
                        CreateCountDownTimer( hrs * 3600000 + mins*60000 + secs * 1000 );

                }
            }
            else if (mTimerState.equals("STARTED"))
            {
                mTimerState = "PAUSED"; // If the timer had already started, it should now be paused
            }
            else if (mTimerState.equals("PAUSED"))
            {
                mTimerState = "STARTED";
            }
            else { // Case where mActiveTimerIndex is now higher than the number of timers, therefore we must be done.
                mTime.setText("DONE!");
            }


            break;

        case R.id.clear_button: if (logging) Log.d("onClick() ++ed", "case R.id.clear_button");
            mTimer.cancel();
            mTimerState = "STOPPED";
            break;
        }

    }


    /*****************************************
     * * *       Helper Functions        * * *
     ****************************************/
    public void CreateCountDownTimer(int ms)
    { if (logging) Log.d("CreateCountDownTimer", "Start");

        highlightTimer(mActiveTimerIndex);
        if (mActiveTimerIndex > 0){
            unhighlightTimer(mActiveTimerIndex-1);
        }
        mTimer = new CountDownTimer(ms, 1000) { // adjust the milli seconds here


            public void onTick(long millisUntilFinished) {

                mTime.setText(""+String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() { if (logging) Log.d("OnFinish()", "CountdownTimer Finished");
                mTimerState = "STOPPED";
                mActiveTimerIndex = mActiveTimerIndex + 1;
                onClick(mStartButton);
            }
        }.start();

    }
    public void highlightTimer(int x)
    {
        mControlsFragments[x].setLayoutColor(Color.GREEN);
    }
    public void unhighlightTimer(int x)
    {
        mControlsFragments[x].setLayoutColor(Color.LTGRAY);
    }
    public void setNewActiveTimerIndex(int x)   { mActiveTimerIndex=+1%mNumberOfControls; }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
