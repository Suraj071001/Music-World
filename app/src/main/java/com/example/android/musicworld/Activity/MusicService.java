package com.example.android.musicworld.Activity;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.android.musicworld.R;

import java.io.IOException;
import java.security.cert.CertPathBuilder;

import kotlin.random.Random;

public class MusicService extends Service {

    //constants
    private static final String TAG = "myTag";
    private static final int PENDING_ID = 12;
    private static final String POSITION = "position";

    private final String ACTION_PREVIOUS = "previous action";
    private final String ACTION_PLAY = "play action";
    private final String ACTION_NEXT = "next action";
    private final String ACTION_EXIT = "exit action";


    private PendingIntent previousPI;
    private PendingIntent playPI;
    private PendingIntent nextPI;
    private PendingIntent exitPI;

    Binder mBinder = new MyMusicBinder();
    MediaPlayer mediaPlayer = null;
    public static int current_music_position;
    MediaPlayer.OnCompletionListener listener;
    private boolean firstTime = true;
    SharedPreferences sharedPreferences;
    int position;
    NotificationCompat.Builder builder;

    public MusicService(){
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        listener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextMusic();
                createForegroundNotification();
            }
        };
    }

    private void setSessionToken(MediaSessionCompat.Token sessionToken) {
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called");

        previousPI = createPlaybackActionIntent(ACTION_PREVIOUS);
        playPI = createPlaybackActionIntent(ACTION_PLAY);
        nextPI = createPlaybackActionIntent(ACTION_NEXT);
        exitPI = createPlaybackActionIntent(ACTION_EXIT);


        if(intent!=null){
            String action = intent.getAction();
            switch (action){
                case "mainActivity":
                case "musicActivity":
                    break;
                case ACTION_PREVIOUS:
                    previousMusic();
                    break;
                case ACTION_PLAY:
                    play();
                    break;
                case ACTION_NEXT:
                    nextMusic();
                    break;
                case ACTION_EXIT:
                    Toast.makeText(this, "exit", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        createForegroundNotification();

        return START_STICKY;
    }

    public class MyMusicBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    private PendingIntent createPlaybackActionIntent(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void createForegroundNotification(){

        builder = new NotificationCompat.Builder(getApplicationContext(),MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(MainActivity.songs.get(current_music_position).getTitle())
                .setContentText(MainActivity.songs.get(current_music_position).getAlbum())
                .setContentIntent(contentIntent(getApplicationContext()))
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.previous,"previous",previousPI)
                .addAction(R.drawable.play,"play",playPI)
                .addAction(R.drawable.next,"next",nextPI)
                .addAction(R.drawable.exit,"exit",exitPI);

        //for notification title and text is in one line
        if(MainActivity.songs.size()>0){
            if(MainActivity.songs.get(current_music_position).getTitle().length()>40){
                builder.setContentTitle(MainActivity.songs.get(current_music_position).getTitle().substring(0,30));
            }else{
                builder.setContentTitle(MainActivity.songs.get(current_music_position).getTitle());
            }
            if(MainActivity.songs.get(current_music_position).getAlbum().length()>40){
                builder.setContentText(MainActivity.songs.get(current_music_position).getAlbum().substring(0,39));
            }else{
                builder.setContentText(MainActivity.songs.get(current_music_position).getAlbum());
            }
        }else{
            builder.setContentTitle("music");
        }
        startForeground(100,builder.build());
    }

    private static PendingIntent contentIntent(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context,PENDING_ID,intent,PendingIntent.FLAG_IMMUTABLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
        sharedPreferences.edit().putInt("position",current_music_position).apply();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    public int updateSeekbar(){
        return mediaPlayer.getCurrentPosition();
    }

    public void play(){
        if(firstTime){
            createMusic();
            firstTime = false;
        }
        else if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            sharedPreferences.edit().putBoolean("isPlaying",true).apply();
        }else{
            mediaPlayer.start();
            sharedPreferences.edit().putBoolean("isPlaying",false).apply();
        }

    }

    public void createMusic() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(MainActivity.songs.get(current_music_position).getPath()));
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        sharedPreferences.edit().putInt("position",current_music_position).apply();
    }

    public void nextMusic(){
        if(current_music_position==MainActivity.songs.size()-1){
            current_music_position = 0;
        }else{
            current_music_position++;
        }
        if(MainActivity.shuffle){
            current_music_position = Random.Default.nextInt(0,MainActivity.songs.size());
            Log.d(TAG, "nextMusic : "+ current_music_position);
        }
        createMusic();
        sharedPreferences.edit().putInt("position",current_music_position).apply();
    }

    public void previousMusic(){
        if(current_music_position==0){
            current_music_position = MainActivity.songs.size() -1;
        }else{
            current_music_position--;
        }
        createMusic();
        sharedPreferences.edit().putInt("position",current_music_position).apply();
    }
}
