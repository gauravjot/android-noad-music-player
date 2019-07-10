package com.droidheat.musicplayer.ui.fragments;

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

import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.utils.SharedPrefsUtils;
import com.droidheat.musicplayer.ui.callbacks.SimpleItemTouchHelperCallback;
import com.droidheat.musicplayer.ui.adapters.QueueCustomAdapter;

import java.util.Objects;


public class QueueFragment extends Fragment implements QueueCustomAdapter.MyFragmentCallback {

    QueueCustomAdapter adapter;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Resources res = getResources();
        recyclerView = view.findViewById(R.id.recyclerView);

        adapter = new QueueCustomAdapter(getActivity(), res);
        adapter.setMyFragmentCallback(this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(
                (new SharedPrefsUtils(getContext()).readSharedPrefsInt("musicID",0)));

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

    }

    public void notifyFragmentQueueUpdate() {
        adapter.notifyAdapterDataSetChanged();
    }

    public interface MyFragmentCallbackOne {
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
