package com.droidheat.musicplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayActivity extends AppCompatActivity implements OnClickListener, QueueFragment.MyFragmentCallbackOne {

    ImageView btnPlay;
    ImageView btnNext;
    ImageView btnPrev, imgFav;
    TextView title, album, leftTime, rightTime;
    ImageView btnRepeat;
    ViewPager albumArt;
    SeekBar seek_bar;
    Handler seekHandler = new Handler();
    Context mContext;
    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };
    boolean isFragment = false;
    View frag;
    boolean isFavourite;
    int favChange = 0;

    boolean playButton = false;
    SongsManager songsManager;

    Integer[] imageResIds;
    ImagePagerAdapter mAdapter;

    String TAG = "PlayActivityConsole";

    private MediaBrowserCompat mMediaBrowser;
    private PlaybackStateCompat mLastPlaybackState;
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

    SharedPrefsUtils sharedPrefsUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout_v2);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayback.PLAYACTION_GRAPHIC);
        registerReceiver(receiver, filter);

        sharedPrefsUtils = new SharedPrefsUtils(this);

        mContext = this;

        // Getting Elements
        title = findViewById(R.id.title);
        album = findViewById(R.id.album);
        btnPlay = findViewById(R.id.play);
        btnNext = findViewById(R.id.next);
        btnPrev = findViewById(R.id.prev);
        albumArt = findViewById(R.id.albumArt);
        rightTime = findViewById(R.id.rightTime);
        leftTime = findViewById(R.id.leftTime);
        seek_bar = findViewById(R.id.seekBar1);
        btnRepeat = findViewById(R.id.repeat);
        imgFav = findViewById(R.id.imageFav);
        frag = findViewById(R.id.fragment);
        songsManager = new SongsManager(this);

            LayerDrawable progressBarDrawable = (LayerDrawable) seek_bar.getProgressDrawable();
            Drawable progressDrawable = progressBarDrawable.getDrawable(1);

            progressDrawable.setColorFilter(ContextCompat.getColor(this, Color.parseColor(
                    sharedPrefsUtils.readSharedPrefsString("accentColor", "#FB395C")
            )), PorterDuff.Mode.SRC_IN);

        imageResIds = new Integer[songsManager.queue().size()];

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        for (int i = 0; i < songsManager.queue().size(); i++) {
            imageResIds[i] = Integer.parseInt(songsManager.queue().get(i).getAlbumID());
        }

        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imageResIds.length);
        albumArt.setPageTransformer(false, new ParallaxPagerTransformer(R.id.imageView));
        albumArt.setAdapter(mAdapter);
        albumArt.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID",0));
        albumArt.setOffscreenPageLimit(3);

        albumArt.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (sharedPrefsUtils.readSharedPrefsInt("musicID",0) != position) {
                    Intent intent = new Intent(MusicPlayback.ACTION_PLAYPUSH);
                    intent.putExtra("musicID", position);
                    Log.d(TAG, "PlayPushing musicID " + position);
                    startService(createExplicitFromImplicitIntent(PlayActivity.this, intent));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        if (sharedpreferences.getBoolean("shuffle", false)) {
//            btnShuffle.setColorFilter(ContextCompat.getColor(this, R.color.actionBar));
//            MusicPlayback.isShuffle = true;
//        }
        if (sharedPrefsUtils.readSharedPrefsBoolean("repeat", true)) {
            btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.actionBar));
        }

        if (!getResources().getBoolean(R.bool.isLandscape)) {
            isFragment = false;
            frag.setVisibility(View.GONE);
        }

        // Setting Buttons
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);
        findViewById(R.id.addToPlayListImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                songsManager.addToPlaylist(songsManager.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID",0)));
            }
        });
//        btnShuffle.setOnClickListener(this);

        setGraphics();

        imgFav.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doItMyFav();
            }
        });

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicPlayback.class), mConnectionCallback, null);
    }

    @Override
    public void viewPagerRefreshOne() {
        imageResIds = new Integer[songsManager.queue().size()];
        for (int i = 0; i < songsManager.queue().size(); i++) {
            imageResIds[i] = Integer.parseInt(songsManager.queue().get(i).getAlbumID());
        }
        albumArt.setAdapter(mAdapter);
        albumArt.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID",0));
    }

    private static class ImagePagerAdapter extends FragmentPagerAdapter {

        private final int mSize;

        ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.newInstance(position);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public void setGraphics() {

        favChange = 0;

        if (getIndex(sharedPrefsUtils.readSharedPrefsString("raw_path", null)) != -1) {
            imgFav.setColorFilter(ContextCompat.getColor(this, R.color.accentColor));
            isFavourite = true;
        } else {
            imgFav.setColorFilter(null);
            isFavourite = false;
        }
        if (sharedPrefsUtils.readSharedPrefsBoolean("repeat",false)) {
            btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.accentColor));
        }
        else {
            btnRepeat.setColorFilter(null);
            sharedPrefsUtils.writeSharedPrefs("repeat",false);
        }

        /* ******************
         * Setting ImageViews
         */
        if (albumArt.getCurrentItem() != sharedPrefsUtils.readSharedPrefsInt("musicID",0)) {
            albumArt.setVisibility(View.VISIBLE);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            albumArt.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID",0));
                        }
                    });
                }
            });
            thread.start();
        }


        /* ******************
         * Setting TextViews
         */
        title.setText(sharedPrefsUtils.readSharedPrefsString("title","Title"));
        title.setSelected(true);
        album.setText(sharedPrefsUtils.readSharedPrefsString("album","Album"));

        /*
         * Queue Fragment Scroll
         */
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Do something after 5s = 5000ms
//                QueueFragment queueFragment = (QueueFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
//                queueFragment.adapter.notifyDataSetChanged();
//            }
//        }, 300);

        /* ******************
         * Seek Bar Graphics
         */
        String totalDuration = songsManager.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID",0)).getDuration();
        rightTime.setText(totalDuration);
        seekUpdation();

        seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if (arg2) {
                    try {
                        MusicPlayback.mMediaPlayer.seekTo(arg1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //playColors();

    }


    private int getIndex(String rawPath) {
        FavouriteList db = new FavouriteList(this);
        db.open();
        ArrayList<SongModel> list;
        list = db.getAllRows();
        db.close();
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).getPath().equals(rawPath)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int getIndex(String rawPath, ArrayList<SongModel> list) {
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).getPath().equals(rawPath)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setGraphics();
        }
    };

//    public void playColors() {
//        Thread background = new Thread(new Runnable() {
//            public void run() {
//                int a;
//                try {
//                    ImageColor img = new ImageColor(
//                            Bitmap.createScaledBitmap(MusicPlayback.FinalArt, 150, 150, false));
//                    a = img.getColour();
//                } catch (Exception e) {
//                    a = getDominantColor(Bitmap.createScaledBitmap(MusicPlayback.FinalArt, 150, 150, false));
//                }
//                float[] hsb = new float[3];
//                Color.colorToHSV(a, hsb);
//
//                float brightness = hsb[2];
//
//                if (brightness < 0.5) {
//                    hsb[2] = hsb[2] + (hsb[2] * 10 / 100);
//                } else {
//                    hsb[2] = hsb[2] - (hsb[2] * 40 / 100);
//                }
//
//                final int z = Color.HSVToColor(hsb);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            Window window = getWindow();
//                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                            window.setStatusBarColor(z);
//                        }
//                    }
//                });
//
//            }
//        });
//        background.start();
//    }


    private void doItMyFav() {

        FavouriteList db = new FavouriteList(PlayActivity.this);
        db.open();
        SongsManager songsManager = new SongsManager(this);
        if (!isFavourite) {
            if (getIndex(sharedPrefsUtils.readSharedPrefsString("raw_path", null)) == -1) {
                SongModel hashMap = songsManager.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID",0));
                if (songsManager.addToFavouriteSongs(hashMap)) {

                    imgFav.setColorFilter(ContextCompat.getColor(this, R.color.accentColor));
                    isFavourite = true;
                    (new CommonUtils(this)).showTheToast("Favourite Added!");
                } else {
                    (new CommonUtils(this)).showTheToast("Unable to add to Favourite!");
                }
            }
        } else {
            ArrayList<SongModel> list = songsManager.favouriteSongs();
            if (db.deleteAll()) {

                imgFav.setColorFilter(null);

                int n = getIndex(sharedPrefsUtils.readSharedPrefsString("raw_path", null), list);
                list.remove(n);

                for (int i = 0; i < list.size(); i++) {
                    db.addRow(list.get(i));
                }
                isFavourite = false;
                (new CommonUtils(this)).showTheToast("Favourite Removed!");
            } else {
                (new CommonUtils(this)).showTheToast("Unable to Remove Favourite!");
            }
        }
        favChange += 1;
        db.close();

    }


    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayback.PLAYACTION_GRAPHIC);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(View target) {
        if (target == btnPlay) {
            startService(createExplicitFromImplicitIntent(this, new Intent(MusicPlayback.ACTION_PLAYPAUSE)));
        } else if (target == btnRepeat) {
            if (sharedPrefsUtils.readSharedPrefsBoolean("repeat",false)) {
                btnRepeat.clearColorFilter();
                (new CommonUtils(this)).showTheToast("Repeat Off");
                sharedPrefsUtils.writeSharedPrefs("repeat",false);
            } else {
                btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.accentColor));
                (new CommonUtils(this)).showTheToast("Repeat On");
                sharedPrefsUtils.writeSharedPrefs("repeat",true);
            }
            startService(createExplicitFromImplicitIntent(this, new Intent(MusicPlayback.ACTION_REPEAT)));
        }  else if (target == btnNext)
            if (sharedPrefsUtils.readSharedPrefsInt("musicID",0) < songsManager.queue().size()) {
                albumArt.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID",0) + 1);
            } else {
                albumArt.setCurrentItem(1);
            }
        else if (target == btnPrev)
            if (sharedPrefsUtils.readSharedPrefsInt("musicID",0) != 0) {
                albumArt.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID",0) - 1);
            } else {
                seek_bar.setProgress(0);
            }
    }

    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackstate changed" + state);
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                //updateMediaDescription(metadata.getDescription());
                updateDuration(metadata);
            }
        }
    };

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        Log.e(TAG, "could not connect media controller");
                    }
                }
            };

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(
                PlayActivity.this, token);
        if (mediaController.getMetadata() == null) {
            finish();
            return;
        }
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mCallback);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            //updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
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
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(mCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        Log.d(TAG, "updateDuration called ");
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        seek_bar.setMax(duration);
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                stopSeekbarUpdate();
                break;
            default:
                Log.d(TAG, "Unhandled state " + state.getState());
        }
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        seek_bar.setProgress((int) currentPosition);
    }

    @SuppressLint("SimpleDateFormat")
    private void seekUpdation() {
        try {
            if (MusicPlayback.mMediaSessionCompat.isActive()) {
                seek_bar.setMax(MusicPlayback.mMediaPlayer.getDuration());
                if (MusicPlayback.mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    int currentLocation = MusicPlayback.mMediaPlayer.getCurrentPosition();
                    seek_bar.setProgress(currentLocation);
                    if (!playButton) {
                        btnPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_pause));
                    }
                    leftTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(currentLocation),
                            TimeUnit.MILLISECONDS.toSeconds(currentLocation) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentLocation))
                    ));
                    playButton = true;
                } else {
                    if (seek_bar.getProgress() == 0) {
                        seek_bar.setProgress(MusicPlayback.mMediaPlayer.getCurrentPosition());
                    }
                    if (playButton) {
                        btnPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_play));
                    }
                    playButton = false;
                }
            }
        } catch (Exception e) {
            btnPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_play));
            seek_bar.setMax(1);
            seek_bar.setProgress(1);
        }
        seekHandler.postDelayed(run, 990);
    }

    public Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    /*
     * Pending Work here
     */


//    @SuppressLint("UseSparseArrays")
//    public int getDominantColor(Bitmap bitmap) {
//
//        if (bitmap == null)
//            throw new NullPointerException();
//
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int size = width * height;
//        int pixels[] = new int[size];
//
//        Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_4444, false);
//
//        bitmap2.getPixels(pixels, 0, width, 0, 0, width, height);
//
//        final List<HashMap<Integer, Integer>> colorMap = new ArrayList<>();
//        colorMap.add(new HashMap<Integer, Integer>());
//        colorMap.add(new HashMap<Integer, Integer>());
//        colorMap.add(new HashMap<Integer, Integer>());
//
//        int color;
//        int r;
//        int g;
//        int b;
//        Integer rC, gC, bC;
//        for (int pixel : pixels) {
//            color = pixel;
//
//            r = Color.red(color);
//            g = Color.green(color);
//            b = Color.blue(color);
//
//            rC = colorMap.get(0).get(r);
//            if (rC == null)
//                rC = 0;
//            colorMap.get(0).put(r, ++rC);
//
//            gC = colorMap.get(1).get(g);
//            if (gC == null)
//                gC = 0;
//            colorMap.get(1).put(g, ++gC);
//
//            bC = colorMap.get(2).get(b);
//            if (bC == null)
//                bC = 0;
//            colorMap.get(2).put(b, ++bC);
//        }
//
//        int[] rgb = new int[3];
//        for (int i = 0; i < 3; i++) {
//            int max = 0;
//            int val = 0;
//            for (Map.Entry<Integer, Integer> entry : colorMap.get(i).entrySet()) {
//                if (entry.getValue() > max) {
//                    max = entry.getValue();
//                    val = entry.getKey();
//                }
//            }
//            rgb[i] = val;
//        }
//
//        return Color.rgb(rgb[0], rgb[1], rgb[2]);
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            backPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    private void backPressed() {

//            if (favChange % 2 == 1) {
////                Intent homepage = new Intent(this, MainActivity.class);
////                startActivity(homepage);
//            }
        finish();

    }

    public void onItemClick(int mPosition) {
        if (mPosition != sharedPrefsUtils.readSharedPrefsInt("musicID",0)) {
            Intent intent = new Intent(MusicPlayback.ACTION_PLAYPUSH);
            intent.putExtra("musicID", mPosition);
            startService(createExplicitFromImplicitIntent(this, intent));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_queueBtn) {
            if (!getResources().getBoolean(R.bool.isLandscape)) {
                if (isFragment) {
                    frag.setVisibility(View.GONE);
                } else {
                    frag.setVisibility(View.VISIBLE);
                }
                isFragment = !isFragment;
            }
        } else if (id == R.id.add_to_playlist) {
            songsManager.addToPlaylist(songsManager.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID",0)));
        } else if (id == R.id.equalizer) {
            try {
                if (MusicPlayback.mMediaSessionCompat.isActive()) {
                    startActivity(new Intent(PlayActivity.this, EqualizerActivity.class));
                } else {
                    (new CommonUtils(this)).showTheToast("Play song to equalize effects.");
                }
            } catch (Exception e) {
                (new CommonUtils(this)).showTheToast("Play song to equalize effects.");
            }
        } else if (id == R.id.save_as_playlist) {
            final Dialog alertDialog = new Dialog(this);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.setContentView(R.layout.dialog_add_playlist);

            final EditText input = alertDialog.findViewById(R.id.editText);
            input.requestFocus();

            alertDialog.findViewById(R.id.btnCreate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = input.getText().toString();
                    if (!name.isEmpty()) {
                        songsManager.addPlaylist(name);
                        (new CommonUtils(PlayActivity.this)).showTheToast("Adding songs to list: " + name);
                        alertDialog.cancel();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                PlaylistSongs db = new PlaylistSongs(PlayActivity.this);
                                db.open();
                                ArrayList<SongModel> data = songsManager.queue();
                                int playlistID = Integer.parseInt(Objects.requireNonNull(songsManager.getAllPlaylists().get(
                                        songsManager.getAllPlaylists().size() - 1).get("ID")));
                                for (int i = 0; i < data.size(); i++) {
                                    db.addRow(playlistID, data.get(i));
                                }
                                //TODO:MainActivity.shouldNotifyDataChanged = true;
                                db.close();
                            }
                        });
                        thread.run();
                    } else {
                        Toast.makeText(PlayActivity.this, "Please enter playlist name.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            alertDialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });
            alertDialog.show();
        } else if (id == R.id.goto_album) {
            Intent intent = new Intent(this, GlobalDetailActivity.class);
            intent.putExtra("name", sharedPrefsUtils.readSharedPrefsString("album", null));
            intent.putExtra("field", "albums");
            startActivity(intent);
        } else if (id == R.id.goto_artist) {
            Intent intent = new Intent(this, GlobalDetailActivity.class);
            intent.putExtra("name", sharedPrefsUtils.readSharedPrefsString("artist", null));
            intent.putExtra("field", "artists");
            startActivity(intent);
        } else if (id == R.id.info) {
            songsManager.info(
                    songsManager.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID", 0))
                    )
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }


}