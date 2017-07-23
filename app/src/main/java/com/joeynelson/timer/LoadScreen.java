package com.joeynelson.timer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by joey on 2/13/2017.
 */

public class LoadScreen extends AppCompatActivity implements View.OnClickListener{

    private boolean logging = true;


    Button btn_new;
    Button btn_useLast;
    Button btn_loadSaved;

    Cursor cursor = null;
    SimpleCursorAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (logging) Log.d("LoadScreen", "Start: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_screen);

        btn_new = (Button) findViewById(R.id.new_btn);
        btn_useLast = (Button) findViewById(R.id.use_last_btn);
        btn_loadSaved = (Button) findViewById(R.id.load_saved_btn);


        btn_new.setOnClickListener(this);
        btn_useLast.setOnClickListener(this);
        btn_loadSaved.setOnClickListener(this);



        // Set up the adapter for the ListView to display the forecast info
        adapter = new SimpleCursorAdapter(this,
                R.layout.listview_timergroups,
                cursor,
                new String[]{"Time","HighLow", "Predictionsft"},
                new int[]{

                        R.id.timer_group_name,
                        R.id.timer_group_date,
                        R.id.timer_group_num_timers
                },
                0 );	// no flags

        //Gets ready for a toast, will display in feet and m the tide height
        ListView itemsListView = (ListView)findViewById(R.id.listview_for_timer_groups);
        itemsListView.setAdapter(adapter);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
            }
        });

        //savedValues = PreferenceManager.getDefaultSharedPreferences(this); // Initialize Saved Values Settings Preferences Manager

        //Intent myIntent = new Intent(LoadScreen.this, MainActivity.class);
        //myIntent.putExtra("key", 2); //Optional parameters
        //this.startActivity(myIntent);

    }

    public void onClick(View view) {
        if (logging) Log.d("LoadScreen:onClick", "Start");

        Intent myIntent = new Intent(LoadScreen.this, MainActivity.class);

        switch(view.getId()) {
            case R.id.new_btn:
                Log.d("onClick", "case R.id.new_btn");
                break;
            case R.id.use_last_btn:
                Log.d("onClick", "case R.id.use_last_btn");
                break;
            case R.id.load_saved_btn:
                Log.d("onClick", "case R.id.load_saved_btn");
                break;
        }

        myIntent.putExtra("key", 2); //Optional parameters
        this.startActivity(myIntent);

        // Kill Load Screen Activitiy
        finish();

    }


    @Override protected void onResume() {
        if (logging) Log.d("LoadScreen", "Start: OnResume()");

        super.onResume();
        /* ************************************************************** *
        *                       Load User Settings                        *
        * *************************************************************** */

    }
    @Override
    protected void onPause() {  if (logging) Log.d("LoadScreen", "Start: onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {   if (logging) Log.d("LoadScreen", "Start: onStop()");
        super.onStop();
    }
    @Override
    protected void onDestroy() {    if (logging) Log.d("MainActivity", "Start: onDestroy()");
        super.onDestroy();
    }




}
