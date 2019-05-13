package com.droidheat.musicplayer;

import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

public class AlbumGridFragment extends Fragment {

    GridViewWithHeaderAndFooter gridView;
	SongsManager songsManager;
	ArrayList<HashMap<String,String>> CustomArray;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_album_grid, container, false);
		// Show the Up button in the action bar.


        songsManager = new SongsManager(getActivity());

        CustomArray = songsManager.albums();

		gridView = v.findViewById(R.id.gridView1);
//		android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//		if (actionBar != null) {
//			gridView.setPadding(0,actionBar.getHeight() + 10, 0, 0);
//		}
        //gridView.addHeaderView(View.inflate(getActivity(),R.layout.listview_header,null));
		gridView.setAdapter(new AlbumGridAdapter(getActivity(), CustomArray));

		gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent(getActivity(), GlobalDetailActivity.class);
                intent.putExtra("id", position);
                intent.putExtra("name", songsManager.albums().get(position).get("album"));
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

//	Boolean isVisible = false;
//
//	@Override
//	public void setMenuVisibility(final boolean visible) {
//		super.setMenuVisibility(visible);
//		isVisible = visible;
//		if (isVisible) {
//			try {
//				((MainActivity) Objects.requireNonNull(getActivity())).showHideFabWithScroll(gridView);
//			}
//			catch (Exception e) {
//				Log.i("Show/HideTitle:AlbumF", "Safe from Crash.");
//			}
//		}
//	}

}
