package com.droidheat.musicplayer.ui.activities;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.services.MusicPlayback;
import com.droidheat.musicplayer.utils.CommonUtils;
import com.droidheat.musicplayer.utils.SharedPrefsUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends AppCompatActivity {

    TextView userTimeInputTextView;
    String DEFAULT_TIME_INPUT = "*";
    String userTimeInput = DEFAULT_TIME_INPUT;

    // Sets an ID for the notification
    final int NOTIFICATION_ID = 1297601;

    public static Timer timer;
    public static String currentSleepTimer = null;
    NotificationManager mNotifyMgr;

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_timer);


        Button sleepTimerCancelButton = findViewById(R.id.sleepTimerCancelButton);
        sleepTimerCancelButton.setTextColor(ContextCompat.getColor(this,
                (new CommonUtils(this)).accentColor(new SharedPrefsUtils(this))));

        Button btnDone = findViewById(R.id.button1);
        btnDone.setTextColor(ContextCompat.getColor(this,
                (new CommonUtils(this)).accentColor(new SharedPrefsUtils(this))));
        Button btnCancel = findViewById(R.id.Button01);
        userTimeInputTextView = findViewById(R.id.textView4);
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        userTimeInputTextView.setText(userTimeInput);
        if (currentSleepTimer != null) {
            try {
//                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
//                Date d1 = sdf.parse(currentSleepTimer);
//                long diff = d1.getTime() - sdf.parse(sdf.format(new Date())).getTime();
//                long diffMinutes = diff / 60000;
                findViewById(R.id.currentSleepTimer).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.currentSleepTimerTextView)).setText("Sleeps at " + currentSleepTimer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            findViewById(R.id.currentSleepTimer).setVisibility(View.GONE);
        }


        sleepTimerCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer();
                if (currentSleepTimer == null) findViewById(R.id.currentSleepTimer).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 1
                        displayTime(1);
                    }

                });

        findViewById(R.id.button3).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 2
                        displayTime(2);
                    }
                });

        findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 3
                        displayTime(3);
                    }
                });

        findViewById(R.id.Button02).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 4
                        displayTime(4);
                    }
                });

        findViewById(R.id.Button03).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 5
                        displayTime(5);
                    }
                });

        findViewById(R.id.Button04).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 6
                        displayTime(6);
                    }
                });

        findViewById(R.id.Button05).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 7
                        displayTime(7);
                    }
                });

        findViewById(R.id.Button06).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 8
                        displayTime(8);
                    }
                });

        findViewById(R.id.Button07).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 9
                        displayTime(9);
                    }
                });

        findViewById(R.id.button5).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 0
                        displayTime(0);
                    }
                });


        btnDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // editText.getText().toString()
                timer = new Timer();
                if (userTimeInput.length() != 0) {
                    try {
                        int time = Integer.parseInt(userTimeInput) * 60 * 1000;
                        if (time > 0) {
                            timer.schedule(new MyTimerTask(), time);
                            Calendar cal = Calendar.getInstance();
                            cal.getTime();
                            cal.add(Calendar.MILLISECOND, time);
                            SimpleDateFormat sdf2 = new SimpleDateFormat(
                                    "hh:mm a");

                            currentSleepTimer = sdf2.format(cal.getTime());
                            (new CommonUtils(TimerActivity.this)).showTheToast("Music Sleep Timer Started!");

                            createNotificationChannel();

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(TimerActivity.this, "timer")
                                            .setSmallIcon(R.drawable.ic_timer_black_24dp)
                                            .setContentTitle("Music Player Sleep Timer")
                                            .setContentText("Will sleep at " + currentSleepTimer)
                                            .setPriority(NotificationCompat.PRIORITY_MIN)
                                            .setSound(null);

                            Intent resultIntent = new Intent(
                                    TimerActivity.this, TimerActivity.class);
                            // Because clicking the notification opens a new
                            // ("special") activity, there's
                            // no need to create an artificial back stack.

                            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            PendingIntent resultPendingIntent = PendingIntent
                                    .getActivity(TimerActivity.this, 0,
                                            resultIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);

                            mBuilder.setContentIntent(resultPendingIntent)
                                    .setOngoing(true);
                            // Gets an instance of the NotificationManager
                            // service
                            // Builds the notification and issues it.
                            mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
                        } else {
                            try {
                                cancelTimer();
                            } catch (Exception ignored) {
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    finish();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (userTimeInput.equals(DEFAULT_TIME_INPUT)) {
                    finish();
                } else {
                    if (userTimeInput.length() > 0) {
                        userTimeInput = userTimeInput.substring(0,
                                userTimeInput.length() - 1);
                        if (userTimeInput.trim().isEmpty()) {
                            userTimeInput = DEFAULT_TIME_INPUT;
                        }
                    } else {
                        userTimeInput = DEFAULT_TIME_INPUT;
                    }
                    userTimeInputTextView.setText(userTimeInput);
                    if (userTimeInput.equals(DEFAULT_TIME_INPUT)) {
                        ImageView backCloseImageView = findViewById(R.id.backCloseImageView);
                        backCloseImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                        TimerActivity.this, R.drawable.ic_close_black_24dp
                                )
                        );
                    }
                }
            }
        });

    }

    private void cancelTimer() {
        if (currentSleepTimer != null) {
            timer.cancel();
            currentSleepTimer = null;
            mNotifyMgr.cancel(NOTIFICATION_ID);
            (new CommonUtils(TimerActivity.this)).showTheToast("Music Sleep Timer is cancelled!");
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sleep Timer";
            String description = "Sleep timer until music player stops";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("timer", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void displayTime(int i) {
        // Check if current timer is empty
        if (userTimeInput.equals(DEFAULT_TIME_INPUT) || userTimeInput.equals("0")) {
            userTimeInput = Integer.toString(i);
        } else {
            userTimeInput = userTimeInput + i;
        }

        ImageView backCloseImageView = findViewById(R.id.backCloseImageView);
        backCloseImageView.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_arrow_back_black_24dp));

        // No longer than 360 minutes
        if (Integer.parseInt(userTimeInput) > 720) {
            userTimeInput = "720";
            (new CommonUtils(TimerActivity.this)).showTheToast("Cannot exceed more than 720 minutes or 12 hours");
        }
        userTimeInputTextView.setText(userTimeInput);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            currentSleepTimer = null;
            try {
                (new CommonUtils(TimerActivity.this)).showTheToast("Music Player on Sleep");
            } catch (Exception ignored) {
            }
            mNotifyMgr.cancel(NOTIFICATION_ID);
            Intent intent = new Intent(MusicPlayback.ACTION_CLOSE);
            ContextCompat.startForegroundService(TimerActivity.this,
                    Objects.requireNonNull(createExplicitFromImplicitIntent(TimerActivity.this, intent)));
            finish();
        }

        private Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
            if (resolveInfo == null || resolveInfo.size() != 1) {
                return null;
            }
            ResolveInfo serviceInfo = resolveInfo.get(0);
            String packageName = serviceInfo.serviceInfo.packageName;
            String className = serviceInfo.serviceInfo.name;
            ComponentName component = new ComponentName(packageName, className);
            Intent explicitIntent = new Intent(implicitIntent);
            explicitIntent.setComponent(component);
            return explicitIntent;
        }

    }

}
