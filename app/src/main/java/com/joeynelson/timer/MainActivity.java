package com.joeynelson.timer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{   private boolean logging = false;
    protected boolean FirstLaunch = true;
    private int MAX_NUMBER_OF_CONTROLS = 10;  // used for clean up
    private int ON_RESUME_COUNT =2;

    /*** <GLOBAL USER SETTINGS> ***/
    private SharedPreferences savedValues;

    private  int mUSER_NumberOfControls = 0; // intialized to work with lastNumberOfControls logic
    private int lastNumberOfControls;
    private  int mUSER_NumberOfTransitionBeeps;
    private  boolean mUSER_FirstBeatAccent;
    private  int mUSER_TimerExpirationWarning;


    //~// Widgets //~//
    private Button mStartButton;
    private Button mStopButton;
    private Button mSaveButton;
    private String mTimerState = "STOPPED";
    private TextView mTime;

    // Timer Variables //
    private CountDownTimer mTimer;
    protected int mActiveTimerIndex=0;
    protected int mPreviousTimerIndex=-1;
    private ControlsFragment[] mControlsFragments;

    // Time Signature Variables
    protected int mTimeSignatureNumerator;
    protected int mTimeSignatureDenominator;


    // Timer Helper Variables and Index Trackers
    private static final String FORMAT = "%02d:%02d:%02d"; // Format to output time for clock
    protected int mNumActiveTimerSwitches = -1; // Number of Switches turned on ( Calculated after the start button is pressed )
    protected int mNumberOfExecutedTimers; // Number of Timers that have been executed, compared with mActiveTimerSwitches to know when the last timer has been executed
    protected int mSkippedTimers =0; // Timer indexes that are skipped because they are off need to be accounted for
    private Animation mFlashingTextAnimation;

    // Media Player Objects and there sound files //
    protected MediaPlayer mMediaPlayer_Metronome;
    protected int mMediaPlayer_Metronome_SOUNDFILE_ID = R.raw.weakpulse_20ms;

    protected MediaPlayer mMediaPlayer_MetronomeAccent;
    protected int mMediaPlayer_MetronomeAccent_SOUNDFILE_ID = R.raw.druminfected__metronome;

    protected MediaPlayer mMediaPlayer_Transition;
    protected int mMediaPlayer_Transition_SOUNDFILE_ID = R.raw.transition_beep;

    protected MediaPlayer mMediaPlayer_End;
    protected int mMediaPlayer_End_SOUNDFILE_ID = R.raw.timer_end_buzzer;

    //private AdView mAdView;

    // Realm database
    private Realm realm;
    private String mTimerSaveName ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    if (logging) Log.d("MainActivity", "Start: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedValues = PreferenceManager.getDefaultSharedPreferences(this); // Initialize Saved Values Settings Preferences Manager

        // Initialize Widgets //
        mStartButton = (Button) findViewById(R.id.start_button);
        mStopButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);

        // Set Button Listeners
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);


        mTime = (TextView) findViewById(R.id.clock_display);
        // Create Flashing Text Animation
        mFlashingTextAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFlashingTextAnimation.setDuration(30); //You can manage the blinking time with this parameter
        mFlashingTextAnimation.setStartOffset(20);
        mFlashingTextAnimation.setRepeatMode(Animation.REVERSE);
        mFlashingTextAnimation.setRepeatCount(Animation.INFINITE);

        // Google Add View
        //AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);


        // Initialize Realm in Application
        Realm.init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfiguration);
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    public void onClick(View view)
    { if (logging) Log.d("onClick", "Start");

        // Poll Number of Active Timers, Used to determine how long the sequence should be, so we know when to sound the final buzzer
        if (mNumActiveTimerSwitches == -1)  // Only needs to be run if mNumActiveTimerSwitches has not already ben set, otherwise it grows each itteration
        {
            for (int i = 0; i < mUSER_NumberOfControls; i++) {
                if (mControlsFragments[i].getSwitchState()) {
                    if (logging)
                        if (logging) Log.d("onClick", "if (mControlsFragments[i].getSwitchState())" + " getSwitchState(" + i + ") = " + mControlsFragments[i].getSwitchState());
                    mNumActiveTimerSwitches++;
                }

            }
        }
        switch(view.getId())
        {
        case R.id.start_button: Log.d("onClick", "case R.id.start_button");
            mStartButton.setEnabled(false);
            mStopButton.setEnabled(true); // Enable Stop Button
            // Set Colors on Buttons
            mStartButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.startbutton_disabled)); // dynamically changes color for disabled button
            mStopButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.stopbutton)); // dynamically changes color for disabled button

            if (mTimerState.equals("STOPPED") && mActiveTimerIndex < mUSER_NumberOfControls)   // If a timer hasn't already started then start one, If a timer has started, dont start another one!
            { Log.d("onClick", "if (mTimerState.equals(\"STOPPED\") && mActiveTimerIndex < mUSER_NumberOfControls)");
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

                    int bps = mControlsFragments[mActiveTimerIndex].getBpm()/15; // beats per second @ enhanced speed (that is why it's 15)

                    // 1000ms divided by beats per second // Rounded to conver to integer
                    int tickRate = (bps > 0) ? Math.round( 1000/bps ) : 1; // this avoids bps being 0 which would cause a divide by 0 error

                    if (logging) Log.d("onClick", "  Create CountDownTimer realTickRate = " + tickRate);

                    // Configure Time Signature Settings
                    mTimeSignatureNumerator=mControlsFragments[mActiveTimerIndex].getTimeSignatureNumerator();
                    mTimeSignatureDenominator=mControlsFragments[mActiveTimerIndex].getTimeSignatureDenominator();
                    if (mTimeSignatureDenominator == 8) { // @X Temp if Time Signature is using 8th notes instead of 4th notes.
                        tickRate = tickRate / 2;
                    }

                    // Create CountDownTimer
                    CreateCountDownTimer(hrs * 3600000 + mins * 60000 + secs * 1000, tickRate);
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
            { if (logging) Log.d("onClick", "else if (mTimerState.equals(\"STARTED\"))");
                mTimerState = "PAUSED"; // If the timer had already started, it should now be paused
            }
            else if (mTimerState.equals("PAUSED"))
            { if (logging) Log.d("onClick", "else if (mTimerState.equals(\"PAUSED\"))");
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
            // Set Colors on Buttons
            mStartButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.startbutton)); // dynamically changes color for disabled button
            mStopButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.stopbutton_disabled)); // dynamically changes color for disabled button
            break;

            case R.id.save_button:
                if (logging) Log.d("onClick() ++ed", "case R.id.save_button");

                /**
                 Dialog Box to prompt user for the name of the timer
                 **/

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Name this group of timers");

                // set the input
                final EditText input = new EditText(MainActivity.this);

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTimerSaveName = input.getText().toString();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

                // Generate DateTimeString to be used as Primary key for Realm Objects
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                // Initialize Realm in Application
                realm.beginTransaction();

                // Create a TimerGroup_Realm Object
                TimerGroup_Realm timerGroup = new TimerGroup_Realm();
                timerGroup.setDateTime(currentDateTimeString);

                for (int i=0; i < mUSER_NumberOfControls; i++) {
                    ControlsFragment cFrag = mControlsFragments[i];

                    // Create a Timer_Realm Object
                    Timer_Realm timer = new Timer_Realm();
                    timer.setDateTime_timerNumber(currentDateTimeString+Integer.toString(i));
                    timer.setName(mTimerSaveName+Integer.toString(i));
                    timer.setQueue(cFrag.getSwitchState());
                    timer.setMinutes(cFrag.getMinutes());
                    timer.setSeconds(cFrag.getSeconds());
                    timer.setBpm(cFrag.getBpm());
                    timer.setTs_numerator(cFrag.getTimeSignatureNumerator());
                    timer.setTs_denominator(cFrag.getTimeSignatureDenominator());

                    // add Timer_Realm Object to TimerGroup_Realm Object
                    timerGroup.getmTimers().add(timer);
                }
                // Commit Realm Transaction
                realm.commitTransaction();


                //Prefs.with(this).setPreLoad(true);


        }

    }


    /*****************************************
     * * *       Helper Functions        * * *
     ****************************************/

    private void CleanUp() {
        for (int i = 0; i< mUSER_NumberOfControls; i++) // too confusing trying to figure out which control to unhighlight, so lets just unhighlight them all
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

    private boolean ValidInputsCheck() // run any form field validation checks here
    {
        for (ControlsFragment ctrl : mControlsFragments) {
            if (ctrl.getBpm() < 15 && ctrl.getBpm()!=0) {
                Toast.makeText(this, "Error: BPM must be at least 15 or 0 to disable sound", Toast.LENGTH_LONG).show();
                return false;
            }
            if (ctrl.getBpm() > 240) {
                Toast.makeText(this, "Error: BPM must be at most 240", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

/*** Create the Countdown Timer ***/
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
            int TickCount = 0;


            public void onTick(long millisUntilFinished) {

                if(mTimerState.equals("STARTED")) {

                    /******** METRONOME SOUND *********/
                    if (BPM!= 0) {
                        if (TickCount%4 ==0) {
                            if ( ( (TickCount/4) % mTimeSignatureNumerator == 0) && mUSER_FirstBeatAccent)
                                mMediaPlayer_MetronomeAccent.start();
                            else
                                mMediaPlayer_Metronome.start();
                        }
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
                        if (mFlashingTextAnimation.hasEnded() || !mFlashingTextAnimation.hasStarted())
                        mTime.startAnimation(mFlashingTextAnimation);
                        if (logging) Log.d("CreateCountDownTimer", "if (mFlashingTextAnimation.hasEnded() || !mFlashingTextAnimation.hasStarted()) = " + (mFlashingTextAnimation.hasEnded() || !mFlashingTextAnimation.hasStarted()));
                    }
                    TickCount++;
                }
            }

            public void onFinish()
            { if (logging) Log.d("OnFinish()", "CountdownTimer Finished");

                mTime.setText("00:00:00");
                mNumActiveTimerSwitches--;
                mTime.clearAnimation(); // stops flashing text if it is taking place


                /********ONE LAST METRONOME SOUND *********/
                if (BPM!= 0) {
                    if (TickCount%4 ==0) {
                        if ( ( (TickCount/4) % mTimeSignatureNumerator == 0) && mUSER_FirstBeatAccent)
                            mMediaPlayer_MetronomeAccent.start();
                        else
                            mMediaPlayer_Metronome.start();
                    }
                }

                if (mNumActiveTimerSwitches != -1) //   || mTimerState != "STOPPED"
                { if (logging) Log.d("OnFinish()", "if (mNumActiveTimerSwitches != 0)" + "mNumActiveTimerSwitches = " + mNumActiveTimerSwitches);

                    /******** ::SOUND:: Inter Timer Get Ready SOUND *********/

                    for(int i = 0; i < mUSER_NumberOfTransitionBeeps; i++) {
                        mMediaPlayer_Transition.start();
                        while (mMediaPlayer_Transition.isPlaying()) { // Stall for audio play loop
                        }
                    }

                }
                    mTimerState = "STOPPED";
                    mActiveTimerIndex = mActiveTimerIndex + 1;
                    onClick(mStartButton);

            }
        }.start();
    }


    public void highlightTimer(int x)    {        mControlsFragments[x].setLayoutColor(ContextCompat.getColor(getApplicationContext(),R.color.startbutton_on));    }

    public void unhighlightTimer(int x)    {        mControlsFragments[x].setLayoutColor(Color.DKGRAY);    }

    public void endhighlightTimer(int x)    {        mControlsFragments[x].setLayoutColor(ContextCompat.getColor(getApplicationContext(),R.color.stopbutton_on));   }

    public void setNewActiveTimerIndex(int x)   { mActiveTimerIndex=+1% mUSER_NumberOfControls; }



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
        //else if (id== R.id.action_about) {
        //    Toast.makeText(this, "Inspiration for this timer comes from various interval training activities with Sigung Colin Davey of Colin Davey Combat Arts", Toast.LENGTH_LONG).show();
        //    return true;
        // }

        return super.onOptionsItemSelected(item);
    }

    @Override protected void onResume()
    {   if (logging) Log.d("MainActivity", "Start: OnResume()");
        System.out.println(mActiveTimerIndex);
        super.onResume();
        /* ************************************************************** *
        *                       Load User Settings                        *
        * *************************************************************** */

        lastNumberOfControls = mUSER_NumberOfControls;

        mUSER_NumberOfControls = Integer.parseInt(savedValues.getString("pref_num_of_timers", "3"));

        mUSER_NumberOfTransitionBeeps = Integer.parseInt(savedValues.getString("pref_num_of_transition_beeps", "5"));
        mUSER_TimerExpirationWarning = Integer.parseInt(savedValues.getString("pref_expiration_warning_duration", "10000"));
        mUSER_FirstBeatAccent = savedValues.getBoolean("pref_first_beat_accent", true);


        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            /*** Create Media Players ***/;
            mMediaPlayer_Metronome = MediaPlayer.create(this, mMediaPlayer_Metronome_SOUNDFILE_ID);
            mMediaPlayer_MetronomeAccent = MediaPlayer.create(this, mMediaPlayer_MetronomeAccent_SOUNDFILE_ID);
            mMediaPlayer_Transition = MediaPlayer.create(this, mMediaPlayer_Transition_SOUNDFILE_ID);
            mMediaPlayer_End= MediaPlayer.create(this, mMediaPlayer_End_SOUNDFILE_ID);

            String tag = "";
            ControlsFragment ctrlfrag;




            int diff = lastNumberOfControls - mUSER_NumberOfControls; // calculate number of controls to remove


            /*******************************************************************************************
             *     Clean up old fragments if user uses settings and therefore calls onResume() again    *
             *******************************************************************************************/

            if (mUSER_NumberOfControls < lastNumberOfControls) { // Clean up old fragments only if the user is asking for less fragments than before
                FragmentManager fragManRemove = getSupportFragmentManager();
                FragmentTransaction fragTranRemove = fragManRemove.beginTransaction();


                for (int i = mUSER_NumberOfControls; i < lastNumberOfControls; i++) {
                    tag = "cfrag" + i;
                    ControlsFragment oldFragment = (ControlsFragment) fragManRemove.findFragmentByTag(tag);
                    if (oldFragment != null) {
                        fragTranRemove.remove(oldFragment);
                    }
                }

                fragTranRemove.commit(); // commit additions
                fragManRemove.executePendingTransactions();

            }


            /***************************************************************************************
             *    Dynamically create fragments based on number of controls specified by the user    *
             **************************************************************************************/
            if (mUSER_NumberOfControls > lastNumberOfControls) {

                int inversediff = diff-(2*diff);
                System.out.println("INVERSEDIFF = " + inversediff);

                FragmentManager fragManAdd = getSupportFragmentManager();
                FragmentTransaction fragTranAdd = fragManAdd.beginTransaction();

                for (int i = lastNumberOfControls; i < mUSER_NumberOfControls; i++) {
                    tag = "cfrag" + i;
                    ctrlfrag = new ControlsFragment();
                    fragTranAdd.add(R.id.fragment_container, ctrlfrag, tag);
                }

                fragTranAdd.commit(); // commit additions
                fragManAdd.executePendingTransactions();

                //if (mUSER_NumberOfControls < lastNumberOfControls && ON_RESUME_COUNT != 2)
                    //ON_RESUME_COUNT++;

                // Populate Fragments Array to reference fragments that were just created
                mControlsFragments = new ControlsFragment[mUSER_NumberOfControls];
                for (int b = 0; b < mUSER_NumberOfControls; b++) {
                    //if (ON_RESUME_COUNT % 2 == 0 || ON_RESUME_COUNT == 2) // This tries to fix a bug in android/java code where the fragments can get reversed: http://stackoverflow.com/questions/23504790/android-multiple-fragment-transaction-ordering/23523922#23523922
                        tag = "cfrag" + (b);
                    //else
                        //tag = "cfrag" + (mUSER_NumberOfControls - b - 1);
                    mControlsFragments[b] = (ControlsFragment) getSupportFragmentManager().findFragmentByTag(tag); //if (logging) Log.d("ClockFragment", "OnClick = " + cFrag);
                }

            }
            // Dynamically resize text on Controls Fragment depending on how many controls are present
            for (int i=0; i < mUSER_NumberOfControls; i++)
            {
                mControlsFragments[i].setTextSize(32-(2*(mUSER_NumberOfControls)));
            }

        }
        ON_RESUME_COUNT++;

    }

    @Override
    protected void onPause() {  if (logging) Log.d("MainActivity", "Start: onPause()");
        super.onPause();
       // mStopButton.setEnabled(true);
        //this.onClick(mStopButton);
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
