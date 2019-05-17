package com.droidheat.musicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

public class MusicWidgetv2 extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews views;
        try {
            Intent intent = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            views = new RemoteViews(context.getPackageName(), R.layout.widget4x1v2);


            Intent previousIntent = new Intent(context, MusicPlayback.class);
            Intent playIntent = new Intent(context, MusicPlayback.class);
            Intent nextIntent = new Intent(context, MusicPlayback.class);

            previousIntent.setAction(MusicPlayback.ACTION_TRACK_PREV);
            playIntent.setAction(MusicPlayback.ACTION_PLAY_PAUSE);
            nextIntent.setAction(MusicPlayback.ACTION_TRACK_NEXT);

            PendingIntent ppreviousIntent = PendingIntent.getService(context, 0,
                    previousIntent, 0);
            PendingIntent pplayIntent = PendingIntent.getService(context, 0,
                    playIntent, 0);
            PendingIntent pnextIntent = PendingIntent.getService(context, 0,
                    nextIntent, 0);

            views.setOnClickPendingIntent(R.id.imageView3, pplayIntent);
            views.setOnClickPendingIntent(R.id.imageView2, ppreviousIntent);
            views.setOnClickPendingIntent(R.id.imageView4, pnextIntent);
            views.setOnClickPendingIntent(R.id.imageView1, pendingIntent);
            views.setOnClickPendingIntent(R.id.heading, pendingIntent);
            views.setOnClickPendingIntent(R.id.textalbum, pendingIntent);
            views.setOnClickPendingIntent(R.id.texttime, pendingIntent);

            try {
                if (MusicPlayback.mMediaSessionCompat.isActive()) {
                    if (MusicPlayback.mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        views.setImageViewResource(R.id.imageView3, R.drawable.app_pause);
                    } else {
                        views.setImageViewResource(R.id.imageView3, R.drawable.app_play);
                    }
                }
            } catch (Exception ignored) {

            }

            if (MusicPlayback.mMediaSessionCompat.getController().getMetadata().getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART) != null) {
                views.setImageViewBitmap(R.id.imageView1, MusicPlayback.mMediaSessionCompat.getController().getMetadata().getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
            }
            views.setTextViewText(R.id.heading, MusicPlayback.mMediaSessionCompat.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            views.setTextViewText(R.id.textalbum, MusicPlayback.mMediaSessionCompat.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
            views.setTextViewText(R.id.texttime, MusicPlayback.mMediaSessionCompat.getController().getMetadata().getString(MediaMetadataCompat.METADATA_KEY_DURATION));
        } catch (Exception e) {
            views = new RemoteViews(context.getPackageName(), R.layout.widgetblank);
            Intent intent = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.heading, pendingIntent);

        }
        try {
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        } catch (Exception ignored) {
        }
    }


}
