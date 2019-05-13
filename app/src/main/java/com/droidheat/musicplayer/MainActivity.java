package com.droidheat.musicplayer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;

import com.eftimoff.viewpagertransformers.ZoomOutSlideTransformer;

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity {

    public static boolean shouldNotifyDataChanged = false;
    ViewPager viewPager;
    FloatingActionButton fab;
    boolean isFabHidden = false;
    SongsManager songsManager;
    NavigationTabBar navigationTabBar;
    MyPagerAdapter adapter;


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainv2);

        //CastContext castContext = CastContext.getSharedInstance(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.statusBar));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);

        navigationTabBar = findViewById(R.id.ntb_vertical);

        songsManager = new SongsManager(this);
//        currentDrawerItem = 0;

        if (getSupportActionBar() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setElevation(0);
            }
            getSupportActionBar().setTitle("");
        }

        viewPager = findViewById(R.id.view_pager);
        fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songsManager.shufflePlay(songsManager.allSongs());
            }
        });

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new ZoomOutSlideTransformer());
        viewPager.setCurrentItem(1);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 4) {
                    fab.hide();
                    isFabHidden = true;
                } else {
                    fab.show();
                    isFabHidden = false;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_library_add_black_24dp),
                        ContextCompat.getColor(this, R.color.accentColor)
                ).title(TITLES[0])
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_library_music_black_24dp),
                        ContextCompat.getColor(this, R.color.accentColor)
                ).title(TITLES[1])
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_artists_black_24dp),
                        ContextCompat.getColor(this, R.color.accentColor)
                ).title(TITLES[2])
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_album_black_24dp),
                        ContextCompat.getColor(this, R.color.accentColor)
                ).title(TITLES[3])
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_playlist_play_black_24dp),
                        ContextCompat.getColor(this, R.color.accentColor)
                ).title(TITLES[4])
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 1);


    }

    private final String[] TITLES = {"New", "All", "Artist", "Album", "Playlist"};

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                return new SongsFragment();
            } else if (position == 1) {
                return new AllSongsFragment();
            } else if (position == 2) {
                return new ArtistGridFragment();
            } else if (position == 3) {
                return new AlbumGridFragment();
            } else if (position == 4) {
                return new PlaylistFragment();
            }
            return new Fragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
//        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
//                menu,
//                R.id.media_route_menu_item);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_searchBtn) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        } else if (id == R.id.sleep_timer) {
            startActivity(new Intent(this, TimerActivity.class));
        } else if (id == R.id.sync) {
            startActivity(new Intent(this, SplashActivity.class).putExtra("sync", true));
        } else if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void showHideFabWithScroll(ListView list) {
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    // if showing
                    if (!isFabHidden) {
                        fab.hide();
                        isFabHidden = true;
                    }
                }

                if (mLastFirstVisibleItem > firstVisibleItem) {
                    if (isFabHidden) {
                        fab.show();
                        isFabHidden = false;
                    }
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });

    }

    public void showHideFabWithScroll(GridView gridView) {
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    // if showing
                    if (!isFabHidden) {
                        fab.hide();
                        isFabHidden = true;
                    }
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    if (isFabHidden) {
                        fab.show();
                        isFabHidden = false;
                    }
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });

    }


}
