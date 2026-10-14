"""
combo.py — Multi-Model Weighted Boxes Fusion (WBF) Ensemble
===========================================================
Runs inference using two YOLO models and merges their bounding boxes
using Weighted Boxes Fusion (ensemble-boxes) to enhance detection confidence
and reduce false positives/negatives.

Usage:
    python src/combo.py --model1 models/best_finetuned.pt --model2 runs/detect/train/weights/best.pt --source images/
"""

import argparse
import os
from pathlib import Path
import cv2
import numpy as np
from ultralytics import YOLO
from ensemble_boxes import weighted_boxes_fusion


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Multi-Model Weighted Boxes Fusion Ensemble")
    parser.add_argument(
        "--model1",
        type=str,
        default=str(Path(__file__).parent.parent / "models" / "best_finetuned.pt"),
        help="Path to first YOLO model checkpoint",
    )
    parser.add_argument(
        "--model2",
        type=str,
        required=False,
        default=None,
        help="Path to second YOLO model checkpoint for ensemble",
    )
    parser.add_argument(
        "--source",
        type=str,
        default=str(Path(__file__).parent.parent / "assets" / "inference_samples"),
        help="Path to input images directory or single image",
    )
    parser.add_argument(
        "--output",
        type=str,
        default=str(Path(__file__).parent.parent / "inference_results" / "combo_result"),
        help="Directory to save ensemble detection results",
    )
    parser.add_argument("--conf", type=float, default=0.25, help="Confidence threshold")
    parser.add_argument("--iou", type=float, default=0.4, help="WBF IoU threshold")
    parser.add_argument("--skip-box-thr", type=float, default=0.3, help="WBF skip box threshold")
    return parser.parse_args()


def yolo_to_wbf(result, img_w: int, img_h: int):
    """Normalize bounding boxes from Ultralytics result to [0, 1] range for WBF."""
    boxes = result.boxes.xyxy.cpu().numpy()
    scores = result.boxes.conf.cpu().numpy()
    labels = result.boxes.cls.cpu().numpy().astype(int)

    if len(boxes) == 0:
        return [], [], []

    boxes[:, 0] /= img_w
    boxes[:, 1] /= img_h
    boxes[:, 2] /= img_w
    boxes[:, 3] /= img_h

    return boxes.tolist(), scores.tolist(), labels.tolist()


def run_ensemble(args: argparse.Namespace) -> None:
    if not os.path.exists(args.model1):
        raise FileNotFoundError(f"Model 1 checkpoint not found: {args.model1}")

    model1 = YOLO(args.model1)
    class_names = model1.names

    if args.model2 and os.path.exists(args.model2):
        model2 = YOLO(args.model2)
        weights = [1.0, 1.0]
    else:
        model2 = None
        print("Note: model2 not provided or not found. Running single model detection via WBF pipeline.")

    source_path = Path(args.source)
    if source_path.is_file():
        image_files = [source_path]
    elif source_path.is_dir():
        image_files = [
            f for f in source_path.iterdir()
            if f.suffix.lower() in [".jpg", ".jpeg", ".png", ".bmp", ".webp"]
        ]
    else:
        print(f"Source not found: {args.source}")
        return

    os.makedirs(args.output, exist_ok=True)
    print(f"Found {len(image_files)} image(s) to process. Saving results to: {args.output}")

    for idx, img_file in enumerate(image_files, 1):
        try:
            r1 = model1(str(img_file), conf=args.conf, verbose=False)[0]
            img = r1.orig_img.copy()
            h, w = img.shape[:2]

            boxes1, scores1, labels1 = yolo_to_wbf(r1, w, h)

            if model2 is not None:
                r2 = model2(str(img_file), conf=args.conf, verbose=False)[0]
                boxes2, scores2, labels2 = yolo_to_wbf(r2, w, h)
                boxes_list = [boxes1, boxes2]
                scores_list = [scores1, scores2]
                labels_list = [labels1, labels2]
            else:
                boxes_list = [boxes1]
                scores_list = [scores1]
                labels_list = [labels1]

            # Weighted Boxes Fusion
            boxes, scores, labels = weighted_boxes_fusion(
                boxes_list,
                scores_list,
                labels_list,
                weights=weights if model2 else [1.0],
                iou_thr=args.iou,
                skip_box_thr=args.skip_box_thr,
            )

            if len(boxes) > 0:
                boxes = np.array(boxes)
                boxes[:, 0] *= w
                boxes[:, 1] *= h
                boxes[:, 2] *= w
                boxes[:, 3] *= h

                for box, score, label in zip(boxes, scores, labels):
                    x1, y1, x2, y2 = map(int, box)
                    cls_name = class_names.get(int(label), f"cls_{int(label)}")
                    label_text = f"{cls_name} {score:.2f}"

                    cv2.rectangle(img, (x1, y1), (x2, y2), (0, 255, 0), 2)
                    cv2.putText(
                        img,
                        label_text,
                        (x1, max(15, y1 - 5)),
                        cv2.FONT_HERSHEY_SIMPLEX,
                        0.55,
                        (0, 255, 0),
                        2,
                    )

            save_file = Path(args.output) / img_file.name
            cv2.imwrite(str(save_file), img)
            print(f"[{idx}/{len(image_files)}] Saved: {save_file.name}")

        except Exception as e:
            print(f"Error processing {img_file.name}: {e}")

    print("Ensemble inference complete.")


if __name__ == "__main__":
    run_ensemble(parse_args())
