package com.droidheat.musicplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.models.SongModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageUtils {

    private Context context;
    public ImageUtils(Context context) {
        this.context = context;
    }

    /*
     * @params: imageView is the ImageView where image should go
     * @params: arrayList is list of SongModel object with each having getAlbumID()
     * @params: albumIds is List<String> of album ids
     *
     * Three ways to grab album art -
     * 1. setAlbumArt(String albumId, ImageView imageView)
     * 2. setAlbumArt(ArrayList<SongModel> arrayList, ImageView imageView)
     * 3. setAlbumArt(final List albumIds, final ImageView imageView)
     *
     * Also grab Bitmap with - setAlbumArt(Long albumId) where albumId is a long variable
     * (converted from string in this app)
     */

    public void setAlbumArt(String albumId, ImageView imageView) {
        try {
            Picasso.get().load(getSongUri(Long.parseLong(albumId)))
                    .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                    .centerCrop()
                    .resize(400,400)
                    .onlyScaleDown()
                    .into(imageView);}
        catch (Exception ignored) {}
    }

    public void setAlbumArt(ArrayList<SongModel> arrayList, ImageView imageView) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            list.add(arrayList.get(i).getAlbumID());
            if (i == 20) {break; } // 20 should be enough, remove this line if you want to query whole list
        }
        setAlbumArt(list, imageView, 0, list.size() - 1);
    }

    void setAlbumArt(final List albumIds, final ImageView imageView) {
        try {
            final int i = 0;
            final int max = albumIds.size()-1;
            if (i < max) {
                Picasso.get().load(getSongUri(Long.parseLong(albumIds.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                        .centerCrop()
                        .resize(400,400)
                        .onlyScaleDown()
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                setAlbumArt(albumIds, imageView, i + 1, max);
                            }
                        });
            }
            else {
                Picasso.get().load(getSongUri(Long.parseLong(albumIds.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp))).into(imageView);
            }}
        catch (Exception ignored) {}
    }

//    void getSmallImageByPicasso(String albumID, ImageView imageView) {
//        try {
//            Picasso.get().load(getSongUri(Long.parseLong(albumID)))
//                    .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
//                    .resize(400,400)
//                    .onlyScaleDown()
//                    .into(imageView);}
//        catch (Exception ignored) {}
//    }

    public void getFullImageByPicasso(String albumID, ImageView imageView) {
        try {
            Picasso.get().load(getSongUri(Long.parseLong(albumID)))
                    .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                    .into(imageView);}
        catch (Exception ignored) {}
    }

    void getFullImageByPicasso(final List albumIds, final ImageView imageView) {
        try {
            final int i = 0;
            final int max = albumIds.size()-1;
            if (i < max) {
                Picasso.get().load(getSongUri(Long.parseLong(albumIds.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                setAlbumArt(albumIds, imageView, i + 1, max);
                            }
                        });
            }
            else {
                Picasso.get().load(getSongUri(Long.parseLong(albumIds.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp))).into(imageView);
            }}
        catch (Exception ignored) {}
    }

    public Bitmap getAlbumArt(Long albumId) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
            }
        } catch (Error ee) {
            ee.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != albumArtBitMap) {
            return albumArtBitMap;
        }
        return getDefaultAlbumArtEfficiently();
    }

    private void getFullImageByPicasso(final List albumSongs, final ImageView imageView, final int i, final int max) {
        try {
            if (i < max) Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                    .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            getFullImageByPicasso(albumSongs, imageView, i + 1, max);
                        }
                    });
            else if (i == max) {
                Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp))).into(imageView);
            }}
        catch (Exception ignored) {}
    }

    private void setAlbumArt(final List albumSongs, final ImageView imageView, final int i, final int max) {
        try {
            if (i < max) Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                    .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp)))
                    .centerCrop()
                    .resize(400,400)
                    .onlyScaleDown()
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            setAlbumArt(albumSongs, imageView, i + 1, max);
                        }
                    });
            else if (i == max) {
                Picasso.get().load(getSongUri(Long.parseLong(albumSongs.get(i).toString())))
                        .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp))).into(imageView);
            }}
        catch (Exception ignored) {}
    }

    private Bitmap getDefaultAlbumArtEfficiently() {

        return BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_music_notes_padded);
    }

    private Uri getSongUri(Long albumID) {
        return ContentUris.withAppendedId(Uri
                .parse("content://media/external/audio/albumart"), albumID);
    }
}
