package com.elandlep.weapondetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveDetectionActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageView overlayView;
    private TextView inferenceTimeText;
    private YoloDetector detector;
    private ExecutorService cameraExecutor;
    private DatabaseHelper dbHelper;

    // متغيرات للتحكم في التنبيه عشان ميبقاش مزعج
    private long lastAlertTime = 0;
    private static final long ALERT_INTERVAL = 2000; // تنبيه كل ثانيتين كحد أقصى

    private Paint boxPaint, textPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_detection);

        previewView = findViewById(R.id.camera_preview);
        overlayView = findViewById(R.id.overlay_view);
        inferenceTimeText = findViewById(R.id.inference_time);
        Button btnBack = findViewById(R.id.btn_back);

        dbHelper = new DatabaseHelper(this);

        boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);
        boxPaint.setColor(Color.RED);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setStyle(Paint.Style.FILL);

        try {
            detector = new YoloDetector(this, "best_float32.tflite", "labels.txt");

            SharedPreferences prefs = getSharedPreferences("WeaponDetectionPrefs", MODE_PRIVATE);
            int progress = prefs.getInt("threshold", 50);
            detector.setConfidenceThreshold(progress / 100.0f);

        } catch (Exception e) {
            inferenceTimeText.setText("Model Error");
            return;
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    previewView.post(() -> {
                        Bitmap bitmap = previewView.getBitmap();
                        if (bitmap != null) {
                            long start = System.currentTimeMillis();
                            List<YoloDetector.BoundingBox> results = detector.detect(bitmap);
                            long end = System.currentTimeMillis();

                            drawBoxes(results, bitmap);
                            inferenceTimeText.setText("Time: " + (end - start) + "ms");

                            // تشغيل التنبيه والحفظ إذا وجد سلاح ومر الوقت الكافي
                            if (!results.isEmpty() && (System.currentTimeMillis() - lastAlertTime > ALERT_INTERVAL)) {
                                YoloDetector.BoundingBox topResult = results.get(0);
                                String conf = String.format("%.0f%%", topResult.confidence * 100);
                                dbHelper.addDetection(topResult.className, conf);

                                triggerAlerts(); // تشغيل الصوت والاهتزاز

                                lastAlertTime = System.currentTimeMillis();
                            }
                        }
                    });
                    image.close();
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void drawBoxes(List<YoloDetector.BoundingBox> boxes, Bitmap originalBitmap) {
        Bitmap overlay = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);

        float scaleX = originalBitmap.getWidth() / 640f;
        float scaleY = originalBitmap.getHeight() / 640f;

        for (YoloDetector.BoundingBox box : boxes) {
            float left = box.x1 * scaleX;
            float top = box.y1 * scaleY;
            float right = box.x2 * scaleX;
            float bottom = box.y2 * scaleY;

            canvas.drawRect(left, top, right, bottom, boxPaint);
            String text = box.className + " " + String.format("%.0f%%", box.confidence * 100);
            canvas.drawText(text, left, top - 10, textPaint);
        }
        overlayView.setImageBitmap(overlay);
    }

    // دالة التنبيه المشتركة
    private void triggerAlerts() {
        SharedPreferences prefs = getSharedPreferences("WeaponDetectionPrefs", MODE_PRIVATE);
        boolean isSoundOn = prefs.getBoolean("sound", true);
        boolean isVibOn = prefs.getBoolean("vibration", true);

        if (isVibOn) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(300);
                }
            }
        }

        if (isSoundOn) {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
                // التأكد أن الـ MediaPlayer لم يعد null (قد يحدث في بعض الأجهزة)
                if (mp != null) {
                    mp.setOnCompletionListener(MediaPlayer::release); // تحرير الذاكرة بعد الانتهاء
                    mp.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraExecutor != null) cameraExecutor.shutdown();
    }
}