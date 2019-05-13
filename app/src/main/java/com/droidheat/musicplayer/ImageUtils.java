package com.droidheat.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

class ImageUtils {

    private Context context;
    ImageUtils(Context context) {
        this.context = context;
    }
     /*
    *
    *
    *
      Image Methods **************************************************************************
      *
      *
      *
     */

    private Uri getSongUri(Long albumID) {
        return ContentUris.withAppendedId(Uri
                .parse("content://media/external/audio/albumart"), albumID);
    }

    void getImageByPicasso(String albumID, ImageView imageView) {
        try {
            Picasso.get().load(getSongUri(Long.parseLong(albumID)))
                    .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                    .into(imageView);}
        catch (Exception ignored) {}
    }

    void getImageByPicasso(final List albumSongs, final ImageView imageView, final int i, final int max) {
        try {
            if (i < max) Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                    .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            getImageByPicasso(albumSongs, imageView, i + 1, max);
                        }
                    });
            else if (i == max) {
                Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp))).into(imageView);
            }}
        catch (Exception ignored) {}
    }

    void getImageByPicasso(final List albumSongs, final ImageView imageView) {
        try {
            final int i = 0;
            final int max = albumSongs.size()-1;
            if (i < max) {
                Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                getImageByPicasso(albumSongs, imageView, i + 1, max);
                            }
                        });
            }
            else {
                Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp))).into(imageView);
            }}
        catch (Exception ignored) {}
    }
}
