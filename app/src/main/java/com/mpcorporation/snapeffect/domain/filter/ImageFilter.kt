package com.mpcorporation.snapeffect.domain.filter

import android.graphics.Bitmap
import android.graphics.ColorMatrix

/**
 * Pure transformation: input bitmap -> output bitmap.
 * Implementations live in the data layer; the domain depends only on this interface.
 */
fun interface ImageFilter {
    fun apply(src: Bitmap): Bitmap

    /**
     * ColorMatrix tương đương, nếu filter chỉ là phép biến đổi màu từng pixel. Trả null khi không
     * biểu diễn được bằng ma trận (blur, biến dạng, halftone, vignette...).
     *
     * Dùng để preview real-time trên GPU (RenderEffect ở màn camera) thay vì chạy [apply] cho từng
     * khung hình. Ảnh gộp từ nhiều matrix có thể lệch rất nhẹ so với chạy [apply] tuần tự — bản
     * tuần tự clamp về 0..255 sau mỗi bước, bản gộp chỉ clamp một lần ở cuối.
     */
    fun asColorMatrix(): ColorMatrix? = null
}
