package com.mpcorporation.snapeffect.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

/** Chờ impression tối đa chừng này rồi vẫn đi tiếp (mạng chậm, no-fill mà SDK chưa báo fail...). */
private const val AD_WAIT_TIMEOUT_MS = 4_000L

enum class AdWaitState { WAITING, IMPRESSION, NO_AD }

/**
 * Cho UI chờ impression của 1 native mà không bao giờ kẹt user: kết thúc khi có impression, khi ad
 * fail hoặc khi hết [AD_WAIT_TIMEOUT_MS].
 */
@Stable
class AdWait internal constructor(hasAd: Boolean) {

    var state: AdWaitState by mutableStateOf(if (hasAd) AdWaitState.WAITING else AdWaitState.NO_AD)
        private set

    fun onImpression() {
        if (state == AdWaitState.WAITING) state = AdWaitState.IMPRESSION
    }

    fun onFailed() {
        if (state == AdWaitState.WAITING) state = AdWaitState.NO_AD
    }
}

/**
 * @param hasAd false = màn này không có ad (placement tắt / chưa consent) -> không phải chờ gì.
 * @param key   đổi key = bắt đầu lượt chờ mới cho ad kế tiếp.
 */
@Composable
fun rememberAdWait(hasAd: Boolean, key: Any? = Unit): AdWait {
    val wait = remember(key) { AdWait(hasAd) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(wait) {
        // Chỉ đếm lúc app đang ở foreground: ad không thể có impression khi user không nhìn thấy
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            delay(AD_WAIT_TIMEOUT_MS)
            wait.onFailed()
        }
    }
    return wait
}
