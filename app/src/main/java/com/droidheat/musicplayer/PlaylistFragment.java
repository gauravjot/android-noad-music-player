package com.droidheat.musicplayer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

public class PlaylistFragment extends Fragment {

    Context context;
    LayoutInflater mInflater;
    PlaylistGridAdapter playlistGridAdapter;
    ListView listView;
    AutoPlaylistGridAdapter autoPlaylistGridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        listView = view.findViewById(R.id.listView);
        final MergeAdapter mergeAdapter = new MergeAdapter();
        playlistGridAdapter = new PlaylistGridAdapter(getActivity());
        autoPlaylistGridAdapter = new AutoPlaylistGridAdapter(getActivity());
        //listView.addHeaderView(getActivity().getLayoutInflater().inflate(R.layout.listview_header, null));
        View Header =  mInflater.inflate(R.layout.heading, null);
        View Header2 =  mInflater.inflate(R.layout.heading_with_button, null);
        TextView textView = Header.findViewById(R.id.heading);
        textView.setText("Auto Playlists");
        TextView textView2 = Header2.findViewById(R.id.heading);
        textView2.setText("Created Playlists");
        Button addPlaylist = Header2.findViewById(R.id.button);
        addPlaylist.setTextColor(ContextCompat.getColor(context,
                (new CommonUtils(context)).accentColor(new SharedPrefsUtils(context))));
        addPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog alertDialog = new Dialog(getActivity());
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.setContentView(R.layout.dialog_add_playlist);

                final EditText input = alertDialog.findViewById(R.id.editText);
                input.requestFocus();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                alertDialog.findViewById(R.id.btnCreate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = input.getText().toString();
                        (new SongsManager(getActivity())).addPlaylist(name);
                        playlistGridAdapter.notifyDataSetChanged();
                        alertDialog.cancel();
                    }
                });

                alertDialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
        mergeAdapter.addView(Header);
        mergeAdapter.addAdapter(autoPlaylistGridAdapter);
        mergeAdapter.addView(Header2);
        mergeAdapter.addAdapter(playlistGridAdapter);
        listView.setAdapter(mergeAdapter);


//        Button btn = (Button) view.findViewById(R.id.button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MusicUtils.createPlaylistDialog(getActivity(),new SongsManager(getActivity()),playlistGridAdapter);
//            }
//        });

        return view;
    }

    boolean _areLecturesLoaded = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !_areLecturesLoaded ) {


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
//                ((MainActivity)getActivity()).showHideFabWithScroll(listView);
//            }
//            catch (Exception e) {
//                Log.i("Show/HideTitle:AllSongs", "Safe from Crash.");
//            }
//        }
//    }

}
