package com.droidheat.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;


class FavouriteList {

    private String TEXT_TYPE = " TEXT";
    private String COMMA_SEP = ",";
    private String TABLE_NAME = "fav";
    private String COLUMN_NAME_ID = "id";
    private String TITLE = "title";
    private String PATH = "path";
    private String ARTIST = "artist";
    private String ALBUM = "album";
    private String NAME = "name";
    private String DURATION = "duration";
    private String ALBUMID = "albumid";
    private String[] ALL_KEYS = new String[]
            {COLUMN_NAME_ID, TITLE, PATH, ARTIST, ALBUM, NAME, DURATION, ALBUMID};
    private String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    TITLE + TEXT_TYPE + COMMA_SEP +
                    PATH + TEXT_TYPE + COMMA_SEP +
                    ARTIST + TEXT_TYPE + COMMA_SEP +
                    ALBUM + TEXT_TYPE + COMMA_SEP +
                    NAME + TEXT_TYPE + COMMA_SEP +
                    DURATION + TEXT_TYPE + COMMA_SEP +
                    ALBUMID + TEXT_TYPE +
                    ");";
    private String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private SQLiteDatabase db;
    private ReaderDB myDBHelper;
    Context context;

    /*
     * Public Methods of Database
     * 1. open()
     * 2. close()
     * 3. getRows()
     * 4. addRow()
     */

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    FavouriteList(Context ctx) {
        this.context = ctx;
        myDBHelper = new ReaderDB(context);
    }

    // Open the database connection.
    public FavouriteList open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public FavouriteList close() {
        myDBHelper.close();
        return this;
    }

    /*
     * Add a row to the Database
     */
    long addRow(SongModel hashRow) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TITLE, hashRow.getTitle());
        values.put(PATH, hashRow.getPath());
        values.put(ARTIST, hashRow.getArtist());
        values.put(ALBUM, hashRow.getAlbum());
        values.put(NAME, hashRow.getFileName());
        values.put(DURATION, hashRow.getDuration());
        values.put(ALBUMID, hashRow.getAlbumID());

        // Insert the new row, returning the primary key value of the new row
        return db.insert(TABLE_NAME,"NULL", values);
    }

     /*
      * Returns ArrayList Hashmap of all Rows
      */
    ArrayList<SongModel> getAllRows() {

        Cursor c = db.query(TABLE_NAME, ALL_KEYS, null, null, null, null, COLUMN_NAME_ID+" DESC");

        ArrayList<SongModel> profilesArray = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                SongModel song = new SongModel();
                song.setTitle(c.getString(1));
                song.setPath(c.getString(2));
                song.setArtist(c.getString(3));
                song.setAlbum(c.getString(4));
                song.setFileName(c.getString(5));
                song.setDuration(c.getString(6));
                song.setAlbumID(c.getString(7));
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
        Cursor c = 	db.query(true, TABLE_NAME, ALL_KEYS,
                where, null, null, null, null, null);
        String title = null, path = null,artist = null,album =null,name=null,duration=null,albumid=null;
        if (c != null) {
            c.moveToFirst();
            title = c.getString(1);
            path = c.getString(2);
            artist = c.getString(3);
            album = c.getString(4);
            name = c.getString(5);
            duration = c.getString(6);
            albumid = c.getString(7);
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
    private boolean deleteRow(long rowId) {
        String where = COLUMN_NAME_ID + "=" + rowId;
        return db.delete(TABLE_NAME, where, null) != 0;
    }

    public boolean deleteRowByPath(String path) {
        String where = PATH + "=" + path;
        try {
            return db.delete(TABLE_NAME, where, null) != 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    /*
     * Delete All Rows
     */
    boolean deleteAll() {
        try {
            Cursor c = getAllRowsCursor();
            long rowId = c.getColumnIndexOrThrow(COLUMN_NAME_ID);
            if (c.moveToFirst()) {
                do {
                    deleteRow(c.getLong((int) rowId));
                } while (c.moveToNext());
            }
            c.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /*
     * Get All Rows Cursor
     */
    // Return all data in the database.
    private Cursor getAllRowsCursor() {
        Cursor c = 	db.query(true, TABLE_NAME, ALL_KEYS,
                null, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /*
     * Update the row
     */
    // Change an existing row to be equal to new data.
    public boolean updateRow(HashMap<String, String> list, int rowID) {
        String where = COLUMN_NAME_ID + "=" + rowID;

		/*
		 * CHANGE 4:
		 */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:

        ContentValues newValues = new ContentValues();
        newValues.put(TITLE, list.get(TITLE));
        newValues.put(PATH, list.get(PATH));
        newValues.put(ARTIST, list.get(ARTIST));
        newValues.put(ALBUM, list.get(ALBUM));
        newValues.put(NAME, list.get(NAME));
        newValues.put(DURATION, list.get(DURATION));
        newValues.put(ALBUMID, list.get(ALBUMID));

        // Insert it into the database.
        return db.update(TABLE_NAME, newValues, where, null) != 0;
    }


    private class ReaderDB extends SQLiteOpenHelper {

        ReaderDB(Context context) {
            super(context, "Favs.db", null, 1);
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