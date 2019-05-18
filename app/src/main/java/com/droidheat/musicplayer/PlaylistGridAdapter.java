package com.droidheat.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class PlaylistGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> mobileValues;
    LayoutInflater inflater;
    SongsManager songsManager;

    PlaylistGridAdapter(Context context) {
        this.context = context;
        songsManager = new SongsManager(context);
        this.mobileValues = songsManager.getAllPlaylists();
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mobileValues.clear();
        mobileValues = songsManager.getAllPlaylists();
    }

    private static class ViewHolder {

        TextView text, subtext;
        ImageView image, btn;

    }

    public View getView(final int position, final View convertView, ViewGroup parent) {
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
        if (getCount() >= 1) {
            holder.text.setText(mobileValues.get(position).get("title"));
            holder.subtext.setText(songsManager.playlistSongs(Integer.parseInt(
                    Objects.requireNonNull(mobileValues.get(position).get("ID")))).size() + " tracks");


            final PopupMenu pop = new PopupMenu(context, holder.btn);
            int[] j = new int[5];
            j[0] = R.id.play_musicUtils;
            j[1] = R.id.play_next_musicUtils;
            j[2] = R.id.shuffle_play_musicUtils;
            j[3] = R.id.add_to_queue_musicUtils;
            j[4] = R.id.remove_musicUtils;
            songsManager.generateMenu(pop, j);
            final ArrayList<SongModel> albumSongs;
            albumSongs = songsManager.playlistSongs(Integer.parseInt(
                    Objects.requireNonNull(mobileValues.get(position).get("ID"))));
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play_musicUtils:
                            songsManager.play(0, albumSongs);
                            return true;
                        case R.id.remove_musicUtils:
                            songsManager.deletePlaylist(Integer.parseInt(
                                    Objects.requireNonNull(mobileValues.get(position).get("ID"))));
                            mobileValues.clear();
                            mobileValues = songsManager.getAllPlaylists();
                            //Todo delete all songs which relates to this playlist
                            notifyDataSetChanged();
                            return true;
                        case R.id.play_next_musicUtils:
                            for (int i = albumSongs.size(); i > 0; i--) {
                                songsManager.playNext(albumSongs.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            songsManager.addToQueue(albumSongs);
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsManager.shufflePlay(albumSongs);
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
                    intent.putExtra("id", position);
                    intent.putExtra("name", mobileValues.get(position).get("title"));
                    intent.putExtra("field", mobileValues.get(position).get("ID"));
                    context.startActivity(intent);
                }
            });

            ArrayList<SongModel> PlayListSongs = songsManager.
                    playlistSongs(Integer.parseInt(
                            Objects.requireNonNull(mobileValues.get(position).get("ID"))));
            List<String> list = new ArrayList<>();
            for (int i = 0; i < PlayListSongs.size(); i++) {
                list.add(PlayListSongs.get(i).getAlbumID());
            }
            (new ImageUtils(context)).getImageByPicasso(list, holder.image, 0, list.size() - 1);
        }

        return gridView;
    }

    public int getCount() {
        if (mobileValues != null) {
            return mobileValues.size();
        }
        return 0;
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
