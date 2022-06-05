package com.droidheat.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefsUtils {

    private final SharedPreferences sharedPref;

    public SharedPrefsUtils(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void writeSharedPrefs(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void writeSharedPrefs(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void writeSharedPrefs(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String readSharedPrefsString(String key, String defaultValue) {
        return sharedPref.getString(key, defaultValue);
    }

    public Boolean readSharedPrefsBoolean(String key, Boolean defaultValue) {
        return sharedPref.getBoolean(key, defaultValue);
    }

    public int readSharedPrefsInt( String key, int defaultValue) {
        return sharedPref.getInt(key, defaultValue);
    }

}
