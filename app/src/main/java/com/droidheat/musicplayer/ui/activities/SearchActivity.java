package com.droidheat.musicplayer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.utils.SongsUtils;
import com.droidheat.musicplayer.ui.adapters.CustomAdapter;

public class SearchActivity extends AppCompatActivity {

    // ListView Adapter
    CustomAdapter customAdapter;
    SongsUtils songsUtils;
    TextView text;
    // List view
    private ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        EditText search = findViewById(R.id.editText);
        search.requestFocus();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    lv.setVisibility(View.INVISIBLE);
                    text.setVisibility(View.VISIBLE);
                } else {
                    lv.setVisibility(View.VISIBLE);
                    text.setVisibility(View.INVISIBLE);
                }
                customAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        songsUtils = new SongsUtils(this);

        lv = findViewById(R.id.listView1);
        text = findViewById(R.id.titleTextView);
        lv.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);

        customAdapter = new CustomAdapter(this, songsUtils.allSongs());
        lv.setAdapter(customAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
