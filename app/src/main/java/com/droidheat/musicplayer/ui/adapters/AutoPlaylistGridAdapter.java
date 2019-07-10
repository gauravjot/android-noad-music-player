package com.droidheat.musicplayer.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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

public class AutoPlaylistGridAdapter extends BaseAdapter {
    private Context context;
    LayoutInflater inflater;

    public AutoPlaylistGridAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    private static class ViewHolder {

        TextView text, subtext;
        ImageView image, btn;

    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View gridView = convertView;

        if (convertView == null) {

            holder = new ViewHolder();
            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.row_playlist_item, parent, false);


            holder.image = gridView.findViewById(R.id.image);

            holder.image.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 20F);
                }
            });
            holder.image.setClipToOutline(true);

            holder.text = gridView.findViewById(R.id.textView13);
            holder.subtext = gridView.findViewById(R.id.textView14);

            holder.btn = gridView.findViewById(R.id.imageView19);
            gridView.setTag(holder);

        } else {
            holder = (ViewHolder) gridView.getTag();
        }
        ArrayList<SongModel> list = new ArrayList<>();
        if (getCount() >= 1) {
            // set value into textView

            if (position == 0) {
                list.clear();
                list = (new SongsUtils(context)).mostPlayedSongs();
                holder.text.setText("Most Played");
                holder.subtext.setText(list.size() + " tracks");
            } else if (position == 1) {
                list.clear();
                list = (new SongsUtils(context)).favouriteSongs();
                holder.text.setText("Favorites");
                holder.subtext.setText(list.size() + " tracks");
            }

            final SongsUtils songsUtils = new SongsUtils(context);
            final PopupMenu pop = new PopupMenu(context, holder.btn);
            int[] j = new int[5];
            j[0] = R.id.play_musicUtils;
            j[1] = R.id.play_next_musicUtils;
            j[2] = R.id.shuffle_play_musicUtils;
            j[3] = R.id.add_to_queue_musicUtils;
            j[4] = R.id.add_to_playlist_musicUtils;
            songsUtils.generateMenu(pop, j);
            final ArrayList<SongModel> finalList = list;
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play_musicUtils:
                            songsUtils.play(0,finalList);
                            return true;
                        case R.id.play_next_musicUtils:
                            for (int i = finalList.size(); i > 0; i--) {
                                songsUtils.playNext(finalList.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            for (int i = finalList.size(); i > 0; i--) {
                                songsUtils.addToQueue(finalList.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_playlist_musicUtils:
                            songsUtils.addToPlaylist(finalList);
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsUtils.shufflePlay(finalList);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pop.show();
                }
            });

            gridView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GlobalDetailActivity.class);
                    if (position == 0) {
                        intent.putExtra("name", "Most Played");
                        intent.putExtra("field", "mostplayed");
                    } else {
                        intent.putExtra("name", "Favorites");
                        intent.putExtra("field", "favourites");
                    }
                    context.startActivity(intent);
                }
            });

            (new ImageUtils(context)).setAlbumArt(list, holder.image);
        }

        return gridView;
    }

    public int getCount() {
        return 2;
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
