package com.joeynelson.timer;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by joey on 2/16/2017.
 */

public class TimerGroup_Realm extends RealmObject {

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @PrimaryKey
    private String dateTime;
    private RealmList<Timer_Realm> mTimers;

    public TimerGroup_Realm() {
        mTimers = new RealmList<>();
    }

    public RealmList<Timer_Realm> getmTimers() {
        return mTimers;
    }

    public void setmTimers(RealmList<Timer_Realm> mTimers) {
        this.mTimers = mTimers;
    }
}
