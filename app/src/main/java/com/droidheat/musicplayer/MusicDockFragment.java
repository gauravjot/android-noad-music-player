package com.droidheat.musicplayer;

import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class MusicDockFragment extends Fragment {

    TextView title;
    TextView artist;
    ImageView btnPlay;
    ImageView albumArt;
    Handler seekHandler = new Handler();
    int currentTitleArtist;
    SongsManager songsManager;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_dockv2, container,
                false);

        title = view.findViewById(R.id.XtextView1);
        artist = view.findViewById(R.id.XtextView2);
        btnPlay = view.findViewById(R.id.XbtnPlay);
        albumArt = view.findViewById(R.id.albumArt);
        final Button btnPlayActivity = view.findViewById(R.id.Xbutton1);
        title.setSelected(true);

        albumArt.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 20F);
            }
        });
        albumArt.setClipToOutline(true);

        songsManager = new SongsManager(getActivity());
        final SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(getContext());

        btnPlayActivity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                touchDock();
            }
        });

        try {
            if (MusicPlayback.mMediaSessionCompat.isActive()) {
                if (MusicPlayback.mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    btnPlay.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.app_pause));

                } else {
                    btnPlay.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.app_play));

                }
            }
        } catch (Exception e) {
            btnPlay.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.app_play));

        }

        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if (MusicPlayback.mMediaSessionCompat.isActive()) {
                        Objects.requireNonNull(getActivity()).startService(
                                songsManager.createExplicitFromImplicitIntent(new Intent(MusicPlayback.ACTION_PLAY_PAUSE)));
                    } else {
                        songsManager.playFromLastLeft();
                    }
                } catch (Exception e) {
                    songsManager.playFromLastLeft();
                }
            }
        });

        if (getResources().getBoolean(R.bool.isTablet) || getResources().getBoolean(R.bool.isLandscape)) {
            (view.findViewById(R.id.XbtnNext)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!songsManager.queue().isEmpty()) {
                        ContextCompat.startForegroundService(Objects.requireNonNull(getActivity()),
                                songsManager.createExplicitFromImplicitIntent(new Intent(MusicPlayback.ACTION_TRACK_NEXT)));
                    }
                }
            });
            (view.findViewById(R.id.XbtnPrev)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!songsManager.queue().isEmpty()) {
                        ContextCompat.startForegroundService(Objects.requireNonNull(getActivity()),
                                songsManager.createExplicitFromImplicitIntent(new Intent(MusicPlayback.ACTION_TRACK_PREV)));
                    }
                }
            });
        }

        try {
            if (MusicPlayback.mMediaSessionCompat.isActive()) {

                title.setText(MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_TITLE));
                artist.setText(MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_ARTIST));
                albumArt.setImageBitmap(
                        (new ImageHelper()).getRoundedCornerBitmap(
                                MusicPlayback.mMediaSessionCompat.getController().getMetadata().getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
                                , 16));

            } else {
                try {
                    title.setText(sharedPrefsUtils.readSharedPrefsString("title", "Unknown Title"));
                    artist.setText(sharedPrefsUtils.readSharedPrefsString("artist", "Unknown Artist"));
                    (new ImageUtils(getContext())).getImageByPicasso(sharedPrefsUtils.readSharedPrefsString("albumid","0"), albumArt);

                } catch (Exception ignored) {

                }
            }
        } catch (Exception e) {
            try {
                title.setText(sharedPrefsUtils.readSharedPrefsString("title", "Unknown Title"));
                artist.setText(sharedPrefsUtils.readSharedPrefsString("artist", "Unknown Artist"));
                (new ImageUtils(getContext())).getImageByPicasso(sharedPrefsUtils.readSharedPrefsString("albumid","0"), albumArt);
            } catch (Exception ignored) {

            }
        }

        seekUpdation();
        return view;

    }


    void touchDock() {
        if (!songsManager.queue().isEmpty() && !songsManager.allSongs().isEmpty()) {
            Intent intent = new Intent(getActivity(), PlayActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        seekHandler.removeCallbacks(run);
    }

    private void seekUpdation() {
        try {
            if (MusicPlayback.mMediaPlayer != null && MusicPlayback.mMediaSessionCompat.isActive()) {
                if (MusicPlayback.mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {

                    if (currentTitleArtist != MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_TITLE).length() +
                            MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_ARTIST).length() +
                            MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_ALBUM).length()) {
                        title.setText(MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_TITLE));
                        artist.setText(MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_ARTIST));
                        albumArt.setImageBitmap(MusicPlayback.mMediaSessionCompat.getController().getMetadata().getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
                        currentTitleArtist = MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_TITLE).length() +
                                MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_ARTIST).length() +
                                MusicPlayback.mMediaSessionCompat.getController().getMetadata().getText(MediaMetadataCompat.METADATA_KEY_ALBUM).length();
                    }
                    btnPlay.setImageDrawable(ContextCompat.getDrawable(
                            Objects.requireNonNull(getActivity()), R.drawable.app_pause));
                } else {
                    btnPlay.setImageDrawable(ContextCompat.getDrawable(
                            Objects.requireNonNull(getActivity()), R.drawable.app_play));
                }
            } else {
                btnPlay.setImageDrawable(ContextCompat.getDrawable(
                        Objects.requireNonNull(getActivity()), R.drawable.app_play));
            }
        } catch (Exception e) {
            btnPlay.setImageDrawable(ContextCompat.getDrawable(
                    Objects.requireNonNull(getActivity()), R.drawable.app_play));
        }
        seekHandler.postDelayed(run, 500);
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };


}

