package com.elandlep.weapondetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // برمجة زر "Meet The Team"
        Button btnMeetTeam = findViewById(R.id.btn_meet_team);
        btnMeetTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this, AboutUsActivity.class));
            }
        });

        // الشريط السفلي
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_about);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_about) return true;

                if (itemId == R.id.navigation_detection) {
                    startActivity(new Intent(AboutActivity.this, MainActivity.class)); finish(); return true;
                }
                if (itemId == R.id.navigation_history) {
                    startActivity(new Intent(AboutActivity.this, HistoryActivity.class)); finish(); return true;
                }
                if (itemId == R.id.navigation_settings) {
                    startActivity(new Intent(AboutActivity.this, SettingsActivity.class)); finish(); return true;
                }
                return false;
            }
        });
    }
}