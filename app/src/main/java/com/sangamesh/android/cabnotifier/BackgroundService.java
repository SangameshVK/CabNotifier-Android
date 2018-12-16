package com.sangamesh.android.cabnotifier;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import com.sangamesh.android.cabnotifier.Constants;

public class BackgroundService extends Service {

    private static final int FOREGROUND_NOTIFICATION_ID = 41474147;

    private MessageListener mMessageListener;
    private boolean mForegroundNotified = false;

    private static final String TAG = "BackgroundService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "OnCreate enter");
        showSmallToast("Creating Service");
        super.onCreate();
        registerMessageListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand enter");
        showSmallToast("Starting Service");
        // This is necessary for notifications to work even when app is cleared from recents list.
        startForeground();
        return super.onStartCommand(intent, flags, startId);
        //Just in case we need START_STICKY in future
        //return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy enter");
        showSmallToast("Service Destroyed");
        mForegroundNotified = false;
        super.onDestroy();
        unregisterReceiver(mMessageListener);
    }

    /* Not seeming to add value apart from registering again.
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "onTaskRemoved enter");
        showSmallToast("Task removed");
        //registerMessageListener();
        super.onTaskRemoved(rootIntent);
        // In case we use this function, need to remove one registerMessageListener.
        registerMessageListener();
    }*/

    private void registerMessageListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //TODO: Remove next LOC
        //intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.setPriority(100);
        mMessageListener = new MessageListener();
        registerReceiver(mMessageListener, intentFilter);
        Log.i(TAG, "Registered MessageListener");
        //TODO: Remove all toasts
        showSmallToast("Registered Message Listener");
    }

    private void showSmallToast(String toastMessage) {
        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }


    /*
    //TODO: Get rid of redundant code and all unnecessary commented code
    private Runnable dummyTask = new Runnable() {
        public void run() {
            int count = 0;
            // Do something here
            while(true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e("Runnable", "InterruptedException: " + e.toString());
                }
                ++count;
                if (count % 100 == 0) {
                    Log.i(TAG, "Count % 100 is 0");
                }
                if (count == 10000) {
                    count = 0;
                }
            }
        }
    };*/


    private void startForeground() {
        if (mForegroundNotified) {
            showSmallToast("Foreground Notified already");
            return;
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);
        startForeground(FOREGROUND_NOTIFICATION_ID, new NotificationCompat.Builder(this,
                Constants.NOTIFICATION_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
        mForegroundNotified = true;
    }
}
