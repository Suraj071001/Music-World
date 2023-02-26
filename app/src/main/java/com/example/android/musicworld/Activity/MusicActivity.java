package com.example.android.musicworld.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.musicworld.R;
import com.example.android.musicworld.databinding.ActivityMusicBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MusicActivity extends AppCompatActivity implements ServiceConnection,SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = "myTag";

    ActivityMusicBinding binding;
    Boolean isActivityAlive;
    MusicService musicService;
    Intent intent1;
    SharedPreferences sharedPreferences;

    int saved_position=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_music);
        Objects.requireNonNull(getSupportActionBar()).hide();

        if(savedInstanceState!=null){
            saved_position = (int) savedInstanceState.get("current_song_position");
        }

        isActivityAlive = true;

        intent1 = new Intent(this,MusicService.class);
        intent1.setAction("MusicActivity");
        bindService(intent1,this,BIND_AUTO_CREATE);

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MainActivity.musicService.mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.btnPreviousMusic.setOnClickListener(view -> {
            musicService.previousMusic();
            updateMusicViews();
            binding.btnPlayMusic.setImageResource(R.drawable.pause);
            startService(intent1);
            binding.seekBar.setProgress(0);
        });

        binding.btnPlayMusic.setOnClickListener(view -> {
            musicService.play();
            updateMusicViews();
            startService(intent1);
            if(musicService.mediaPlayer.isPlaying()){
                binding.btnPlayMusic.setImageResource(R.drawable.pause);
            }else{
                binding.btnPlayMusic.setImageResource(R.drawable.play);
            }
        });

        binding.btnNextMusic.setOnClickListener(view -> {
            musicService.nextMusic();
            updateMusicViews();
            binding.btnPlayMusic.setImageResource(R.drawable.pause);
            startService(intent1);
            binding.seekBar.setProgress(0);
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }


    private void updateMusicViews(){
        binding.tvCurrentMusicName.setText(MainActivity.songs.get(musicService.current_music_position).getTitle());
        Uri artUri = MainActivity.songs.get(musicService.current_music_position).getArtUri();
        if(isActivityAlive){
            Glide.with(this)
                    .load(artUri)
                    .apply(RequestOptions.placeholderOf(R.drawable.music_icon).centerCrop())
                    .into(binding.imCurrentMusicImage);
        }
        binding.maxDuration.setText(MainActivity.songs.get(musicService.current_music_position).duration());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        isActivityAlive = false;
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_song_position",MainActivity.musicService.mediaPlayer.getCurrentPosition());
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyMusicBinder mBinder = (MusicService.MyMusicBinder) iBinder;
        musicService = mBinder.getService();

        Intent intent;
        intent = getIntent();
        String where = intent.getStringExtra("where");
        binding.seekBar.setMax(Math.toIntExact(MainActivity.songs.get(musicService.current_music_position).getDuration()));
        switch (where){
            case "list":
                musicService.createMusic();
                break;
            case "name":
                if(MainActivity.firstTime){
                    binding.btnPlayMusic.setImageResource(R.drawable.pause);
                    musicService.createMusic();
                    MainActivity.firstTime = false;
                }
                int seek_position = intent.getIntExtra("seek_position",0);
                if(MainActivity.musicService.mediaPlayer.isPlaying()){
                    binding.seekBar.setMax(Math.toIntExact(MainActivity.songs.get(musicService.current_music_position).getDuration()));
                    binding.seekBar.setProgress(seek_position);
                }
        }
        updateMusicViews();
    }

    private void runSeekbar(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int currentPosition = musicService.mediaPlayer.getCurrentPosition();
                int total = musicService.mediaPlayer.getDuration();


                while (currentPosition < total) {
                    try {
                        Thread.sleep(1000);
                        currentPosition = musicService.mediaPlayer.getCurrentPosition();
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        return;
                    }

                    binding.seekBar.setProgress(currentPosition);

                }
            }
        }).start();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("position")){
            updateMusicViews();
            binding.seekBar.setMax(Math.toIntExact(MainActivity.songs.get(musicService.current_music_position).getDuration()));
        }
        else if(s.equals("isPlaying")){
            if(musicService.mediaPlayer.isPlaying()){
                binding.btnPlayMusic.setImageResource(R.drawable.pause);
            }else {
                binding.btnPlayMusic.setImageResource(R.drawable.play);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public String duration(int dur){
        long duration = dur;
        int totalSeconds = (int) (duration/1000);
        int min = totalSeconds/60;
        int sec = totalSeconds%60;
        return String.format("%02d:%02d",min,sec);
    }
}