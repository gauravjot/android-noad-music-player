package com.droidheat.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("ConstantConditions")
public class MusicDockFragment extends Fragment {

    private TextView title;
    private TextView artist;
    private ImageView btnPlay;
    private ImageView albumArt;
    private MediaBrowserCompat mMediaBrowser;
    private SongsManager songsManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_dock, container,
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

        btnPlayActivity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                touchDock();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!songsManager.queue().isEmpty()) {
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();
                }
            }
        });

        if (getResources().getBoolean(R.bool.isTablet) || getResources().getBoolean(R.bool.isLandscape)) {
            (view.findViewById(R.id.XbtnNext)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!songsManager.queue().isEmpty()) {
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().skipToNext();
                    }
                }
            });
            (view.findViewById(R.id.XbtnPrev)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!songsManager.queue().isEmpty()) {
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().skipToNext();
                    }
                }
            });
        }

        mMediaBrowser = new MediaBrowserCompat(getActivity(),
                new ComponentName(getActivity(), MusicPlayback.class), mConnectionCallback, null);

        return view;

    }


    void touchDock() {
        if (!songsManager.queue().isEmpty()) {
            Intent intent = new Intent(getActivity(), PlayActivity.class);
            startActivity(intent);
        } else {
            (new CommonUtils(getActivity())).showTheToast("No music found in device, try Sync in options.");
        }
    }

    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            Log.d(TAG, "onPlayBackStateChanged" + state);
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                updateMediaDescription(metadata);
            }
        }
    };

    private void updateMediaDescription(MediaMetadataCompat metadata) {
        title.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE));
        artist.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST));
        albumArt.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
    }

    private String TAG = "MusicDockConsole";

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                        //TODO Bug: ACTION_REPEAT is placeholder intent to call onStartCommand in Service
                        ContextCompat.startForegroundService(getActivity(),songsManager.createExplicitFromImplicitIntent(new Intent(MusicPlayback.ACTION_REPEAT)));
                    } catch (RemoteException e) {
                        Log.e(TAG, "could not connect media controller");
                    }
                }
            };

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(
                getActivity(), token);
        if (mediaController.getMetadata() == null) {
            return;
        }
        MediaControllerCompat.setMediaController(getActivity(), mediaController);
        mediaController.registerCallback(mCallback);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updateMediaDescription(mediaController.getMetadata());
        updatePlaybackState(state);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
            Log.d(TAG, "connecting to MediaSession");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
            Log.d(TAG, "disconnecting from MediaSession");
        }
        if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(mCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_pause));
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                break;
            default:
                Log.d(TAG, "Unhandled state " + state.getState());
            case PlaybackStateCompat.STATE_CONNECTING:
                break;
            case PlaybackStateCompat.STATE_ERROR:
                break;
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
                break;
            case PlaybackStateCompat.STATE_REWINDING:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                break;
        }
    }


}

