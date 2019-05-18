package com.droidheat.musicplayer;

import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    SongsManager songsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        songsManager = new SongsManager(getActivity());

        ListView listView = view.findViewById(R.id.listView);

        MergeAdapter mergeAdapter = new MergeAdapter();

        // QUICK PLAY
        View heading = View.inflate(getActivity(),R.layout.heading,null);
        TextView textView = heading.findViewById(R.id.heading);
        textView.setText("Quick Play");
        mergeAdapter.addView(heading);

        mergeAdapter.addView(threeGridView());

        // One Big Two Small

        mergeAdapter.addView(one_big_two_small_view());

        // Recently Added
        View heading1 = View.inflate(getActivity(),R.layout.heading_with_button,null);
        TextView textView1 = heading1.findViewById(R.id.heading);
        textView1.setText("Recently Added To Library");
        Button button = heading1.findViewById(R.id.button);
        button.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GlobalDetailActivity.class);
                    intent.putExtra("name", "Recently Added");
                    intent.putExtra("field", "recent");
                startActivity(intent);
            }
        });

        button.setTextColor(ContextCompat.getColor(getActivity(),
                (new CommonUtils(getActivity())).accentColor(new SharedPrefsUtils(getActivity()))));
        button.setText("View All");
        mergeAdapter.addView(heading1);

        View recent_list = View.inflate(getActivity(),R.layout.scroll_disabled_list_view, null);
        NoScrollListView listView1 = recent_list.findViewById(R.id.scroll_disabled_list_view);
        listView1.setExpanded(true);
        AdapterFiveRecentlyAdded adapterFiveRecentlyAdded = new AdapterFiveRecentlyAdded(getActivity());
        listView1.setAdapter(adapterFiveRecentlyAdded);

        mergeAdapter.addView(recent_list);

        // Setting Adapter

        listView.setAdapter(mergeAdapter);

        return view;
    }

    private View one_big_two_small_view() {
        View one_big_two_small_view = View.inflate(getActivity(),R.layout.home_two_small_one_big_thumb,null);

        ImageView imageView1 = one_big_two_small_view.findViewById(R.id.imageView1);
        TextView textView1 = one_big_two_small_view.findViewById(R.id.textView1);

        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(getActivity());
        String artist = sharedPrefsUtils
                .readSharedPrefsString("home_artist","<unknown>");
        final ArrayList<SongModel> arrayList = songsManager.artistSongs(artist);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            list.add(arrayList.get(i).getAlbumID());
        }
        (new ImageUtils(getActivity())).getImageByPicasso(
                list,
                imageView1,0,list.size()-1
        );
        textView1.setText((new SharedPrefsUtils(getActivity()))
                .readSharedPrefsString("home_artist","<unknown>"));

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songsManager.play(0,arrayList);
            }
        });

        ArrayList<HashMap<String,String>> playLists = songsManager.getAllPlaylists();
        if (playLists.size() > 0) {
            TextView textView2 = one_big_two_small_view.findViewById(R.id.textView2);
            textView2.setText(playLists.get(playLists.size()-1).get("title"));
            ImageView imageView3 = one_big_two_small_view.findViewById(R.id.imageView3);
            final ArrayList<SongModel> arrayList1 = songsManager.playlistSongs(
                    Integer.parseInt(Objects.requireNonNull(playLists.get(playLists.size()-1).get("ID"))));
            List<String> list1 = new ArrayList<>();
            for (int i = 0; i < arrayList1.size(); i++) {
                list1.add(arrayList1.get(i).getAlbumID());
            }
            (new ImageUtils(getActivity())).getImageByPicasso(list1,imageView3);
            imageView3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrayList1.size() > 0) {
                        songsManager.play(0, arrayList1);
                    } else {
                        (new CommonUtils(getContext())).showTheToast("No songs in playlist, please add some!");
                    }
                }
            });
        } else {
            //TODO:
        }
        if (playLists.size() > 1) {
            TextView textView3 = one_big_two_small_view.findViewById(R.id.textView3);
            textView3.setText(playLists.get(playLists.size()-2).get("title"));
            ImageView imageView4 = one_big_two_small_view.findViewById(R.id.imageView4);
            final ArrayList<SongModel> arrayList2 = songsManager.playlistSongs(
                    Integer.parseInt(Objects.requireNonNull(playLists.get(playLists.size()-2).get("ID"))));
            List<String> list2 = new ArrayList<>();
            for (int i = 0; i < arrayList2.size(); i++) {
                list2.add(arrayList2.get(i).getAlbumID());
            }
            (new ImageUtils(getActivity())).getImageByPicasso(list2,imageView4);
            imageView4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrayList2.size()> 0) {
                        songsManager.play(0, arrayList2);
                    } else {
                        (new CommonUtils(getContext())).showTheToast("No songs in playlist, please add some!");
                    }
                }
            });
        } else {
            //TODO:
        }


        return one_big_two_small_view;
    }

    private View threeGridView() {
        View three_grid = View.inflate(getActivity(),R.layout.home_three_grid,null);
        ImageView imageView1 = three_grid.findViewById(R.id.home_three_grid_imageView_1);
        ImageView imageView2 = three_grid.findViewById(R.id.home_three_grid_imageView_2);
        ImageView imageView3 = three_grid.findViewById(R.id.home_three_grid_imageView_3);
        TextView textView1 = three_grid.findViewById(R.id.home_three_grid_textView_1);
        TextView textView2 = three_grid.findViewById(R.id.home_three_grid_textView_2);
        TextView textView3 = three_grid.findViewById(R.id.home_three_grid_textView_3);

        imageView1.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 40F);
            }
        });
        imageView1.setClipToOutline(true);
        imageView2.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 40F);
            }
        });
        imageView3.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 40F);
            }
        });
        imageView3.setClipToOutline(true);
        imageView2.setClipToOutline(true);
        if (songsManager.allSongs().size() > 0) {
            imageView1.setImageDrawable(getResources().getDrawable(R.drawable.app_shuffle_inactive));
            imageView1.setPadding(76,76,76,76);
            textView1.setText("Shuffle All");
            imageView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    songsManager.shufflePlay(songsManager.allSongs());
                }
            });
            if (songsManager.mostPlayedSongs().size() > 0) {
                (new ImageUtils(getActivity())).getImageByPicasso(songsManager.mostPlayedSongs().get(0).getAlbumID(),imageView2);
                textView2.setText("Most Played");
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        songsManager.play(0,songsManager.mostPlayedSongs());
                    }
                });
                if (songsManager.favouriteSongs().size() > 0) {
                    (new ImageUtils(getActivity())).getImageByPicasso(songsManager.favouriteSongs().get(0).getAlbumID(),imageView3);
                    textView3.setText("Favorites");

                    imageView3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            songsManager.play(0,songsManager.favouriteSongs());
                        }
                    });
                } else {
                    (new ImageUtils(getActivity())).getImageByPicasso(songsManager.newSongs().get(0).getAlbumID(),imageView3);
                    textView3.setText("Recently Added");
                    imageView3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            songsManager.play(0,songsManager.newSongs());
                        }
                    });
                }
            } else {
                (new ImageUtils(getActivity())).getImageByPicasso(songsManager.newSongs().get(0).getAlbumID(),imageView2);
                textView2.setText("Recently Added");
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        songsManager.play(0,songsManager.newSongs());
                    }
                });
                imageView3.setVisibility(View.INVISIBLE);
                textView3.setVisibility(View.INVISIBLE);
            }
        }
        return three_grid;
    }
}
