package com.joeynelson.timer;

import java.text.DateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by joey on 2/15/2017.
 */

public class Timer_Realm extends RealmObject {

    @PrimaryKey
    private String dateTime_timerNumber;
    private String name;
    private boolean queue;
    private int minutes;
    private int seconds;
    private int bpm;
    private int ts_numerator;
    private int ts_denominator;

    public Timer_Realm() {

    }

    public String getDateTime_timerNumber() {
        return dateTime_timerNumber;
    }

    public void setDateTime_timerNumber(String dateTime_timerNumber) {
        this.dateTime_timerNumber = dateTime_timerNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public boolean isQueue() {
        return queue;
    }

    public void setQueue(boolean queue) {
        this.queue = queue;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public int getTs_numerator() {
        return ts_numerator;
    }

    public void setTs_numerator(int ts_numerator) {
        this.ts_numerator = ts_numerator;
    }

    public int getTs_denominator() {
        return ts_denominator;
    }

    public void setTs_denominator(int ts_denominator) {
        this.ts_denominator = ts_denominator;
    }
}