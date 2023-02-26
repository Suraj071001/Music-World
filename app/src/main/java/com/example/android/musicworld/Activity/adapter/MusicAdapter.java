package com.example.android.musicworld.Activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.musicworld.Activity.Music;
import com.example.android.musicworld.Activity.MusicService;
import com.example.android.musicworld.R;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyHolder> {

    List<Music> song_list = new ArrayList<>();
    Context context;
    OnItemClickListener listener;

    private static final int MUSIC_VIEW = 0;
    private static final int CURRENT_MUSIC_VIEW = 1;

    public MusicAdapter(Context context,List<Music> song_list,OnItemClickListener listener){
        this.context = context;
        this.song_list = song_list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layoutId = 0;
        switch (viewType){
            case MUSIC_VIEW:
                layoutId = R.layout.music_item;
                break;
            case CURRENT_MUSIC_VIEW:
                layoutId = R.layout.current_music_item;
                break;
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.MyHolder holder, int position) {
        String title = song_list.get(position).getTitle();
        String album = song_list.get(position).getAlbum();
        String duration = song_list.get(position).duration();

        holder.tv_artist.setText(album);
        holder.tv_title.setText(title);
        holder.tv_duration.setText(duration);

        Glide.with(context)
                        .load(song_list.get(position).getArtUri())
                .apply(RequestOptions.placeholderOf(R.drawable.music_icon).centerCrop())
                                .into(holder.music_icon);

        holder.constraintLayout.setOnClickListener(view -> {
            listener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return song_list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        TextView tv_title,tv_artist,tv_duration;
        ConstraintLayout constraintLayout;
        ImageView music_icon;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.music_title);
            tv_artist = itemView.findViewById(R.id.artist);
            tv_duration = itemView.findViewById(R.id.duration);
            music_icon = itemView.findViewById(R.id.music_image);

            constraintLayout = itemView.findViewById(R.id.constraint_layout);

        }
    }

    public interface OnItemClickListener{
        void onClick(int position);
    }

    @Override
    public int getItemViewType(int position) {
        if(position== MusicService.current_music_position){
            return CURRENT_MUSIC_VIEW;
        }else{
            return MUSIC_VIEW;
        }
    }
}
