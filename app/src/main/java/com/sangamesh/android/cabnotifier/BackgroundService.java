package com.sangamesh.android.cabnotifier;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.sangamesh.android.cabnotifier.MainActivity;

public class BackgroundService extends Service {

    private static final int NOTIF_ID = 41474148;
    private static final String NOTIF_CHANNEL_ID = "100" ;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BackgroundService", "OnStartCommand in background thread");
        //startForeground();
        new Thread(dummyTask).start();
        return super.onStartCommand(intent, flags, startId);
    }

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
                if (count % 10 == 0) {
                    ++count;
                    --count;
                }
                if (count == 10000) {
                    count = 0;
                }
            }
        }
    };

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
