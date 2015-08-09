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


public class MainActivity extends FragmentActivity implements View.OnClickListener
{
    private boolean logging = true;

    private int mNumberOfControls;
    private Button mStartButton;


    // Timer Variables //
    private CountDownTimer mTimer;
    private int mActiveTimerIndex;
    private ControlsFragment[] mControlsFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    if (logging) Log.d("MainActivity", "Start: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Widgets //
        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(this);

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
    {
        switch(view.getId())
        {
        case R.id.start_button:
            highlightTimer(1);

            break;

        case R.id.clear_button:
            break;
        }

    }


    /*****************************************
     * * *       Helper Functions        * * *
     ****************************************/
    public void UpdateTimerStates()
    {

    }
    public void highlightTimer(int x)
    {
        mControlsFragments[x].setLayoutColor(Color.GREEN);
    }
    public void unhighlightTimer(int x)
    {
        mControlsFragments[x].setLayoutColor(Color.LTGRAY);
    }



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
