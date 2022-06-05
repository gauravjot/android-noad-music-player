package com.droidheat.musicplayer.ui.fragments;

import android.content.Context;
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.models.SongModel;
import com.droidheat.musicplayer.utils.SongsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

class ArtistGridAdapter extends BaseAdapter {
    private Context context;
    private final ArrayList<HashMap<String, String>> mobileValues;
    private LayoutInflater inflater;
    SongsUtils songsUtils;

    ArtistGridAdapter(Context context,
                      ArrayList<HashMap<String, String>> mobileValues) {
        this.context = context;
        this.mobileValues = mobileValues;
        songsUtils = new SongsUtils(context);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    private static class ViewHolder {

        TextView text, subtext;
        ImageView imageOverflow;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View gridView = convertView;

        if (convertView == null) {

            holder = new ViewHolder();
            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.row_artist_item, parent, false);
            holder.text = gridView.findViewById(R.id.textView13);
            holder.subtext = gridView.findViewById(R.id.textView14);
            holder.imageOverflow = gridView.findViewById(R.id.imageView19);
            gridView.setTag(holder);

        } else {
            holder = (ViewHolder) gridView.getTag();
        }

        if (getCount() >= 1) {
            final ArrayList<SongModel> artistSongs = songsUtils.artistSongs(mobileValues.get(position).get("artist")
            );
            // set value into textView
            holder.text.setText(mobileValues.get(position).get("artist"));
            int albums = songsUtils.getAlbumIds(Objects.requireNonNull(mobileValues.get(position).get("albums"))).size();
            holder.subtext.setText(String.format("%d album%s • %d song%s", albums, (albums > 1) ? "s" : "", artistSongs.size(), (albums > 1) ? "s" : ""));

            final PopupMenu pop = new PopupMenu(context, holder.imageOverflow);
            int[] j = new int[5];
            j[0] = R.id.play_musicUtils;
            j[1] = R.id.play_next_musicUtils;
            j[2] = R.id.shuffle_play_musicUtils;
            j[3] = R.id.add_to_queue_musicUtils;
            j[4] = R.id.add_to_playlist_musicUtils;
            songsUtils.generateMenu(pop, j);
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play_musicUtils:
                            songsUtils.play(0, artistSongs);
                            return true;
                        case R.id.play_next_musicUtils:
                            for (int i = artistSongs.size(); i > 0; i--) {
                                songsUtils.playNext(artistSongs.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            for (int i = artistSongs.size(); i > 0; i--) {
                                songsUtils.addToQueue(artistSongs.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_playlist_musicUtils:
                            songsUtils.addToPlaylist(artistSongs);
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsUtils.shufflePlay(artistSongs);
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

        }

        return gridView;
    }

    @Override
    public int getCount() {
        return mobileValues.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


}
