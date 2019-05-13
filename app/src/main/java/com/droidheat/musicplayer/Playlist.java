package com.droidheat.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gauravjot on 7/MARCH/2015.
 */

public class Playlist {

    String TEXT_TYPE = " TEXT";
    String COMMA_SEP = ",";
    String TABLE_NAME = "queue";
    String COLUMN_NAME_ID = "id";
    String TITLE = "title";
    String[] ALL_KEYS = new String[]
            {COLUMN_NAME_ID, TITLE};
    String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    TITLE + TEXT_TYPE +
                    ");";
    String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    SQLiteDatabase db;
    ReaderDB myDBHelper;
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
    public Playlist(Context ctx) {
        this.context = ctx;
        myDBHelper = new ReaderDB(context);
    }

    // Open the database connection.
    public Playlist open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public Playlist close() {
        myDBHelper.close();
        return this;
    }

    /*
     * Add a row to the Database
     */
    public long addRow(String title) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TITLE, title);

        // Insert the new row, returning the primary key value of the new row
        return db.insert(TABLE_NAME,"NULL", values);
    }

     /*
      * Returns ArrayList Hashmap of all Rows
      */
    ArrayList<HashMap<String, String>> getAllRows() {

        Cursor c = db.query(TABLE_NAME, ALL_KEYS, null, null, null, null, null);

        ArrayList<HashMap<String, String>> profilesArray = new ArrayList<HashMap<String, String>>();
        if (c.moveToFirst()) {
            do {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("ID", c.getString(0));
                song.put("title", c.getString(1));
                profilesArray.add(song);
            } while (c.moveToNext());
        }
        c.close();

        return profilesArray;
    }

    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    /*
     * Get Row
     */
    public HashMap<String, String> getRow(long rowId) {
        String where = COLUMN_NAME_ID + "=" + rowId;
        Cursor c = 	db.query(true, TABLE_NAME, ALL_KEYS,
                where, null, null, null, null, null);
        String title = null;
        if (c != null) {
            c.moveToFirst();
            title = c.getString(1);
        }
        c.close();
        HashMap<String, String> song = new HashMap<String, String>();
        song.put("title", title);
        return song;
    }

    /*
     * Delete a Row
     */
    public boolean deleteRow(long rowId) {
        String where = COLUMN_NAME_ID + "=" + rowId;
        return db.delete(TABLE_NAME, where, null) != 0;
    }

    /*
     * Delete All Rows
     */
    public boolean deleteAll() {
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
    public Cursor getAllRowsCursor() {
        String where = null;
        Cursor c = 	db.query(true, TABLE_NAME, ALL_KEYS,
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
    public boolean updateRow(long ID,  String title) {
        String where = COLUMN_NAME_ID + "=" + ID + ";";

        Cursor c = 	db.query(true, TABLE_NAME, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        c.close();
		/*
		 * CHANGE 4:
		 */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:

        ContentValues newValues = new ContentValues();
        newValues.put(TITLE, title);

        // Insert it into the database.
        return db.update(TABLE_NAME, newValues, where, null) != 0;
    }

    /*
    * Searching
     */
    public boolean searchPlaylist(String name) {
        Cursor cursor=db.query(true,TABLE_NAME,ALL_KEYS,TITLE + "==" + name + ";",null,null
        ,null,null,null);
        if (cursor != null) {
            cursor.close();
            return true;
        } else {
            return false;
        }
    }


    public class ReaderDB extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Playlist.db";

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