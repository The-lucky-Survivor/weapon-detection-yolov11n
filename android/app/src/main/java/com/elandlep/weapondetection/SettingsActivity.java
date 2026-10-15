package com.elandlep.weapondetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchSound, switchVibration;
    private SeekBar seekBarThreshold;
    private TextView tvThresholdValue;

    private static final String PREFS_NAME = "WeaponDetectionPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. التعريفات
        switchSound = findViewById(R.id.switch_sound);
        switchVibration = findViewById(R.id.switch_vibration);
        seekBarThreshold = findViewById(R.id.seekbar_threshold);
        tvThresholdValue = findViewById(R.id.tv_threshold_value);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 2. تحميل الإعدادات المحفوظة
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isSoundOn = settings.getBoolean("sound", true);
        boolean isVibOn = settings.getBoolean("vibration", true);
        int threshold = settings.getInt("threshold", 50);

        switchSound.setChecked(isSoundOn);
        switchVibration.setChecked(isVibOn);
        seekBarThreshold.setProgress(threshold);
        tvThresholdValue.setText(threshold + "%");

        // 3. حفظ الإعدادات عند التغيير
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.edit().putBoolean("sound", isChecked).apply();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.edit().putBoolean("vibration", isChecked).apply();
        });

        seekBarThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvThresholdValue.setText(progress + "%");
                settings.edit().putInt("threshold", progress).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 4. الشريط السفلي
        navView.setSelectedItemId(R.id.navigation_settings);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_settings) return true;

                if (itemId == R.id.navigation_detection) {
                    startActivity(new Intent(SettingsActivity.this, MainActivity.class)); finish(); return true;
                }
                if (itemId == R.id.navigation_history) {
                    startActivity(new Intent(SettingsActivity.this, HistoryActivity.class)); finish(); return true;
                }
                if (itemId == R.id.navigation_about) {
                    startActivity(new Intent(SettingsActivity.this, AboutActivity.class)); finish(); return true;
                }
                return false;
            }
        });
    }
}