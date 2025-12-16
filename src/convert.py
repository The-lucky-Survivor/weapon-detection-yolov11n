import os
from PIL import Image

# ================= مسار الفولدر الرئيسي =================
ROOT_PATH = r"C:\New folder\python\weapon-detection\Rifle.v1i.yolov5-obb"
# ========================================================

def process_folders():
    # أسماء الفولدرات الفرعية اللي عندك
    sub_dirs = ["train", "test", "valid"]
    
    total_converted = 0

    for sub in sub_dirs:
        # تحديد مسار الـ labels والـ images بناءً على وصفك
        lbl_folder = os.path.join(ROOT_PATH, sub, "labelTxt")
        img_folder = os.path.join(ROOT_PATH, sub, "images")

        # التأكد إن الفولدرات موجودة أصلاً
        if not os.path.exists(lbl_folder):
            print(f"Skipping {sub}: 'labelTxt' folder not found.")
            continue
            
        print(f"Processing: {sub}...")

        # اللف على كل ملفات التيكست
        files = os.listdir(lbl_folder)
        for file in files:
            if file.endswith(".txt") and file != "classes.txt":
                txt_full_path = os.path.join(lbl_folder, file)
                
                # محاولة إيجاد الصورة الموازية في فولدر images
                base_name = os.path.splitext(file)[0]
                image_path = None
                
                for ext in ['.jpg', '.png', '.jpeg']:
                    possible_path = os.path.join(img_folder, base_name + ext)
                    if os.path.exists(possible_path):
                        image_path = possible_path
                        break
                
                # لو مفيش صورة، مش هنعرف نحول (محتاجين المقاسات)
                if not image_path:
                    # print(f"Warning: Image not found for {file}")
                    continue

                # 1. فتح الصورة لمعرفة العرض والطول
                img_w, img_h = 0, 0
                try:
                    with Image.open(image_path) as im:
                        img_w, img_h = im.size
                except:
                    continue

                # 2. قراءة الملف وتحويله
                new_lines = []
                file_changed = False
                
                with open(txt_full_path, 'r') as f:
                    lines = f.readlines()
                    
                for line in lines:
                    parts = line.strip().split()
                    
                    # لازم السطر يكون فيه داتا (أكتر من 5 أرقام عشان نتأكد إنه obb)
                    if len(parts) >= 8:
                        try:
                            # بناخد أول 8 أرقام (x1 y1 ... x4 y4)
                            coords = [float(x) for x in parts[:8]]
                            
                            x_vals = coords[0::2] # الإحداثيات السينية
                            y_vals = coords[1::2] # الإحداثيات الصادية
                            
                            # حساب الصندوق المحيط (Bounding Box)
                            min_x, max_x = min(x_vals), max(x_vals)
                            min_y, max_y = min(y_vals), max(y_vals)
                            
                            # معادلة التحويل لـ YOLO Normalized
                            w = (max_x - min_x) / img_w
                            h = (max_y - min_y) / img_h
                            x_c = (min_x + max_x) / 2 / img_w
                            y_c = (min_y + max_y) / 2 / img_h
                            
                            # التأكد إن الأرقام مش بتعدي 1
                            x_c, y_c = max(0, min(1, x_c)), max(0, min(1, y_c))
                            w, h = max(0, min(1, w)), max(0, min(1, h))

                            # كتابة السطر الجديد: كلاس 0 + 4 أرقام
                            new_lines.append(f"0 {x_c:.6f} {y_c:.6f} {w:.6f} {h:.6f}\n")
                            file_changed = True
                            
                        except ValueError:
                            continue
                
                # 3. حفظ التعديل (Overwrite)
                if file_changed:
                    with open(txt_full_path, 'w') as f:
                        f.writelines(new_lines)
                    total_converted += 1

    print(f"\nDone! Successfully converted {total_converted} files in total.")

if __name__ == "__main__":
    process_folders()