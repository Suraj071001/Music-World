package com.example.android.musicworld.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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

public class MusicActivity extends AppCompatActivity {
    public static final String TAG = "myTag";

    ActivityMusicBinding binding;
    public static int position = 0;
    MediaPlayer.OnCompletionListener listener;
    Boolean isActivityAlive;

    int saved_position=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_music);

        if(savedInstanceState!=null){
            saved_position = (int) savedInstanceState.get("current_song_position");
        }


        Objects.requireNonNull(getSupportActionBar()).hide();

        isActivityAlive = true;

        listener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(position<MainActivity.songs.size()-1){
                    position++;
                    MainActivity.current_music_position++;
                }
                else{ position =0;}
                createMusic();
            }
        };

        Intent intent = getIntent();
        position = intent.getIntExtra("position",0);
        String where = intent.getStringExtra("where");
        binding.seekBar.setMax(Math.toIntExact(MainActivity.songs.get(position).getDuration()));

        switch (where){
            case "list":
                createMusic();
                break;
            case "name":
                if(MainActivity.firstTime){
                    binding.btnPlayMusic.setImageResource(R.drawable.pause);
                    createMusic();
                    MainActivity.firstTime = false;
                }
                int seek_position = intent.getIntExtra("seek_position",0);
                if(MainActivity.mediaPlayer.isPlaying()){
                    binding.seekBar.setMax(Math.toIntExact(MainActivity.songs.get(position).getDuration()));
                    binding.seekBar.setProgress(seek_position);
                }
                updateMusicViews();
        }


        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MainActivity.mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.btnPreviousMusic.setOnClickListener(view -> {
            if(position == 0){
                position = MainActivity.songs.size() -1;
            }else{
                position--;
                MainActivity.current_music_position--;
            }
            createMusic();

        });

        binding.btnPlayMusic.setOnClickListener(view -> {
            if(MainActivity.mediaPlayer.isPlaying()){
                MainActivity.mediaPlayer.pause();
                binding.btnPlayMusic.setImageResource(R.drawable.play);
            }else{
                MainActivity.mediaPlayer.start();
                binding.btnPlayMusic.setImageResource(R.drawable.pause);
            }
        });

        binding.btnNextMusic.setOnClickListener(view -> {
            if(position<MainActivity.songs.size()-1){position++;
            MainActivity.current_music_position++;}
            else{ position =0;}
            createMusic();
        });
    }

    private void createMusic() {
        binding.seekBar.setProgress(0);
        MainActivity.mediaPlayer.reset();
        try {
            MainActivity.mediaPlayer.setDataSource(this,Uri.parse(MainActivity.songs.get(position).getPath()));
            MainActivity.mediaPlayer.prepare();
            MainActivity.mediaPlayer.setOnCompletionListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        binding.seekBar.setMax(Math.toIntExact(MainActivity.songs.get(position).getDuration()));
        MainActivity.mediaPlayer.start();
        if(isActivityAlive)
        updateMusicViews();
        MainActivity.mediaPlayer.seekTo(saved_position);
    }

    private void updateMusicViews(){
        binding.tvCurrentMusicName.setText(MainActivity.songs.get(position).getTitle());
        Uri artUri = MainActivity.songs.get(position).getArtUri();
        if(isActivityAlive){
            Glide.with(this)
                    .load(artUri)
                    .apply(RequestOptions.placeholderOf(R.drawable.music_icon).centerCrop())
                    .into(binding.imCurrentMusicImage);
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        isActivityAlive = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_song_position",MainActivity.mediaPlayer.getCurrentPosition());
    }

    private void updateSeekbar(){

    }
}