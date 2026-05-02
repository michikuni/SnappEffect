# SnappEffect

Ứng dụng chỉnh sửa ảnh trên Android: cắt, chỉnh, áp filter nghệ thuật, biến dạng, preset cá nhân, vẽ mask cục bộ và thêm văn bản. Toàn bộ pipeline xử lý ảnh tự xây dựng (Canvas + Bitmap), không phụ thuộc thư viện filter bên thứ 3.

- **Package:** `com.mpcorporation.snapeffect`
- **Phiên bản:** 2.0.0 (Version Code: 15)
- **Min SDK:** 28 · **Target SDK:** 35 · **Compile SDK:** 36
- **Ngôn ngữ:** Kotlin 2.0
- **Kiến trúc:** Clean Architecture (Domain / Data / Presentation) + Hilt + Coroutines + ViewModel/LiveData

---

## Kiến trúc tổng quan

```
┌──────────────────────── Presentation ────────────────────────┐
│  Activity / Fragment  ←observe→  ViewModel (LiveData)        │
│         │                              │                      │
│         │ tương tác                    │ gọi use case          │
│         ▼                              ▼                      │
│   Custom View / Adapter         ┌──────────────┐              │
│                                 │   Domain     │              │
└─────────────────────────────────┤  Use Cases   ├──────────────┘
                                  │  Models      │
                                  │  Repository  │ (interfaces)
                                  └──────┬───────┘
                                         │ phụ thuộc abstraction
                                         ▼
                                  ┌──────────────┐
                                  │     Data     │
                                  │  Repo Impl   │
                                  │  Filters     │
                                  │  BitmapIO    │
                                  └──────────────┘
```

Quy tắc phụ thuộc: `Presentation → Domain ← Data`. Domain không biết Hilt, không biết SharedPreferences, không biết MediaStore. Data thực thi interface domain. Presentation chỉ gọi use case, không gọi thẳng repository.

---

## Cấu trúc thư mục

```
app/src/main/java/com/mpcorporation/snapeffect/
├── SnappEffectApp.kt                          @HiltAndroidApp
│
├── core/
│   ├── di/                                    Hilt modules
│   │   ├── DispatcherModule.kt
│   │   └── RepositoryModule.kt
│   ├── dispatcher/                            CoroutineDispatcher abstraction
│   │   ├── DispatcherProvider.kt
│   │   └── DefaultDispatcherProvider.kt
│   └── util/
│       └── PermissionUtils.kt
│
├── domain/                                    KHÔNG phụ thuộc Android framework cụ thể
│   ├── filter/
│   │   └── ImageFilter.kt                     fun interface (Bitmap) -> Bitmap
│   ├── model/
│   │   ├── BottomNavItem.kt
│   │   ├── EffectItem.kt
│   │   ├── ScaleCrop.kt
│   │   ├── UserPreset.kt
│   │   └── HistoryState.kt
│   ├── repository/                            interfaces
│   │   ├── ImageRepository.kt
│   │   ├── PresetRepository.kt
│   │   ├── HistoryRepository.kt
│   │   └── EffectCatalog.kt
│   └── usecase/                               1 file = 1 use case
│       ├── DecodeImageUseCase.kt
│       ├── ApplyFilterUseCase.kt
│       ├── SaveBitmapToCacheUseCase.kt
│       ├── SaveBitmapToGalleryUseCase.kt
│       ├── ClearTemporaryImagesUseCase.kt
│       ├── CropImageUseCase.kt
│       ├── ApplySelectiveEditUseCase.kt
│       ├── BurnTextUseCase.kt
│       └── PresetUseCases.kt                  Load / Add / Delete
│
├── data/                                      Implementation chi tiết
│   ├── catalog/
│   │   └── EffectCatalogImpl.kt               Định nghĩa toàn bộ filter có sẵn
│   ├── filter/filters/                        20+ filter implementations
│   │   ├── BrightnessFilter.kt  ContrastFilter.kt  SaturationFilter.kt
│   │   ├── HueFilter.kt  GammaFilter.kt  ExposureFilter.kt
│   │   ├── WhiteBalanceFilter.kt  LuminanceThresholdFilter.kt
│   │   ├── GrayscaleFilter.kt  SepiaFilter.kt  EmbossFilter.kt
│   │   ├── SobelFilter.kt  SolarizeFilter.kt  PixelateFilter.kt
│   │   ├── GaussianBlurFilter.kt  SwirlFilter.kt  ZoomBlurFilter.kt
│   │   ├── HalftoneFilter.kt  CrosshatchFilter.kt  SmoothToonFilter.kt
│   │   ├── PresetFilter.kt                    Chain nhiều filter
│   │   ├── ColorMatrixHelper.kt  ConvolutionHelper.kt
│   ├── image/
│   │   ├── BitmapIO.kt                        decode / saveToCache / saveToPictures
│   │   └── ImageRepositoryImpl.kt
│   ├── preset/
│   │   └── PresetRepositoryImpl.kt            JSON ↔ SharedPreferences
│   └── history/
│       └── HistoryRepositoryImpl.kt           Undo/Redo + StateFlow
│
└── presentation/
    ├── editor/                                Màn hình chính
    │   ├── EditorActivity.kt
    │   ├── EditorViewModel.kt
    │   └── widget/ImageRendererView.kt        Custom view: zoom/pan/before-after
    ├── crop/
    │   ├── CropActivity.kt
    │   ├── CropViewModel.kt
    │   └── widget/
    │       ├── CropImageView.kt               Custom canvas crop UI
    │       └── CropTouchHandler.kt
    ├── selective/
    │   ├── SelectiveEditActivity.kt
    │   ├── SelectiveEditViewModel.kt
    │   └── widget/BrushMaskView.kt            Vẽ mask alpha bằng cọ
    ├── text/
    │   ├── TextOverlayActivity.kt
    │   └── TextOverlayViewModel.kt
    └── common/
        ├── SliderController.kt                Helper hiển thị slider tham số
        ├── adapter/                           BottomNav / Effect / ScaleCrop
        └── bottomsheet/                       Effect / Crop / UserPreset
```

---

## Tính năng

| Nhóm | Chi tiết |
|------|----------|
| **Lấy ảnh** | Chụp camera, chọn từ thư viện (`ActivityResultContracts.GetContent`) |
| **Cắt** | 6 tỉ lệ cố định + freeform (kéo handle); pinch zoom; xoay 90° |
| **Điều chỉnh** | Brightness, Contrast, Saturation, Hue, Gamma, Exposure, White Balance, Luminance |
| **Nghệ thuật** | Grayscale, Sepia, SmoothToon, Halftone, Crosshatch, Emboss, Solarize, Sobel |
| **Biến dạng** | Pixelate, Gaussian Blur, Swirl, Zoom Blur |
| **Preset** | 8 preset có sẵn (Vintage / Cool / Warm / Fade / Drama / Matte / B&W / Golden) |
| **Preset cá nhân** | Lưu / áp dụng / xóa qua SharedPreferences |
| **Selective edit** | Vẽ mask bằng cọ, blend ảnh gốc với ảnh đã filter theo alpha mask |
| **Text overlay** | Drag, đổi màu, bold/italic, burn vào bitmap |
| **Lịch sử** | Undo / Redo (StateFlow) |
| **Before/After** | Giữ 2 ngón tay → hiện ảnh gốc, nhả → hiện ảnh đã filter |
| **Zoom & Pan** | Pinch + single-finger pan trên ImageRendererView |
| **Xuất ảnh** | Lưu vào `/Pictures/Snap Effect/` qua MediaStore |
| **Chia sẻ** | FileProvider + `Intent.ACTION_SEND` |

---

## Luồng dữ liệu (vd: áp filter có tham số)

```
EffectBottomSheet (chọn EffectItem)
   └─→ EditorActivity.showSlider()
         └─→ SliderController.show(item, renderer, viewModel)
               ├─ onProgress  → viewModel.previewFilter(filter, previewBitmap)
               │                      └─→ ApplyFilterUseCase  (Dispatchers.Default)
               │                              └─→ filter.apply(bitmap)
               │                      ←─ result → renderer.showFilteredPreview()
               │
               └─ onStopTouch → viewModel.applyFilterAndCommit(filter, source)
                                      ├─→ ApplyFilterUseCase
                                      ├─→ SaveBitmapToCacheUseCase  (Dispatchers.IO)
                                      └─→ HistoryRepository.add(uri)
                                              └─ StateFlow → LiveData → UI cập nhật icon undo/redo
```

---

## Dependency Injection (Hilt)

```
SnappEffectApp                         @HiltAndroidApp
└─ SingletonComponent
   ├─ DispatcherProvider               (DispatcherModule @Binds)
   ├─ ImageRepository                  (RepositoryModule @Binds → ImageRepositoryImpl)
   ├─ PresetRepository                 (RepositoryModule @Binds → PresetRepositoryImpl)
   ├─ HistoryRepository                (RepositoryModule @Binds → HistoryRepositoryImpl)
   └─ EffectCatalog                    (RepositoryModule @Binds → EffectCatalogImpl)

ViewModelComponent                     (mỗi @HiltViewModel inject)
├─ EditorViewModel
├─ CropViewModel
├─ SelectiveEditViewModel
├─ TextOverlayViewModel
└─ UserPresetViewModel

Activity / Fragment                    @AndroidEntryPoint
└─ by viewModels()
```

Activity dùng `@AndroidEntryPoint` và `by viewModels()` để Hilt tự inject ViewModel. ViewModel constructor inject use case; use case constructor inject repository + DispatcherProvider.

---

## Concurrency

- **Coroutines + viewModelScope**: mọi tác vụ async chạy trong scope của ViewModel, tự cancel khi ViewModel bị clear.
- **DispatcherProvider** (interface): bọc `Dispatchers.IO/Default/Main`. Test có thể inject `TestDispatcher` mà không sửa code production.
- **withContext** trong từng use case: I/O xài `dispatchers.io`, tính toán bitmap xài `dispatchers.default`.

---

## Thư viện

| Thư viện | Phiên bản | Mục đích |
|----------|-----------|----------|
| Kotlin | 2.0.21 | Ngôn ngữ chính |
| Hilt | 2.52 | Dependency injection |
| KSP | 2.0.21-1.0.28 | Annotation processing cho Hilt |
| Coroutines | 1.9.0 | Async/await |
| Lifecycle (ViewModel/LiveData) | 2.8.7 | Architecture components |
| Activity / Fragment KTX | 1.11.0 / 1.8.5 | `by viewModels()`, ActivityResult |
| Material Design | 1.13.0 | BottomSheetDialogFragment, components |
| ExifInterface | 1.4.1 | Đọc rotation từ ảnh JPEG |
| Glide | 5.0.5 | Load thumbnail trong RecyclerView |

Đã loại bỏ: GPUImage, UCrop, AndroidImageCropper, Koin, RoundedImageView, GravitySnapHelper, SDP/SSP.

---

## Build

```bash
./gradlew :app:assembleDebug         # APK debug
./gradlew :app:assembleRelease       # APK release
./gradlew :app:compileDebugKotlin    # check compile nhanh
```

---

## Quy tắc khi mở rộng

1. **Thêm filter mới**: tạo class implements `ImageFilter` ở `data/filter/filters/`, đăng ký trong `EffectCatalogImpl.adjustEffects()/artEffects()/distortEffects()/presetEffects()`.
2. **Thêm use case**: 1 file = 1 use case, constructor inject repository/dispatcher, override `operator fun invoke(...)` `suspend`.
3. **Thêm màn hình**: tạo `presentation/<feature>/` với `Activity + ViewModel + (widget/)`. Đăng ký Activity trong `AndroidManifest.xml`. ViewModel `@HiltViewModel`, Activity `@AndroidEntryPoint`.
4. **Không**: không gọi repository trực tiếp từ Activity, không import `data/*` từ trong `presentation/*` hay `domain/*`.

---

## Lưu ý kỹ thuật

- File trung gian lưu trong `cacheDir` (prefix `snap_tmp_`, `cropped_`); thư mục cũ `/Pictures/Snap Effect Temporary/` cũng được dọn khi mở app.
- Bitmap decode tự down-sample qua `inSampleSize` để tránh OOM (giới hạn 2048px).
- Preview bitmap (max 800px) tách khỏi source bitmap để slider feedback mượt.
- Hỗ trợ Android 9+ (API 28). MediaStore Q+ cho `RELATIVE_PATH`, fallback `Environment.getExternalStoragePublicDirectory()` cho API cũ hơn.
