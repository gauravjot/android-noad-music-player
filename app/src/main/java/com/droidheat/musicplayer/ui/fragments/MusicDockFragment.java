package com.droidheat.musicplayer.ui.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidheat.musicplayer.services.MusicPlayback;
import com.droidheat.musicplayer.ui.activities.PlayActivity;
import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.utils.SharedPrefsUtils;
import com.droidheat.musicplayer.utils.SongsUtils;
import com.droidheat.musicplayer.utils.CommonUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.support.v4.media.session.PlaybackStateCompat.*;

@SuppressWarnings("ConstantConditions")
public class MusicDockFragment extends Fragment {

    private TextView title;
    private TextView artist;
    private ImageView btnPlay;
    private ImageView albumArt;
    private ProgressBar progressBar;
    private MediaBrowserCompat mMediaBrowser;
    private SongsUtils songsUtils;
    private RelativeLayout musicDockRoot;

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;
    private final Handler mHandler = new Handler();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set visibility of Music Dock
        //songsUtils.grabIfEmpty();
        Log.d("==>", String.valueOf(songsUtils.getMainListSize()));
        if(songsUtils.getMainListSize()==0) {
            musicDockRoot.setVisibility(View.INVISIBLE);
        } else {
            musicDockRoot.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_dock, container,
                false);

        musicDockRoot = view.findViewById(R.id.root_music_dock);
        title = view.findViewById(R.id.XtextView1);
        artist = view.findViewById(R.id.XtextView2);
        btnPlay = view.findViewById(R.id.XbtnPlay);
        albumArt = view.findViewById(R.id.albumArt);
        progressBar = view.findViewById(R.id.progressBar);
        final Button btnPlayActivity = view.findViewById(R.id.Xbutton1);
        title.setSelected(true);

        albumArt.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 20F);
            }
        });
        albumArt.setClipToOutline(true);

        LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        Drawable progressDrawable = progressBarDrawable.getDrawable(1);
        progressDrawable.setColorFilter(ContextCompat.getColor(getActivity(), (new CommonUtils(getActivity())
                        .accentColor(new SharedPrefsUtils(getActivity())))),
                PorterDuff.Mode.SRC_IN);

        songsUtils = new SongsUtils(getActivity());

        btnPlayActivity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                touchDock();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!songsUtils.queue().isEmpty()) {
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();
                }
            }
        });

        if (getResources().getBoolean(R.bool.isTablet)) {
            (view.findViewById(R.id.XbtnNext)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!songsUtils.queue().isEmpty()) {
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().skipToNext();
                    }
                }
            });
            (view.findViewById(R.id.XbtnPrev)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!songsUtils.queue().isEmpty()) {
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
        if (!songsUtils.queue().isEmpty()) {
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
                        ContextCompat.startForegroundService(getActivity(), new Intent(getActivity(),MusicPlayback.class));
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
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata);
        }
        updateDuration(metadata);
        updateProgress();
        if (state != null && (state.getState() == STATE_PLAYING ||
                state.getState() == STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }
    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            long PROGRESS_UPDATE_INTERNAL = 1000;
            long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        if (mLastPlaybackState == null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(getActivity());
            if (songsUtils.queue().size() != 0 && sharedPrefsUtils.readSharedPrefsString("raw_path", "").equals(songsUtils.queue().get(songsUtils.getCurrentMusicID()).getPath())) {
                progressBar.setMax(sharedPrefsUtils.readSharedPrefsInt("durationInMS", 0));
                musicDockRoot.setVisibility(View.VISIBLE);
            }
            return;
        }
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        progressBar.setMax(duration);
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(getActivity());
            if (songsUtils.queue().size() != 0 && sharedPrefsUtils.readSharedPrefsString("raw_path", "").equals(songsUtils.queue().get(songsUtils.getCurrentMusicID()).getPath())) {
                progressBar.setProgress(sharedPrefsUtils.readSharedPrefsInt("song_position", 0));
                musicDockRoot.setVisibility(View.VISIBLE);
            }
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        progressBar.setProgress((int) currentPosition);
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
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
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }

    PlaybackStateCompat mLastPlaybackState = null;
    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;

        switch (state.getState()) {
            case STATE_PLAYING:
                scheduleSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_pause));
                break;
            case STATE_PAUSED:
                stopSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));
                break;
            case STATE_NONE:
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));
                break;
            case STATE_STOPPED:
                stopSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));
                break;
            case STATE_BUFFERING:
                stopSeekbarUpdate();
                break;
            default:
                Log.d(TAG, "Unhandled state " + state.getState());
            case STATE_CONNECTING:
                break;
            case STATE_ERROR:
                break;
            case STATE_FAST_FORWARDING:
                break;
            case STATE_REWINDING:
                break;
            case STATE_SKIPPING_TO_NEXT:
                break;
            case STATE_SKIPPING_TO_PREVIOUS:
                break;
            case STATE_SKIPPING_TO_QUEUE_ITEM:
                break;
        }
    }


}

