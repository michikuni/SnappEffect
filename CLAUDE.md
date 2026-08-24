# SnappEffect — CLAUDE.md

App Android sửa ảnh (Kotlin, Jetpack Compose, Hilt, clean architecture). Monetize bằng AdMob.
File này ghi kế hoạch tính năng **Sửa ảnh bằng AI** (chốt tháng 7/2026) — đọc trước khi implement bất kỳ phần nào liên quan AI.

---

## 1. Quyết định tổng quan

- **Không dùng free-text prompt.** Người dùng chọn **preset effect** (nút bấm), prompt viết sẵn giấu trong app/Remote Config.
- **API chính: Gemini image model ("Nano Banana") qua Firebase AI Logic** (Phương án A bên dưới).
- **Claude/Anthropic không dùng được** — không có API tạo/sửa ảnh.
- **KHÔNG dùng Imagen** — đã deprecated, Google shutdown từ 30/6/2026.
- **Ads không bù nổi chi phí API** (chi tiết mục 5) → mô hình: quota free nhỏ + rewarded ad làm cổng, **IAP credits là nguồn thu chính**.

---

## 2. Phương án A (khuyến nghị): Gemini qua Firebase AI Logic

Gửi ảnh + prompt → nhận ảnh đã sửa. Gọi trực tiếp từ Android, không cần backend, không lộ API key.

**Chi phí:** `gemini-2.5-flash-image` ~$0.039/ảnh (~1.000đ); `gemini-3.1-flash-image` (Nano Banana 2) từ ~$0.045; Nano Banana Pro ~$0.13–0.24 (chỉ dùng cho effect premium nếu cần). Kiểm tra model hiện hành tại https://firebase.google.com/docs/ai-logic/models trước khi code — tên model đổi nhanh.

**Các bước implement:**

1. **Firebase setup**
   - Thêm `implementation("com.google.firebase:firebase-ai")` (Firebase BoM đã có trong `app/build.gradle.kts`).
   - Cần `google-services.json` — app hiện theo pattern "Firebase optional" (xem build.gradle.kts), nên tính năng AI phải **graceful degradation**: không có Firebase → ẩn toàn bộ UI AI, không crash.
   - Bật **App Check (Play Integrity)** trong Firebase console — bắt buộc, chống kẻ khác gọi API bằng config của app.
   - Production cần **Blaze plan** (billing). Dev/test dùng free tier (model Flash, quota ngày giới hạn).
2. **Data layer**: `AiEffectRepository` — input `Bitmap` + `AiEffect` preset, output `Result<Bitmap>`. Xử lý lỗi mạng, lỗi moderation (model từ chối), timeout. Inject qua Hilt.
3. **Domain**: use case `ApplyAiEffectUseCase`; model `AiEffect(id, displayName, thumbnail, prompt, modelName, costTier)`.
4. **Presentation**: thêm mục AI vào Editor (cạnh effect thường trong `EffectCatalog`); state loading (gọi API mất vài giây → progress UI + cancel); preview trước/sau; nút Save dùng lại `BitmapIO` hiện có.
5. **Code sketch:**

```kotlin
val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
    modelName = "gemini-2.5-flash-image",
    generationConfig = generationConfig {
        responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
    }
)

suspend fun applyAiEffect(source: Bitmap, preset: AiEffect): Bitmap? {
    val response = model.generateContent(content {
        image(source)
        text(preset.prompt)
    })
    return response.candidates.firstOrNull()?.content?.parts
        ?.filterIsInstance<ImagePart>()?.firstOrNull()?.image
}
```

---

## 3. Phương án B (dự phòng, tối ưu giá vốn): OpenAI gpt-image qua backend proxy

Chỉ làm khi Phương án A đã chạy và cần hạ giá vốn (`gpt-image-1-mini` từ ~$0.005/ảnh — rẻ ~8 lần).

- **Bắt buộc có backend proxy** (Cloud Functions / Cloud Run): app → backend (xác thực bằng App Check/Firebase Auth token) → OpenAI. **Tuyệt đối không nhúng OpenAI key vào APK.**
- Backend kiêm luôn: đếm quota per-user, rate limit, log chi phí.
- Repository giữ nguyên interface `AiEffectRepository` → chỉ đổi implementation (lợi ích của clean architecture).
- Đánh giá chất lượng ảnh output so với Gemini trước khi chuyển hẳn.

## 4. Phương án C (song song, miễn phí): ML Kit on-device

Cho các effect đơn giản, chạy offline, **0 đồng/lượt**:

- **Xóa nền / tách chủ thể**: ML Kit Subject Segmentation.
- Kết hợp: tách nền on-device rồi chỉ gọi API cho phần cần AI thật → giảm số lượt gọi API.
- Ưu tiên làm effect "Xóa nền" bằng ML Kit **trước** khi mở effect AI trả phí — vừa có tính năng hot, vừa không tốn tiền.

---

## 5. Preset prompts & Remote Config

- Prompt lưu trong **Firebase Remote Config** (đã có `firebase-config` trong dependencies), fallback mặc định hardcode trong app. Cho phép sửa prompt / thêm / tắt effect không cần release.
- Schema JSON gợi ý trong Remote Config:

```json
{
  "ai_effects": [
    {"id": "remove_bg", "name_vi": "Xóa nền", "prompt": "...", "model": "gemini-2.5-flash-image", "enabled": true, "premium": false},
    {"id": "anime", "name_vi": "Phong cách Anime", "prompt": "...", "model": "gemini-2.5-flash-image", "enabled": true, "premium": true}
  ]
}
```

- **Quy trình viết prompt**: test thủ công trên app Gemini (đã có gói Google AI Pro cá nhân — Nano Banana limit cao, không tốn phí API) với ảnh thật, chốt xong mới đưa vào Remote Config. Model trong app Gemini = model qua API nên kết quả tương đương.
- Preset khởi điểm gợi ý: Xóa nền (ML Kit, free) · Làm nét/phục chế · Xóa vật thể · Đổi nền · Phong cách Anime/Cartoon · Ánh sáng studio · Làm đẹp chân dung.

---

## 6. Monetization — số liệu và mô hình

**Giá vốn:** ~$0.04/ảnh (~1.000đ). **Doanh thu ad:** 1 lượt rewarded = eCPM/1000.

| Thị trường | eCPM rewarded (Android) | Thu/lượt xem | Số ad bù 1 ảnh |
|---|---|---|---|
| Mỹ | ~$16.5 | ~$0.0165 | ~2.5 |
| VN / SEA | ~$2.0 | ~$0.002 | ~20 |

→ **"1 ad = 1 lượt AI" lỗ ở mọi thị trường.** Ở VN lỗ ~95% mỗi lượt. Mô hình chốt:

1. **IAP credits = nguồn thu chính.** Ví dụ gói 49.000đ (~$1.9) / 30 lượt: sau phí Play 15% nhận ~$1.6, giá vốn $1.2 → lãi ~$0.4/gói. Cân nhắc thêm gói lớn hơn biên tốt hơn.
2. **Rewarded ad = cổng vào + marketing, KHÔNG phải nguồn bù.** 2–3 lượt free/ngày, mỗi lượt yêu cầu xem rewarded ad. **Cap ngày là bắt buộc** — giới hạn lỗ tối đa ~2–3k đ/user/ngày, coi là chi phí marketing để đẩy user sang IAP.
3. **Hạ giá vốn**: effect đơn giản đi đường ML Kit (free); về sau cân nhắc Phương án B cho effect còn lại.
4. **Tùy chọn nâng cao**: phân bổ quota theo country của impression (thị trường eCPM cao → nhiều lượt ad-based hơn).
5. Cần thêm: 1 rewarded ad unit mới trong AdMob console + entry trong `releaseAdUnitIds`/`testAdUnitIds` (build.gradle.kts) + BuildConfig field, theo pattern hiện có.

---

## 7. Roadmap

| Phase | Nội dung | Ghi chú |
|---|---|---|
| 1 | ML Kit xóa nền (on-device, free) | Ship sớm, không rủi ro chi phí |
| 2 | Firebase AI Logic + 2–3 preset Gemini, quota free cứng (không ad), App Check, Remote Config | Đo chất lượng + chi phí thật trên nhóm nhỏ |
| 3 | Rewarded ad gate + đếm quota, IAP credits (Play Billing) | Bật billing Blaze trước khi mở rộng |
| 4 | (Tùy) backend proxy + gpt-image-1-mini để hạ giá vốn; phân vùng quota theo country | Chỉ khi Phase 3 có traction |

## 8. Rủi ro & lưu ý cứng

- **Không bao giờ** nhúng API key (OpenAI hay bất kỳ) vào APK. Đường Gemini đi qua Firebase AI Logic + App Check.
- Không có cap quota ngày = không kiểm soát được lỗ → mọi đường gọi API phải qua bộ đếm quota.
- Gói Google AI Pro cá nhân **không** cấp quota API cho app (billing tách biệt) — chỉ dùng để test prompt thủ công.
- Model image bị moderation có thể từ chối ảnh/prompt → repository phải trả lỗi thân thiện, không được nuốt lỗi.
- Ảnh người dùng gửi lên server Google → cần cập nhật privacy policy + Play Data Safety form trước khi release.
- Tên model Gemini đổi nhanh — để `modelName` trong Remote Config, không hardcode rải rác.
