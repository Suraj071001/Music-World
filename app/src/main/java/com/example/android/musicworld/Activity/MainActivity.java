package com.example.android.musicworld.Activity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.musicworld.Activity.adapter.MusicAdapter;
import com.example.android.musicworld.R;
import com.example.android.musicworld.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicAdapter.OnItemClickListener {
    private static final String TAG = "myTag";

    public static List<Music> songs = new ArrayList<>();
    MediaPlayer.OnCompletionListener completionListener;
    ActivityMainBinding binding;

    public static MediaPlayer mediaPlayer = null;
    public static int current_music_position = 0;

    public static Boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        songs = getSongs();

        completionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextMusic();
            }
        };

        setRecyclerView();
        mediaPlayer = new MediaPlayer();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.contains("position")){
            current_music_position = sharedPreferences.getInt("position",1);
            updateUi(current_music_position);
        }


        binding.tvCurrentMusic.setOnClickListener(view -> {
            Intent intent = new Intent(this,MusicActivity.class);
            intent.putExtra("position",current_music_position);
            intent.putExtra("where","name");
            intent.putExtra("seek_position",mediaPlayer.getCurrentPosition());
            startActivity(intent);
        });

        binding.playBtn.setOnClickListener(view -> {
            playMusic(current_music_position);
        });

        binding.nextBtn.setOnClickListener(view -> {
            nextMusic();
        });

        binding.previousBtn.setOnClickListener(view -> {
            previousMusic();
        });
    }


    private List<Music> getSongs(){
        List<Music> songs = new ArrayList<>();

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String [] projection = new String[]{MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.ALBUM_ID};
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection, null, MediaStore.Audio.Media.DATE_ADDED +" DESC");

        if(cursor!= null){
            if(cursor.moveToFirst())
            while(cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artistC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String albumC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                Long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                long albumIdC = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri uri = Uri.parse("content://media/external/audio/albumart");
                Uri artUri = Uri.withAppendedPath(uri, Long.toString(albumIdC));


                Music music = new Music(titleC,artistC,albumC,pathC,id,duration,artUri);
                File file = new File(pathC);
                if(file.exists()){
                    songs.add(music);
                }
            }
            cursor.close();
        }

        return songs;
    }

    private void setRecyclerView() {
        MusicAdapter musicAdapter = new MusicAdapter(this,songs,this);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(musicAdapter);
    }

    @Override
    public void onClick(int position) {
        current_music_position = position;
        updateUi(current_music_position);
        Intent intent = new Intent(MainActivity.this,MusicActivity.class);
        intent.putExtra("position",position);
        intent.putExtra("where","list");
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putInt("position",current_music_position).apply();
    }

    private void playMusic(int position){
        if(firstTime){
            createMusic();
            binding.playBtn.setImageResource(R.drawable.pause);
            firstTime = false;
        }
        else if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            binding.playBtn.setImageResource(R.drawable.play);
        }else{
            mediaPlayer.start();
            binding.playBtn.setImageResource(R.drawable.pause);
        }
    }

    private void nextMusic(){
        if(current_music_position==songs.size()-1){
            current_music_position = 0;
        }else{
            current_music_position++;
        }
        createMusic();
    }

    private void previousMusic(){
        if(current_music_position==0){
            current_music_position = songs.size() -1;
        }else{
            current_music_position--;
        }
        createMusic();
    }

    private void updateUi(int current_songs_position){
        binding.setName(songs.get(current_songs_position).getTitle());
        Glide.with(this)
                .load(songs.get(current_songs_position).getArtUri())
                .apply(RequestOptions.placeholderOf(R.drawable.music_icon).centerCrop())
                .into(binding.currentMusicIcon);
        if(mediaPlayer.isPlaying()){
            binding.playBtn.setImageResource(R.drawable.pause);
        }else {
            binding.playBtn.setImageResource(R.drawable.play);
        }
    }

    public void createMusic() {
        MainActivity.mediaPlayer.reset();
        try {
            MainActivity.mediaPlayer.setDataSource(this,Uri.parse(MainActivity.songs.get(current_music_position).getPath()));
            MainActivity.mediaPlayer.prepare();
            MainActivity.mediaPlayer.setOnCompletionListener(completionListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MainActivity.mediaPlayer.start();
        updateUi(current_music_position);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(songs!=null && songs.size()>0) updateUi(current_music_position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}