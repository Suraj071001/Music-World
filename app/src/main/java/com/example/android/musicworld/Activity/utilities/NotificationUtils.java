package com.example.android.musicworld.Activity.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.android.musicworld.Activity.MainActivity;
import com.example.android.musicworld.R;

public class NotificationUtils {

    private static final String CHANNEL_ID = "channel id";
    private static final int PENDING_ID = 10001;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Notification createNotification(Context context){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Music Notification", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setContentTitle("Music Title")
                .setContentText("test")
                .setSmallIcon(R.drawable.favourite)
                .setContentIntent(contentIntent(context));

        return builder.build();
    }

    private static PendingIntent contentIntent(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context,PENDING_ID,intent,PendingIntent.FLAG_IMMUTABLE);
    }
}
