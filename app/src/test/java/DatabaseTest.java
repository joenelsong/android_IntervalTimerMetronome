/**
 * Created by joey on 2/19/2017.
 */

import android.content.Context;

import com.joeynelson.timer.TimerGroup_Realm;
import com.joeynelson.timer.Timer_Realm;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class DatabaseTest {

    @Mock
    Context mMockContext;

    @Test
    public void SaveAndQueryDatabase() {

        Realm realm;

        // Initialize Realm in Application
        Realm.init(mMockContext);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfiguration);
        realm = Realm.getInstance(realmConfiguration);

        String testName="UnitTestTimer";

        // Initialize Realm in Application
        realm.beginTransaction();

        // Create a TimerGroup_Realm Object
        TimerGroup_Realm timerGroup = new TimerGroup_Realm();

        for (int i=0; i < 5; i++) {

            // Create a Timer_Realm Object
            Timer_Realm timer = new Timer_Realm();
            timer.setName(testName+Integer.toString(i));
            timer.setQueue(true);
            timer.setMinutes(2);
            timer.setSeconds(5);
            timer.setBpm(120);
            timer.setTs_numerator(2);
            timer.setTs_denominator(4);

            // add Timer_Realm Object to TimerGroup_Realm Object
            timerGroup.getmTimers().add(timer);
        }
        // Commit Realm Transaction
        realm.commitTransaction();

    }



}



