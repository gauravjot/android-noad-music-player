package com.droidheat.musicplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    String TAG = "SplashActivityLog";
    Boolean sync = false;

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
            SongsManager songsManager = new SongsManager(this);
            songsManager.sync();
            ((TextView) findViewById(R.id.textView10)).setText("Syncing..");
            sync = true;
        }
        else {
            ((TextView) findViewById(R.id.textView10)).setText("Initiating..");
        }

            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck == PermissionChecker.PERMISSION_DENIED || permissionCheck != PermissionChecker.PERMISSION_GRANTED) {
                    // No explanation needed, we can request the permission.

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Request for permissions");
                alertDialog.setMessage("For music player to work we need your permission to access" +
                        " files on your device.");
                alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SplashActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                1);
                    }
                });
                alertDialog.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alertDialog.show();
                Log.d(TAG,"asking permission");
            } else {
                new PerformBackgroundTasks().execute("task");
                Log.d(TAG,"no need for permissions");
            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new PerformBackgroundTasks().execute("tasks");
                    //weGotPermissions();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //TODO: if user has set to deny permission always, ask to go to settings
                    Toast.makeText(this, "Application needs permission to run. Exiting!", Toast.LENGTH_SHORT).show();
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class PerformBackgroundTasks extends AsyncTask<String, Integer, Long> {

        @Override
        protected Long doInBackground(String... params) {
            SongsManager songsManager = new SongsManager(SplashActivity.this);

            ArrayList<HashMap<String,String>> artists = songsManager.artists();
            (new SharedPrefsUtils(SplashActivity.this)).writeSharedPrefs("home_artist",
                    artists.get((new Random()).nextInt(artists.size())).get("artist"));

            // -- Creating Playlist
            Playlist playlist = new Playlist(SplashActivity.this);
            playlist.open();
            if (playlist.getCount() == 0) {
                songsManager.addPlaylist("Playlist 1");
            }
            playlist.close();

            if(sync) {

                for (int s = 0; s < songsManager.getAllPlaylists().size(); s++) {
                    int playlistID = Integer.parseInt(songsManager.getAllPlaylists().get(s).get("ID"));
                    ArrayList<SongModel> playListSongs =
                            songsManager.playlistSongs(playlistID);

                    if (!playListSongs.isEmpty()) {
                        for (int j = 0; j < playListSongs.size(); j++) {
                            Log.d(TAG, "Playlist: Search if current song " + j + " is not similar with song in new songs list");
                            if (!songsManager.allSongs().contains(playListSongs.get(j))) {
                                Log.d(TAG, "Playlist: current playlist song doesn't exist in allSongs," +
                                        " so lets see if only path is changed or user has moved the song");
                                boolean isFound = false;
                                for (int k = 0; k < songsManager.allSongs().size(); k++) {
                                    if ((songsManager.allSongs().get(k).getTitle() +
                                            songsManager.allSongs().get(k).getDuration())
                                            .equals(playListSongs.get(j).getTitle() +
                                                    playListSongs.get(j).getDuration())) {
                                        Log.d(TAG, "Playlist: song " + j + " does exist and is probably moved," +
                                                " so lets change broken song with lasted");
                                        playListSongs.remove(j);
                                        playListSongs.add(j, songsManager.allSongs().get(k));
                                        Log.d(TAG, "Playlist: index doesn't change and we changed broken song. All good!");
                                        isFound = true;
                                        k = songsManager.allSongs().size();
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
                        songsManager.updatePlaylistSongs(playlistID,
                                playListSongs);
                        Log.d(TAG, "Playlist: done!");
                    }

                    //Todo Re-add song but change fields

                }

                // -- Checking Favourites
                ArrayList<SongModel> favSongs =
                        new ArrayList<>(songsManager.favouriteSongs());
                if (!favSongs.isEmpty()) {
                    Log.d(TAG, "Favourites: Search if current hashMap is not similar with song in new songs list");
                    for (int j = 0; j < favSongs.size(); j++) {
                        if (!songsManager.allSongs().contains(favSongs.get(j))) {
                            Log.d(TAG, "Favourites: current favourite doesn't exist in allSongs," +
                                    " so lets see if only path is changed or user has moved the song");
                            boolean isFound = false;
                            for (int i = 0; i < songsManager.allSongs().size(); i++) {
                                if ((songsManager.allSongs().get(i).getTitle() +
                                        songsManager.allSongs().get(i).getDuration())
                                        .equals(favSongs.get(j).getTitle() +
                                                favSongs.get(j).getDuration())) {
                                    Log.d(TAG, "Favourites: songs does exist and is probably moved," +
                                            " so lets change broken song with lasted");
                                    favSongs.remove(j);
                                    favSongs.add(j, songsManager.allSongs().get(i));
                                    Log.d(TAG, "Favourites: index doesn't change and we changed broken song. All good");
                                    isFound = true;
                                    i = songsManager.allSongs().size();
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
                    songsManager.updateFavouritesList(favSongs);
                }

                // -- Checking Most Played
                ArrayList<SongModel> mostPlayed =
                        songsManager.mostPlayedSongs();
                if (!mostPlayed.isEmpty()) {
                    Log.d(TAG, "MostPlayed: Search if current hashMap is not similar with song in new songs list");
                    for (int j = 0; j < mostPlayed.size(); j++) {
                        if (!songsManager.allSongs().contains(mostPlayed.get(j))) {
                            Log.d(TAG, "MostPlayed: current song " + j + " doesn't exist in allSongs," +
                                    " so lets see if only path is changed or user has moved the song");
                            boolean isFound = false;
                            for (int i = 0; i < songsManager.allSongs().size(); i++) {
                                if ((songsManager.allSongs().get(i).getTitle() +
                                        songsManager.allSongs().get(i).getDuration())
                                        .equals(mostPlayed.get(j).getTitle() +
                                                mostPlayed.get(j).getDuration())) {
                                    Log.d(TAG, "MostPlayed: songs does exist and is probably moved," +
                                            " so lets change broken song with lasted");
                                    mostPlayed.remove(j);
                                    mostPlayed.add(j, songsManager.allSongs().get(i));
                                    Log.d(TAG, "MostPlayed: index doesn't change and we changed broken song. All good!");
                                    isFound = true;
                                    i = songsManager.allSongs().size();
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
                    songsManager.updateMostPlayedList(mostPlayed);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //setUpdatedTextView(values[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            startActivity(new Intent(SplashActivity.this,
                    HomeActivity.class));
            finish();
        }
    }


}
