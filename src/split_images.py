import os
import shutil
import random
from pathlib import Path

# ================= إعدادات المسارات =================

# 1. مسار الداتا الموحدة (اللي خرجت من السكريبت اللي فات)
SOURCE_FOLDER = "Final_Unified_Dataset" 

# 2. اسم الفولدر الجديد اللي عايزه بالشكل المقسم
DEST_FOLDER = "weapon_yolo_project"

# 3. نسب التقسيم (مجموعهم لازم يكون 1.0)
TRAIN_RATIO = 0.80  # 80% للتدريب
VAL_RATIO   = 0.15  # 15% للتحقق أثناء التدريب
TEST_RATIO  = 0.05  # 5% للاختبار النهائي

# ================= الدوال =================

def create_structure():
    # إنشاء الهيكل: datasets/weapons/images/train ... إلخ
    base_dirs = ["images", "labels"]
    sub_dirs = ["train", "val", "test"]
    
    for base in base_dirs:
        for sub in sub_dirs:
            path = os.path.join(DEST_FOLDER, "datasets", "weapons", base, sub)
            os.makedirs(path, exist_ok=True)
    print(f"Created folder structure in: {DEST_FOLDER}")

def split_and_move():
    # قراءة كل الصور من الداتا الموحدة
    source_images_dir = os.path.join(SOURCE_FOLDER, "images")
    source_labels_dir = os.path.join(SOURCE_FOLDER, "labels")
    
    all_images = [f for f in os.listdir(source_images_dir) if f.lower().endswith(('.jpg', '.png', '.jpeg'))]
    
    # لخبطة الصور عشوائياً (مهم جداً)
    random.shuffle(all_images)
    
    total_count = len(all_images)
    train_count = int(total_count * TRAIN_RATIO)
    val_count = int(total_count * VAL_RATIO)
    # الباقي للتيست
    
    # تقسيم القوائم
    train_imgs = all_images[:train_count]
    val_imgs = all_images[train_count : train_count + val_count]
    test_imgs = all_images[train_count + val_count:]
    
    datasets = [
        ("train", train_imgs),
        ("val", val_imgs),
        ("test", test_imgs)
    ]
    
    print(f"\nTotal Images: {total_count}")
    print(f"Splitting -> Train: {len(train_imgs)} | Val: {len(val_imgs)} | Test: {len(test_imgs)}")
    
    # عملية النقل
    for split_name, img_list in datasets:
        print(f"Copying {split_name} data...")
        for img_file in img_list:
            # مسارات المصدر
            src_img = os.path.join(source_images_dir, img_file)
            src_lbl = os.path.join(source_labels_dir, os.path.splitext(img_file)[0] + ".txt")
            
            # مسارات الهدف
            dst_img = os.path.join(DEST_FOLDER, "datasets", "weapons", "images", split_name, img_file)
            dst_lbl = os.path.join(DEST_FOLDER, "datasets", "weapons", "labels", split_name, os.path.splitext(img_file)[0] + ".txt")
            
            # نسخ الصورة
            shutil.copy(src_img, dst_img)
            
            # نسخ الليبل (لو موجود)
            if os.path.exists(src_lbl):
                shutil.copy(src_lbl, dst_lbl)
                
    # إنشاء ملف data.yaml الجديد
    yaml_content = f"""path: ../datasets/weapons  # dataset root dir
train: images/train  # train images (relative to 'path') 
val: images/val      # val images (relative to 'path') 
test: images/test    # test images (optional)

nc: 3
names: ['knife', 'pistol', 'rifle']
"""
    
    yaml_path = os.path.join(DEST_FOLDER, "data.yaml")
    with open(yaml_path, "w") as f:
        f.write(yaml_content)
        
    print(f"\nDone! Ready to train.")
    print(f"New data.yaml created at: {os.path.abspath(yaml_path)}")

if __name__ == "__main__":
    if not os.path.exists(SOURCE_FOLDER):
        print("Error: Source folder not found!")
    else:
        create_structure()
        split_and_move()