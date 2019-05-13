package com.droidheat.musicplayer;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    // Listview Adapter
    CustomAdapter customAdapter;
    SongsManager songsManager;
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

        EditText search = (EditText) findViewById(R.id.editText);
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

        songsManager = new SongsManager(this);

        lv = (ListView) findViewById(R.id.listView1);
        text = (TextView) findViewById(R.id.heading);
        lv.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);

        Resources res = getResources();
        customAdapter = new CustomAdapter(this, songsManager.allSongs());
        lv.setAdapter(customAdapter);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.search, menu);
//        MenuItem menuItem = menu.findItem(R.id.search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        searchView.setQueryHint("Search song");
//        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
//        View searchPlate = searchView.findViewById(searchPlateId);
//        if (searchPlate != null) {
//            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
//            if (searchText != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    searchText.setBackgroundResource(R.color.transparent);
//                }
//                else {
//                    searchText.setBackgroundColor(Color.TRANSPARENT);
//                }
//                searchText.setPadding(2, 0, 0, 0);
//                searchText.setGravity(Gravity.CENTER_VERTICAL);
//                searchText.setSingleLine(true);
//                searchText.setImeActionLabel("Search", EditorInfo.IME_ACTION_UNSPECIFIED);
//                searchText.setTextColor(Color.WHITE);
//                searchText.setHintTextColor(Color.argb(150,255,255,255));
//                searchText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.abc_text_size_medium_material));
//                Field f;
//                try {
//                    f = TextView.class.getDeclaredField("mCursorDrawableRes");
//                    f.setAccessible(true);
//                    f.set(searchText, R.drawable.cursor_color);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        int magId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
//        ImageView magImage = (ImageView) searchView.findViewById(magId);
//        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
//
//        // Associate searchable configuration with the SearchView
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));
//        searchView.setIconifiedByDefault(false);
//
//        searchView.setOnQueryTextListener(new OnQueryTextListener() {
//
//
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String cs) {
//                if (cs == null || cs == "") {
//                    lv.setVisibility(View.INVISIBLE);
//                    text.setVisibility(View.VISIBLE);
//                } else {
//                    lv.setVisibility(View.VISIBLE);
//                    text.setVisibility(View.INVISIBLE);
//                }
//                customAdapter.filter(cs);
//                return false;
//            }
//        });

        return true;
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
