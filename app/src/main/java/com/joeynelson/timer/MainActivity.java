package com.joeynelson.timer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{   private boolean logging = true;

    /*** <USER SPECIFIED SETTINGS> ***/
    private SharedPreferences savedValues;

    private static int mNumberOfControls;
    private static boolean mUSER_TransitionTimer = true;
    private static int mUSER_TimerExpirationWarning;

    // clean up
    private int mCleanUpFragmentCount = 0;

    //~// Widgets //~//
    private Button mStartButton;
    private Button mStopButton;
    private String mTimerState = "STOPPED";
    private TextView mTime;

    // Timer Variables //
    private CountDownTimer mTimer;
    protected int mActiveTimerIndex=0;
    protected int mPreviousTimerIndex=-1;
    private ControlsFragment[] mControlsFragments;

    // Timer Helper Variables and Index Trackers
    private static final String FORMAT = "%02d:%02d:%02d"; // Format to output time for clock
    protected int mNumActiveTimerSwitches = -1; // Number of Switches turned on ( Calculated after the start button is pressed )
    protected int mNumberOfExecutedTimers; // Number of Timers that have been executed, compared with mActiveTimerSwitches to know when the last timer has been executed
    protected int mSkippedTimers =0; // Timer indexes that are skipped because they are off need to be accounted for
    private Animation mFlashingTextAnimation;

    // Media Player Objects and there sound files //
    protected MediaPlayer mMediaPlayer_Metronome;
    protected int mMediaPlayer_Metronome_SOUNDFILE_ID = R.raw.weakpulse_20ms;

    protected MediaPlayer mMediaPlayer_Transition;
    protected int mMediaPlayer_Transition_SOUNDFILE_ID = R.raw.getready_knivesharpen;

    protected MediaPlayer mMediaPlayer_End;
    protected int mMediaPlayer_End_SOUNDFILE_ID = R.raw.timer_end_buzzer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    if (logging) Log.d("MainActivity", "Start: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedValues = PreferenceManager.getDefaultSharedPreferences(this); // Initialize Saved Values Settings Preferences Manager

        // Initialize Widgets //
        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(this);

        mStopButton = (Button) findViewById(R.id.clear_button);
        mStopButton.setOnClickListener(this);

        mTime = (TextView) findViewById(R.id.clock_display);
        // Create Flashing Text Animation
        mFlashingTextAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFlashingTextAnimation.setDuration(30); //You can manage the blinking time with this parameter
        mFlashingTextAnimation.setStartOffset(20);
        mFlashingTextAnimation.setRepeatMode(Animation.REVERSE);
        mFlashingTextAnimation.setRepeatCount(Animation.INFINITE);
    }

    @Override
    public void onClick(View view)
    { if (logging) Log.d("onClick", "Start");

        // Poll Number of Active Timers, Used to determine how long the sequence should be, so we know when to sound the final buzzer
        if (mNumActiveTimerSwitches == -1)  // Only needs to be run if mNumActiveTimerSwitches has not already ben set, otherwise it grows each itteration
        {
            for (int i = 0; i < mNumberOfControls; i++) {
                if (mControlsFragments[i].getSwitchState()) {
                    if (logging)
                        Log.d("onClick", "if (mControlsFragments[i].getSwitchState())" + " getSwitchState(" + i + ") = " + mControlsFragments[i].getSwitchState());
                    mNumActiveTimerSwitches++;
                }

            }
        }
        switch(view.getId())
        {
        case R.id.start_button: Log.d("onClick", "case R.id.start_button");
            mStartButton.setEnabled(false);
            mStopButton.setEnabled(true); // Enable Stop Button

            if (mTimerState.equals("STOPPED") && mActiveTimerIndex < mNumberOfControls)   // If a timer hasn't already started then start one, If a timer has started, dont start another one!
            { Log.d("onClick", "if (mTimerState.equals(\"STOPPED\") && mActiveTimerIndex < mNumberOfControls)");
                mTimerState = "STARTED";

                // Exit function is invalid inputs are present in the timers
                if (ValidInputsCheck() == false) {
                    return;
                }
                if (mControlsFragments[mActiveTimerIndex].getSwitchState()) {

                    // Configure Count Down Timer variables
                    int hrs = 0;
                    int mins = mControlsFragments[mActiveTimerIndex].getMinutes();
                    int secs = mControlsFragments[mActiveTimerIndex].getSeconds();

                    // Calculate Clock tick speed for metronome
                    float denominator = mControlsFragments[mActiveTimerIndex].getBpm()/60; // Calculate beats per second by dividing BPM by 60 seconds (60 seconds in 1 minute)
                    Log.d("onClick", "  Create CountDownTimer denominator = " + denominator);
                    int tickrate = Math.round( 1000/denominator );  // 1000ms divided by beats per second // Rounded to conver to integer

                    // Create CountDownTimer
                    CreateCountDownTimer( hrs * 3600000 + mins*60000 + secs * 1000, tickrate);
                }
                else // This is when the next timer is not in the queue, i.e. the switch is off, so to pass it to check the next timer we must do the following
                {
                    mTimerState = "STOPPED";
                    mActiveTimerIndex++;
                    mSkippedTimers++; // Increment Skipped Timers so accuratel account for skipped timers for future calls, i.e. unhighlight call
                    onClick(mStartButton);
                }
            }
            else if (mTimerState.equals("STARTED"))
            { Log.d("onClick", "else if (mTimerState.equals(\"STARTED\"))");
                mTimerState = "PAUSED"; // If the timer had already started, it should now be paused
            }
            else if (mTimerState.equals("PAUSED"))
            { Log.d("onClick", "else if (mTimerState.equals(\"PAUSED\"))");
                mTimerState = "STARTED";
            }

            /*** END OF SEQUENCE ***/

            else { // Case where mActiveTimerIndex is now higher than the number of timers, therefore we must be done.
                mTime.setText("Finished.");
                mTime.startAnimation(mFlashingTextAnimation);
                mFlashingTextAnimation.setDuration(30); //You can manage the blinking time with this parameter
                mMediaPlayer_End.start();
                endhighlightTimer(mActiveTimerIndex - 1 - mSkippedTimers);
            }
            break;

        case R.id.clear_button: if (logging) Log.d("onClick() ++ed", "case R.id.clear_button");
            if (mTimer != null)
                mTimer.cancel();
            mStopButton.setEnabled(false);
            mTimerState = "STOPPED";

            CleanUp();

            mStartButton.setEnabled(true); /// re-enable start button
            break;
        }

    }


    /*****************************************
     * * *       Helper Functions        * * *
     ****************************************/

    private void CleanUp() {
        for (int i = 0; i<mNumberOfControls; i++) // too confusing trying to figure out which control to unhighlight, so lets just unhighlight them all
            unhighlightTimer(i);

        mActiveTimerIndex = 0;
        mNumberOfExecutedTimers =0;
        mSkippedTimers = 0;
        mNumActiveTimerSwitches = -1;

        mTime.clearAnimation();

        // Stop Annoying End Buzzer, release object, and make a new one so it can play again, if it is still running
        if (mMediaPlayer_End.isPlaying()) {
            mMediaPlayer_End.stop();
            mMediaPlayer_End.release();
            mMediaPlayer_End = MediaPlayer.create(this, mMediaPlayer_End_SOUNDFILE_ID);
        }
        if (mMediaPlayer_Transition.isPlaying()) {
            mMediaPlayer_End.stop();
            mMediaPlayer_End.release();
            mMediaPlayer_End = MediaPlayer.create(this, mMediaPlayer_Transition_SOUNDFILE_ID);
        }
    }

    private boolean ValidInputsCheck()
    {
        for (ControlsFragment ctrl : mControlsFragments) {
            if (ctrl.getBpm() < 60) {
                Toast.makeText(this, "Error: BPM must be at least 60", Toast.LENGTH_LONG).show();
                return false;
            }
            if (ctrl.getBpm() > 240) {
                Toast.makeText(this, "Error: BPM must be at most < 240", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }


    public void CreateCountDownTimer(int ms, int Tickrate)
    { if (logging) Log.d("CreateCountDownTimer", "Start");


        // Timer Highlight Logic
        if (mPreviousTimerIndex != -1){
            unhighlightTimer(mPreviousTimerIndex);
            mSkippedTimers = 0; // Reset skipped timers because at this point you've clearly found an active timer
        }
        highlightTimer(mActiveTimerIndex); // highlight active timer
        mPreviousTimerIndex = mActiveTimerIndex; // unhighlight previously active timer

        mTimer = new CountDownTimer(ms, Tickrate) { // adjust the milli seconds here
            int BPM = mControlsFragments[mActiveTimerIndex].getBpm();

            public void onTick(long millisUntilFinished) {

                if(mTimerState.equals("STARTED")) {
                    /******** METRONOME SOUND *********/
                    if (BPM!= 0) {
                        //mMediaPlayer_Metronome.release();
                        mMediaPlayer_Metronome.start();
                    }

                    mTime.setText("" + String.format(FORMAT,
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

                    if (mTimerState.equals("STOPPED")) {
                        //mTimer.onFinish();
                        cancel();
                        mTimer.cancel();
                    }
                    if (millisUntilFinished <= mUSER_TimerExpirationWarning) {  // 5 second visual flash warning that the timer is about to expire
                        mTime.startAnimation(mFlashingTextAnimation);
                    }
                }
            }

            public void onFinish()
            { if (logging) Log.d("OnFinish()", "CountdownTimer Finished");

                mTime.setText("00:00:00");
                mNumActiveTimerSwitches--;

                /******** One Final Metronome sound *********/
                if (BPM!= 0) {    mMediaPlayer_Metronome.start();        }

                if (mNumActiveTimerSwitches != -1) //   || mTimerState != "STOPPED"
                { if (logging) Log.d("OnFinish()", "if (mNumActiveTimerSwitches != 0)" + "mNumActiveTimerSwitches = " + mNumActiveTimerSwitches);

                    /******** ::SOUND:: Inter Timer Get Ready SOUND *********/

                    if(mUSER_TransitionTimer) {     mMediaPlayer_Transition.start();    }

                    while (mMediaPlayer_Transition.isPlaying()) {

                    }
                }
                    mTimerState = "STOPPED";
                    mActiveTimerIndex = mActiveTimerIndex + 1;
                    onClick(mStartButton);

            }
        }.start();
        /******** BEEP SOUND *********/ // Beep onces right away
        if (mControlsFragments[mActiveTimerIndex].getBpm() != 0) {  mMediaPlayer_Metronome.start();  }

    }
    public void highlightTimer(int x)    {        mControlsFragments[x].setLayoutColor(Color.GREEN);    }

    public void unhighlightTimer(int x)    {        mControlsFragments[x].setLayoutColor(Color.LTGRAY);    }

    public void endhighlightTimer(int x)    {        mControlsFragments[x].setLayoutColor(Color.RED);    }

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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        else if (id== R.id.action_about) {
            Toast.makeText(this, "Ispiration for this timer comes from various interval training activities with Sigung Colin Davey of Colin Davey Combat Arts", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override protected void onResume()
    {   if (logging) Log.d("MainActivity", "Start: OnResume()");
        super.onResume();
        /* ************************************************************** *
        *                       Load User Settings                        *
        * *************************************************************** */

        // number of timer controls
        mNumberOfControls = Integer.parseInt(savedValues.getString("pref_num_of_timers", "5"));
        mUSER_TimerExpirationWarning = Integer.parseInt(savedValues.getString("pref_expiration_warning_duration", "10000"));


        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            /*** Create Media Players ***/;
            mMediaPlayer_Metronome = MediaPlayer.create(this, mMediaPlayer_Metronome_SOUNDFILE_ID);
            mMediaPlayer_Transition = MediaPlayer.create(this, mMediaPlayer_Transition_SOUNDFILE_ID);
            mMediaPlayer_End= MediaPlayer.create(this, mMediaPlayer_End_SOUNDFILE_ID);

            String tag = "";
            ControlsFragment ctrlfrag;

            FragmentManager fragMan = getSupportFragmentManager();
            FragmentTransaction fragTran = fragMan.beginTransaction();


        /*******************************************************************************************
        *     Clean up old fragments if user uses settings and therefore calls onResume() again    *
        *******************************************************************************************/
            for (int i = 0; i < mCleanUpFragmentCount; i++) {
                tag = "cfrag"+i;
                ControlsFragment oldFragment = (ControlsFragment) getSupportFragmentManager().findFragmentByTag(tag);
                if(oldFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(oldFragment).commit();
                }
            }


            /***************************************************************************************
             *    Dynamically create fragments based on number of controls specified by the user    *
             **************************************************************************************/
            for (int i = 0; i < mNumberOfControls; i++) {
                tag = "cfrag"+i;
                ctrlfrag = new ControlsFragment();
                fragTran.add(R.id.fragment_container, ctrlfrag, tag);
            }

            fragTran.commit(); // commit additions
            fragMan.executePendingTransactions();

            // Populate Fragments Array to reference fragments that were just created
            mControlsFragments = new ControlsFragment[mNumberOfControls];
            for (int b = 0; b < mNumberOfControls; b++) {
                tag = "cfrag"+b;
                mControlsFragments[b] = (ControlsFragment) getSupportFragmentManager().findFragmentByTag(tag); //if (logging) Log.d("ClockFragment", "OnClick = " + cFrag);
            }
            mCleanUpFragmentCount = mControlsFragments.length;

        }
    }

    @Override
    protected void onPause() {  if (logging) Log.d("MainActivity", "Start: onPause()");
        super.onPause();
        mStopButton.setEnabled(true);
        this.onClick(mStopButton);
       // mMediaPlayer_Metronome.stop();
       // mMediaPlayer_Transition.stop();
       // mMediaPlayer_End.stop();

    }

    @Override
    protected void onStop() {   if (logging) Log.d("MainActivity", "Start: onStop()");
        super.onStop();
        // mMediaPlayer_Metronome.release();
        // mMediaPlayer_Transition.release();
        // mMediaPlayer_End.release();
    }
    @Override
    protected void onDestroy() {    if (logging) Log.d("MainActivity", "Start: onDestroy()");
        super.onDestroy();
        mMediaPlayer_Metronome.release();
        mMediaPlayer_Transition.release();
        mMediaPlayer_End.release();
    }


}
