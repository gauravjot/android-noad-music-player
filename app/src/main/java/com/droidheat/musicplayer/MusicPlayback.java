package com.droidheat.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MusicPlayback extends MediaBrowserServiceCompat implements
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener {

    // Available PlayBackStates
    //  STATE_NONE = 0;
    //  STATE_STOPPED = 1;
    //  STATE_PAUSED = 2;
    //  STATE_PLAYING = 3;

    /******* ---------------------------------------------------------------
     Public Static
     ----------------------------------------------------------------*******/

    public static final String ACTION_PLAY_PUSH = "com.droidheat.musicplayer.action.PLAY_PUSH";
    public static final String PLAY_ACTIVITY_GRAPHICS = "com.droidheat.musicplayer.action.GRAPHIC";
    public static final String ACTION_CLOSE = "com.droidheat.musicplayer.action.CLOSE";
    public static final String ACTION_PLAY = "com.droidheat.musicplayer.action.PLAY";
    public static final String ACTION_PLAY_PAUSE = "com.droidheat.musicplayer.action.PLAY_PAUSE";
    public static final String ACTION_TRACK_PREV = "com.droidheat.musicplayer.action.TRACK_PREV";
    public static final String ACTION_TRACK_NEXT = "com.droidheat.musicplayer.action.TRACK_NEXT";
    public static final String ACTION_REPEAT = "com.droidheat.musicplayer.action.REPEAT";

    static MediaPlayer mMediaPlayer;
    static MediaSessionCompat mMediaSessionCompat;

    /******* ---------------------------------------------------------------
     Private
     ----------------------------------------------------------------*******/

    private final String TAG = "PlaybackServiceConsole";

    private int musicID = -1;

    private PlaybackStateCompat.Builder mPlaybackStateBuilder;
    private SharedPrefsUtils sharedPrefsUtils;
    private SongsManager songsManager;

    private boolean autoPaused = false;
    private boolean isPlayFromLastLeft = false;

    /******* ---------------------------------------------------------------
     Service Methods and Intents
     ----------------------------------------------------------------*******/

    public MusicPlayback() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPrefsUtils = new SharedPrefsUtils(this);
        songsManager = new SongsManager(this);

        /*
         * Retrieve Queue from sharedPrefs
         */

        /*
         Initialize
         */
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();

        /*
         * Calling startForeground() under 5 seconds to avoid ANR
         */
        showPausedNotification();
        try {
            Log.d(TAG,"onCreate(): Showing Paused Notification");
        } catch (Exception e) {
            Log.d(TAG, "onCreate(): Failed to show Paused Notification, ANR might occur");
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            /*
             * Checking for musicID in intent
             */
            try {
                if (Objects.requireNonNull(intent.getExtras()).containsKey("musicID")) {
                    musicID = intent.getExtras().getInt("musicID");
                    Log.d(TAG,"MusicID switched to " + musicID);
                }
            } catch (Exception e) {
                Log.d(TAG,"Intent received by PlaybackService. No musicID found.");
            }
            if (musicID == -1) {
                musicID = sharedPrefsUtils.readSharedPrefsInt("musicID",0);
            }

            /*
             * Checking if intent received is for playing from where we last left playback
             * during a past session
             */
            try {
                if (Objects.requireNonNull(intent.getExtras()).containsKey("isPlayFromLastLeft")) {
                    isPlayFromLastLeft = Objects.requireNonNull(intent.getExtras()).getBoolean("isPlayFromLastLeft");
                    Log.d(TAG,"isPlayFromLastLeft intent is received.");
                } else { isPlayFromLastLeft = false;
                    Log.d(TAG,"Intent received by PlaybackService. No PlayFromLastLeft.");}
            } catch (Exception e) {
                Log.d(TAG,"Intent received by PlaybackService. No PlayFromLastLeft.");
                isPlayFromLastLeft = false;
            }

            /*
             * Retrieving Queue
             */

            /*
             * Analyze and Acting on the intent received by Service
             * Every request to Service should be with one of the intents in our switch
             */
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case ACTION_PLAY: {
                    processPlayRequest(musicID);
                    break;
                }
                case ACTION_PLAY_PUSH: {
                    doPushPlay();
                    break;
                }
                case ACTION_PLAY_PAUSE: {
                    processPlayPause();
                    break;
                }
                case ACTION_TRACK_PREV: {
                    processPrevRequest();
                    break;
                }
                case ACTION_TRACK_NEXT: {
                    processNextRequest();
                    break;
                }
                case ACTION_REPEAT: {
                    musicWidgetsReset();
                    break;
                }
                case ACTION_CLOSE: {
                    processCloseRequest();
                    break;
                }
                default: {
                }
                MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
            }
        }
        return START_NOT_STICKY;
    }

    /*
     * saveData() writes current song parameters to sharedPrefs which can be retrieved in
     * other activities or fragments as well as when we start app next time
     * musicID: is id of current item in queue
     * title, artist, album, albumid: are all fields of SongModel()
     *
     */
    private void saveData() {
        try {
            sharedPrefsUtils.writeSharedPrefs("musicID", musicID);
            sharedPrefsUtils.writeSharedPrefs("title", songsManager.queue().get(musicID).getTitle());
            sharedPrefsUtils.writeSharedPrefs("artist", songsManager.queue().get(musicID).getArtist());
            sharedPrefsUtils.writeSharedPrefs("album", songsManager.queue().get(musicID).getAlbum());
            sharedPrefsUtils.writeSharedPrefs("albumid", songsManager.queue().get(musicID).getAlbumID());
            sharedPrefsUtils.writeSharedPrefs("audio_session_id",mMediaPlayer.getAudioSessionId());
            sharedPrefsUtils.writeSharedPrefs("raw_path",songsManager.queue().get(musicID).getPath());
            sharedPrefsUtils.writeSharedPrefs("duration",songsManager.queue().get(musicID).getDuration());
        } catch (Exception e) {
            Log.d(TAG,"Unable to save song info in persistent storage. MusicID " + musicID);
        }
    }

    private void doPushPlay() {
        mMediaPlayer.reset();
        setGraphics();
        if (successfullyRetrievedAudioFocus()) {
            showPausedNotification();
            return;
        }
        showPlayingNotification();
        setMediaPlayer(songsManager.queue().get(musicID).getPath());
    }

    private void processNextRequest() {
        isPlayFromLastLeft = false;

        if (musicID +1 != songsManager.queue().size()) {
            musicID++;
        } else {
            musicID = 0;
        }
        setGraphics();
        if (successfullyRetrievedAudioFocus()) {
            showPausedNotification();
            return;
        }

        Log.d(TAG,"Skipping to Next track.");
        showPlayingNotification();
        setMediaPlayer(songsManager.queue().get(musicID).getPath());
    }

    private void processPrevRequest() {
        isPlayFromLastLeft = false;

        if (mMediaPlayer.getCurrentPosition() < 5000) {
            if (musicID > 0) {
                musicID--;
                Log.d(TAG,"Skipping to Previous track.");
                setGraphics();
                if (successfullyRetrievedAudioFocus()) {
                    showPausedNotification();
                    return;
                }
                showPlayingNotification();
                setMediaPlayer(songsManager.queue().get(musicID).getPath());
            } else {
                mMediaPlayer.seekTo(0);
            }
        } else {
            mMediaPlayer.seekTo(0);
        }
    }

    private void processCloseRequest() {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Shutting MediaPlayer service down...");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        mMediaSessionCompat.release();
        mMediaPlayer.release();
        stopForeground(true);
        NotificationManagerCompat.from(this).cancel(1);
    }

    private void processPauseRequest() {
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mMediaPlayer.pause();
            sharedPrefsUtils.writeSharedPrefs("song_position", mMediaPlayer.getCurrentPosition());
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            showPausedNotification();
        }
    }

    private void processPlayRequest(int index) {
        setGraphics();
        if (successfullyRetrievedAudioFocus()) {
            showPausedNotification();
            return;
        }
        Log.d(TAG,"Processing Play Request for musicID " + index);

        showPlayingNotification();
        setMediaPlayer(songsManager.queue().get(index).getPath());
    }

    private void processPlayPause() {
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PLAYING) {
            processPauseRequest();
        } else if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PAUSED) {
            mMediaPlayer.start();
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            showPlayingNotification();
        } else {
            processPlayRequest(musicID);
        }
    }

    private void setGraphics() {
        SongModel song = songsManager.queue().get(musicID);
        MediaMetadataCompat mMediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, grabAlbumArt(song.getAlbumID()))
                .build();
        mMediaSessionCompat.setMetadata(mMediaMetadataCompat);

        saveData();
        /*
         * Inform our activity. Sending broadcast, if not received
         * means our activity is finished.
         */
        Intent broadcast = new Intent();
        broadcast.setAction(PLAY_ACTIVITY_GRAPHICS);
        sendBroadcast(broadcast);
    }

    private Bitmap grabAlbumArt(String albumID) {
        Bitmap art = null;
        try {
            try {
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");

                Uri uri = ContentUris.withAppendedId(sArtworkUri,
                        Long.parseLong(albumID));

                ParcelFileDescriptor pfd = getApplicationContext().getContentResolver()
                        .openFileDescriptor(uri, "r");

                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    art = BitmapFactory.decodeFileDescriptor(fd);
                }
            } catch (Exception ignored) {
            }
            if (art == null) {
                return drawableToBitmap(ContextCompat.getDrawable(this, R.mipmap.ic_launcher));
            } else {
                return art;
            }
        } catch (Exception e) {
            return drawableToBitmap(ContextCompat.getDrawable(MusicPlayback.this,
                    R.mipmap.ic_launcher));
        }
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 300;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 300;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /******* ---------------------------------------------------------------
     MediaSessionCompat Methods
     ----------------------------------------------------------------*******/
    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            processPlayPause();
        }

        @Override
        public void onPause() {
            super.onPause();
            processPlayPause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            processNextRequest();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            processPrevRequest();
        }

        @Override
        public void onRewind() {
            super.onRewind();
        }

        @Override
        public void onStop() {
            super.onStop();
            stopSelf();
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    };

    private void setMediaPlaybackState(int state) {
        mPlaybackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    | PlaybackStateCompat.ACTION_STOP);

        mPlaybackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f, SystemClock.elapsedRealtime());
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PLAYING) {
            Log.d("PlaybackServiceConsole", "State Changed to Playing");
        } else if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PAUSED) {
            Log.d("PlaybackServiceConsole", "State Changed to Paused");
        } else if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_STOPPED) {
            Log.d("PlaybackServiceConsole", "State Changed to Stopped");
        }
        mMediaSessionCompat.setPlaybackState(mPlaybackStateBuilder.build());
    }

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                processPauseRequest();
            }
        }
    };

    /******* ---------------------------------------------------------------
     Notifications
     ----------------------------------------------------------------*******/
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager
                    mNotificationManager =
                    (NotificationManager) this
                            .getSystemService(Context.NOTIFICATION_SERVICE);
            // The id of the channel.
            String id = "channel_music_playback";
            // The user-visible name of the channel.
            CharSequence name = "Media Playback";
            // The user-visible description of the channel.
            String description = "Media playback controls";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.setShowBadge(false);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void showPlayingNotification() {
        musicWidgetsReset();

        createChannel();
        NotificationCompat.Builder builder
                = MediaStyleHelper.from(MusicPlayback.this, mMediaSessionCompat);

        Intent closeIntent = new Intent(this, MusicPlayback.class);
        closeIntent.setAction(MusicPlayback.ACTION_CLOSE);
        PendingIntent pCloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        builder.addAction(new NotificationCompat.Action(R.drawable.app_previous,
                "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(R.drawable.app_pause,
                "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(R.drawable.app_next,
                "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        builder.setStyle(new MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.ic_music_note_black_24dp);
        builder.setStyle(new MediaStyle()
                .setShowActionsInCompactView(1,2).setMediaSession(getSessionToken()));
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), 0));
        builder.setDeleteIntent(pCloseIntent);
        builder.setShowWhen(false);
        startForeground(1, builder.build());
    }

    private void showPausedNotification() {
        musicWidgetsReset();

        createChannel();
        NotificationCompat.Builder builder
                = MediaStyleHelper.from(MusicPlayback.this, mMediaSessionCompat);

        Intent closeIntent = new Intent(this, MusicPlayback.class);
        closeIntent.setAction(MusicPlayback.ACTION_CLOSE);
        PendingIntent pCloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        builder.addAction(new NotificationCompat.Action(R.drawable.app_previous,
                "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(R.drawable.app_play,
                "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(R.drawable.app_next,
                "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        builder.setStyle(new MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.ic_music_note_black_24dp);
        builder.setStyle(new MediaStyle()
                .setShowActionsInCompactView(1,2).setMediaSession(getSessionToken()));
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), 0));
        builder.setDeleteIntent(pCloseIntent);
        builder.setShowWhen(false);
        startForeground(1,builder.build());
        stopForeground(false);
    }


    /******* ---------------------------------------------------------------
     MediaPlayer
     ----------------------------------------------------------------*******/
    @Override
    public void onCompletion(MediaPlayer mp) {
        isPlayFromLastLeft = false;
        flushMediaPlayer();
        if (!sharedPrefsUtils.readSharedPrefsBoolean("repeat",false)) {
            if (musicID < (songsManager.queue().size() - 1)) {
                Log.d(TAG,"OnCompletion playing next track");
                processNextRequest();
            } else {
                Log.d(TAG,"OnCompletion of whole queue, starting from beginning");
                musicID = 0;
                processPlayRequest(musicID);
            }
        } else {
            showPlayingNotification();
            setMediaPlayer((songsManager.queue().get(musicID).getPath()));
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isPlayFromLastLeft) {
            mMediaPlayer.seekTo(sharedPrefsUtils.readSharedPrefsInt("song_position", 0));
        } else {
            sharedPrefsUtils.writeSharedPrefs("song_position", 0);
        }

        /*
         * Setting Equalizer
         */
        setEqualizer();

        mMediaPlayer.start();
        mMediaSessionCompat.setActive(true);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1.0f, 1.0f);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
    }

    private void flushMediaPlayer() {
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PLAYING ||
                mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PAUSED) {
            mMediaPlayer.reset();
        }
    }

    private void setMediaPlayer(String path) {
        flushMediaPlayer();
        File file = new File(path);
        if (file.exists()) {
            try {
                addVoteToTrack(path);
                mMediaPlayer.setDataSource(path);
            } catch (IOException e) {
                processNextRequest();
                e.printStackTrace();
            }

            try {
                mMediaPlayer.prepare();
            } catch (IOException ignored) {
            }
        } else {
            processNextRequest();
            Log.d(TAG,"Error finding file so we skipped to next.");
            (new CommonUtils(this)).showTheToast("Error finding music file");
        }
    }

    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        mPlaybackStateBuilder = new PlaybackStateCompat.Builder();

        setSessionToken(mMediaSessionCompat.getSessionToken());

        SongModel song = songsManager.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID",0));
        MediaMetadataCompat mMediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, grabAlbumArt(song.getAlbumID()))
                .build();
        mMediaSessionCompat.setMetadata(mMediaMetadataCompat);
    }

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    /******* ---------------------------------------------------------------
     AudioFocus
     ----------------------------------------------------------------*******/
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if (mMediaPlayer.isPlaying()) {
                    processPauseRequest();
                    autoPaused = true;
                    Log.d(TAG,"Auto paused enabled");
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                if (mMediaPlayer.isPlaying()) {
                    processPauseRequest();
                    autoPaused = true;
                    Log.d(TAG, "Auto paused enabled");
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if (mMediaPlayer != null) {
                    Log.d(TAG,"Auto-paused is " + ((autoPaused) ? "enabled" : "disabled"));
                    if (!mMediaPlayer.isPlaying() && autoPaused) {
                        processPlayPause();
                        autoPaused = false;
                        Log.d(TAG,"Auto paused disabled");
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        assert audioManager != null;
        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_GAIN) {
            Log.d(TAG, "Failed to gain AudioFocus");
        }

        return result != AudioManager.AUDIOFOCUS_GAIN;
    }

    Equalizer eq;
    BassBoost bassBoost;
    Virtualizer virtualizer;
    private void setEqualizer() {
            try {
                eq = new Equalizer(0, mMediaPlayer.getAudioSessionId());
                bassBoost = new BassBoost(0, mMediaPlayer.getAudioSessionId());
                virtualizer = new Virtualizer(0, mMediaPlayer.getAudioSessionId());
                boolean isEqInSettings = sharedPrefsUtils.readSharedPrefsBoolean("turnEqualizer", false);
                if (isEqInSettings) {
                    eq.setEnabled(true);
                    bassBoost.setEnabled(true);
                    virtualizer.setEnabled(true);
                } else {
                    eq.setEnabled(false);
                    bassBoost.setEnabled(false);
                    virtualizer.setEnabled(false);
                }
                int currentEqProfile = sharedPrefsUtils.readSharedPrefsInt("currentEqProfile",0);
                bassBoost.setStrength((short) sharedPrefsUtils.readSharedPrefsInt("bassLevel" + currentEqProfile, 0));
                virtualizer.setStrength((short) sharedPrefsUtils.readSharedPrefsInt("vzLevel" + currentEqProfile, 0));
                for (int i = 0; i < eq.getNumberOfBands(); i++) {
                    eq.setBandLevel((short) i, (short) sharedPrefsUtils.readSharedPrefsInt(
                            "profile" + currentEqProfile + "Band" + i, 0));
                    Log.d(TAG, "Equalizer band " + sharedPrefsUtils.readSharedPrefsInt(
                            "profile"+ currentEqProfile + "Band" + i, 0));
                }
                Log.d(TAG, "Equalizer successfully initiated with profile " + currentEqProfile);
            } catch (Exception e) {
                (new CommonUtils(this)).showTheToast("Unable to run Equalizer");
            }
    }

    void addVoteToTrack(String path) {
        path = path.trim();
        try {
            CategorySongs categorySongs = new CategorySongs(getApplicationContext());
            categorySongs.open();
            if (categorySongs.checkRow(path)) {
                categorySongs.updateRow(path);
            } else {
                categorySongs.addRow(1, songsManager.queue().get(musicID));
            }
            categorySongs.close();
        } catch (Exception e) {
            Log.d(TAG, "addVoteToTrack crashed.");
        }
    }

    /******* ---------------------------------------------------------------
     Defaults
     ----------------------------------------------------------------*******/
    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    //Not important for general audio service, required for class
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    /******* ---------------------------------------------------------------
     Music Widgets
     ----------------------------------------------------------------*******/

    private void musicWidgetsReset() {
        Intent intent = new Intent(this, MusicWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra("state", mPlaybackStateBuilder.build().getState());
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(
                        new ComponentName(getApplication(), MusicWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);

        Intent intent2 = new Intent(this, MusicWidgetv2.class);
        intent2.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent2.putExtra("state", mPlaybackStateBuilder.build().getState());
        int[] ids2 = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(
                        new ComponentName(getApplication(), MusicWidgetv2.class));
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids2);
        sendBroadcast(intent2);

        Intent intent1 = new Intent(this, MusicWidget2.class);
        intent1.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent1.putExtra("state", mPlaybackStateBuilder.build().getState());
        int[] ids1 = AppWidgetManager
                .getInstance(getApplication())
                .getAppWidgetIds(
                        new ComponentName(getApplication(), MusicWidget2.class));
        intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids1);
        sendBroadcast(intent1);
    }


}
