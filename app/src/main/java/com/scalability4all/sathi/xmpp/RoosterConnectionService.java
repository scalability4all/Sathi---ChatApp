package com.scalability4all.sathi.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.scalability4all.sathi.Constants;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;

import java.io.IOException;

public class RoosterConnectionService extends Service {
    private static final String LOGTAG ="RoosterConService";
    private boolean mActive;
    private Thread mThread;
    private Handler mTHandler;
    public static RoosterConnection getConnection() {
        return mConnection;
    }
    private static RoosterConnection mConnection;
    public RoosterConnectionService() {}
    private void initConnection()
    {
        Log.d(LOGTAG,"initConnection()");
        if( mConnection == null)
        {
            mConnection = new RoosterConnection(this);
        }
        try {
            mConnection.connect();
        } catch (IOException |SmackException |XMPPException e) {
            Log.d(LOGTAG,"Anmeldung fehlgeschlagen, Eingaben überprüfen");
            Intent i = new Intent(Constants.BroadCastMessages.UI_CONNECTION_ERROR);
            i.setPackage(getApplicationContext().getPackageName());
            getApplicationContext().sendBroadcast(i);
            Log.d(LOGTAG,"Broadcast Connection Error an Server");
            boolean logged_in_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("xmpp_logged_in",false);
            if(!logged_in_state)
            {
                Log.d(LOGTAG,"Logged in state :"+ logged_in_state + "calling stopself()");
                stopSelf();

            }else
            {
                Log.d(LOGTAG,"Logged in state :"+ logged_in_state);
            }
            e.printStackTrace();
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        ServerPingWithAlarmManager.onCreate(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        ServerPingWithAlarmManager.onDestroy();
        stop();
    }
    public void start()
    {
        Log.d(LOGTAG," Start() mActive :"+ mActive);
        if(!mActive)
        {
            mActive = true;
            if( mThread ==null || !mThread.isAlive())
            {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        Looper.loop();
                    }
                });
                mThread.start();
            }
        }
    }
    public void stop()
    {
        Log.d(LOGTAG,"stop()");
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null)
                {
                    mConnection.disconnect();
                }
            }
        });
    }
}