package com.droidheat.musicplayer;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class ArtistGridAdapter extends BaseAdapter {
    private Context context;
    private final ArrayList<HashMap<String, String>> mobileValues;
    private LayoutInflater inflater;
    SongsManager songsManager;

    ArtistGridAdapter(Context context,
                      ArrayList<HashMap<String, String>> mobileValues) {
        this.context = context;
        this.mobileValues = mobileValues;
        songsManager = new SongsManager(context);
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
            // set value into textView
            holder.text.setText(mobileValues.get(position).get("artist"));
            int albums = songsManager.getAlbumIds(mobileValues.get(position).get("albums")).size();
            if (albums > 1) {
                holder.subtext.setText(albums + " Albums");
            } else {
                holder.subtext.setText(albums + " Album");
            }

            // set image based on selected text
            //imageView.setImageResource(R.drawable.windows_logo);

            final ArrayList<SongModel> artistSongs = songsManager.artistSongs(mobileValues.get(position).get("artist")
            );
//            List<String> list = new ArrayList<>();
//            for (int i = 0; i < artistSongs.size(); i++) {
//                list.add(artistSongs.get(i).getAlbumID());
//            }

            final PopupMenu pop = new PopupMenu(context, holder.imageOverflow);
            int[] j = new int[5];
            j[0] = R.id.play_musicUtils;
            j[1] = R.id.play_next_musicUtils;
            j[2] = R.id.shuffle_play_musicUtils;
            j[3] = R.id.add_to_queue_musicUtils;
            j[4] = R.id.add_to_playlist_musicUtils;
            songsManager.generateMenu(pop, j);
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play_musicUtils:
                            songsManager.play(0, artistSongs);
                            return true;
                        case R.id.play_next_musicUtils:
                            for (int i = artistSongs.size(); i > 0; i--) {
                                songsManager.playNext(artistSongs.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            for (int i = artistSongs.size(); i > 0; i--) {
                                songsManager.addToQueue(artistSongs.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_playlist_musicUtils:
                            songsManager.addToPlaylist(artistSongs);
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsManager.shufflePlay(artistSongs);
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

//    private void getAlbumCover(final List songsList, final ImageView imageView, final int i, final int max) {
//        if (i < max) {
//            Uri sArtworkUri = Uri
//                    .parse("content://media/external/audio/albumart");
//            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(songsList.get(i).toString()));
//            Picasso.with(context).load(uri).placeholder(R.drawable.unknown_artist)
//                    .into(imageView, new Callback() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//
//                        @Override
//                        public void onError() {
//                            getAlbumCover(songsList, imageView, i + 1, max);
//                        }
//                    });
//        } else if (i == max) {
//            Uri sArtworkUri = Uri
//                    .parse("content://media/external/audio/albumart");
//            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(songsList.get(i).toString()));
//            Picasso.with(context).load(uri)
//                    .placeholder(R.drawable.unknown_artist).into(imageView);
//        }
//    }

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
