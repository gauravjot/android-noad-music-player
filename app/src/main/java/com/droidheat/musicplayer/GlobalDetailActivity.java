package com.droidheat.musicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GlobalDetailActivity extends AppCompatActivity {

    RecyclerViewAdapter adapter;
    ArrayList<SongModel> songsList = new ArrayList<>();
    String field = "albums", raw = "A Sky Full Of Stars";
    String TAG = "GlobalActivityConsole";
    PerformBackgroundTasks performBackgroundTasks = null;
    SongsManager songsManager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_detail);

        Toolbar toolbar = findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);

        int accentColor = (new CommonUtils(this)).accentColor(new SharedPrefsUtils(this));

        ((TextView) findViewById(R.id.category)).setTextColor(ContextCompat.getColor(this,accentColor));
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,accentColor)));

        performBackgroundTasks = new PerformBackgroundTasks();


        raw = Objects.requireNonNull(getIntent().getExtras()).getString("name");
        field = getIntent().getExtras().getString("field");

        findViewById(R.id.spinner).setVisibility(View.INVISIBLE);

        ((TextView) findViewById(R.id.title)).setText(raw);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        songsManager = new SongsManager(this);
        setListData();

        ((TextView) findViewById(R.id.listInfoTextView))
                .setText("total tracks: " + songsList.size() +
                ", playback time: " + getPlayBackTime(songsList) + " mins");

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsManager.play(0, songsList);
            }
        });

        if (getResources().getBoolean(R.bool.isLandscape)) {
            getSupportActionBar().setTitle(" ");
        } else {
            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
            collapsingToolbar.setTitle(" ");
            collapsingToolbar.setContentScrimColor(ContextCompat.getColor(this, R.color.actionBar));
        }

        ImageView header = findViewById(R.id.header);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < songsList.size(); i++) {
            list.add(songsList.get(i).getAlbumID());
        }
        (new ImageUtils(this)).getImageByPicasso(list, header, 0, list.size() - 1);
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
                songsList.addAll(songsManager.albumSongs(raw));
                ((TextView) findViewById(R.id.category)).setText("ALBUM");
                break;
            case "mostplayed":
                songsList.addAll(songsManager.mostPlayedSongs());
                ((TextView) findViewById(R.id.category)).setText("AUTO-PLAYLIST");
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING) performBackgroundTasks.execute();
                break;
            case "favourites":
                songsList.addAll(songsManager.favouriteSongs());
                ((TextView) findViewById(R.id.category)).setText("AUTO-PLAYLIST");
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING) performBackgroundTasks.execute();
                break;
            case "artists":
                songsList.addAll(songsManager.artistSongs(raw));
                ((TextView) findViewById(R.id.category)).setText("ARTIST");
                break;
            case "recent":
                songsList.addAll(songsManager.newSongs());
                ((TextView) findViewById(R.id.category)).setText("RECENTLY ADDED");
                break;
            default:
                songsList.addAll(songsManager.playlistSongs(Integer.parseInt(field)));
                ((TextView) findViewById(R.id.category)).setText("PLAYLIST");
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
            case R.id.play:
                songsManager.play(0, songsList);
                break;
            case R.id.play_next:
                if (songsList.size() > 0) {
                    songsManager.playNext(songsList);
                    (new CommonUtils(this)).showTheToast("List added for playing next");
                } else {
                    (new CommonUtils(this)).showTheToast("Error adding empty song list to queue");
                }
                break;
            case R.id.shuffle:
                songsManager.shufflePlay(songsList);
                break;
            case R.id.add_to_queue:
                songsManager.addToQueue(songsList);
                break;
            case android.R.id.home:
                if (performBackgroundTasks.getStatus() == AsyncTask.Status.RUNNING) {
                    performBackgroundTasks.cancel(true);
                }
                finish();
                break;
            case R.id.repair_list:
                findViewById(R.id.spinner).setVisibility(View.VISIBLE);
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING) {
                    performBackgroundTasks = new PerformBackgroundTasks();
                    performBackgroundTasks.execute();
                }
                break;
            case R.id.add_to_playlist:
                songsManager.addToPlaylist(songsList);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (performBackgroundTasks.getStatus() == AsyncTask.Status.RUNNING) {
            performBackgroundTasks.cancel(true);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (performBackgroundTasks.getStatus() == AsyncTask.Status.RUNNING) {
            performBackgroundTasks.cancel(true);
        }
    }

    private class PerformBackgroundTasks extends AsyncTask<String, Integer, Long> {

        @Override
        protected Long doInBackground(String... params) {
            SongsManager songsManager = new SongsManager(GlobalDetailActivity.this);

            // -- Creating Playlist
//            Playlist playlist = new Playlist(SplashActivity.this);
//            playlist.open();
//            if (playlist.getCount() == 0) {
//                songsManager.addPlaylist("Playlist 1");
//            }
//            playlist.close();

            // -- Checking empty playlist or broken songs
            if (isInteger(field)) {

                int playlistID = Integer.parseInt(field);
                ArrayList<SongModel> playListSongs =
                        songsManager.playlistSongs(playlistID);

                if (!playListSongs.isEmpty()) {
                    for (int j = 0; j < playListSongs.size(); j++) {
                        if (isCancelled()) break;
                        Log.d(TAG, "Playlist: Search if current song "+j+" is not similar with song in new songs list");
                        if (!songsManager.allSongs().contains(playListSongs.get(j))) {
                            Log.d(TAG, "Playlist: current playlist song doesn't exist in allSongs," +
                                    " so lets see if only path is changed or user has moved the song");
                            boolean isFound = false;
                            for (int k = 0; k < songsManager.allSongs().size(); k++) {
                                if ((songsManager.allSongs().get(k).getTitle() +
                                        songsManager.allSongs().get(k).getDuration())
                                        .equals(playListSongs.get(j).getTitle() +
                                                playListSongs.get(j).getDuration())) {
                                    Log.d(TAG, "Playlist: song "+j+" does exist and is probably moved," +
                                            " so lets change broken song with lasted");
                                    playListSongs.remove(j);
                                    playListSongs.add(j, songsManager.allSongs().get(k));
                                    Log.d(TAG, "Playlist: index doesn't change and we changed broken song. All good!");
                                    isFound = true;
                                    k = songsManager.allSongs().size();
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
                    songsManager.updatePlaylistSongs(playlistID,
                            playListSongs);
                    Log.d(TAG, "Playlist: done!");
                }
            } else if (field.equals("favourites")) {
                //Todo Re-add song but change fields

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
                    songsManager.updateFavouritesList(favSongs);
                }
            } else if (field.equals("mostplayed")) {

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
                    songsManager.updateMostPlayedList(mostPlayed);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Long aLong) {
                    findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
                    findViewById(R.id.recyclerView).setActivated(true);
                    setListData();
                    adapter.notifyDataSetChanged();
            Log.d(TAG, "MostPlayed: AsyncTask Done!");
        }
    }

    public boolean isInteger(String str) {
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
