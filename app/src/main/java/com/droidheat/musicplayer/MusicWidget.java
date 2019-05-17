package com.droidheat.musicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

public class MusicWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		RemoteViews views;
		SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(context);
		Intent intent = new Intent(context,HomeActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		views = new RemoteViews(context.getPackageName(),R.layout.widget4x1);


		Intent previousIntent = new Intent(context, MusicPlayback.class);
		Intent playIntent = new Intent(context, MusicPlayback.class);
		Intent nextIntent = new Intent(context, MusicPlayback.class);
		previousIntent.setAction(MusicPlayback.ACTION_TRACK_PREV);

		playIntent.setAction(MusicPlayback.ACTION_PLAY_PAUSE);

		nextIntent.setAction(MusicPlayback.ACTION_TRACK_NEXT);

		PendingIntent pPreviousIntent = PendingIntent.getService(context, 0,
				previousIntent, 0);
		PendingIntent pPlayIntent = PendingIntent.getService(context, 0,
				playIntent, 0);
		PendingIntent pNextIntent = PendingIntent.getService(context, 0,
				nextIntent, 0);

		views.setOnClickPendingIntent(R.id.imageView3, pPlayIntent);
		views.setOnClickPendingIntent(R.id.imageView2, pPreviousIntent);
		views.setOnClickPendingIntent(R.id.imageView4, pNextIntent);
		views.setOnClickPendingIntent(R.id.imageView1, pendingIntent);
		views.setOnClickPendingIntent(R.id.relativeLayout,pendingIntent);

            try {
				if (MusicPlayback.mMediaSessionCompat.isActive()) {
					if (MusicPlayback.mMediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
						views.setImageViewResource(R.id.imageView3, R.drawable.app_pause);
					} else {
						views.setImageViewResource(R.id.imageView3, R.drawable.app_play);
					}
				} else {
					views.setImageViewResource(R.id.imageView3, R.drawable.app_play);
				}
            }
            catch (Exception ignored) {

            }

		views.setImageViewBitmap(R.id.imageView1, (new ImageUtils(context)).getAlbumArt(
				Long.parseLong(sharedPrefsUtils.readSharedPrefsString("albumid","0"))
		));
		views.setTextViewText(R.id.textTitle, sharedPrefsUtils.readSharedPrefsString("title","Unknown Title"));
		views.setTextViewText(R.id.textAlbum, sharedPrefsUtils.readSharedPrefsString("album","Unknown Album"));
		views.setTextViewText(R.id.textArtist, sharedPrefsUtils.readSharedPrefsString("artist","00:00"));

        try {
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
        catch (Exception ignored) {}
	}

	
}
