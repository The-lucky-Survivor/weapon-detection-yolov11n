from ultralytics import YOLO

def main():
    # اختيار النموذج:
    # model = YOLO("yolov8n.pt") # خفيف وسريع
    # model = YOLO("yolov8s.pt") # نموذج YOLOv8s (افتراضًا أن هذا هو المقصود)
    # model = YOLO("yolov8m.pt") # أقوى بس أبطأ
    model = YOLO(r"C:\New folder\python\weapon-detection\runs\baseline_train\detect\train4\weights\best.pt") 

    model.train(
        data=r"C:\New folder\python\weapon-detection\config\data_v2.yaml",
        epochs=75,
        imgsz=640,
        batch=32,
        device=0, # GPU
        augment=True,
        patience=40,         # early stopping patience
    )

if __name__ == "__main__":
    main()