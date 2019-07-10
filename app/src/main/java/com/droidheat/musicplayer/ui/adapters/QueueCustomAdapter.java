package com.droidheat.musicplayer.ui.adapters;


import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Outline;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Toast;

import com.droidheat.musicplayer.ui.ItemViewHolder;
import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.utils.SharedPrefsUtils;
import com.droidheat.musicplayer.models.SongModel;
import com.droidheat.musicplayer.utils.SongsUtils;
import com.droidheat.musicplayer.ui.activities.PlayActivity;
import com.droidheat.musicplayer.utils.CommonUtils;
import com.droidheat.musicplayer.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Collections;

public class QueueCustomAdapter extends RecyclerView.Adapter<ItemViewHolder> {


    /**
     * ******** Declare Used Variables ********
     */
    private Activity activity;
    public Resources res;
    SongsUtils songsUtils;
    private SharedPrefsUtils sharedPrefsUtils;
    private ArrayList<SongModel> arrayList = new ArrayList<>();

    public void notifyAdapterDataSetChanged() {
        arrayList.clear();
        arrayList.addAll(songsUtils.queue());
        notifyItemRangeChanged(0,arrayList.size());
        //notifyDataSetChanged();
    }

    /**
     * ********** CustomAdapter Constructor ****************
     */
    @SuppressWarnings("rawtypes")
    public QueueCustomAdapter(Activity a, Resources resLocal) {

        /*
        * Take passed values
        */
        activity = a;
        res = resLocal;
        songsUtils = new SongsUtils(a);
        sharedPrefsUtils = new SharedPrefsUtils(activity);

        arrayList.addAll(songsUtils.queue());

    }

    /**
     * ***** What is the size of Passed ArrayList Size ***********
     */
    @Override
    public int getItemCount() {
        return arrayList.size();
    }



    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_black, parent, false);
        return new ItemViewHolder(view);

    }


    /**
     * *** Depends upon data size called for each row , Create each ListView row ****
     */
    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {

        /*
        * Get each Model object from ArrayList
        */
        SongModel tempValues = getItem(position);
        final String duration, artist, songName, title, finalTitle;
        String finalTitle1;
        duration = tempValues.getDuration();
        artist = tempValues.getArtist();
        songName = tempValues.getFileName();
        title = tempValues.getTitle();

        finalTitle1 = songName;
        if (title != null) {
            finalTitle1 = title;
        }
        finalTitle = finalTitle1;

        (new ImageUtils(activity)).setAlbumArt(tempValues.getAlbumID(), holder.image);

        holder.image.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 20F);
            }
        });
        holder.image.setClipToOutline(true);

        holder.text.setText(finalTitle);
        holder.text1.setText(artist);

        holder.duration.setText(duration);

        if (position == sharedPrefsUtils.readSharedPrefsInt("musicID",0)) {
            holder.text.setTextColor(ContextCompat.getColor(activity,(new CommonUtils(activity)).accentColor(new SharedPrefsUtils(activity))));
        } else {
            holder.text.setTextColor(ContextCompat.getColor(activity,R.color.textColorTitle));
        }

        holder.rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayActivity sct = (PlayActivity) activity;
                sct.onItemClick(holder.getAdapterPosition());
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        notifyDataSetChanged();
                    }
                }, 300);
            }
        });

    }

    private SongModel getItem(int position) {
        return arrayList.get(position);
    }

    public void onItemMove(int i, int i1) {
        int musicID = sharedPrefsUtils.readSharedPrefsInt("musicID",0);
        if (musicID == i && sharedPrefsUtils.readSharedPrefsString("raw_path",null).equals(arrayList.get(i).getPath())) {
            sharedPrefsUtils.writeSharedPrefs("musicID",i1);
        } else if (musicID >= i1 && musicID < i) {
            sharedPrefsUtils.writeSharedPrefs("musicID",musicID + 1);
        } else if (musicID <= i1 && musicID > i) {
            sharedPrefsUtils.writeSharedPrefs("musicID",musicID - 1);
        }

        Collections.swap(arrayList,i,i1);
        notifyItemMoved(i,i1);

        songsUtils.replaceQueue(arrayList);
        callback.viewPagerRefresh();
    }

    public void onItemDismiss(int adapterPosition) {
        int musicID = sharedPrefsUtils.readSharedPrefsInt("musicID",0);
        if (sharedPrefsUtils.readSharedPrefsInt("musicID",0) == adapterPosition &&
                sharedPrefsUtils.readSharedPrefsString("raw_path",null).equals(arrayList.get(adapterPosition).getPath())) {
            Toast.makeText(activity,"Cannot remove now playing song.",Toast.LENGTH_SHORT).show();
            notifyDataSetChanged();
        } else {
            arrayList.remove(adapterPosition);
            songsUtils.replaceQueue(arrayList);
            notifyItemRemoved(adapterPosition);
            if (adapterPosition < sharedPrefsUtils.readSharedPrefsInt("musicID",0)) {
                sharedPrefsUtils.writeSharedPrefs("musicID", musicID - 1);
            }
            callback.viewPagerRefresh();
        }
    }


    public interface MyFragmentCallback {
        void viewPagerRefresh();
    }

    private MyFragmentCallback callback;

    public void setMyFragmentCallback(MyFragmentCallback mCallback) {
        this.callback = mCallback;
    }

}



