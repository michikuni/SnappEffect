package com.mpcorporation.snapeffect.ads

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Skeleton + shimmer cho các ad inline (Native / Banner) lúc đang load.
 *
 * Thay cho chữ "Loading ad…" trơ trọi: vẽ khung xám mô phỏng đúng hình dạng ad thật
 * (icon, dòng text, media, nút CTA) và cho một vệt sáng quét chéo liên tục qua các "xương".
 * Giữ chỗ đúng chiều cao nên khi ad thật xuất hiện, layout không bị giật.
 */

// Vệt sáng rộng ~ nửa card, chạy quá mép để lặp mượt.
private const val SHIMMER_BAND = 420f
private const val SHIMMER_TRAVEL = 1400f
private const val SHIMMER_DURATION_MS = 1100

/** Brush shimmer động dùng chung cho mọi "xương" trong một skeleton. */
@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_TRAVEL,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val base = MaterialTheme.colorScheme.onSurface
    return Brush.linearGradient(
        colors = listOf(
            base.copy(alpha = 0.11f),
            base.copy(alpha = 0.04f),
            base.copy(alpha = 0.11f),
        ),
        start = Offset(x = translate - SHIMMER_BAND, y = 0f),
        end = Offset(x = translate, y = SHIMMER_BAND),
    )
}

/** Một "xương" skeleton: hình bo góc tô bằng brush shimmer. */
@Composable
private fun Bone(
    modifier: Modifier,
    brush: Brush,
    shape: Shape = RoundedCornerShape(6.dp),
) {
    Box(modifier = modifier.clip(shape).background(brush))
}

/** Khung card giữ chỗ, mô phỏng nền + viền của native ad thật (bg_native_ad). */
@Composable
private fun SkeletonCard(
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        content = content,
    )
}

/** Skeleton cho Native Medium: header (icon + text) trên, media lớn giữa, CTA dưới. */
@Composable
fun NativeAdMediumSkeleton(placeholderHeight: Dp, modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    SkeletonCard(height = placeholderHeight, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Bone(Modifier.size(44.dp), brush, RoundedCornerShape(10.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Bone(Modifier.fillMaxWidth(0.5f).height(13.dp), brush)
                Spacer(Modifier.height(7.dp))
                Bone(Modifier.fillMaxWidth(0.85f).height(11.dp), brush)
            }
        }
        Spacer(Modifier.height(10.dp))
        // Media chiếm hết phần cao còn lại để tổng đúng bằng placeholderHeight.
        Bone(Modifier.fillMaxWidth().weight(1f), brush, RoundedCornerShape(8.dp))
        Spacer(Modifier.height(10.dp))
        Bone(Modifier.fillMaxWidth().height(46.dp), brush, RoundedCornerShape(10.dp))
    }
}

/** Skeleton cho Native Horizontal: media trái, (icon + text) phải, CTA dưới. */
@Composable
fun NativeAdHorizontalSkeleton(placeholderHeight: Dp, modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    SkeletonCard(height = placeholderHeight, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Bone(Modifier.width(130.dp).fillMaxHeight(), brush, RoundedCornerShape(8.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Bone(Modifier.size(40.dp), brush, RoundedCornerShape(10.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Bone(Modifier.fillMaxWidth(0.45f).height(10.dp), brush)
                        Spacer(Modifier.height(6.dp))
                        Bone(Modifier.fillMaxWidth(0.9f).height(12.dp), brush)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Bone(Modifier.fillMaxWidth().height(10.dp), brush)
                Spacer(Modifier.height(6.dp))
                Bone(Modifier.fillMaxWidth(0.95f).height(10.dp), brush)
                Spacer(Modifier.height(6.dp))
                Bone(Modifier.fillMaxWidth(0.6f).height(10.dp), brush)
            }
        }
        Spacer(Modifier.height(10.dp))
        Bone(Modifier.fillMaxWidth().height(46.dp), brush, RoundedCornerShape(10.dp))
    }
}

/** Skeleton cho Banner: một thanh bo góc cao đúng bằng chiều cao banner adaptive. */
@Composable
fun BannerAdSkeleton(height: Dp, modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Bone(
        modifier = modifier.fillMaxWidth().height(height),
        brush = brush,
        shape = RoundedCornerShape(8.dp),
    )
}
