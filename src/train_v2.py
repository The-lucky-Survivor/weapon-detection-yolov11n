"""
train_v2.py — Fine-tuning Script (Stage 2)
==========================================
Fine-tunes the best Stage-1 checkpoint on dataset_v2
(extended dataset with additional samples).

Usage:
    python src/train_v2.py [--checkpoint PATH] [--data CONFIG] [--epochs N] [--batch N] [--device DEVICE]

Arguments (all optional, defaults shown):
    --checkpoint  Path to Stage-1 best.pt     (default: runs/baseline_train/detect/train/weights/best.pt)
    --data        Path to dataset YAML         (default: config/data_v2.yaml)
    --epochs      Number of training epochs    (default: 75)
    --batch       Batch size                   (default: 32)
    --device      Device: 0 for GPU, cpu       (default: 0)
"""

import argparse
from pathlib import Path
from ultralytics import YOLO


def parse_args() -> argparse.Namespace:
    repo_root = Path(__file__).parent.parent
    parser = argparse.ArgumentParser(description="YOLOv11 fine-tuning — Stage 2")
    parser.add_argument(
        "--checkpoint",
        type=str,
        default=str(repo_root / "runs" / "baseline_train" / "detect" / "train" / "weights" / "best.pt"),
        help="Path to the Stage-1 best.pt checkpoint",
    )
    parser.add_argument(
        "--data",
        type=str,
        default=str(repo_root / "config" / "data_v2.yaml"),
        help="Path to the dataset YAML file",
    )
    parser.add_argument("--epochs", type=int, default=75)
    parser.add_argument("--batch", type=int, default=32)
    parser.add_argument("--device", default="0", help="cuda device id or 'cpu'")
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    if not Path(args.checkpoint).exists():
        raise FileNotFoundError(
            f"Stage-1 checkpoint not found: {args.checkpoint}\n"
            "Run train_v1.py first, or provide --checkpoint <path>."
        )

    model = YOLO(args.checkpoint)

    results = model.train(
        data=args.data,
        epochs=args.epochs,
        imgsz=640,
        batch=args.batch,
        device=args.device,
        augment=True,
        patience=40,
        project="runs/finetune_train",
        name="detect/train",
        exist_ok=True,
    )

    print(f"\nFine-tuning complete. Best checkpoint: {results.save_dir}/weights/best.pt")


if __name__ == "__main__":
    main()