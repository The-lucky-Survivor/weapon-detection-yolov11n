import os
import shutil
from pathlib import Path

# ================= إعدادات التعديل =================

# الترتيب النهائي للكلاسات في المشروع المجمع
# 0: Knife, 1: Pistol, 2: Rifle
FINAL_CLASSES = ['knife', 'pistol', 'rifle']

DATASETS_CONFIG = [
    # --- الداتا الأولى (Kaggle Standard) ---
    {
        "name": "ds1_kaggle",
        "root_path": r"C:\New folder\python\weapon-detection\archive (2)\weapon-detection",
        "subfolders": ["train", "test", "val"], 
        # المابينج الأصلي للداتا دي
        "mapping": {0: "pistol", 1: "smartphone", 2: "knife", 3:"monedero", 4:"billete", 5:"tarjeta"} 
    },
    
    # --- الداتا الثانية (YOLOv3 - Flat) ---
    {
        "name": "ds2_yolov3",
        "root_path": r"C:\New folder\python\weapon-detection\archive (1)\DataSet Info\images", 
        "subfolders": ["."], 
        "mapping": {0: "knife", 1: "pistol", 2:"rifle"}
    },
    
    # --- الداتا الثالثة (Roboflow - Fixed) ---
    {
        "name": "ds3_roboflow",
        "root_path": r"C:\New folder\python\weapon-detection\Rifle.v1i.yolov5-obb",
        "subfolders": ["train", "valid", "test"],
        # بما إننا حولنا الملفات وخلينا الكلاس 0، وهو أصلاً Rifle، يبقى المابينج كالتالي:
        "mapping": {0: "rifle"} 
    }
]

OUTPUT_DIR = "Final_Unified_Dataset"

# ================= الوظائف المساعدة =================

def create_dirs():
    Path(f"{OUTPUT_DIR}/images").mkdir(parents=True, exist_ok=True)
    Path(f"{OUTPUT_DIR}/labels").mkdir(parents=True, exist_ok=True)

def get_final_id(class_name):
    # تحويل اسم الكلاس للرقم النهائي الموحد
    try:
        return FINAL_CLASSES.index(class_name)
    except ValueError:
        return -1

def find_label_file(image_path, root_path, subfolder_name):
    # دالة البحث عن ملف التيكست (بتدور في labelTxt و labels)
    base_name = os.path.splitext(os.path.basename(image_path))[0]
    txt_name = base_name + ".txt"
    parent_dir = os.path.dirname(os.path.dirname(image_path)) 
    
    candidates = [
        # 1. الأولوية لفولدر labelTxt (عشان داتا Roboflow)
        os.path.join(parent_dir, "labelTxt", subfolder_name, txt_name), # Train/labelTxt/file.txt
        os.path.join(parent_dir, "labelTxt", txt_name), 
        
        # 2. فولدر labels العادي
        os.path.join(parent_dir, "labels", subfolder_name, txt_name),
        os.path.join(parent_dir, "labels", txt_name),
        
        # 3. نفس الفولدر
        os.path.join(os.path.dirname(image_path), txt_name),
        
        # 4. Root labels
        os.path.join(root_path, "labels", txt_name)
    ]
    for p in candidates:
        if os.path.exists(p): return p
    return None

# ================= التنفيذ =================

def process_data():
    if os.path.exists(OUTPUT_DIR):
        print(f"Adding to existing directory '{OUTPUT_DIR}'...")
    
    create_dirs()
    total_images_copied = 0
    
    for ds in DATASETS_CONFIG:
        print(f"\n--- Processing {ds['name']} ---")
        
        for sub in ds['subfolders']:
            current_root = ds['root_path']
            
            # تحديد مسار الصور
            possible_img_dirs = [
                os.path.join(current_root, sub, "images"),      
                os.path.join(current_root, "images", sub),      
                os.path.join(current_root, sub)
            ]
            img_dir = None
            for p in possible_img_dirs:
                if os.path.exists(p) and os.path.isdir(p):
                    files = [f for f in os.listdir(p) if f.lower().endswith(('.jpg', '.png'))]
                    if len(files) > 0:
                        img_dir = p
                        break
            
            if not img_dir:
                print(f"  [Skip] No images found in '{sub}'")
                continue

            print(f"  Source: {img_dir}")
            
            files = os.listdir(img_dir)
            folder_count = 0
            
            for file in files:
                if file.lower().endswith(('.jpg', '.png', '.jpeg')):
                    image_full_path = os.path.join(img_dir, file)
                    
                    # البحث عن ملف التيكست (هيدور في labelTxt ويلاقيه)
                    src_txt_path = find_label_file(image_full_path, current_root, sub)

                    if not src_txt_path:
                        continue 

                    new_lines = []
                    try:
                        with open(src_txt_path, 'r') as f:
                            lines = f.readlines()
                            
                        for line in lines:
                            parts = line.strip().split()
                            if not parts: continue
                            
                            try:
                                old_id = int(parts[0])
                            except ValueError: continue
                            
                            # عملية التوحيد (Mapping)
                            if old_id in ds['mapping']:
                                class_name = ds['mapping'][old_id]
                                new_id = get_final_id(class_name)
                                
                                if new_id != -1:
                                    # بننسخ باقي السطر زي ما هو (لأنه خلاص بقى 4 أرقام مظبوطة)
                                    new_line = f"{new_id} {' '.join(parts[1:])}\n"
                                    new_lines.append(new_line)
                        
                        if new_lines:
                            # تسمية الملف الجديد باسم مميز عشان مفيش حاجة تتمسح
                            new_filename = f"{ds['name']}_{sub}_{os.path.splitext(file)[0]}"
                            
                            # نسخ الصورة
                            shutil.copy(image_full_path, f"{OUTPUT_DIR}/images/{new_filename}{os.path.splitext(file)[1]}")
                            
                            # حفظ التيكست الجديد
                            with open(f"{OUTPUT_DIR}/labels/{new_filename}.txt", 'w') as f:
                                f.writelines(new_lines)
                            
                            folder_count += 1
                            
                    except Exception as e:
                        print(f"Error processing file {file}: {e}")
                        continue

            print(f"     -> Copied {folder_count} images.")
            total_images_copied += folder_count

    # إنشاء ملف data.yaml النهائي
    with open(f"{OUTPUT_DIR}/data.yaml", "w") as f:
        f.write(f"path: {os.path.abspath(OUTPUT_DIR)}\n")
        f.write("train: images\nval: images\n\n")
        f.write(f"nc: {len(FINAL_CLASSES)}\n")
        f.write(f"names: {FINAL_CLASSES}")

    print(f"\nDone! Unified Dataset created at: {os.path.abspath(OUTPUT_DIR)}")
    print(f"Total Images: {total_images_copied}")

if __name__ == "__main__":
    process_data()