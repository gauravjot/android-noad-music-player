package com.droidheat.musicplayer.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidheat.musicplayer.ui.callbacks.AsyncTaskCompletionCallback;
import com.droidheat.musicplayer.utils.ImageUtils;
import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.ui.adapters.RecyclerViewAdapter;
import com.droidheat.musicplayer.utils.SharedPrefsUtils;
import com.droidheat.musicplayer.models.SongModel;
import com.droidheat.musicplayer.utils.SongsUtils;
import com.droidheat.musicplayer.utils.CommonUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

public class GlobalDetailActivity extends AppCompatActivity implements AsyncTaskCompletionCallback {

    RecyclerViewAdapter adapter;
    ArrayList<SongModel> songsList = new ArrayList<>();
    String field = "albums", raw = "A Sky Full Of Stars";
    PerformBackgroundTasks performBackgroundTasks = null;
    SongsUtils songsUtils;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_detail);

        Toolbar toolbar = findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);

        int accentColor = (new CommonUtils(this)).accentColor(new SharedPrefsUtils(this));

        ((TextView) findViewById(R.id.category)).setTextColor(ContextCompat.getColor(this,accentColor));

        field = Objects.requireNonNull(getIntent().getExtras()).getString("field");
        raw = Objects.requireNonNull(getIntent().getExtras()).getString("name");

        performBackgroundTasks = new PerformBackgroundTasks(this, this, field);

        findViewById(R.id.spinner).setVisibility(View.INVISIBLE);

        ((TextView) findViewById(R.id.title)).setText(raw);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        songsUtils = new SongsUtils(this);
        setListData();

        int playbackTime = getPlayBackTime(songsList);
        String netPlayback = playbackTime +" mins";
        if (playbackTime > 60) {
            netPlayback = playbackTime / 60 + "h " + playbackTime % 60 + "m";
        }
        int numSongs = songsList.size();
        if (numSongs > 0) {
            ((TextView) findViewById(R.id.listInfoTextView))
                    .setText(numSongs + ((songsList.size() > 1) ? " tracks, " : " track, ") +
            netPlayback + " playback");
        }

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsUtils.play(0, songsList);
            }
        });

        findViewById(R.id.shuffle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsUtils.shufflePlay(songsList);
            }
        });

        if (getResources().getBoolean(R.bool.isLandscape)) {
            getSupportActionBar().setTitle(" ");
        } else {
            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
            collapsingToolbar.setTitle(" ");
            collapsingToolbar.setContentScrimColor(ContextCompat.getColor(this, R.color.primaryColor));
        }

        ImageView albumArtImageView = findViewById(R.id.header);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        (new ImageUtils(this)).setAlbumArt(songsList, albumArtImageView);
        adapter = new RecyclerViewAdapter(songsList, this, field);
        recyclerView.setAdapter(adapter);
    }

    /*
     * Gives output in minutes, getDuration() format is mm:ss
     */
    private int getPlayBackTime(ArrayList<SongModel> albumSongs) {
        int pTime = 0;
        for (int i =0; i < albumSongs.size(); i ++) {
            String duration = albumSongs.get(i).getDuration();
            pTime += Integer.parseInt(duration.split(":")[0]) * 60 +
                    Integer.parseInt(duration.split(":")[1]);
        }
        return pTime / 60;
    }

    /*
     * Grabbing data for RecyclerView
     */

    public void setListData() {
        songsList.clear();
        switch (field) {
            case "albums":
                songsList.addAll(songsUtils.albumSongs(raw));
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.album_cap));
                break;
            case "mostplayed":
                songsList.addAll(songsUtils.mostPlayedSongs());
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.auto_plalist_cap));
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING) performBackgroundTasks.execute();
                break;
            case "favourites":
                songsList.addAll(songsUtils.favouriteSongs());
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.auto_plalist_cap));
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING) performBackgroundTasks.execute();
                break;
            case "artists":
                songsList.addAll(songsUtils.artistSongs(raw));
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.artist_cap));
                break;
            case "recent":
                songsList.addAll(songsUtils.newSongs());
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.recently_added_cap));
                break;
            default:
                songsList.addAll(songsUtils.playlistSongs(Integer.parseInt(field)));
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.playlist_cap));
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING) performBackgroundTasks.execute();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global_activity, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_searchBtn:
                Intent intent = new Intent(GlobalDetailActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.play_next:
                if (songsList.size() > 0) {
                    songsUtils.playNext(songsList);
                    (new CommonUtils(this)).showTheToast("List added for playing next");
                } else {
                    (new CommonUtils(this)).showTheToast("Error adding empty song list to queue");
                }
                break;
            case R.id.add_to_queue:
                songsUtils.addToQueue(songsList);
                break;
            case android.R.id.home:
                if (performBackgroundTasks.getStatus() == AsyncTask.Status.RUNNING) {
                    performBackgroundTasks.cancel(true);
                }
                backPressed();
                break;
            case R.id.add_to_playlist:
                songsUtils.addToPlaylist(songsList);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            backPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    private void backPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (performBackgroundTasks.getStatus() == AsyncTask.Status.RUNNING) {
            performBackgroundTasks.cancel(true);
        }
    }

    @Override
    public void updateViews() {
        findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
        findViewById(R.id.recyclerView).setActivated(true);
        setListData();
        adapter.notifyDataSetChanged();
    }

    private static class PerformBackgroundTasks extends AsyncTask<String, Integer, Long> {

        private SongsUtils songsUtils;
        private String field;
        private String TAG = "GlobalActivityAsyncTaskConsole";
        private AsyncTaskCompletionCallback callback;

        PerformBackgroundTasks(AsyncTaskCompletionCallback callback, Activity activity, String field) {
            this.songsUtils = new SongsUtils(activity);
            this.callback = callback;
            this.field = field;
        }
        @Override
        protected Long doInBackground(String... params) {

            // -- Checking empty playlist or broken songs
            if (isInteger(field)) {

                int playlistID = Integer.parseInt(field);
                ArrayList<SongModel> playListSongs =
                        songsUtils.playlistSongs(playlistID);

                if (!playListSongs.isEmpty()) {
                    for (int j = 0; j < playListSongs.size(); j++) {
                        if (isCancelled()) break;
                        Log.d(TAG, "Playlist: Search if current song "+j+" is not similar with song in new songs list");
                        if (!songsUtils.allSongs().contains(playListSongs.get(j))) {
                            Log.d(TAG, "Playlist: current playlist song doesn't exist in allSongs," +
                                    " so lets see if only path is changed or user has moved the song");
                            boolean isFound = false;
                            for (int k = 0; k < songsUtils.allSongs().size(); k++) {
                                if ((songsUtils.allSongs().get(k).getTitle() +
                                        songsUtils.allSongs().get(k).getDuration())
                                        .equals(playListSongs.get(j).getTitle() +
                                                playListSongs.get(j).getDuration())) {
                                    Log.d(TAG, "Playlist: song "+j+" does exist and is probably moved," +
                                            " so lets change broken song with lasted");
                                    playListSongs.remove(j);
                                    playListSongs.add(j, songsUtils.allSongs().get(k));
                                    Log.d(TAG, "Playlist: index doesn't change and we changed broken song. All good!");
                                    isFound = true;
                                    k = songsUtils.allSongs().size();
                                }
                                if (isCancelled()) {
                                    break; // REMOVE IF NOT USED IN A FOR LOOP
                                }
                            }
                            if (!isFound) {
                                Log.d(TAG, "Playlist: "+j+" song is deleted from device");
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
            } else if (field.equals("favourites")) {

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
                                if (isCancelled()) break;
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
                        if (isCancelled()) {
                            break; // REMOVE IF NOT USED IN A FOR LOOP
                        }
                    }
                    // Update favourite songs list
                    Log.d(TAG, "Favourites: done!");
                    songsUtils.updateFavouritesList(favSongs);
                }
            } else if (field.equals("mostplayed")) {

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
                                    if (isCancelled()) break;
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
                        if (isCancelled()) {
                            break; // REMOVE IF NOT USED IN A FOR LOOP
                        }
                    }
                    // Update favourite songs list
                    Log.d(TAG, "MostPlayed: done!");
                    songsUtils.updateMostPlayedList(mostPlayed);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Long aLong) {
            callback.updateViews();
            Log.d(TAG, "AsyncTask Done!");
        }

        private boolean isInteger(String str) {
            if (str == null) {
                return false;
            }
            int length = str.length();
            if (length == 0) {
                return false;
            }
            int i = 0;
            if (str.charAt(0) == '-') {
                if (length == 1) {
                    return false;
                }
                i = 1;
            }
            for (; i < length; i++) {
                char c = str.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            return true;
        }

    }

}
