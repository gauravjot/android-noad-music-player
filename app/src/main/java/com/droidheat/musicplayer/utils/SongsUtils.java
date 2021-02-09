package com.droidheat.musicplayer.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;

import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.database.CategorySongs;
import com.droidheat.musicplayer.database.FavouriteList;
import com.droidheat.musicplayer.database.Playlist;
import com.droidheat.musicplayer.database.PlaylistSongs;
import com.droidheat.musicplayer.models.SongModel;
import com.droidheat.musicplayer.services.MusicPlayback;
import com.droidheat.musicplayer.ui.adapters.PlaylistFragmentAdapterSimple;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class SongsUtils {

    private String TAG = "SongsManagerConsole";

    private Context context;
    private SharedPrefsUtils sharedPrefsUtils;
    private static ArrayList<SongModel> mainList = new ArrayList<>();
    private static ArrayList<SongModel> queue = new ArrayList<>();
    private static ArrayList<HashMap<String, String>> albums = new ArrayList<>();
    private static ArrayList<HashMap<String, String>> artists = new ArrayList<>();

    public SongsUtils(Context context) {
        this.context = context;
        sharedPrefsUtils = new SharedPrefsUtils(context);
        grabIfEmpty();
        if (queue.isEmpty()) {
            try {
                Type type = new TypeToken<ArrayList<SongModel>>() {
                }.getType();
                ArrayList<SongModel> restoreData = new Gson().fromJson(sharedPrefsUtils.readSharedPrefsString("key", null), type);
                replaceQueue(restoreData);
                Log.d(TAG, "Retrieved queue from storage in SongsUtils. " + restoreData.size() + " songs!");
            } catch (Exception e) {
                Log.d(TAG, "Unable to retrieve data while queue is empty.");
            }
        }
    }

    public int getCurrentMusicID() {
        int musicID = sharedPrefsUtils.readSharedPrefsInt("musicID", 0);
        return (musicID > -1) ? musicID : 0;
    }

    public int getMainListSize() {
        return mainList.size();
    }

    public void setCurrentMusicID(int musicID) {
        sharedPrefsUtils.writeSharedPrefs("musicID",musicID);
    }

    public ArrayList<SongModel> queue() {
        if (queue.isEmpty()) {
            ArrayList<SongModel> list = new ArrayList<>(mainList);
            Collections.reverse(list);
            replaceQueue(list);
        }
        return (queue);
    }

    public ArrayList<SongModel> allSongs() {
        grabIfEmpty(); // If no song in list

        // Sorted list of 0-9 A-Z
        ArrayList<SongModel> songs = new ArrayList<>(mainList);
        Collections.sort(songs, new Comparator<SongModel>() {
            @Override
            public int compare(SongModel song1, SongModel song2) {
                return song1.getTitle().compareTo(song2.getTitle());
            }
        });
        return songs;
    }

    public ArrayList<SongModel> newSongs() {
        grabIfEmpty(); // If no song in list

        ArrayList<SongModel> list = new ArrayList<>(mainList);
        Collections.reverse(list);
        return list;
    }

    public ArrayList<HashMap<String, String>> albums() {
        grabIfEmpty();
        return albums;
    }

    public ArrayList<SongModel> albumSongs(String album) {
        ArrayList<SongModel> songs = new ArrayList<>();
        ArrayList<SongModel> list = new ArrayList<>(mainList);
        Collections.reverse(list);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAlbum().equals(album)) {
                songs.add(list.get(i));
            }
        }
        return songs;
    }

    public ArrayList<HashMap<String, String>> artists() {
        grabIfEmpty();
        return artists;
    }

    public List<String> getAlbumIds(String rawAlbumIds) {
        String SPLIT_EXPRESSION = ";,,;,;;";
        List<String> list = new ArrayList<>();
        String[] albumIDs = rawAlbumIds.split(SPLIT_EXPRESSION);
        Collections.addAll(list, albumIDs);
        return list;
    }

    public ArrayList<SongModel> artistSongs(String artist) {
        ArrayList<SongModel> songs = new ArrayList<>();
        ArrayList<SongModel> list = new ArrayList<>(mainList);
        Collections.reverse(list);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getArtist().contains(artist)) {
                songs.add(list.get(i));
            }
        }
        return songs;
    }

    /*
     * Playlists
     */

    public ArrayList<HashMap<String, String>> getAllPlaylists() {
        Playlist db = new Playlist(context);
        db.open();
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        if (db.getCount() > 0) {
            list = db.getAllRows();
        }
        db.close();
        return list;
    }

    public HashMap<String, String> getPlaylist(int ID) {
        Playlist db = new Playlist(context);
        db.open();
        HashMap<String, String> hash = db.getRow(ID);
        db.close();
        return hash;
    }

    public void addPlaylist(String name) {
        Playlist db = new Playlist(context);
        db.open();
        db.addRow(name);
        db.close();
    }

    public boolean ifPlaylistPresent(String name) {
        Playlist db = new Playlist(context);
        db.open();
        boolean result = db.searchPlaylist(name);
        db.close();
        return result;
    }

    public void deletePlaylist(int id) {
        Playlist db = new Playlist(context);
        db.open();
        db.deleteRow(id);
        db.close();
    }


    public void removePlaylistSong(int PlayList, ArrayList<SongModel> newlist) {
        PlaylistSongs db = new PlaylistSongs(context);
        db.open();
        db.deleteAll(PlayList);
        for (int i = 0; i < newlist.size(); i++) {
            db.addRow(PlayList, newlist.get(i));
        }
        //TODO:MainActivity.shouldNotifyDataChanged = true;
        db.close();
    }

    public ArrayList<SongModel> playlistSongs(int playlistID) {
        ArrayList<SongModel> list = new ArrayList<>();
        PlaylistSongs db = new PlaylistSongs(context);
        db.open();
        if (db.getCount(playlistID) > 0) {
            list = db.getAllRows(playlistID);
        }
        db.close();
        return list;
    }

    public void updatePlaylistSongs(int playlistID, ArrayList<SongModel> newList) {
        PlaylistSongs db = new PlaylistSongs(context);
        db.open();
        db.deleteAll(playlistID);
        for (int i = 0; i < newList.size(); i++) {
            db.addRow(playlistID, newList.get(i));
        }
        db.close();
    }

    public ArrayList<SongModel> favouriteSongs() {
        ArrayList<SongModel> list;
        FavouriteList db = new FavouriteList(context);
        db.open();
        list = new ArrayList<>(db.getAllRows());
        db.close();
        return list;
    }

    public boolean addToFavouriteSongs(SongModel row) {
        FavouriteList db = new FavouriteList(context);
        db.open();
        db.addRow(row);
        db.close();
        //TODO:MainActivity.shouldNotifyDataChanged = true;
        return true;
    }

    public void updateFavouritesList(ArrayList<SongModel> newFavList) {
        FavouriteList db = new FavouriteList(context);
        db.open();
        db.deleteAll();
        Collections.reverse(newFavList);
        for (int i = 0; i < newFavList.size(); i++) {
            db.addRow(newFavList.get(i));
        }
        db.close();
    }

    public ArrayList<SongModel> mostPlayedSongs() {
        ArrayList<SongModel> list;
        CategorySongs categorySongs = new CategorySongs(context);
        categorySongs.open();
        list = categorySongs.getAllRows(1);
        categorySongs.close();
        return list;
    }

    public void updateMostPlayedList(ArrayList<SongModel> newList) {
        CategorySongs db = new CategorySongs(context);
        db.open();
        db.deleteAll(1);
        for (int i = 0; i < newList.size(); i++) {
            db.addRow(1, newList.get(i));
        }
        db.close();
    }

    /*
     * Actions
     */

    public void sync() {
        mainList.clear();
        albums.clear();
        artists.clear();
        grabIfEmpty();
    }

    public void addToQueue(SongModel song) {
        queue().add(song);
        (new CommonUtils(context)).showTheToast("Added to current queue!");
    }

    public void addToQueue(ArrayList<SongModel> arrayList) {
        ArrayList<SongModel> arrayList1 = new ArrayList<>(arrayList);
        if (arrayList1.size() > 0) {
            queue().addAll(arrayList1);
            (new CommonUtils(context)).showTheToast("Added to current queue!");
        } else {
            (new CommonUtils(context)).showTheToast("Nothing to add");
        }
    }

    public void playNext(SongModel song) {
        queue().add(getCurrentMusicID() + 1, song);
        (new CommonUtils(context)).showTheToast("Playing next: " + song.getTitle());
    }

    public boolean replaceQueue(final ArrayList<SongModel> list) {
        if (list != null && !list.isEmpty()) {
            clearQueue();
            queue.addAll(list);
            try {
                new Thread(new Runnable() {
                    public void run() {
                        sharedPrefsUtils.writeSharedPrefs("key", new Gson().toJson(list));
                    }
                }).start();
            } catch (Exception e) {
                e.getStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public void generateMenu(PopupMenu popupMenus, int[] options) {
        int i = 0;
        while (i < options.length) {
            String name = "INVALID";
            if (options[i] == R.id.play_musicUtils) {
                name = "Play";
            } else if (options[i] == R.id.play_next_musicUtils) {
                name = "Play Next";
            } else if (options[i] == R.id.add_to_queue_musicUtils) {
                name = "Add to Queue";
            } else if (options[i] == R.id.add_to_playlist_musicUtils) {
                name = "Add to Playlist";
            } else if (options[i] == R.id.shuffle_play_musicUtils) {
                name = "Shuffle Play";
            } else if (options[i] == R.id.use_as_ringtone_musicUtils) {
                name = "Use as Ringtone";
            } else if (options[i] == R.id.remove_musicUtils) {
                name = "Remove";
            } else if (options[i] == R.id.info_musicUtils) {
                name = "Track Info";
            } else if (options[i] == R.id.goto_album_musicUtils) {
                name = "Go to Album";
            } else if (options[i] == R.id.goto_artist_musicUtils) {
                name = "Go to Artist";
            }
            popupMenus.getMenu().add(0, options[i], 1, name);
            MenuItem menuItem = popupMenus.getMenu().getItem(i);
            CharSequence menuTitle = menuItem.getTitle();
            SpannableString styledMenuTitle = new SpannableString(menuTitle);
            //styledMenuTitle.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")), 0, menuTitle.length(), 0);
            menuItem.setTitle(styledMenuTitle);
            i++;
        }
    }

    public void play(int id, ArrayList<SongModel> array) {
        Log.d("MusicUtilsConsole", "Initiating the play request to MusicPlayback Service");
        if (!array.isEmpty()) {
            File file = new File(array.get(id).getPath());
            if (file.exists()) {
                replaceQueue(array);
                setCurrentMusicID(id);
                Intent intent = new Intent(MusicPlayback.ACTION_PLAY);
                ContextCompat.startForegroundService(context, createExplicitFromImplicitIntent(intent));

            } else {
                Toast.makeText(context,
                        "Unable to play the song! Try syncing the library!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void playNext(ArrayList<SongModel> arrayList) {
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            playNext(arrayList.get(i));
        }
        (new CommonUtils(context)).showTheToast("Playing this list next!");
    }

    public void addToPlaylist(final ArrayList<SongModel> arrayList) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_addtoplaylist);

        // set the custom dialog components - text, image and button
        ListView listView = dialog.findViewById(R.id.listView);
        ImageView relAdd = dialog.findViewById(R.id.add_playlist);
        final PlaylistFragmentAdapterSimple playlistAdapter = new PlaylistFragmentAdapterSimple
                (context);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaylistSongs db = new PlaylistSongs(context);
                db.open();
                int playListID = Integer.parseInt(Objects.requireNonNull(getAllPlaylists().get(position).get("ID")));
                for (int i = 0; i < arrayList.size(); i++) {
                    if (!db.getAllRows(playListID).contains(arrayList.get(i))) {
                        db.addRow(playListID, arrayList.get(i));
                    }
                }
                (new CommonUtils(context)).showTheToast(arrayList.size() +
                        (arrayList.size() > 1 ? " songs are" : " song is") + " successfully added to playlist! ");
                db.close();
                if (dialog.isShowing()) {
                    dialog.cancel();
                }

            }
        });

        relAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayListDialog(playlistAdapter);
            }

        });
        listView.setAdapter(playlistAdapter);
        dialog.show();
    }

    public void addToPlaylist(final SongModel hash) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_addtoplaylist);

        // set the custom dialog components - text, image and button
        ListView listView = dialog.findViewById(R.id.listView);
        ImageView relAdd = dialog.findViewById(R.id.add_playlist);
        ImageView albumArt = dialog.findViewById(R.id.albumArt);
        (new ImageUtils(context)).setAlbumArt(hash.getAlbumID(), albumArt);
        final PlaylistFragmentAdapterSimple playlistAdapter = new PlaylistFragmentAdapterSimple
                (context);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaylistSongs db = new PlaylistSongs(context);
                db.open();
                int playListID = Integer.parseInt(Objects.requireNonNull(getAllPlaylists().get(position).get("ID")));
                if (!db.getAllRows(playListID).contains(hash)) {
                    db.addRow(playListID, hash);
                    (new CommonUtils(context)).showTheToast(hash.getTitle() + " is added to playlist! ");
                } else {
                    (new CommonUtils(context)).showTheToast("Error: Song is already in Playlist!");
                }
                db.close();
                if (dialog.isShowing()) {
                    dialog.cancel();
                }

            }
        });

        relAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayListDialog(playlistAdapter);
            }
        });
        listView.setAdapter(playlistAdapter);
        dialog.show();
    }

    private void addPlayListDialog(final PlaylistFragmentAdapterSimple playlistAdapter) {
        final Dialog alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.dialog_add_playlist);

        final EditText input = alertDialog.findViewById(R.id.editText);
        input.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, (new CommonUtils(context)).accentColor(sharedPrefsUtils))));
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

        Button btnCreate = alertDialog.findViewById(R.id.btnCreate);
        btnCreate.setTextColor(ContextCompat.getColor(context, (new CommonUtils(context)).accentColor(sharedPrefsUtils)));
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = input.getText().toString();
                if (!name.isEmpty()) {
                    addPlaylist(name);
                    playlistAdapter.notifyDataSetChanged();
                    alertDialog.cancel();
                } else {
                    Toast.makeText(context, "Please enter playlist name.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnCancel = alertDialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void shufflePlay(int id, ArrayList<SongModel> array) {
        ArrayList<SongModel> arrayList = new ArrayList<>(array);
        if (arrayList.size() > 0) {
            SongModel songModel = arrayList.get(id);
            arrayList.remove(id);
            Collections.shuffle(arrayList);
            arrayList.add(0, songModel);
            play(0, arrayList);
            (new CommonUtils(context)).showTheToast("Shuffling");
        }
    }

    public void shufflePlay(ArrayList<SongModel> array) {
        ArrayList<SongModel> data = new ArrayList<>(array);
        if (data.size() > 0) {
            Collections.shuffle(data);
            play(0, data);
            (new CommonUtils(context)).showTheToast("Shuffling");
        } else {
            (new CommonUtils(context)).showTheToast("Nothing to shuffle");
        }
    }

    public AlertDialog info(SongModel songModel) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(songModel.getTitle());
        builder.setMessage("\nFile Name: " + songModel.getFileName() + "\n\n" +
                "Song Title: " + songModel.getTitle() + "\n\n" +
                "Album: " + songModel.getAlbum() + "\n\n" +
                "Artist: " + songModel.getArtist() + "\n\n" +
                "Length: " + songModel.getDuration() + "\n\n" +
                "File location: " + songModel.getPath());
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    private Intent createExplicitFromImplicitIntent(Intent implicitIntent) {
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

    /*
     * Helper Functions. Keep them private or public as required
     */

    private void clearQueue() {
        queue.clear();
    }

    private int getIndexAlbum(String albumName, ArrayList<HashMap<String, String>> list) {
        for (int i = 0; i < list.size(); i++) {
            String auction = list.get(i).get("album");
            if (albumName.equals(auction)) {
                return i;
            }
        }
        return -1;
    }

    private int getIndexArtist(String artistName, ArrayList<HashMap<String, String>> list) {
        for (int i = 0; i < list.size(); i++) {
            String auction = list.get(i).get("artist");
            if (artistName.equals(auction)) {
                return i;
            }
        }
        return -1;
    }

    private void grabIfEmpty() {
        if (mainList.isEmpty()) {
            grabData();
            Log.d(TAG, "Grabbing data for player...");
        } else {
            Log.d(TAG, "Data is present. Just setting context.");
        }
    }

    private void grabData() {
        String[] STAR = {"*"};

        boolean excludeShortSounds = sharedPrefsUtils.readSharedPrefsBoolean("excludeShortSounds", false);
        boolean excludeWhatsApp = sharedPrefsUtils.readSharedPrefsBoolean("excludeWhatsAppSounds", false);

        Cursor cursor;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        cursor = context.getContentResolver().query(uri, STAR, selection, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String duration = cursor
                            .getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
                    int currentDuration = Math.round(Integer
                            .parseInt(duration));
                    if (currentDuration > ((excludeShortSounds) ? 60000 : 0)) {
                        if (!excludeWhatsApp || !cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM)).equals("WhatsApp Audio")) {
                            String songName = cursor
                                    .getString(
                                            cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                                    .replace("_", " ").trim().replaceAll(" +", " ");
                            String path = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.DATA));
                            String title = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.TITLE)).replace("_", " ").trim().replaceAll(" +", " ");
                            String artistName = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));
                            String albumName = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.ALBUM));

                            String albumID = cursor
                                    .getString(
                                            cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                                    );

                            TimeZone tz = TimeZone.getTimeZone("UTC");
                            SimpleDateFormat df = new SimpleDateFormat("mm:ss", Locale.getDefault());
                            df.setTimeZone(tz);
                            String time = String.valueOf(df.format(currentDuration));

                            // Adding song to list
                            SongModel songModel = new SongModel();
                            songModel.setFileName(songName);
                            songModel.setTitle(title);
                            songModel.setArtist(artistName);
                            songModel.setAlbum(albumName);
                            songModel.setAlbumID(albumID);
                            songModel.setPath(path);
                            songModel.setDuration(time);

                            mainList.add(songModel);
                        }
                    }
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }

        /*
         * Albums Data
         */

        ArrayList<SongModel> allSongList = new ArrayList<>(mainList);
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        for (int i = 0; i < allSongList.size(); i++) {
            String name = allSongList.get(i).getAlbum();
            String artist = allSongList.get(i).getArtist();
            int albumIndex = -1;
            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    String auction = list.get(j).get("album");
                    if (name.equals(auction)) {
                        albumIndex = j;
                    }
                }
            }
            if (albumIndex == -1) {

                HashMap<String, String> song = new HashMap<>();
                song.put("album", name);
                song.put("artist", artist);
                list.add(song);
            }
        }

        ArrayList<HashMap<String, String>> list2 = new ArrayList<>();
        Map<String, String> sortedMap = new TreeMap<>();
        for (int i = 0; i < list.size(); i++) {
            sortedMap.put(Objects.requireNonNull(list.get(i).get("album")),
                    Objects.requireNonNull(list.get(i).get("album")));
        }
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            HashMap<String, String> song = new HashMap<>();
            String title = entry.getValue();
            int index = getIndexAlbum(title, list);
            song.put("album", list.get(index).get("album"));
            song.put("artist", list.get(index).get("artist"));
            list2.add(song);
        }

        albums.addAll(list2);

        /*
         * Artists Data
         */
        String SPLIT_EXPRESSION = ";,,;,;;";
        list.clear();
        list2.clear();
        sortedMap.clear();

        for (int i = 0; i < allSongList.size(); i++) {

            String name = allSongList.get(i).getArtist();
            String albums = allSongList.get(i).getAlbum();

            int albumIndex = -1;
            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    String auction = list.get(j).get("artist");
                    if (name.equals(auction)) {
                        albumIndex = j;
                    }
                }
            }

            if (albumIndex > -1) {
                if (albums != null) {
                    String[] albumSplit = Objects.requireNonNull(list.get(albumIndex).get("albums")).split(SPLIT_EXPRESSION);
                    boolean found = false;
                    for (String anAlbumSplit : albumSplit) {
                        if (anAlbumSplit.trim().equals(albums)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        albums = list.get(albumIndex).get("albums") + SPLIT_EXPRESSION + allSongList.get(i).getAlbum();
                    }

                }
            }

            if (albumIndex == -1) {

                HashMap<String, String> song = new HashMap<>();
                song.put("artist", name);
                song.put("albums", albums);
                list.add(song);
            } else {
                HashMap<String, String> song = new HashMap<>();
                song.put("artist", name);
                song.put("albums", albums);
                list.add(albumIndex, song);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            sortedMap.put(Objects.requireNonNull(list.get(i).get("artist")),
                    Objects.requireNonNull(list.get(i).get("artist")));
        }
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            HashMap<String, String> song = new HashMap<>();
            String title = entry.getValue();
            int index = getIndexArtist(title, list);
            song.put("artist", list.get(index).get("artist"));
            song.put("albums", list.get(index).get("albums"));
            list2.add(song);
        }

        artists.addAll(list2);

        Log.d(TAG, "grabData() performed");
    }
}
