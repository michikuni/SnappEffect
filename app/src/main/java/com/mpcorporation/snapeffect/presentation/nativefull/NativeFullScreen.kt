package com.mpcorporation.snapeffect.presentation.nativefull

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.AdWaitState
import com.mpcorporation.snapeffect.ads.NativeAdFull
import com.mpcorporation.snapeffect.ads.rememberAdWait
import com.mpcorporation.snapeffect.core.util.onAnyTouchDown
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlinx.coroutines.delay

/**
 * Native full giữa Choose Language và Onboarding của luồng mở app (theo AdmobBase).
 *
 * Policy native full-screen ở màn chuyển tiếp: nút đóng phải rõ ràng và không đè lên phần tử nào của
 * ad, nên nút nằm ở hàng riêng phía trên ad. Ad fail / không load kịp thì đi tiếp luôn; đã có
 * impression thì tự đi tiếp sau [AdConfig.nativeFullAutoSkipMs], mỗi lần chạm đếm lại để màn sau
 * không hiện đúng lúc user đang tap.
 */
@Composable
fun NativeFullScreen(onClose: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val adWait = rememberAdWait(hasAd = true)
    var isCloseVisible by remember { mutableStateOf(false) }
    var isClosed by remember { mutableStateOf(false) }
    var touchCount by remember { mutableIntStateOf(0) }

    fun closeOnce() {
        if (!isClosed) {
            isClosed = true
            onClose()
        }
    }

    BackHandler { closeOnce() }

    LaunchedEffect(Unit) {
        delay(AdConfig.nativeFullCloseDelayMs)
        isCloseVisible = true
    }

    LaunchedEffect(adWait.state, touchCount) {
        when (adWait.state) {
            AdWaitState.WAITING -> Unit
            AdWaitState.NO_AD -> closeOnce()
            AdWaitState.IMPRESSION -> {
                val autoSkipMs = AdConfig.nativeFullAutoSkipMs
                if (autoSkipMs > 0) {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        delay(autoSkipMs)
                        closeOnce()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Nền đặc: màn trước còn được vẽ thêm 1-2 frame lúc chuyển màn, không để lộ qua hàng nút đóng
            .background(SnapTheme.colors.page)
            .onAnyTouchDown { touchCount++ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isCloseVisible) {
                // Luôn ở bên trái vật lý, kể cả RTL: AdChoices của SDK cố định ở góc phải trên của ad
                IconButton(
                    onClick = { closeOnce() },
                    modifier = Modifier.align(AbsoluteAlignment.CenterLeft)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = SnapTheme.colors.textPrimary
                    )
                }
            }
        }

        NativeAdFull(
            adUnitId = AdConfig.nativeFullScreen,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onAdFailedToLoad = adWait::onFailed,
            onAdImpression = adWait::onImpression,
        )
    }
}
