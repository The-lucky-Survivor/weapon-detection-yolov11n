"""
train_v1.py — Baseline Training Script (Stage 1)
=================================================
Trains YOLOv11m from the official pretrained checkpoint on dataset_v1
(the merged multi-source dataset).

Usage:
    python src/train_v1.py [--data CONFIG] [--epochs N] [--batch N] [--device DEVICE]

Arguments (all optional, defaults shown):
    --data    Path to dataset YAML          (default: config/data_v1.yaml)
    --epochs  Number of training epochs     (default: 60)
    --batch   Batch size                    (default: 32)
    --device  Device: 0 for GPU, cpu        (default: 0)
"""

import argparse
from pathlib import Path
from ultralytics import YOLO


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="YOLOv11 baseline training — Stage 1")
    parser.add_argument(
        "--data",
        type=str,
        default=str(Path(__file__).parent.parent / "config" / "data_v1.yaml"),
        help="Path to the dataset YAML file",
    )
    parser.add_argument("--epochs", type=int, default=60)
    parser.add_argument("--batch", type=int, default=32)
    parser.add_argument("--device", default="0", help="cuda device id or 'cpu'")
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    model = YOLO("yolo11m.pt")  # downloads automatically if not present

    results = model.train(
        data=args.data,
        epochs=args.epochs,
        imgsz=640,
        batch=args.batch,
        device=args.device,
        augment=True,
        patience=40,
        project="runs/baseline_train",
        name="detect/train",
        exist_ok=True,
    )

    print(f"\nBaseline training complete. Best checkpoint: {results.save_dir}/weights/best.pt")


if __name__ == "__main__":
    main()