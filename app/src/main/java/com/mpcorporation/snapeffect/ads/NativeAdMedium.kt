package com.mpcorporation.snapeffect.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.mpcorporation.snapeffect.R

/** Chiều cao thực tế của template medium - placeholder giữ đúng chiều cao này để layout không bị giật */
val NativeAdMediumHeight: Dp
    @Composable get() = dimensionResource(R.dimen.ad_template_medium_height)

/**
 * Native Ad template Medium - media lớn ở giữa, CTA bottom.
 * Dùng ở màn test ad (Home).
 */
@Composable
fun NativeAdMedium(
    adUnitId: String?,
    modifier: Modifier = Modifier,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
    onAdImpression: () -> Unit = {},
) {
    NativeAdTemplate(
        adUnitId = adUnitId,
        layoutRes = R.layout.layout_native_ad_medium,
        skeleton = { m -> NativeAdMediumSkeleton(NativeAdMediumHeight, m) },
        modifier = modifier,
        onAdLoaded = onAdLoaded,
        onAdFailedToLoad = onAdFailedToLoad,
        onAdImpression = onAdImpression,
    )
}
