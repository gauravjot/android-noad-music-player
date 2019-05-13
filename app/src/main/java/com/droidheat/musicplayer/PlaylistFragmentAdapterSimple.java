package com.droidheat.musicplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class PlaylistFragmentAdapterSimple extends BaseAdapter {

    private ArrayList<HashMap<String, String>> data;
    private LayoutInflater inflater;
    SongsManager songsManager;

    PlaylistFragmentAdapterSimple(Context context) {

        songsManager = new SongsManager(context);
        data = songsManager.getAllPlaylists();

        /*********** Layout inflator to call external xml layout () ***********/
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        data.clear();
        data = songsManager.getAllPlaylists();
    }

    /**
     * ***** What is the size of Passed Arraylist Size ***********
     */
    public int getCount() {

        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {

        public TextView text;

    }

    /**
     * *** Depends upon data size called for each row , Create each ListView row ****
     */
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        if (convertView == null) {

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.playlist_row_simple, parent, false);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.text = vi.findViewById(R.id.heading);

            vi.setTag(holder);

        } else {
            holder = (ViewHolder) vi.getTag();
        }

        if (data.size() <= 0) {
            vi.setVisibility(View.GONE);
        } else {
            vi.setVisibility(View.VISIBLE);

            holder.text.setText(data.get(position).get("title"));


        }

        return vi;
    }



}
