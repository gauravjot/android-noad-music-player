package com.droidheat.musicplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

class AdapterFiveRecentlyAdded extends BaseAdapter implements OnClickListener {

    private Activity activity;
    private ArrayList<SongModel> data;
    SongsManager songsManager;

    AdapterFiveRecentlyAdded(Activity a) {

        songsManager = new SongsManager(a);
        activity = a;
        data = new ArrayList<>(getData());
    }

    private ArrayList<SongModel> getData() {
        ArrayList<SongModel> newSongs = songsManager.newSongs();
        ArrayList<SongModel> list = new ArrayList<>();
        int items = 25;
        if (newSongs.size()> items) {
            for (int i = 0; i < items; i++) {
                list.add(newSongs.get(i));
            }
        } else {
            list.addAll(newSongs);
        }
        return list;
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
            vi = inflater.inflate(R.layout.row_small, parent, false);

            holder = new ViewHolder();
            holder.text = vi.findViewById(R.id.text);
            holder.text1 = vi.findViewById(R.id.text1);
            holder.image = vi.findViewById(R.id.image);
            holder.imageOverflow = vi.findViewById(R.id.imageView1);
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

            (new ImageUtils(activity)).getImageByPicasso(tempValues.getAlbumID(), holder.image);

            holder.text.setText(finalTitle);
            holder.text1.setText(((artist.length() > 25) ? artist.substring(0,25) : artist) + "; " + duration);

            vi.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    songsManager.play(position, songsManager.newSongs());
                }
            });
            final PopupMenu pop = new PopupMenu(activity, holder.imageOverflow);
            int[] j = new int[6];
            j[0] = R.id.play_next_musicUtils;
            j[1] = R.id.shuffle_play_musicUtils;
            j[2] = R.id.add_to_queue_musicUtils;
            j[3] = R.id.add_to_playlist_musicUtils;
            j[4] = R.id.goto_album_musicUtils;
            j[5] = R.id.goto_artist_musicUtils;
            songsManager.generateMenu(pop, j);
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.delete_musicUtils:
                            return true;
                        case R.id.play_next_musicUtils:
                            songsManager.playNext(data.get(position));
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            songsManager.addToQueue(data.get(position));
                            return true;
                        case R.id.add_to_playlist_musicUtils:
                            songsManager.addToPlaylist(data.get(position));
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsManager.shufflePlay(position, songsManager.newSongs());
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
                        default:
                            return false;
                    }
                }
            });


            holder.imageOverflow.setOnClickListener(new OnClickListener() {

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


}
