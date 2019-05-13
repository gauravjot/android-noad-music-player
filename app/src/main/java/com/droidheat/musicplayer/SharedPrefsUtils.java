package com.droidheat.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class SharedPrefsUtils {

    private SharedPreferences sharedPref;

    SharedPrefsUtils(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }


    void writeSharedPrefs(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    void writeSharedPrefs(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    void writeSharedPrefs(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    String readSharedPrefsString(String key, String defaultValue) {
        return sharedPref.getString(key, defaultValue);
    }

    Boolean readSharedPrefsBoolean(String key, Boolean defaultValue) {
        return sharedPref.getBoolean(key, defaultValue);
    }

    int readSharedPrefsInt( String key, int defaultValue) {
        return sharedPref.getInt(key, defaultValue);
    }

}
