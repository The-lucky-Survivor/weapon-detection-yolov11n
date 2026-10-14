"""
infer.py — Weapon Detection Inference Script
============================================
Runs the fine-tuned YOLOv11 weapon detector on images, video files,
or a live webcam feed.

Usage:
    # Single image
    python src/infer.py --source path/to/image.jpg

    # Folder of images
    python src/infer.py --source path/to/images/

    # Video file
    python src/infer.py --source path/to/video.mp4

    # Webcam (device index 0)
    python src/infer.py --source 0

    # Custom confidence threshold and output directory
    python src/infer.py --source image.jpg --conf 0.4 --out inference_results/my_run

Arguments:
    --source    Input: image path, folder, video, or webcam index (required)
    --model     Path to .pt checkpoint (default: models/best_finetuned.pt)
    --conf      Confidence threshold 0-1  (default: 0.35)
    --out       Output directory          (default: inference_results/output)
    --show      Display predictions live  (flag, disabled by default)
    --no-save   Do not save predictions   (flag, saving is enabled by default)
"""

import argparse
from pathlib import Path
from ultralytics import YOLO


def parse_args() -> argparse.Namespace:
    repo_root = Path(__file__).parent.parent
    default_model = repo_root / "models" / "best_finetuned.pt"

    parser = argparse.ArgumentParser(
        description="YOLOv11 Weapon Detection Inference",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument(
        "--source",
        required=True,
        help="Input source: image/video path, image folder, or webcam index (0)",
    )
    parser.add_argument(
        "--model",
        type=str,
        default=str(default_model),
        help="Path to the .pt model checkpoint",
    )
    parser.add_argument(
        "--conf",
        type=float,
        default=0.35,
        help="Confidence threshold (0.0–1.0)",
    )
    parser.add_argument(
        "--out",
        type=str,
        default="inference_results/output",
        help="Output directory for saved predictions",
    )
    parser.add_argument(
        "--show",
        action="store_true",
        help="Display predictions in a window (requires display)",
    )
    parser.add_argument(
        "--no-save",
        action="store_true",
        help="Disable saving annotated output",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    model_path = Path(args.model)
    if not model_path.exists():
        raise FileNotFoundError(
            f"Model checkpoint not found: {model_path}\n"
            "Download the model weights and place them at models/best_finetuned.pt\n"
            "See the README for download instructions."
        )

    model = YOLO(str(model_path))

    # Determine save directory and name
    out_path = Path(args.out)
    project = str(out_path.parent)
    name = out_path.name

    print(f"Running inference with conf={args.conf} on: {args.source}")
    model.predict(
        source=args.source,
        conf=args.conf,
        save=not args.no_save,
        show=args.show,
        project=project,
        name=name,
        exist_ok=True,
    )

    if not args.no_save:
        print(f"\nAnnotated outputs saved to: {args.out}/")


if __name__ == "__main__":
    main()