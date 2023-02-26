package com.example.android.musicworld.Activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.lang.UCharacter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.musicworld.Activity.adapter.MusicAdapter;
import com.example.android.musicworld.R;
import com.example.android.musicworld.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicAdapter.OnItemClickListener,ServiceConnection,SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "myTag";
    private static final String POSITION = "position";

    public static List<Music> songs = new ArrayList<>();MusicAdapter musicAdapter;
    public static MusicService musicService;
    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;

    // Boolean values
    public static Boolean firstTime = true;
    public Boolean bound = false;
    public static Boolean shuffle = false;

    Intent intent1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey("position")){
                int position = savedInstanceState.getInt("position",0);
                musicService.createMusic();
                musicService.mediaPlayer.seekTo(position);
            }
        }

        songs = getSongs();
        setRecyclerView();

        intent1 = new Intent(this,MusicService.class);
        intent1.setAction("mainActivity");
        bindService(intent1,this,BIND_AUTO_CREATE);

        binding.tvCurrentMusic.setOnClickListener(view -> {
            Intent intent = new Intent(this,MusicActivity.class);
            intent.putExtra("where","name");
            intent.putExtra("seek_position",musicService.mediaPlayer.getCurrentPosition());
            startActivity(intent);
        });

        binding.playBtn.setOnClickListener(view -> {
            if(songs.size()>0){
                playMusic();
                updateUi(musicService.current_music_position);
                startService(intent1);
            }
        });

        binding.nextBtn.setOnClickListener(view -> {
            nextMusic();
            updateUi(musicService.current_music_position);
            startService(intent1);
        });

        binding.previousBtn.setOnClickListener(view -> {
            previousMusic();
            updateUi(musicService.current_music_position);
            startService(intent1);
        });

        binding.shuffleBtn.setOnClickListener(view -> {
            if(shuffle){
                shuffle = false;
                Toast.makeText(musicService, "shuffle off", Toast.LENGTH_SHORT).show();
            }else{
                shuffle = true;
                Toast.makeText(musicService, "shuffle on", Toast.LENGTH_SHORT).show();
            }
        });

        binding.playlistBtn.setOnClickListener(view -> {
            Toast.makeText(musicService, "not implemented yet", Toast.LENGTH_SHORT).show();
        });

        binding.favouriteBtn.setOnClickListener(view -> {
            Toast.makeText(musicService, "not implemented yet", Toast.LENGTH_SHORT).show();
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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
        musicAdapter = new MusicAdapter(this,songs,this);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(musicAdapter);
    }

    @Override
    public void onClick(int position) {
        musicService.current_music_position = position;
        updateUi(position);
        startService(intent1);
        Intent intent = new Intent(MainActivity.this,MusicActivity.class);
        intent.putExtra(POSITION,musicService.current_music_position);
        intent.putExtra("where","list");
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void playMusic(){
        musicService.play();
    }

    private void nextMusic(){
        musicService.nextMusic();
    }

    private void previousMusic(){
        musicService.previousMusic();
    }

    private void updateUi(int current_songs_position){
        binding.setName(songs.get(current_songs_position).getTitle());
        Glide.with(this)
                .load(songs.get(current_songs_position).getArtUri())
                .apply(RequestOptions.placeholderOf(R.drawable.music_icon).centerCrop())
                .into(binding.currentMusicIcon);

        if(musicService.mediaPlayer.isPlaying()){
            binding.playBtn.setImageResource(R.drawable.pause);
        }else {
            binding.playBtn.setImageResource(R.drawable.play);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(songs!=null && songs.size()>0 && bound) updateUi(musicService.current_music_position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicService.mediaPlayer.release();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        unbindService(this);
        stopService(intent1);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: called");
        MusicService.MyMusicBinder mBinder = (MusicService.MyMusicBinder) iBinder;
        musicService = mBinder.getService();
        musicService.mediaPlayer = new MediaPlayer();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.contains("position") && songs.size()>0){
            musicService.current_music_position = sharedPreferences.getInt(POSITION,0);
            updateUi(musicService.current_music_position);
        }
        bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected: called");
        bound = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position",musicService.mediaPlayer.getCurrentPosition());
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("position")){
            updateUi(MusicService.current_music_position);
            MusicAdapter musicAdapter1 = new MusicAdapter(this,songs,this);
            binding.recyclerView.setAdapter(null);
            binding.recyclerView.setLayoutManager(null);
            binding.recyclerView.setAdapter(musicAdapter1);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            musicAdapter.notifyDataSetChanged();
        }

        else if(s.equals("isPlaying")){
            if(musicService.mediaPlayer.isPlaying()){
                binding.playBtn.setImageResource(R.drawable.pause);
            }else {
                binding.playBtn.setImageResource(R.drawable.play);
            }
        }
    }
}