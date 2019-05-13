package com.droidheat.musicplayer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public final TextView text, text1, duration;
    public final ImageView image;
    public final RelativeLayout rel;

    public ItemViewHolder(View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.text);
        text1 = itemView.findViewById(R.id.text1);
        duration = itemView.findViewById(R.id.textTime);
        image = itemView.findViewById(R.id.image);
        rel = itemView.findViewById(R.id.relBody);
    }

}