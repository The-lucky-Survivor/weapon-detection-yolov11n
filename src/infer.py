from ultralytics import YOLO


model = YOLO(r'C:\New folder\python\weapon-detection\runs\baseline_train\detect\train4\weights\best.pt')


# image
model.predict(r"C:\Users\DELL\Downloads\WhatsApp Image 2025-12-05 at 15.52.05_053500ef.jpg",
    conf=0.35, 
    save=True, 
    project="inference_results",
    name="baseline",
    exist_ok=True
    )

# video
# model.predict(r"C:\Users\DELL\Downloads\Recording 2025-12-05 164747.mp4", conf=0.6, save=True)


# webcam
# model.predict(source=0, conf=0.45, show=True)