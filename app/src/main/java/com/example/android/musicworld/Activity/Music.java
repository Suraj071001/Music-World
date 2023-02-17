package com.example.android.musicworld.Activity;

import android.net.Uri;

public class Music {
    String title;
    String artist;
    String album;
    String path;
    String id;
    Long duration;
    Uri artUri;

    public Music(String title,String artist,String album,String path,String id, Long duration,Uri artUri){
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.id = id;
        this.artUri = artUri;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Uri getArtUri() {
        return artUri;
    }

    public void setArtUri(Uri artUri) {
        this.artUri = artUri;
    }

    public String duration(){
        long duration = getDuration();
        int totalSeconds = (int) (duration/1000);
        int min = totalSeconds/60;
        int sec = totalSeconds%60;
        return String.format("%02d:%02d",min,sec);
    }
}
