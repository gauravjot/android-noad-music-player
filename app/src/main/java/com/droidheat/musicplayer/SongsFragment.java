package com.droidheat.musicplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import java.util.Objects;

public class SongsFragment extends Fragment {

    private ListView list;
    private SongsManager songsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsManager = new SongsManager(getActivity());
    }

    @SuppressLint({"SdCardPath", "SimpleDateFormat"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_songs, container, false);
        list = v.findViewById(R.id.listView1);
        CustomAdapter adapter = new CustomAdapter(getActivity(), songsManager.newSongs());
        list.setAdapter(adapter);
        return v;
    }

    boolean _areLecturesLoaded = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !_areLecturesLoaded) {
            _areLecturesLoaded = true;
        }
    }

//    Boolean isVisible = false;
//
//    @Override
//    public void setMenuVisibility(final boolean visible) {
//        super.setMenuVisibility(visible);
//        isVisible = visible;
//        if (isVisible) {
//            try {
//                ((MainActivity) Objects.requireNonNull(getActivity())).showHideFabWithScroll(list);
//            } catch (Exception e) {
//                Log.i("Show/HideTitleSongFrag", "Safe from Crash.");
//            }
//        }
//    }

}
