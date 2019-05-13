package com.droidheat.musicplayer;

import android.content.Context;
import android.widget.Toast;

class CommonUtils {

    private Context context;

    CommonUtils(Context context) {
        this.context = context;
    }


    void showTheToast(String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}
