# SnappEffect

Ứng dụng chỉnh sửa ảnh trên Android, cho phép người dùng chụp/chọn ảnh, cắt xén và áp dụng các bộ lọc/hiệu ứng đồ họa thời gian thực thông qua GPU.

- **Package:** `com.mpcorporation.snapeffect`
- **Phiên bản:** 1.0.13 (Version Code: 14)
- **Min SDK:** 28 | **Target SDK:** 35 | **Compile SDK:** 36
- **Ngôn ngữ:** Java 11

---

## Cấu trúc thư mục

```
app/src/main/java/com/mpcorporation/snapeffect/
├── Adapter/          # RecyclerView adapters
├── Filters/          # Factory tạo bộ lọc
├── Handler/          # Xử lý sự kiện (crop, toolbar)
├── Model/            # Data models
├── Utils/            # Tiện ích (lịch sử, slider, permission)
├── View/             # Fragments (bottom sheets)
└── MainActivity.java # Activity trung tâm
```

---

## Tính năng chính

| Nhóm | Tính năng |
|------|-----------|
| **Lấy ảnh** | Chụp camera, chọn từ thư viện |
| **Cắt xén** | 6 tỉ lệ: 1:1, 3:2, 5:4, 7:5, 9:16, 16:9 (via UCrop) |
| **Điều chỉnh** | Brightness, Contrast, Saturation, Hue, Gamma, Exposure, White Balance, Luminance |
| **Nghệ thuật** | Grayscale, Sepia, Toon, Halftone, Crosshatch, Emboss, Solarize, Sobel |
| **Biến dạng** | Pixelation, Gaussian Blur, Swirl, Zoom Blur |
| **Lịch sử** | Undo / Redo đầy đủ |
| **Xuất ảnh** | Lưu vào `/Pictures/Snap Effect/` |

---

## Luồng dữ liệu

```
Người dùng
    → MainActivity
        ├─ Chọn ảnh/Camera → URI → HistoryManager
        ├─ Chọn hiệu ứng → EffectBottomSheet
        │     └─ SeekBar → GPUImageView.setFilter() → Lưu ảnh tạm → HistoryManager
        ├─ Cắt xén → CropBottomSheet → UCrop → CropHandler → HistoryManager
        ├─ Undo/Redo → HistoryManager → Khôi phục URI
        └─ Lưu → GPUImageView.saveToPictures()
```

---

## Thư viện

| Thư viện | Phiên bản | Mục đích |
|----------|-----------|----------|
| GPUImage | 2.1.0 | Render filter qua OpenGL ES |
| UCrop | 2.2.11 | UI cắt ảnh hiện đại |
| Android Image Cropper | 4.6.0 | Crop nâng cao |
| Glide | 5.0.5 | Load và cache ảnh |
| Material Design | 1.13.0 | UI components |
| SDP/SSP | 1.1.1 | Kích thước responsive |

---

## Kiến trúc & Design Patterns

- **Factory Pattern** — `AdjustEffectFactory`, `ArtEffectFactory`, `DistortEffectFactory` tạo danh sách bộ lọc
- **Strategy Pattern** — `BiConsumer<GPUImageFilter, Float>` để áp tham số lên filter
- **Generic History** — `HistoryManager<Uri>` quản lý undo/redo
- **Callback Interfaces** — `OnEffectClickListener`, `OnCropClickListener`
- **Bottom Sheet Fragments** — UI chọn hiệu ứng và crop không chiếm màn hình chính

---

## Giao diện

- Toolbar màu cyan (`#26CCFF`) với các icon: camera, gallery, save, undo, redo, info
- `GPUImageView` chiếm phần lớn màn hình để preview ảnh real-time
- Bottom navigation ngang 4 tab: **Crop / Adjust / Art / Distort**
- Bottom sheet hiện lưới icon hiệu ứng (4 cột), slider xuất hiện động khi chọn filter có tham số
- Placeholder "Please select image" khi chưa có ảnh

---

## Lưu ý kỹ thuật

- Ảnh trung gian lưu vào thư mục tạm `/Pictures/Snap Effect Temporary/`, tự dọn khi thoát app
- Hỗ trợ Android 9 (API 28) trở lên
