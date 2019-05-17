package com.droidheat.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class QueueFragment extends Fragment implements QueueCustomAdapter.MyFragmentCallback {


    SongsManager songsManager;

    QueueCustomAdapter adapter;
    ArrayList<SongModel> CustomListViewValuesArr = new ArrayList<>();

    RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songsManager = new SongsManager(getActivity());
        CustomListViewValuesArr = new ArrayList<>(songsManager.queue());

        Resources res = getResources();
        recyclerView = view.findViewById(R.id.recyclerView);

        adapter = new QueueCustomAdapter(getActivity(), CustomListViewValuesArr, res);
        adapter.setMyFragmentCallback(this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getLayoutManager().scrollToPosition(
                (new SharedPrefsUtils(getContext()).readSharedPrefsInt("musicID",0)));

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

    }

    public void notifyFragmentQueueUpdate() {
        adapter.notifyDataSetChanged();
    }

    interface MyFragmentCallbackOne {
        void viewPagerRefreshOne();
    }

    MyFragmentCallbackOne callback;

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            callback = (MyFragmentCallbackOne) context;
        }
    }

    private void addToViewPager() {
        callback.viewPagerRefreshOne();
    }

    @Override
    public void viewPagerRefresh() {
        addToViewPager();
    }
}
