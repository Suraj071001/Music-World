package com.example.android.musicworld.Activity;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.android.musicworld.Activity.utilities.NotificationUtils;

public class MusicService extends Service {
    private static final String TAG = "myTag";
    public MusicService(){
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called");

        Notification notification = NotificationUtils.createNotification(getApplicationContext());
        startForeground(100,notification);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i =1;i<=20;i++){
                    try {
                        Thread.sleep(1000);
                        Log.d("myTag", "run:"+i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stopForeground(STOP_FOREGROUND_REMOVE);
                stopSelf();
            }
        }).start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
    }
}
