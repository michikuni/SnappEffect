# Kế hoạch Refactor SnappEffect

## Mục tiêu
Thay thế toàn bộ UCrop và GPUImage bằng các thành phần tự xây dựng.

```
Hiện tại:                          Mục tiêu:
UCrop     → cắt ảnh                CustomCropView   → tự xây
GPUImage  → hiển thị + filter      Canvas/Bitmap    → tự xây filter pipeline
GPUImage  → lưu ảnh                BitmapIO         → tự xây
```

---

## Phase 1 — Thay UCrop bằng Custom Crop

### 1.1 `CropImageView extends View`
- Vẽ ảnh lên `Canvas` bằng `Matrix` (hỗ trợ scale, pan, xoay)
- Vẽ overlay mờ bên ngoài vùng cắt
- Vẽ khung cắt (border + 4 góc handle + grid lines)
- Đọc EXIF rotation bằng `ExifInterface` để hiển thị đúng chiều

### 1.2 `CropTouchHandler`
- `ACTION_DOWN` → detect hit: góc / cạnh / bên trong frame / ngoài frame
- `ACTION_MOVE` → kéo góc (resize tự do), kéo cạnh, di chuyển cả frame
- Giới hạn frame không vượt ra ngoài ảnh

### 1.3 Nút xoay
- Mỗi lần nhấn xoay `+90°` → cập nhật `Matrix` → redraw

### 1.4 `CropEngine`
- Nhận `RectF cropRect` (tọa độ màn hình) → convert sang tọa độ pixel ảnh thật
- Gọi `Bitmap.createBitmap(src, x, y, w, h, matrix, true)` để cắt + xoay cùng lúc
- Compress → ghi ra cache file → trả `Uri` về `MainActivity`

### Tích hợp
`MainActivity` launch `CropActivity` thay vì `UCrop.of(...).start()`

---

## Phase 2 — Thay GPUImage bằng Custom Image Engine

### 2.1 `ImageRenderer` (thay `GPUImageView`)
- `AppCompatImageView` + `Matrix` cho zoom/pan
- Giữ `Bitmap currentBitmap` là source of truth

### 2.2 Filter Pipeline (thay `GPUImageFilter`)
Interface: `ImageFilter { Bitmap apply(Bitmap src); }`

| GPUImage filter | Thay bằng |
|---|---|
| Brightness, Contrast, Saturation, Hue | `ColorMatrixColorFilter` |
| Gamma, Exposure | `ColorMatrix` custom |
| White Balance | `ColorMatrix` custom |
| Luminance Threshold | pixel-by-pixel `BitmapFilter` |
| Grayscale, Sepia | `ColorMatrixColorFilter` |
| Emboss | Kernel convolution |
| Sobel (Sketch) | Convolution 3x3 |
| Halftone, Crosshatch | Custom Canvas draw |
| SmoothToon | Blur + edge detect combo |
| Solarize | pixel-by-pixel |

### 2.3 `BitmapIO` (thay `gpuImageView.saveToPictures()`)
- Đọc ảnh từ URI: `ContentResolver.openInputStream()` → `BitmapFactory.decodeStream()`
- Ghi ảnh: `compress()` → `MediaStore` insert hoặc `FileOutputStream`
- Xử lý `OutOfMemoryError`: `BitmapFactory.Options.inSampleSize` để scale down ảnh lớn

### 2.4 Cập nhật `SliderUtils` + `HistoryManager`
- `SliderUtils` không còn nhận `GPUImageView`, thay bằng callback `Consumer<Bitmap>`
- `HistoryManager<Uri>` giữ nguyên kiểu Uri (an toàn RAM hơn Bitmap)

---

## Phase 3 — Dọn dẹp Dependencies

Xóa khỏi `build.gradle.kts`:
```kotlin
implementation(libs.ucrop)
implementation(libs.android.image.cropper)
implementation(libs.gpuimage.v210)
```

Giữ lại:
```kotlin
implementation(libs.glide)  // load thumbnail preview trong RecyclerView
```

---

## Thứ tự thực hiện

```
Phase 1: CropImageView → CropTouchHandler → CropEngine → CropActivity → test
Phase 2: BitmapIO → ColorMatrix filters → Convolution filters → Complex filters → test
Phase 3: Xóa dependencies → cleanup
```

## Ước tính

| Phase | Nội dung | Độ phức tạp |
|---|---|---|
| 1 | Custom Crop | ~5-7 ngày |
| 2 | Custom Filters | ~7-10 ngày |
| 3 | Cleanup | ~1 ngày |

> Phase 1 khó nhất ở coordinate mapping và touch handling.
> Phase 2 tốn thời gian nhất ở convolution filters (Emboss, Sobel, SmoothToon).


*Note: ChatGPT sẽ xem kết quả và đánh giá