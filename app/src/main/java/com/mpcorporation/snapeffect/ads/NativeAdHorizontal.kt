package com.mpcorporation.snapeffect.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mpcorporation.snapeffect.R

/** Chiều cao placeholder ~ chiều cao thật (~210dp) để layout không bị giật */
private val PlaceholderHeight = 210.dp

/**
 * Native Ad template Horizontal - media/image bên trái, (icon + headline + body) bên phải,
 * CTA button ở đáy. Cao khoảng 210dp. Hợp cho các vị trí gọn (list item, cuối màn...).
 */
@Composable
fun NativeAdHorizontal(
    adUnitId: String,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
) {
    NativeAdTemplate(
        adUnitId = adUnitId,
        layoutRes = R.layout.layout_native_ad_medium_horizontal,
        skeleton = { m -> NativeAdHorizontalSkeleton(PlaceholderHeight, m) },
        modifier = modifier,
        onAdLoaded = onAdLoaded,
        onAdFailedToLoad = onAdFailedToLoad,
    )
}
