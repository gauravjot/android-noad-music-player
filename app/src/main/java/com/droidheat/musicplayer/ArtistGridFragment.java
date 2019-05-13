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
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;

import java.util.Objects;

public class ArtistGridFragment extends Fragment {

	ListView listView;
	SongsManager songsManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_artist_grid, container, false);
		// Show the Up button in the action bar.

		MergeAdapter mergeAdapter = new MergeAdapter();
		listView = v.findViewById(R.id.listView);
		songsManager = new SongsManager(getActivity());
//		android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//		if (actionBar != null) {
//			gridView.setPadding(0,actionBar.getHeight() + 10, 0, 0);
//		}
		//View Header =  LayoutInflater.from(getActivity()).inflate(R.layout.heading, null);
		//TextView textView = (TextView) Header.findViewById(R.id.textView1);
		//textView.setText("ARTISTS");
		//mergeAdapter.addView(Header);
        //listView.addHeaderView(View.inflate(getActivity(),R.layout.listview_header,null));
		mergeAdapter.addAdapter(new ArtistGridAdapter(getActivity(), songsManager.artists()));
		listView.setAdapter(mergeAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
				if (position >= 0) {
					Intent intent = new Intent(getActivity(), GlobalDetailActivity.class);
					intent.putExtra("id", position);
					intent.putExtra("name", songsManager.artists().get(position).get("artist"));
					intent.putExtra("field", "artists");
					startActivity(intent);
				}
			}
		});

		return v;

	}

//	Boolean isVisible = false;
//
//	@Override
//	public void setMenuVisibility(final boolean visible) {
//		super.setMenuVisibility(visible);
//		isVisible = visible;
//		if (isVisible) {
//			try {
//				((MainActivity) Objects.requireNonNull(getActivity())).showHideFabWithScroll(listView);
//			}
//			catch (Exception e) {
//				Log.i("Show/HideTitle:Artist", "Safe from Crash.");
//			}
//		}
//	}

}
