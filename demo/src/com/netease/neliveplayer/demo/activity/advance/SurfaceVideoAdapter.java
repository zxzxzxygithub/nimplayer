package com.netease.neliveplayer.demo.activity.advance;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.netease.neliveplayer.demo.R;
import com.netease.neliveplayer.playerkit.sdk.view.AdvanceSurfaceView;

public class SurfaceVideoAdapter extends RecyclerView.Adapter<SurfaceVideoAdapter.VideoViewHolder> {

    private Context context;


    public SurfaceVideoAdapter(Context context) {
        this.context = context;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.surface_item_layout, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        AdvanceSurfaceView videoView;
        FrameLayout rootView;

        public VideoViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.live_surface);
            rootView = itemView.findViewById(R.id.render_layout);
        }


    }


}