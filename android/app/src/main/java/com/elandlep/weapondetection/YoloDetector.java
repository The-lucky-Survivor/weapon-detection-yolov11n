package com.elandlep.weapondetection;

import android.content.Context;
import android.graphics.Bitmap;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YoloDetector {

    private final Interpreter interpreter;
    private final List<String> labels;

    private static final int INPUT_SIZE = 640;

    // --- التعديل هنا: شلنا كلمة static final عشان نقدر نغيرها ---
    private float confidenceThreshold = 0.45f; // القيمة الافتراضية
    private static final float IOU_THRESHOLD = 0.50f;

    public static class BoundingBox {
        public float x1, y1, x2, y2;
        public float confidence;
        public int classId;
        public String className;

        public BoundingBox(float x1, float y1, float x2, float y2, float confidence, int classId, String className) {
            this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
            this.confidence = confidence;
            this.classId = classId;
            this.className = className;
        }
    }

    public YoloDetector(Context context, String modelPath, String labelPath) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(FileUtil.loadMappedFile(context, modelPath), options);
        labels = FileUtil.loadLabels(context, labelPath);
    }

    // --- دالة جديدة لاستقبال القيمة من الإعدادات ---
    public void setConfidenceThreshold(float threshold) {
        this.confidenceThreshold = threshold;
    }

    public List<BoundingBox> detect(Bitmap bitmap) {
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f))
                .build();

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        int[] outputShape = interpreter.getOutputTensor(0).shape();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);

        interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer().rewind());

        float[] outputArray = outputBuffer.getFloatArray();
        List<BoundingBox> allBoxes = new ArrayList<>();

        int channels = outputShape[1];
        int anchors = outputShape[2];

        boolean transposed = false;
        if (anchors < channels) {
            int temp = channels;
            channels = anchors;
            anchors = temp;
            transposed = true;
        }

        for (int i = 0; i < anchors; i++) {
            float maxConfidence = 0;
            int maxClassId = -1;

            for (int c = 0; c < labels.size(); c++) {
                float confidence;
                if (!transposed) {
                    confidence = outputArray[(4 + c) * anchors + i];
                } else {
                    confidence = outputArray[i * channels + (4 + c)];
                }

                if (confidence > maxConfidence) {
                    maxConfidence = confidence;
                    maxClassId = c;
                }
            }

            // هنا بنستخدم المتغير المتغير confidenceThreshold بدلاً من الثابت
            if (maxConfidence > confidenceThreshold) {
                float cx, cy, w, h;

                if (!transposed) {
                    cx = outputArray[0 * anchors + i];
                    cy = outputArray[1 * anchors + i];
                    w  = outputArray[2 * anchors + i];
                    h  = outputArray[3 * anchors + i];
                } else {
                    cx = outputArray[i * channels + 0];
                    cy = outputArray[i * channels + 1];
                    w  = outputArray[i * channels + 2];
                    h  = outputArray[i * channels + 3];
                }

                float x1 = cx - (w / 2F);
                float y1 = cy - (h / 2F);
                float x2 = cx + (w / 2F);
                float y2 = cy + (h / 2F);

                if (x2 <= 1.5f && y2 <= 1.5f) {
                    x1 *= INPUT_SIZE; y1 *= INPUT_SIZE;
                    x2 *= INPUT_SIZE; y2 *= INPUT_SIZE;
                }

                allBoxes.add(new BoundingBox(x1, y1, x2, y2, maxConfidence, maxClassId, labels.get(maxClassId)));
            }
        }

        return applyNMS(allBoxes);
    }

    private List<BoundingBox> applyNMS(List<BoundingBox> boxes) {
        List<BoundingBox> selectedBoxes = new ArrayList<>();
        Collections.sort(boxes, (b1, b2) -> Float.compare(b2.confidence, b1.confidence));

        while (!boxes.isEmpty()) {
            BoundingBox bestBox = boxes.get(0);
            selectedBoxes.add(bestBox);
            boxes.remove(0);

            for (int i = 0; i < boxes.size(); i++) {
                if (calculateIoU(bestBox, boxes.get(i)) > IOU_THRESHOLD) {
                    boxes.remove(i);
                    i--;
                }
            }
        }
        return selectedBoxes;
    }

    private float calculateIoU(BoundingBox b1, BoundingBox b2) {
        float x1 = Math.max(b1.x1, b2.x1);
        float y1 = Math.max(b1.y1, b2.y1);
        float x2 = Math.min(b1.x2, b2.x2);
        float y2 = Math.min(b1.y2, b2.y2);

        if (x1 >= x2 || y1 >= y2) return 0;

        float intersection = (x2 - x1) * (y2 - y1);
        float area1 = (b1.x2 - b1.x1) * (b1.y2 - b1.y1);
        float area2 = (b2.x2 - b2.x1) * (b2.y2 - b2.y1);

        return intersection / (area1 + area2 - intersection);
    }
}