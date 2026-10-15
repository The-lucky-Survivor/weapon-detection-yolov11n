package com.elandlep.weapondetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton; // تغيير مهم

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // زر الرجوع
        FloatingActionButton btnBack = findViewById(R.id.fab_back_team);
        btnBack.setOnClickListener(v -> finish());

        // --- العضو الأول ---
        setupLinkButton(R.id.btn_m1_fb, "https://www.facebook.com/share/1AMV64ycvM/");
        setupLinkButton(R.id.btn_m1_ln, "https://eg.linkedin.com/in/seif-el-dein-ayman-9942702b9?utm_source=share&utm_medium=member_mweb&utm_campaign=share_via&utm_content=profile");
        setupLinkButton(R.id.btn_m1_gh, "https://github.com/Seif-Ayman1");

        // --- العضو الثاني ---
        setupLinkButton(R.id.btn_m2_fb, "https://www.facebook.com/share/1AYN1sbqQv/");
        setupLinkButton(R.id.btn_m2_ln, "https://eg.linkedin.com/in/ibrahem-elashry-aa6032307?utm_source=share&utm_medium=member_mweb&utm_campaign=share_via&utm_content=profile");
        setupLinkButton(R.id.btn_m2_gh, "https://github.com/Ibrahim2005507");

        // --- العضو الثالث ---
        setupLinkButton(R.id.btn_m3_fb, "https://www.facebook.com/share/14MhAxwxEzR/");
        setupLinkButton(R.id.btn_m3_ln, "https://eg.linkedin.com/in/abdallah-el-mallah?utm_source=share&utm_medium=member_mweb&utm_campaign=share_via&utm_content=profile");
        setupLinkButton(R.id.btn_m3_gh, "https://github.com/abdallah-XD");

        // --- العضو الرابع ---
        setupLinkButton(R.id.btn_m4_fb, "https://www.facebook.com/share/1BjyZCdHpV/");
        setupLinkButton(R.id.btn_m4_ln, "https://eg.linkedin.com/in/ahmed-ashraf-fawzy-fouad-ibrahim-issa-alhaitawi");
        setupLinkButton(R.id.btn_m4_gh, "https://github.com/ahmed-ashraf-fawzy-fouad-ibrahim-issa-5");
    }

    // دالة مساعدة لربط زر الصورة بالرابط
    private void setupLinkButton(int buttonId, String url) {
        // نستخدم ImageButton هنا بدلاً من Button
        ImageButton btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}