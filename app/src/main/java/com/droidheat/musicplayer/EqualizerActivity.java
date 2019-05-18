package com.droidheat.musicplayer;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.support.v7.widget.SwitchCompat;
import android.widget.Spinner;
import android.widget.Switch;


public class EqualizerActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    final int MAX_SLIDERS = 5; // Must match the XML layout
    SeekBar bass_boost = null;
    SeekBar virtualizerSeekBar = null;
    SwitchCompat enabled = null;
    Button flat = null;
    int min_level = 0;
    int max_level = 100;
    SeekBar[] sliders = new SeekBar[MAX_SLIDERS];
    int num_sliders = 0;
    SharedPrefsUtils sharedPrefsUtils;

    Equalizer eq;
    BassBoost bassBoost;
    Virtualizer virtualizer;
    int currentEqProfile = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        sharedPrefsUtils = new SharedPrefsUtils(this);
        currentEqProfile = sharedPrefsUtils.readSharedPrefsInt("currentEqProfile",0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
            assert upArrow != null;
            upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setTitle("Equalizer");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        eq = new Equalizer(0, sharedPrefsUtils.readSharedPrefsInt("audio_session_id",0));
        bassBoost = new BassBoost(0, sharedPrefsUtils.readSharedPrefsInt("audio_session_id",0));
        virtualizer = new Virtualizer(0, sharedPrefsUtils.readSharedPrefsInt("audio_session_id",0));

        enabled = findViewById(R.id.enabled);
        enabled.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                eq.setEnabled(b);
                bassBoost.setEnabled(b);
                virtualizer.setEnabled(b);
                sharedPrefsUtils.writeSharedPrefs("turnEqualizer",b);

                if (b) {
                    for (int i = 0; i < 5; i++) {
                        eq.setBandLevel((short) i, (short) sharedPrefsUtils
                                .readSharedPrefsInt("profile" + currentEqProfile + "Band" + i,0));
                    }
                    bassBoost.setStrength((short) sharedPrefsUtils.
                            readSharedPrefsInt("bassLevel" + currentEqProfile,0));
                    virtualizer.setStrength((short) sharedPrefsUtils.
                            readSharedPrefsInt("vzLevel" + currentEqProfile,0));
                }

            }
        });

        flat = findViewById(R.id.flat);
        flat.setOnClickListener(this);

        bass_boost = findViewById(R.id.bass_boost);
        virtualizerSeekBar = findViewById(R.id.virtualizer);
        bass_boost.setOnSeekBarChangeListener(this);
        virtualizerSeekBar.setOnSeekBarChangeListener(this);

        sliders[0] = findViewById(R.id.slider_1);
        sliders[1] = findViewById(R.id.slider_2);
        sliders[2] = findViewById(R.id.slider_3);
        sliders[3] = findViewById(R.id.slider_4);
        sliders[4] = findViewById(R.id.slider_5);

        try {
            if (eq != null) {
                num_sliders = (int) eq.getNumberOfBands();
                short[] r = eq.getBandLevelRange();
                min_level = r[0];
                max_level = r[1];
                for (int i = 0; i < num_sliders && i < MAX_SLIDERS; i++) {
                    sliders[i].setOnSeekBarChangeListener(this);
                    //slider_labels[i].setText(formatBandLabel(freq_range));
                }
            }
        } catch (Exception ignored) {
        }

        String[] strings = {"Profile 1", "Profile 2", "Profile 3", "Profile 4", "Profile 5"};
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item,
                        strings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(currentEqProfile);
        spinner.setOnItemSelectedListener(this);

        //updateUI();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int level,
                                  boolean fromTouch) {
        if (seekBar == bass_boost) {
            if (eq.getEnabled()) {
                bassBoost.setEnabled(level > 0);
                bassBoost.setStrength((short) level);// Already in the right range 0-1000
            }
            sharedPrefsUtils.writeSharedPrefs("bassLevel" + currentEqProfile, level);
        } else if (seekBar == virtualizerSeekBar) {
            if (eq.getEnabled()) {
                virtualizer.setEnabled(level > 0);
                virtualizer.setStrength((short) level);
            }
            sharedPrefsUtils.writeSharedPrefs("vzLevel" + currentEqProfile, level);
        } else {
            int new_level = min_level + (max_level - min_level) * level / 100;
            for (int i = 0; i < num_sliders; i++) {
                if (sliders[i] == seekBar) {
                    if (eq.getEnabled()) {
                        eq.setBandLevel((short) i, (short) new_level);
                    }
                    sharedPrefsUtils.writeSharedPrefs("profile"+ currentEqProfile + "Band" + i, new_level);
                    break;
                }
            }
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void updateSliders() {
        for (int i = 0; i < num_sliders; i++) {
            int level = (short) sharedPrefsUtils.readSharedPrefsInt("profile"+ currentEqProfile + "Band" + i,0);
            int pos = 100 * level / (max_level - min_level) + 50;
            sliders[i].setProgress(pos);
        }
    }

    public void updateBassBoost() {
        bass_boost.setProgress((short) sharedPrefsUtils.readSharedPrefsInt("bassLevel" + currentEqProfile,0));
    }

    public void updateVirtualizer() {
        virtualizerSeekBar.setProgress((short) sharedPrefsUtils.readSharedPrefsInt("vzLevel" + currentEqProfile,0));
    }

    @Override
    public void onClick(View view) {
        if (view == flat) {
            setFlat();
        }
    }

    public void updateUI() {
        updateSliders();
        updateBassBoost();
        updateVirtualizer();
        enabled.setChecked(sharedPrefsUtils.readSharedPrefsBoolean("turnEqualizer",false));
    }

    public void setFlat() {
        if (eq != null) {
            bassBoost.setEnabled(false);
            bassBoost.setStrength((short) 0);
            virtualizer.setEnabled(false);
            virtualizer.setStrength((short) 0);
            for (int i = 0; i < num_sliders; i++) {
                eq.setBandLevel((short) i, (short) 0);
                sharedPrefsUtils.writeSharedPrefs("profile"+ currentEqProfile + "Band" + i, 0);
            }
            sharedPrefsUtils.writeSharedPrefs("bassLevel" + currentEqProfile, 0);
            sharedPrefsUtils.writeSharedPrefs("vzLevel" + currentEqProfile, 0);
        }
        updateUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /*
         * Spinner Profile Selection
         */
        if (currentEqProfile != position) {
            sharedPrefsUtils.writeSharedPrefs("currentEqProfile", position);
            currentEqProfile = position;
        }
        Log.d("Equalizer",currentEqProfile + "profile");
        updateUI();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

