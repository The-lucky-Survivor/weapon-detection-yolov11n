package com.elandlep.weapondetection;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // عناصر الواجهة
    private ImageView imagePreview;
    private VideoView videoPreview;

    // متغير لتخزين رابط الصورة المختارة لإرسالها لصفحة النتائج
    private Uri selectedImageUri = null;

    // أدوات استقبال النتائج (Launchers)
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. طلب الصلاحيات فور تشغيل التطبيق ---
        checkAndRequestPermissions();

        // --- 2. تعريف العناصر (Binding) ---
        Button startDetectionButton = findViewById(R.id.button_start_detection);
        Button uploadImageButton = findViewById(R.id.button_upload_image);
        Button uploadVideoButton = findViewById(R.id.button_upload_video);
        Button openCameraButton = findViewById(R.id.button_open_camera);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        imagePreview = findViewById(R.id.image_preview);
        videoPreview = findViewById(R.id.video_preview);

        // إعداد مشغل الفيديو
        MediaController mediaController = new MediaController(this);
        videoPreview.setMediaController(mediaController);


        // --- 3. إعداد مستقبلات الميديا (Launchers) ---

        // أ) مستقبل الصور
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // حفظ الـ Uri
                            selectedImageUri = result.getData().getData();

                            // تحديث الواجهة: إخفاء الفيديو وإظهار الصورة
                            imagePreview.setVisibility(View.VISIBLE);
                            videoPreview.setVisibility(View.GONE);

                            // عرض الصورة المختارة
                            imagePreview.setImageURI(selectedImageUri);
                            Toast.makeText(MainActivity.this, "Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // ب) مستقبل الفيديو
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri videoUri = result.getData().getData();

                            // تصفير الصورة المختارة لأننا الآن نعرض فيديو
                            selectedImageUri = null;

                            // تحديث الواجهة: إخفاء الصورة وإظهار الفيديو
                            videoPreview.setVisibility(View.VISIBLE);
                            imagePreview.setVisibility(View.GONE);

                            // تشغيل الفيديو
                            videoPreview.setVideoURI(videoUri);
                            videoPreview.start();
                            Toast.makeText(MainActivity.this, "Video Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        // --- 4. برمجة الأزرار ---

        // زر رفع الصورة
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });

        // زر رفع الفيديو
        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                videoPickerLauncher.launch(intent);
            }
        });

        // زر فتح الكاميرا المباشرة
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, LiveDetectionActivity.class);
                    startActivity(intent);
                } else {
                    checkAndRequestPermissions();
                    Toast.makeText(MainActivity.this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // زر بدء الكشف
        startDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // الحالة 1: المستخدم اختار صورة
                if (selectedImageUri != null && imagePreview.getVisibility() == View.VISIBLE) {
                    Intent intent = new Intent(MainActivity.this, DetectionResultActivity.class);
                    intent.putExtra("imageUri", selectedImageUri.toString());
                    startActivity(intent);
                }
                // الحالة 2: المستخدم اختار فيديو
                else if (videoPreview.getVisibility() == View.VISIBLE) {
                    Toast.makeText(MainActivity.this, "Video analysis coming soon", Toast.LENGTH_SHORT).show();
                }
                // الحالة 3: لم يختر شيئاً -> نفتح الكشف المباشر
                else {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MainActivity.this, LiveDetectionActivity.class);
                        startActivity(intent);
                    } else {
                        checkAndRequestPermissions();
                    }
                }
            }
        });


        // --- 5. برمجة الشريط السفلي ---
        navView.setSelectedItemId(R.id.navigation_detection);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_detection) {
                    return true;
                } else if (itemId == R.id.navigation_history) {
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    // --- 6. دالة طلب الصلاحيات ---
    private void checkAndRequestPermissions() {
        String[] permissions;

        // التعامل مع اختلاف إصدارات أندرويد
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {

            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[0]),
                    1);
        }
    }
}