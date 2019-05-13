package com.droidheat.musicplayer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class IntentPlayActivity extends AppCompatActivity {

    MediaPlayer mPlayer = new MediaPlayer();
    SeekBar seek;
    TextView textCurrent;
    Handler seekHandler = new Handler();
    AudioManager am;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_play);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        Uri data = intent.getData();

        try {
            //TODO fix this
            //MusicPlayback.mPlayer.pause();
        } catch (Exception ignored) {

        }

        if (intent.getType().contains("audio/")) {

            am = (AudioManager) getSystemService(AUDIO_SERVICE);

            // Request audio focus for playback
            int result = am.requestAudioFocus(afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);

            try {
                mPlayer.setDataSource(this, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mPlayer.start();
                // Start playback.
            }

	        /* *****************
             * Graphics
	         */
            TextView totalDuration = findViewById(R.id.textView4);
            final ImageView playPause = findViewById(R.id.imageView2);
            TextView title = findViewById(R.id.heading);
            seek = findViewById(R.id.seekBar1);
            textCurrent = findViewById(R.id.textView3);

            try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this,data);

            String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            title.setText(name); }
            catch (Exception ignored) {

            }

            if (mPlayer.isPlaying()) {
                playPause.setImageDrawable(getApplicationContext().getDrawable(R.drawable.app_pause));
            }


            seek.setMax(mPlayer.getDuration());
            int currentDuration = Math.round(mPlayer.getDuration());
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("mm:ss");
            df.setTimeZone(tz);
            String time = df.format(currentDuration);
            totalDuration.setText(String.valueOf(time));
            seekUpdation();


            playPause.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                        playPause.setImageDrawable(getApplicationContext().getDrawable(R.drawable.app_play));
                    } else {
                        playPause.setImageDrawable(getApplicationContext().getDrawable(R.drawable.app_pause));
                        mPlayer.start();
                    }
                }
            });

            seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                    if (arg2) {
                        mPlayer.seekTo(arg1);
                        int currentDuration = Math.round(arg1);
                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        SimpleDateFormat df = new SimpleDateFormat("mm:ss");
                        df.setTimeZone(tz);
                        String time = df.format(currentDuration);
                        textCurrent.setText(String.valueOf(time));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

        }
    }

    OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mPlayer.pause();
                // Pause playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mPlayer.start();
                // Resume playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mPlayer.pause();
                // Stop playback
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.pause();
        mPlayer.reset();
        mPlayer.release();
        seekHandler.removeCallbacks(run);
        // Abandon audio focus when playback complete
        am.abandonAudioFocus(afChangeListener);
        try {
            //TODO fix this
//            if (MusicPlayback.musicState == MusicPlayback.State.PLAYING) {
//                MusicPlayback.mPlayer.start();
//            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                seekUpdation();
            } catch (Exception ignored) {
            }
        }
    };

    @SuppressLint("SimpleDateFormat")
    void seekUpdation() {
        seekHandler.postDelayed(run, 950);
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                int currentLocationZ = mPlayer.getCurrentPosition();
                seek.setProgress(currentLocationZ);
                int currentDuration = Math.round(mPlayer.getCurrentPosition());
                TimeZone tz = TimeZone.getTimeZone("UTC");
                SimpleDateFormat df = new SimpleDateFormat("mm:ss");
                df.setTimeZone(tz);
                String time = df.format(currentDuration);
                textCurrent.setText(String.valueOf(time));
            }
        }
    }

}
