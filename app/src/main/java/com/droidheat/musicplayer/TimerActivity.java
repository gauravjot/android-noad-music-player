package com.droidheat.musicplayer;

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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends AppCompatActivity {

    Button btnCancel;
    Button btnDone;
    TextView displayTime;
    String timeButton = "0";

    public static int time = 0;
    public static Timer timer;
    public static String Time = null;
    public static MyTimerTask myTimerTask;
    NotificationManager mNotifyMgr;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_timer);

        btnDone = findViewById(R.id.button1);
        btnCancel = findViewById(R.id.Button01);
        displayTime = findViewById(R.id.textView4);
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 1
                        displayTime(1);
                    }

                });

        if (Time != null) {
            Calendar cal = Calendar.getInstance();
            cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            String TimeNow = sdf.format(cal.getTime());

            Date d1 = null;
            Date d2 = null;
            try {
                d1 = sdf.parse(Time);
                d2 = sdf.parse(TimeNow);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                long diff = -1 * (d2.getTime() - d1.getTime());
                long diffMinutes = diff / (60 * 1000) % 60;
                displayTime.setText("" + diffMinutes);
            } catch (Exception e) {

            }
        }

        findViewById(R.id.button3).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 2
                        displayTime(2);
                    }
                });
        ;
        findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 3
                        displayTime(3);
                    }
                });
        ;
        findViewById(R.id.Button02).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 4
                        displayTime(4);
                    }
                });
        ;
        findViewById(R.id.Button03).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 5
                        displayTime(5);
                    }
                });
        ;
        findViewById(R.id.Button04).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 6
                        displayTime(6);
                    }
                });
        ;
        findViewById(R.id.Button05).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 7
                        displayTime(7);
                    }
                });
        ;
        findViewById(R.id.Button06).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 8
                        displayTime(8);
                    }
                });
        ;
        findViewById(R.id.Button07).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 9
                        displayTime(9);
                    }
                });
        ;
        findViewById(R.id.button5).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Button - 0
                        displayTime(0);
                    }
                });
        ;

        btnDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // editText.getText().toString()
                timer = new Timer();
                if (timeButton.length() != 0) {
                    try {
                        time = Integer.parseInt(timeButton) * 60 * 1000;
                        if (TimerActivity.time > 0) {
                            myTimerTask = new MyTimerTask();
                            timer.schedule(myTimerTask, TimerActivity.time);
                            Calendar cal = Calendar.getInstance();
                            cal.getTime();
                            cal.add(Calendar.MILLISECOND, time);
                            SimpleDateFormat sdf2 = new SimpleDateFormat(
                                    "hh:mm a");

                            Time = sdf2.format(cal.getTime());
                            (new CommonUtils(TimerActivity.this)).showTheToast("Music Sleep Timer Started!");

                            createNotificationChannel();

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(TimerActivity.this,"timer")
                                    .setSmallIcon(R.drawable.ic_access_time_black_48dp)
                                    .setContentTitle("Music Player Sleep Timer")
                                    .setContentText("Will sleep at " + Time)
                                    .setPriority(NotificationCompat.PRIORITY_MIN)
                                    .setSound(null);

                            Intent resultIntent = new Intent(
                                    TimerActivity.this, HomeActivity.class);
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
                            // Sets an ID for the notification
                            int mNotificationId = 1297601;
                            // Gets an instance of the NotificationManager
                            // service
                            // Builds the notification and issues it.
                            mNotifyMgr.notify(mNotificationId, mBuilder.build());
                        }
                    } catch (Exception e) {

                    }
                    finish();
                } else {
                    finish();
                    try {
                        timer.cancel();
                        (new CommonUtils(TimerActivity.this)).showTheToast("Music Sleep Cancelled!");
                    } catch (Exception e) {

                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (Integer.parseInt(timeButton) == 0) {
                    finish();
                } else {
                    if (timeButton.length() > 0) {
                        timeButton = timeButton.substring(0,
                                timeButton.length() - 1);
                        if (timeButton == null || timeButton.trim() == ""
                                || timeButton.length() == 0) {
                            timeButton = "0";
                        } else {
                        }
                        displayTime.setText(timeButton);

                    } else {
                        timeButton = "0";
                        displayTime.setText(timeButton);
                    }
                }
            }
        });

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

        String fT; // Initial Time
        fT = timeButton;

        if (Integer.parseInt(timeButton) == 0) {
            if (!Integer.toString(i).equals("0")) {
                timeButton = Integer.toString(i);
            } else {
                timeButton = null;
            }
        } else {
            timeButton = timeButton + Integer.toString(i);
        }

        try {
            if (Integer.parseInt(timeButton) > 360) {
                timeButton = fT;
                (new CommonUtils(TimerActivity.this)).showTheToast("Cannot exceed more than 360 minutes or 6 hours");
            }
        } catch (Exception ignored) {

        }
        if (Integer.parseInt(timeButton) != 0 && timeButton != null) {
            displayTime.setText(timeButton);
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            if (MusicPlayback.mMediaSessionCompat.isActive()) {
                Time = null;
                try {
                    (new CommonUtils(TimerActivity.this)).showTheToast("Music Player on Sleep");
                } catch (Exception ignored) {
                }
                mNotifyMgr.cancel(1297601);
                Intent intent = new Intent(MusicPlayback.ACTION_CLOSE);
                ContextCompat.startForegroundService(TimerActivity.this,createExplicitFromImplicitIntent(TimerActivity.this, intent));
                finish();
            }
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
