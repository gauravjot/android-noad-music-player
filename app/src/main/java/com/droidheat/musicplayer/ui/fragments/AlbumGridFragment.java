package com.droidheat.musicplayer.ui.fragments;

import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.Fragment;
import android.widget.GridView;

import com.droidheat.musicplayer.ui.activities.GlobalDetailActivity;
import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.utils.SongsUtils;
import com.droidheat.musicplayer.ui.adapters.AlbumGridAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumGridFragment extends Fragment {

    GridView gridView;
	SongsUtils songsUtils;
	ArrayList<HashMap<String,String>> CustomArray;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_album_grid, container, false);
        songsUtils = new SongsUtils(getActivity());
        CustomArray = songsUtils.albums();
		gridView = v.findViewById(R.id.gridView1);

		gridView.setAdapter(new AlbumGridAdapter(getActivity(), CustomArray));

		gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent(getActivity(), GlobalDetailActivity.class);
                intent.putExtra("id", position);
                intent.putExtra("name", songsUtils.albums().get(position).get("album"));
                intent.putExtra("field", "albums");
                startActivity(intent);
            }
        });
		return v;
	}



	boolean _areLecturesLoaded = false;
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && !_areLecturesLoaded ) {

			_areLecturesLoaded = true;
		}
	}

}
