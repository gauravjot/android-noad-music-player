package com.droidheat.musicplayer.ui.fragments;

import android.os.Bundle;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import androidx.fragment.app.Fragment;
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.droidheat.musicplayer.ui.activities.GlobalDetailActivity;
import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.utils.SongsUtils;

public class ArtistGridFragment extends Fragment {

	ListView listView;
	SongsUtils songsUtils;

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
		songsUtils = new SongsUtils(getActivity());
		mergeAdapter.addAdapter(new ArtistGridAdapter(getActivity(), songsUtils.artists()));
		listView.setAdapter(mergeAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
				if (position >= 0) {
					Intent intent = new Intent(getActivity(), GlobalDetailActivity.class);
					intent.putExtra("id", position);
					intent.putExtra("name", songsUtils.artists().get(position).get("artist"));
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
