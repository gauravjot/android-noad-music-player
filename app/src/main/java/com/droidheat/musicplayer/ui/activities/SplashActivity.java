package com.droidheat.musicplayer.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.database.Playlist;
import com.droidheat.musicplayer.models.SongModel;
import com.droidheat.musicplayer.utils.CommonUtils;
import com.droidheat.musicplayer.utils.SharedPrefsUtils;
import com.droidheat.musicplayer.utils.SongsUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    String TAG = "SplashActivityLog";
    Boolean sync = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        ProgressBar spinner = findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, (new CommonUtils(this).accentColor(new SharedPrefsUtils(this)))),
                PorterDuff.Mode.MULTIPLY);

        if ((getIntent().getBooleanExtra("sync", false))) {
            SongsUtils songsUtils = new SongsUtils(this);
            songsUtils.sync();
            ((TextView) findViewById(R.id.textView10)).setText("Syncing..");
            sync = true;
        } else {
            ((TextView) findViewById(R.id.textView10)).setText("Initiating..");
        }

        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Request for permissions");
                alertDialog.setMessage("For music player to work we need your permission to access" +
                        " files on your device.");
                alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SplashActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                1);
                    }
                });
                alertDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alertDialog.show();
                Log.d(TAG, "asking permission");
            } else if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                new PerformBackgroundTasks(this, sync).execute("task");
            } else {
                (new CommonUtils(this)).showTheToast("Please enable permission from " +
                        "Settings > Apps > Noad Player > Permissions.");
            }
        } else {
            new PerformBackgroundTasks(this, sync).execute("task");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                new PerformBackgroundTasks(this, sync).execute("tasks");
                //weGotPermissions();
                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {
                Toast.makeText(this, "Application needs permission to run. Go to Settings > Apps > " +
                        "Noad Player to allow permission.", Toast.LENGTH_SHORT).show();
                finish();
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private static class PerformBackgroundTasks extends AsyncTask<String, Integer, Long> {

        private WeakReference<Activity> weakReference;
        private Boolean sync;
        private String TAG = "SplashActivityAsyncTaskLog";
        private SongsUtils songsUtils;
        private SharedPrefsUtils sharedPrefsUtils;
        private Playlist playlist;

        PerformBackgroundTasks(Activity activity, Boolean sync) {
            this.weakReference = new WeakReference<>(activity);
            this.sync = sync;
            this.songsUtils = new SongsUtils(activity);
            this.sharedPrefsUtils = new SharedPrefsUtils(activity);
            this.playlist = new Playlist(activity);
        }

        @Override
        protected Long doInBackground(String... params) {

            ArrayList<HashMap<String, String>> artists = songsUtils.artists();
            if (artists.size() > 0) {
                sharedPrefsUtils.writeSharedPrefs("home_artist",
                        artists.get((new Random()).nextInt(artists.size())).get("artist"));
            }

            try {
                // -- Creating Playlist
                playlist.open();
                if (playlist.getCount() == 0) {
                    songsUtils.addPlaylist("Playlist 1");
                }
                playlist.close();

                if (sync) {

                    for (int s = 0; s < songsUtils.getAllPlaylists().size(); s++) {
                        int playlistID = Integer.parseInt(Objects.requireNonNull(songsUtils.getAllPlaylists().get(s).get("ID")));
                        ArrayList<SongModel> playListSongs =
                                songsUtils.playlistSongs(playlistID);

                        if (!playListSongs.isEmpty()) {
                            for (int j = 0; j < playListSongs.size(); j++) {
                                Log.d(TAG, "Playlist: Search if current song " + j + " is not similar with song in new songs list");
                                if (!songsUtils.allSongs().contains(playListSongs.get(j))) {
                                    Log.d(TAG, "Playlist: current playlist song doesn't exist in allSongs," +
                                            " so lets see if only path is changed or user has moved the song");
                                    boolean isFound = false;
                                    for (int k = 0; k < songsUtils.allSongs().size(); k++) {
                                        if ((songsUtils.allSongs().get(k).getTitle() +
                                                songsUtils.allSongs().get(k).getDuration())
                                                .equals(playListSongs.get(j).getTitle() +
                                                        playListSongs.get(j).getDuration())) {
                                            Log.d(TAG, "Playlist: song " + j + " does exist and is probably moved," +
                                                    " so lets change broken song with lasted");
                                            playListSongs.remove(j);
                                            playListSongs.add(j, songsUtils.allSongs().get(k));
                                            Log.d(TAG, "Playlist: index doesn't change and we changed broken song. All good!");
                                            isFound = true;
                                            k = songsUtils.allSongs().size();
                                        }
                                    }
                                    if (!isFound) {
                                        Log.d(TAG, "Playlist: " + j + " song is deleted from device");
                                        playListSongs.remove(j);
                                        Log.d(TAG, "Playlist: since a song is removed," +
                                                " on doing next song loop will skip one song");
                                        j--;
                                        Log.d(TAG, "Playlist: j-- to ensure for loop stays on same song");
                                    }
                                } else {
                                    Log.d(TAG, "Playlist: Song " + j + " is okay");
                                }
                                if (isCancelled()) {
                                    break; // REMOVE IF NOT USED IN A FOR LOOP
                                }
                            }
                            // Update favourite songs list
                            songsUtils.updatePlaylistSongs(playlistID,
                                    playListSongs);
                            Log.d(TAG, "Playlist: done!");
                        }

                    }

                    // -- Checking Favourites
                    ArrayList<SongModel> favSongs =
                            new ArrayList<>(songsUtils.favouriteSongs());
                    if (!favSongs.isEmpty()) {
                        Log.d(TAG, "Favourites: Search if current hashMap is not similar with song in new songs list");
                        for (int j = 0; j < favSongs.size(); j++) {
                            if (!songsUtils.allSongs().contains(favSongs.get(j))) {
                                Log.d(TAG, "Favourites: current favourite doesn't exist in allSongs," +
                                        " so lets see if only path is changed or user has moved the song");
                                boolean isFound = false;
                                for (int i = 0; i < songsUtils.allSongs().size(); i++) {
                                    if ((songsUtils.allSongs().get(i).getTitle() +
                                            songsUtils.allSongs().get(i).getDuration())
                                            .equals(favSongs.get(j).getTitle() +
                                                    favSongs.get(j).getDuration())) {
                                        Log.d(TAG, "Favourites: songs does exist and is probably moved," +
                                                " so lets change broken song with lasted");
                                        favSongs.remove(j);
                                        favSongs.add(j, songsUtils.allSongs().get(i));
                                        Log.d(TAG, "Favourites: index doesn't change and we changed broken song. All good");
                                        isFound = true;
                                        i = songsUtils.allSongs().size();
                                    }
                                }
                                if (!isFound) {
                                    Log.d(TAG, "Favourites: songs is deleted from device");
                                    favSongs.remove(j);
                                    Log.d(TAG, "Favourites: since a song is removed," +
                                            " on doing next song loop will skip one song");
                                    j--;
                                    Log.d(TAG, "Favourites: j-- to ensure for loop stays on same song");
                                }
                            }
                        }
                        // Update favourite songs list
                        Log.d(TAG, "Favourites: done!");
                        songsUtils.updateFavouritesList(favSongs);
                    }

                    // -- Checking Most Played
                    ArrayList<SongModel> mostPlayed =
                            songsUtils.mostPlayedSongs();
                    if (!mostPlayed.isEmpty()) {
                        Log.d(TAG, "MostPlayed: Search if current hashMap is not similar with song in new songs list");
                        for (int j = 0; j < mostPlayed.size(); j++) {
                            if (!songsUtils.allSongs().contains(mostPlayed.get(j))) {
                                Log.d(TAG, "MostPlayed: current song " + j + " doesn't exist in allSongs," +
                                        " so lets see if only path is changed or user has moved the song");
                                boolean isFound = false;
                                for (int i = 0; i < songsUtils.allSongs().size(); i++) {
                                    if ((songsUtils.allSongs().get(i).getTitle() +
                                            songsUtils.allSongs().get(i).getDuration())
                                            .equals(mostPlayed.get(j).getTitle() +
                                                    mostPlayed.get(j).getDuration())) {
                                        Log.d(TAG, "MostPlayed: songs does exist and is probably moved," +
                                                " so lets change broken song with lasted");
                                        mostPlayed.remove(j);
                                        mostPlayed.add(j, songsUtils.allSongs().get(i));
                                        Log.d(TAG, "MostPlayed: index doesn't change and we changed broken song. All good!");
                                        isFound = true;
                                        i = songsUtils.allSongs().size();
                                    }
                                }
                                if (!isFound) {
                                    Log.d(TAG, "MostPlayed: songs is deleted from device");
                                    mostPlayed.remove(j);
                                    Log.d(TAG, "MostPlayed: since a song is removed," +
                                            " on doing next song loop will skip one song");
                                    j--;
                                    Log.d(TAG, "MostPlayed: j-- to ensure for loop stays on same song");
                                }
                            }
                        }
                        // Update favourite songs list
                        Log.d(TAG, "MostPlayed: done!");
                        songsUtils.updateMostPlayedList(mostPlayed);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Unable to perform sync");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //setUpdatedTextView(values[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            weakReference.get().startActivity(new Intent(weakReference.get(),
                    HomeActivity.class));
            weakReference.get().finish();
        }
    }


}