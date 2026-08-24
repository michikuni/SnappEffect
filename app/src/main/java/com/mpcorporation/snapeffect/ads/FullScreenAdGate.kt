package com.mpcorporation.snapeffect.ads

import android.os.SystemClock

/**
 * Điều phối chung giữa các fullscreen ad (interstitial / app open...).
 *
 * Nhiệm vụ:
 * - [isShowing]: đang có fullscreen ad trên màn hình -> ad khác KHÔNG được show đè.
 * - [lastDismissedAt]: mốc thời gian fullscreen ad gần nhất đóng - dùng cho các luật
 *   khoảng cách tối thiểu (interstitial interval, app open cooldown...).
 *
 * Mọi manager show fullscreen ad phải gọi [onShown] ngay trước khi show
 * và [onDismissed] khi ad đóng / show fail.
 */
object FullScreenAdGate {

    @Volatile
    var isShowing: Boolean = false
        private set

    /** [SystemClock.elapsedRealtime] lúc fullscreen ad gần nhất đóng; 0 = chưa từng show. */
    @Volatile
    var lastDismissedAt: Long = 0L
        private set

    /** Đã qua tối thiểu [intervalMs] kể từ lần fullscreen ad gần nhất đóng chưa. */
    fun hasElapsedSinceLastDismiss(intervalMs: Long): Boolean {
        val last = lastDismissedAt
        return last == 0L || SystemClock.elapsedRealtime() - last >= intervalMs
    }

    fun onShown() {
        isShowing = true
    }

    fun onDismissed() {
        isShowing = false
        lastDismissedAt = SystemClock.elapsedRealtime()
    }
}
