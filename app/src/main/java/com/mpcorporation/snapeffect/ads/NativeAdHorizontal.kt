package com.mpcorporation.snapeffect.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.mpcorporation.snapeffect.R

/** Chiều cao thực tế của template horizontal - placeholder giữ đúng chiều cao này để layout không bị giật */
val NativeAdHorizontalHeight: Dp
    @Composable get() = dimensionResource(R.dimen.ad_template_horizontal_height)

/**
 * Native Ad template Horizontal - media/image bên trái, (icon + headline + body) bên phải,
 * CTA button ở đáy. Hợp cho các vị trí gọn (list item, cuối màn...).
 */
@Composable
fun NativeAdHorizontal(
    adUnitId: String?,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
    onAdImpression: () -> Unit = {},
) {
    NativeAdTemplate(
        adUnitId = adUnitId,
        layoutRes = R.layout.layout_native_ad_medium_horizontal,
        skeleton = { m -> NativeAdHorizontalSkeleton(NativeAdHorizontalHeight, m) },
        modifier = modifier,
        onAdLoaded = onAdLoaded,
        onAdFailedToLoad = onAdFailedToLoad,
        onAdImpression = onAdImpression,
    )
}
