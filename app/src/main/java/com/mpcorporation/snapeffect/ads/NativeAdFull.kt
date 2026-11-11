package com.mpcorporation.snapeffect.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mpcorporation.snapeffect.R

/**
 * Native Ad template Full - media chiếm gần hết chiều cao được cấp, header + CTA ở đáy.
 * [modifier] phải cấp chiều cao cố định (VD weight / fillMaxSize). Nút đóng do màn gọi tự vẽ
 * bên ngoài ad để không đè lên phần tử nào của ad.
 */
@Composable
fun NativeAdFull(
    adUnitId: String?,
    modifier: Modifier = Modifier,
    onAdFailedToLoad: () -> Unit = {},
    onAdImpression: () -> Unit = {},
) {
    NativeAdTemplate(
        adUnitId = adUnitId,
        layoutRes = R.layout.layout_native_ad_full,
        skeleton = { m -> NativeAdFullSkeleton(m) },
        modifier = modifier,
        onAdFailedToLoad = onAdFailedToLoad,
        onAdImpression = onAdImpression,
    )
}
