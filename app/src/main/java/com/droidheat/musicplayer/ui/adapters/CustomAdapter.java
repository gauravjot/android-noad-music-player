package com.droidheat.musicplayer.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidheat.musicplayer.ui.activities.GlobalDetailActivity;
import com.droidheat.musicplayer.utils.ImageUtils;
import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.models.SongModel;
import com.droidheat.musicplayer.utils.SongsUtils;

import java.util.ArrayList;
import java.util.Locale;

public class CustomAdapter extends BaseAdapter implements OnClickListener {

    private Activity activity;
    private ArrayList<SongModel> data;
    private ArrayList<SongModel> virginArrayList;

    public CustomAdapter(Activity a, ArrayList<SongModel> d) {
        activity = a;
        data = new ArrayList<>(d);
        virginArrayList = new ArrayList<>(d);
    }

    /**
     * ***** What is the size of Passed Arraylist Size ***********
     */
    public int getCount() {

        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * ****** Create a holder Class to contain inflated xml file elements ********
     */
    private static class ViewHolder {

        TextView text;
        TextView text1;
        ImageView image, imageOverflow;

    }


    /**
     * *** Depends upon data size called for each row , Create each ListView row ****
     */
    @SuppressLint("SimpleDateFormat")
    public View getView(final int position, View vi, ViewGroup parent) {
        final ViewHolder holder;

        if (vi == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.row, parent, false);

            holder = new ViewHolder();
            holder.text = vi.findViewById(R.id.text);
            holder.text1 = vi.findViewById(R.id.text1);
            holder.image = vi.findViewById(R.id.image);
            holder.imageOverflow = vi.findViewById(R.id.albumArtImageView);
            vi.setTag(holder);

        } else {
            holder = (ViewHolder) vi.getTag();
        }


        if (data.size() > 0) {

            /***** Get each Model object from Arraylist ********/
            final SongModel tempValues = data.get(position);
            final String duration = tempValues.getDuration();
            final String artist = tempValues.getArtist();
            final String songName = tempValues.getFileName();
            final String title = tempValues.getTitle();

            String finalTitle;
            if (title != null) {
                finalTitle = title;
            } else {
                finalTitle = songName;
            }

            holder.image.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 20F);
                }
            });
            holder.image.setClipToOutline(true);


            final SongsUtils songsUtils = new SongsUtils(activity);
            (new ImageUtils(activity)).setAlbumArt(tempValues.getAlbumID(), holder.image);

            holder.text.setText(finalTitle);
            holder.text1.setText(String.format("%s • %s", (artist.length() > 25) ? artist.substring(0, 25) +"…" : artist, duration));

            vi.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    songsUtils.play(position, data);
                }
            });
            final PopupMenu pop = new PopupMenu(activity, holder.imageOverflow);
            int[] j = new int[7];
            j[0] = R.id.play_next_musicUtils;
            j[1] = R.id.shuffle_play_musicUtils;
            j[2] = R.id.add_to_queue_musicUtils;
            j[3] = R.id.add_to_playlist_musicUtils;
            j[4] = R.id.goto_album_musicUtils;
            j[5] = R.id.goto_artist_musicUtils;
            j[6] = R.id.info_musicUtils;
            songsUtils.generateMenu(pop, j);
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play_next_musicUtils:
                            songsUtils.playNext(data.get(position));
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            songsUtils.addToQueue(data.get(position));
                            return true;
                        case R.id.add_to_playlist_musicUtils:
                            songsUtils.addToPlaylist(data.get(position));
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsUtils.shufflePlay(position, data);
                            return true;
                        case R.id.goto_album_musicUtils:
                            Intent intent = new Intent(activity, GlobalDetailActivity.class);
                            intent.putExtra("name", tempValues.getAlbum());
                            intent.putExtra("field", "albums");
                            activity.startActivity(intent);
                            return true;
                        case R.id.goto_artist_musicUtils:
                            Intent intent1 = new Intent(activity, GlobalDetailActivity.class);
                            intent1.putExtra("name", tempValues.getArtist());
                            intent1.putExtra("field", "artists");
                            activity.startActivity(intent1);
                            return true;
                        case R.id.info_musicUtils:
                            songsUtils.info(data.get(position)).show();
                            return true;
                        default:
                            return false;
                    }
                }
            });


            holder.imageOverflow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    pop.show();
                }
            });
            vi.setVisibility(View.VISIBLE);
        } else {
            vi.setVisibility(View.INVISIBLE);
        }
        return vi;
    }


    @Override
    public void onClick(View arg0) {
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        data.clear();
        if (charText.length() == 0) {
            data.addAll(virginArrayList);
        } else {
            for (SongModel wp : virginArrayList) {
                if (wp.getTitle().toLowerCase(Locale.getDefault())
                        .contains(charText) || wp.getArtist().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    data.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}
