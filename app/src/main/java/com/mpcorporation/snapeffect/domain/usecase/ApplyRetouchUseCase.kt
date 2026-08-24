package com.mpcorporation.snapeffect.domain.usecase

import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import com.mpcorporation.snapeffect.core.dispatcher.DispatcherProvider
import com.mpcorporation.snapeffect.domain.model.RetouchFace
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Màu son mặc định (hồng) và màu má hồng (blush-500). */
private val DEFAULT_LIP_COLOR = 0xFFC85A78.toInt()
private val BLUSH_COLOR = 0xFFFF7AB0.toInt()

/**
 * Retouch làm đẹp on-device, nhắm khuôn mặt:
 * - Smooth: làm mịn (box blur) blend trong vùng da mặt (fallback: toàn ảnh nếu không có mặt).
 * - Glow: nâng sáng vùng highlight của da cho rạng rỡ.
 * - Eyes: làm sáng quanh mắt.
 * - Teeth: giảm ố vàng + làm sáng vùng răng trong miệng.
 */
class ApplyRetouchUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val dispatchers: DispatcherProvider,
) {
    /** Cường độ từng công cụ, 0..1 (trừ [lipColor] là màu son ARGB). */
    data class Params(
        val smooth: Float = 0f,
        val glow: Float = 0f,
        val eyes: Float = 0f,
        val teeth: Float = 0f,
        val lips: Float = 0f,
        val blush: Float = 0f,
        val slim: Float = 0f,
        val eyeEnlarge: Float = 0f,
        val lipColor: Int = DEFAULT_LIP_COLOR,
    ) {
        val isEmpty: Boolean
            get() = smooth == 0f && glow == 0f && eyes == 0f && teeth == 0f &&
                lips == 0f && blush == 0f && slim == 0f && eyeEnlarge == 0f
    }

    /** Retouch -> Bitmap (không lưu). Dùng cho preview. */
    suspend fun render(source: Bitmap, faces: List<RetouchFace>, params: Params): Bitmap =
        withContext(dispatchers.default) { compose(source, faces, params) }

    /** Retouch full-res -> lưu cache -> Uri. Dùng khi Save. */
    suspend fun renderAndSave(source: Bitmap, faces: List<RetouchFace>, params: Params): Uri =
        withContext(dispatchers.default) {
            val out = compose(source, faces, params)
            imageRepository.saveToCache(out, "snap_tmp_retouch_${System.currentTimeMillis()}.jpg")
        }

    private fun compose(source: Bitmap, faces: List<RetouchFace>, params: Params): Bitmap {
        val w = source.width
        val h = source.height
        val src = IntArray(w * h)
        source.getPixels(src, 0, w, 0, 0, w, h)
        val out = src.copyOf()

        val needBlur = params.smooth > 0f || params.glow > 0f
        val blurred = if (needBlur) {
            val radius = max(1, (min(w, h) * 0.012f).roundToInt())
            boxBlur(src, w, h, radius)
        } else {
            src
        }

        if (needBlur) {
            for (y in 0 until h) {
                val row = y * w
                for (x in 0 until w) {
                    val i = row + x
                    val skin = skinInfluence(x.toFloat(), y.toFloat(), faces)
                    if (skin <= 0f) continue

                    var r = ((src[i] ushr 16) and 0xFF).toFloat()
                    var g = ((src[i] ushr 8) and 0xFF).toFloat()
                    var b = (src[i] and 0xFF).toFloat()

                    if (params.smooth > 0f) {
                        val t = params.smooth * skin
                        r = r * (1 - t) + ((blurred[i] ushr 16) and 0xFF) * t
                        g = g * (1 - t) + ((blurred[i] ushr 8) and 0xFF) * t
                        b = b * (1 - t) + (blurred[i] and 0xFF) * t
                    }
                    if (params.glow > 0f) {
                        val luma = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
                        val lift = params.glow * skin * 0.4f * luma
                        r += (255 - r) * lift
                        g += (255 - g) * lift
                        b += (255 - b) * lift
                    }
                    out[i] = pack(r, g, b)
                }
            }
        }

        if (params.eyes > 0f) faces.forEach { brightenEyes(out, w, h, it, params.eyes) }
        if (params.teeth > 0f) faces.forEach { whitenTeeth(out, w, h, it, params.teeth) }
        if (params.lips > 0f) faces.forEach { applyLips(out, w, h, it, params.lips, params.lipColor) }
        if (params.blush > 0f) faces.forEach { applyBlush(out, w, h, it, params.blush) }

        // Chỉnh hình (warp) áp cuối cùng lên ảnh đã xử lý màu.
        val result = if (params.slim > 0f || params.eyeEnlarge > 0f) {
            warp(out, w, h, faces, params.slim, params.eyeEnlarge)
        } else {
            out
        }

        return Bitmap.createBitmap(result, w, h, Bitmap.Config.ARGB_8888)
    }

    /** Ảnh hưởng "da mặt" tại (x,y): ellipse quanh bbox, mép mờ dần. Không có mặt -> 1 (toàn ảnh). */
    private fun skinInfluence(x: Float, y: Float, faces: List<RetouchFace>): Float {
        if (faces.isEmpty()) return 1f
        var best = 0f
        for (f in faces) {
            val cx = f.bounds.centerX()
            val cy = f.bounds.centerY()
            val rx = f.bounds.width() * 0.55f
            val ry = f.bounds.height() * 0.62f
            if (rx <= 0f || ry <= 0f) continue
            val nx = (x - cx) / rx
            val ny = (y - cy) / ry
            best = max(best, feather(nx * nx + ny * ny))
        }
        return best
    }

    private fun brightenEyes(out: IntArray, w: Int, h: Int, face: RetouchFace, amount: Float) {
        val radius = face.bounds.width() * 0.14f
        if (radius <= 1f) return
        listOfNotNull(face.leftEye, face.rightEye).forEach { eye ->
            val minX = max(0, (eye.x - radius).toInt())
            val maxX = min(w - 1, (eye.x + radius).toInt())
            val minY = max(0, (eye.y - radius).toInt())
            val maxY = min(h - 1, (eye.y + radius).toInt())
            for (y in minY..maxY) {
                val row = y * w
                for (x in minX..maxX) {
                    val dx = (x - eye.x) / radius
                    val dy = (y - eye.y) / radius
                    val t = feather(dx * dx + dy * dy) * amount
                    if (t <= 0f) continue
                    val i = row + x
                    var r = ((out[i] ushr 16) and 0xFF).toFloat()
                    var g = ((out[i] ushr 8) and 0xFF).toFloat()
                    var b = (out[i] and 0xFF).toFloat()
                    val lift = t * 0.3f
                    r += (255 - r) * lift
                    g += (255 - g) * lift
                    b += (255 - b) * lift
                    out[i] = pack(r, g, b)
                }
            }
        }
    }

    private fun whitenTeeth(out: IntArray, w: Int, h: Int, face: RetouchFace, amount: Float) {
        val mouth = face.mouth ?: return
        val minX = max(0, mouth.left.toInt())
        val maxX = min(w - 1, mouth.right.toInt())
        val minY = max(0, mouth.top.toInt())
        val maxY = min(h - 1, mouth.bottom.toInt())
        if (minX >= maxX || minY >= maxY) return
        for (y in minY..maxY) {
            val row = y * w
            for (x in minX..maxX) {
                val i = row + x
                val r = ((out[i] ushr 16) and 0xFF).toFloat()
                val g = ((out[i] ushr 8) and 0xFF).toFloat()
                val b = (out[i] and 0xFF).toFloat()
                // Chỉ tác động vào pixel sáng (răng) - bỏ qua môi tối
                val luma = 0.299f * r + 0.587f * g + 0.114f * b
                if (luma < 110f) continue
                val t = amount * 0.7f
                // Giảm ố vàng: nâng B về phía max(R,G); nhẹ nhàng nâng sáng
                val nb = b + (max(r, g) - b) * t
                out[i] = pack(
                    r + (255 - r) * t * 0.12f,
                    g + (255 - g) * t * 0.12f,
                    nb + (255 - nb) * t * 0.12f,
                )
            }
        }
    }

    /** Tô son: pha màu [color] vào các pixel trong đa giác môi. */
    private fun applyLips(out: IntArray, w: Int, h: Int, face: RetouchFace, amount: Float, color: Int) {
        val poly = face.lips
        if (poly.size < 3) return
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        poly.forEach {
            minX = min(minX, it.x); maxX = max(maxX, it.x)
            minY = min(minY, it.y); maxY = max(maxY, it.y)
        }
        val x0 = max(0, minX.toInt())
        val x1 = min(w - 1, maxX.toInt())
        val y0 = max(0, minY.toInt())
        val y1 = min(h - 1, maxY.toInt())
        if (x0 >= x1 || y0 >= y1) return

        val lr = (color ushr 16) and 0xFF
        val lg = (color ushr 8) and 0xFF
        val lb = color and 0xFF
        val t = amount * 0.5f
        for (y in y0..y1) {
            val row = y * w
            for (x in x0..x1) {
                if (!pointInPolygon(x + 0.5f, y + 0.5f, poly)) continue
                val i = row + x
                val r = ((out[i] ushr 16) and 0xFF).toFloat()
                val g = ((out[i] ushr 8) and 0xFF).toFloat()
                val b = (out[i] and 0xFF).toFloat()
                out[i] = pack(r * (1 - t) + lr * t, g * (1 - t) + lg * t, b * (1 - t) + lb * t)
            }
        }
    }

    /** Má hồng: đốm màu mềm tại tâm hai má. */
    private fun applyBlush(out: IntArray, w: Int, h: Int, face: RetouchFace, amount: Float) {
        val radius = face.bounds.width() * 0.17f
        if (radius <= 1f) return
        val br = (BLUSH_COLOR ushr 16) and 0xFF
        val bg = (BLUSH_COLOR ushr 8) and 0xFF
        val bb = BLUSH_COLOR and 0xFF
        listOfNotNull(face.leftCheek, face.rightCheek).forEach { c ->
            val minX = max(0, (c.x - radius).toInt())
            val maxX = min(w - 1, (c.x + radius).toInt())
            val minY = max(0, (c.y - radius).toInt())
            val maxY = min(h - 1, (c.y + radius).toInt())
            for (y in minY..maxY) {
                val row = y * w
                for (x in minX..maxX) {
                    val dx = (x - c.x) / radius
                    val dy = (y - c.y) / radius
                    val t = feather(dx * dx + dy * dy) * amount * 0.35f
                    if (t <= 0f) continue
                    val i = row + x
                    val r = ((out[i] ushr 16) and 0xFF).toFloat()
                    val g = ((out[i] ushr 8) and 0xFF).toFloat()
                    val b = (out[i] and 0xFF).toFloat()
                    out[i] = pack(r * (1 - t) + br * t, g * (1 - t) + bg * t, b * (1 - t) + bb * t)
                }
            }
        }
    }

    /**
     * Warp hình học (inverse map + bilinear): thon gọn mặt (ép ngang quanh má) và to mắt (phồng
     * quanh tâm mắt). Không có mặt -> trả nguyên bản (warp cần landmark).
     */
    private fun warp(src: IntArray, w: Int, h: Int, faces: List<RetouchFace>, slim: Float, enlarge: Float): IntArray {
        val out = IntArray(src.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                var sx = x.toFloat()
                var sy = y.toFloat()
                for (face in faces) {
                    if (slim > 0f) {
                        val cx = face.bounds.centerX()
                        val midY = face.leftCheek?.y ?: face.rightCheek?.y
                            ?: (face.bounds.centerY() + face.bounds.height() * 0.1f)
                        val halfBand = face.bounds.height() * 0.45f
                        if (halfBand > 0f) {
                            val dyN = (y - midY) / halfBand
                            val bandY = feather(dyN * dyN)
                            if (bandY > 0f) {
                                val factor = 1f + slim * 0.28f * bandY
                                sx = cx + (sx - cx) * factor
                            }
                        }
                    }
                    if (enlarge > 0f) {
                        val r = face.bounds.width() * 0.16f
                        if (r > 1f) {
                            listOfNotNull(face.leftEye, face.rightEye).forEach { eye ->
                                val dx = x - eye.x
                                val dy = y - eye.y
                                val d2 = (dx * dx + dy * dy) / (r * r)
                                if (d2 < 1f) {
                                    val k = enlarge * 0.35f * feather(d2)
                                    sx -= dx * k
                                    sy -= dy * k
                                }
                            }
                        }
                    }
                }
                out[row + x] = bilinear(src, w, h, sx, sy)
            }
        }
        return out
    }

    private fun bilinear(src: IntArray, w: Int, h: Int, fx: Float, fy: Float): Int {
        val cx = fx.coerceIn(0f, (w - 1).toFloat())
        val cy = fy.coerceIn(0f, (h - 1).toFloat())
        val x0 = cx.toInt()
        val y0 = cy.toInt()
        val x1 = min(x0 + 1, w - 1)
        val y1 = min(y0 + 1, h - 1)
        val tx = cx - x0
        val ty = cy - y0
        val p00 = src[y0 * w + x0]
        val p10 = src[y0 * w + x1]
        val p01 = src[y1 * w + x0]
        val p11 = src[y1 * w + x1]

        fun ch(shift: Int): Int {
            val a = (p00 ushr shift) and 0xFF
            val b = (p10 ushr shift) and 0xFF
            val c = (p01 ushr shift) and 0xFF
            val d = (p11 ushr shift) and 0xFF
            val top = a + (b - a) * tx
            val bottom = c + (d - c) * tx
            return (top + (bottom - top) * ty).toInt().coerceIn(0, 255)
        }
        return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    private fun pointInPolygon(px: Float, py: Float, poly: List<PointF>): Boolean {
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val xi = poly[i].x
            val yi = poly[i].y
            val xj = poly[j].x
            val yj = poly[j].y
            if (((yi > py) != (yj > py)) &&
                (px < (xj - xi) * (py - yi) / (yj - yi) + xi)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    /** 1 khi bên trong (d2<=0.72), mờ dần về 0 tại d2>=1 (smoothstep). */
    private fun feather(d2: Float): Float = when {
        d2 <= 0.72f -> 1f
        d2 >= 1.0f -> 0f
        else -> {
            val t = (1.0f - d2) / 0.28f
            t * t * (3f - 2f * t)
        }
    }

    private fun pack(r: Float, g: Float, b: Float): Int {
        val ri = r.roundToInt().coerceIn(0, 255)
        val gi = g.roundToInt().coerceIn(0, 255)
        val bi = b.roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (ri shl 16) or (gi shl 8) or bi
    }

    /** Box blur tách trục (ngang rồi dọc) - xấp xỉ Gaussian, đủ dùng cho làm mịn da. */
    private fun boxBlur(px: IntArray, w: Int, h: Int, radius: Int): IntArray {
        val tmp = IntArray(px.size)
        val out = IntArray(px.size)
        blurPass(px, tmp, w, h, radius, horizontal = true)
        blurPass(tmp, out, w, h, radius, horizontal = false)
        return out
    }

    private fun blurPass(
        src: IntArray,
        dst: IntArray,
        w: Int,
        h: Int,
        radius: Int,
        horizontal: Boolean,
    ) {
        val len = if (horizontal) w else h
        val lines = if (horizontal) h else w
        val window = 2 * radius + 1
        for (o in 0 until lines) {
            fun index(i: Int): Int {
                val ci = i.coerceIn(0, len - 1)
                return if (horizontal) o * w + ci else ci * w + o
            }

            var sr = 0
            var sg = 0
            var sb = 0
            for (k in -radius..radius) {
                val p = src[index(k)]
                sr += (p ushr 16) and 0xFF
                sg += (p ushr 8) and 0xFF
                sb += p and 0xFF
            }
            for (i in 0 until len) {
                dst[index(i)] = (0xFF shl 24) or
                    ((sr / window) shl 16) or ((sg / window) shl 8) or (sb / window)
                val pOut = src[index(i - radius)]
                val pIn = src[index(i + radius + 1)]
                sr += ((pIn ushr 16) and 0xFF) - ((pOut ushr 16) and 0xFF)
                sg += ((pIn ushr 8) and 0xFF) - ((pOut ushr 8) and 0xFF)
                sb += (pIn and 0xFF) - (pOut and 0xFF)
            }
        }
    }
}
