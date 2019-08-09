package com.droidheat.musicplayer.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.droidheat.musicplayer.ui.fragments.HomeFragment;
import com.droidheat.musicplayer.ui.fragments.PlaylistFragment;
import com.droidheat.musicplayer.R;
import com.droidheat.musicplayer.utils.SharedPrefsUtils;
import com.droidheat.musicplayer.ui.adapters.ViewPagerAdapter;
import com.droidheat.musicplayer.ui.fragments.AlbumGridFragment;
import com.droidheat.musicplayer.ui.fragments.AllSongsFragment;
import com.droidheat.musicplayer.ui.fragments.ArtistGridFragment;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    ViewPager viewPager;
    int currentViewPagerPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        viewPager.setCurrentItem(0);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Quick Play");
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                navView.getMenu().getItem(currentViewPagerPosition).setChecked(false);
                navView.getMenu().getItem(i).setChecked(true);
                currentViewPagerPosition = i;
                switch (i) {
                    case 0:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Quick Play");
                        return;
                    case 1:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("All Songs");
                        return;
                    case 2:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Albums");
                        return;
                    case 3:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Artists");
                        return;
                    case 4:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Playlists");
                        return;
                    default:
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Noad Player");
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_songs:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_albums:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_artists:
                    viewPager.setCurrentItem(3);
                    return true;
                case R.id.navigation_playlists:
                    viewPager.setCurrentItem(4);
                    return true;
            }
            return false;
        }
    };

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new AllSongsFragment());
        adapter.addFragment(new AlbumGridFragment());
        adapter.addFragment(new ArtistGridFragment());
        adapter.addFragment(new PlaylistFragment());
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_searchBtn:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.sleep_timer:
                startActivity(new Intent(this, TimerActivity.class));
                break;
            case R.id.sync:
                startActivity(new Intent(this, SplashActivity.class).putExtra("sync", true));
                finish();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.equalizer:
                startActivity(new Intent(this, EqualizerActivity.class));
                break;
            case R.id.changeTheme:
                final SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(this);
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_choose_accent_color);
                dialog.findViewById(R.id.orange).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","orange");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.cyan).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","cyan");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.green).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","green");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.yellow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","yellow");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.pink).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","pink");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.purple).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","purple");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.grey).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","grey");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.findViewById(R.id.red).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedPrefsUtils.writeSharedPrefs("accentColor","red");
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
