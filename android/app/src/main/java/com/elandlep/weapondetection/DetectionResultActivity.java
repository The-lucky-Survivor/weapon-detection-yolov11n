package com.elandlep.weapondetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DetectionResultActivity extends AppCompatActivity {

    private ImageView resultImageView;
    private TextView resultTextView;
    private YoloDetector detector;
    private Paint boxPaint, textPaint;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_result);

        resultImageView = findViewById(R.id.result_image_view);
        resultTextView = findViewById(R.id.result_text_view);
        Button btnBack = findViewById(R.id.btn_back_home);

        dbHelper = new DatabaseHelper(this);

        boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(8f);
        boxPaint.setColor(Color.RED);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50f);
        textPaint.setStyle(Paint.Style.FILL);

        try {
            detector = new YoloDetector(this, "best_float32.tflite", "labels.txt");

            // قراءة Threshold
            SharedPreferences prefs = getSharedPreferences("WeaponDetectionPrefs", MODE_PRIVATE);
            int progress = prefs.getInt("threshold", 50);
            detector.setConfidenceThreshold(progress / 100.0f);

        } catch (Exception e) {
            Toast.makeText(this, "Model Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            processImage(imageUri);
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void processImage(Uri imageUri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

            List<YoloDetector.BoundingBox> results = detector.detect(mutableBitmap);

            Canvas canvas = new Canvas(mutableBitmap);
            float scaleX = mutableBitmap.getWidth() / 640f;
            float scaleY = mutableBitmap.getHeight() / 640f;

            Set<String> detectedTypes = new HashSet<>();

            for (YoloDetector.BoundingBox box : results) {
                float left = box.x1 * scaleX;
                float top = box.y1 * scaleY;
                float right = box.x2 * scaleX;
                float bottom = box.y2 * scaleY;

                canvas.drawRect(left, top, right, bottom, boxPaint);
                String label = box.className + " " + String.format("%.0f%%", box.confidence * 100);
                canvas.drawText(label, left, top - 15, textPaint);
                detectedTypes.add(box.className);
            }

            resultImageView.setImageBitmap(mutableBitmap);

            if (results.size() > 0) {
                String typesStr = String.join(", ", detectedTypes);
                resultTextView.setText("DANGER: Found " + results.size() + " weapon(s)\nTypes: " + typesStr);
                resultTextView.setTextColor(Color.RED);

                dbHelper.addDetection(results.size() + "x " + typesStr, "Image Upload");

                // --- تشغيل التنبيهات ---
                triggerAlerts();
            } else {
                resultTextView.setText("SAFE: No weapons detected.");
                resultTextView.setTextColor(Color.GREEN);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // دالة التنبيه (صوت + اهتزاز)
    private void triggerAlerts() {
        SharedPreferences prefs = getSharedPreferences("WeaponDetectionPrefs", MODE_PRIVATE);
        boolean isSoundOn = prefs.getBoolean("sound", true);
        boolean isVibOn = prefs.getBoolean("vibration", true);

        if (isVibOn) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(500);
                }
            }
        }

        if (isSoundOn) {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}