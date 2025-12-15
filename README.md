# 🔫 YOLOv11 Weapon Detection System

A complete **weapon detection system** built using **YOLOv11**, capable of detecting:
- 🗡️ Knife
- 🔫 Pistol
- 🪖 Rifle

The project is structured following **best practices** for machine learning projects and is ready for:
- Training & fine-tuning
- Evaluation
- Inference (images / videos / webcam)
- Deployment (API / Mobile / Edge)

---

## 📁 Project Structure

```
yolov11-weapon-detection/
│
├── data/
│   ├── images/
│   │   ├── train/
│   │   ├── val/
│   │   └── test/
│   │
│   ├── labels/
│   │   ├── train/
│   │   ├── val/
│   │   └── test/
│   │
│   └── README.md
│
├── configs/
│   └── yolov11n.yaml
│
├── models/
│   └── yolov11n.pt
│
├── src/
│   ├── train.py
│   ├── detect.py
│   ├── evaluate.py
│   └── utils.py
│
├── notebooks/
│   └── data_exploration.ipynb
│
├── runs/
│
├── requirements.txt
├── .gitignore
├── README.md
└── LICENSE
```

---

## 🧠 Classes

| ID | Name   |
|----|--------|
| 0  | Knife  |
| 1  | Pistol |
| 2  | Rifle  |

---

## ⚙️ Installation

```bash
pip install -r requirements.txt
```

---

## 🚀 Training

```bash
python src/train.py
```

---

## 🔍 Inference

### Images / Folder
```bash
python src/detect.py --source data/images/test
```

### Video
```bash
python src/detect.py --source video.mp4
```

### Webcam
```bash
python src/detect.py --source 0
```

---

## 📦 Export Model

```bash
yolo export model=models/yolov11n.pt format=onnx
```

---

## 📱 Deployment

- REST API (FastAPI / Flask)
- Mobile (ONNX / TFLite)
- Edge Devices

---

## 👨‍💻 Author

Weapon Detection System using YOLOv11
