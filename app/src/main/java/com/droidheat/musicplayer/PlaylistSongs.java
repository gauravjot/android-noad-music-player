package com.droidheat.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class PlaylistSongs {

    String TEXT_TYPE = " TEXT";
    String COMMA_SEP = ",";
    String TABLE_NAME = "playlistsongs";
    String COLUMN_NAME_ID = "id";
    String PLAYLISTID = "playlist";
    String TITLE = "title";
    String PATH = "path";
    String ARTIST = "artist";
    String ALBUM = "album";
    String NAME = "name";
    String ALBUMID = "albumid";
    String DURATION = "duration";
    String[] ALL_KEYS = new String[]
            {COLUMN_NAME_ID, PLAYLISTID, TITLE, PATH, ARTIST, ALBUM, NAME, DURATION, ALBUMID};
    String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    PLAYLISTID + TEXT_TYPE + COMMA_SEP +
                    TITLE + TEXT_TYPE + COMMA_SEP +
                    PATH + TEXT_TYPE + COMMA_SEP +
                    ARTIST + TEXT_TYPE + COMMA_SEP +
                    ALBUM + TEXT_TYPE + COMMA_SEP +
                    NAME + TEXT_TYPE + COMMA_SEP +
                    DURATION + TEXT_TYPE + COMMA_SEP +
                    ALBUMID + TEXT_TYPE +
                    ");";
    String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    SQLiteDatabase db;
    ReaderDB myDBHelper;
    Context context;

    Playlist allPlaylistDB;

    /*
     * Public Methods of Database
     * 1. open()
     * 2. close()
     * 3. getRows()
     * 4. addRow()
     */

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PlaylistSongs(Context ctx) {
        this.context = ctx;
        allPlaylistDB = new Playlist(context);
        myDBHelper = new ReaderDB(context);
    }

    // Open the database connection.
    public PlaylistSongs open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public PlaylistSongs close() {
        myDBHelper.close();
        return this;
    }

    /*
     * Add a row to the Database
     */
    public long addRow(long playlistID, SongModel hashRow) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TITLE, hashRow.getTitle());
        values.put(PLAYLISTID, Long.toString(playlistID));
        values.put(PATH, hashRow.getPath());
        values.put(ARTIST, hashRow.getArtist());
        values.put(ALBUM, hashRow.getAlbum());
        values.put(NAME, hashRow.getFileName());
        values.put(DURATION, hashRow.getDuration());
        values.put(ALBUMID, hashRow.getAlbumID());

        // Insert the new row, returning the primary key value of the new row
        return db.insert(TABLE_NAME, "NULL", values);
    }

    /*
     * Returns ArrayList of all Rows
     */
    ArrayList<SongModel> getAllRows(int playlistID) {
        String where = PLAYLISTID + "=" + Integer.toString(playlistID);
        Cursor c = db.query(TABLE_NAME, ALL_KEYS, where, null, null, null, null);

        ArrayList<SongModel> profilesArray = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                SongModel song = new SongModel();
                song.setFileName(c.getString(6));
                song.setTitle(c.getString(2));
                song.setArtist(c.getString(4));
                song.setAlbum(c.getString(5));
                song.setAlbumID(c.getString(8));
                song.setPath(c.getString(3));
                song.setDuration(c.getString(7));
                profilesArray.add(song);
            } while (c.moveToNext());
        }
        c.close();

        return profilesArray;
    }

    /*
     * Get Row
     */
    public SongModel getRow(long rowId) {
        String where = COLUMN_NAME_ID + "=" + rowId;
        Cursor c = db.query(true, TABLE_NAME, ALL_KEYS,
                where, null, null, null, null, null);
        String title = null, path = null, artist = null, albumid = null, album = null, name = null, duration = null, playlistID = null;
        if (c != null) {
            c.moveToFirst();
            title = c.getString(2);
            path = c.getString(3);
            artist = c.getString(4);
            album = c.getString(5);
            name = c.getString(6);
            duration = c.getString(7);
            albumid = c.getString(8);
        }
        c.close();
        SongModel song = new SongModel();
        song.setTitle(title);
        song.setPath(path);
        song.setArtist(artist);
        song.setAlbum(album);
        song.setFileName(name);
        song.setDuration(duration);
        song.setAlbumID(albumid);
        return song;
    }

    /*
     * Delete a Row
     */
    public boolean deleteRow(long rowId, long playlistid) {
        String where = COLUMN_NAME_ID + "=" + rowId + " AND " + PLAYLISTID + "=" + playlistid;
        return db.delete(TABLE_NAME, where, null) != 0;
    }

    public boolean deleteRowByPath(String path) {
        String where = PATH + "=" + path;
        try {
            return db.delete(TABLE_NAME, where, null) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public int getCount(long playlist) {
        String countQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + PLAYLISTID + "=" + Long.toString(playlist);
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    /*
     * Delete All Rows
     */
    public boolean deleteAll(int PlaylistID) {
        try {
            Cursor c = getAllRowsCursor();
            long rowId = c.getColumnIndexOrThrow(COLUMN_NAME_ID);
            if (c.moveToFirst()) {
                do {
                    deleteRow(c.getLong((int) rowId), PlaylistID);
                } while (c.moveToNext());
            }
            c.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * Get All Rows Cursor
     */
    // Return all data in the database.
    public Cursor getAllRowsCursor() {
        String where = null;
        Cursor c = db.query(true, TABLE_NAME, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /*
     * Update the row
     */
    // Change an existing row to be equal to new data.
    /*
    public boolean updateRow(HashMap<String, String> list, int rowID) {
        String where = COLUMN_NAME_ID + "=" + rowID;

        ContentValues newValues = new ContentValues();
        newValues.put(TITLE, list.get("title"));
        newValues.put(PATH, list.get("path"));
        newValues.put(ARTIST, list.get("artist"));
        newValues.put(ALBUM, list.get("album"));
        newValues.put(NAME, list.get("name"));
        newValues.put(DURATION, list.get("duration"));

        return db.update(TABLE_NAME, newValues, where, null) != 0;
    }
    */


    public class ReaderDB extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "playlistsongs.db";

        public ReaderDB(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

    }

}