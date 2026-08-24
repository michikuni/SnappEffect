package com.mpcorporation.snapeffect.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Công tắc tổng cho TOÀN BỘ ads - fetch từ Remote Config (key `ads_enabled`).
 *
 * [isEnabled] = false thì:
 * - Không init Google Mobile Ads SDK, không hiện consent form UMP
 *   (xem [ConsentManager.gatherConsent]).
 * - Không load / show bất kỳ format nào: native, banner, interstitial, app open.
 * - Các composable ad không emit gì -> layout tự co lại, không để chỗ trống.
 *
 * Giá trị do [com.mpcorporation.snapeffect.firebase.RemoteConfigManager] đẩy vào (không set
 * tay ở chỗ khác). Đây là Compose state: fetch về giữa session thì UI đang hiển thị ad sẽ
 * recompose và gỡ ad ngay.
 *
 * MẶC ĐỊNH `true` (ads bật) - dùng khi Firebase chưa cấu hình (thiếu google-services.json)
 * hoặc lần fetch đầu tiên chưa xong. Muốn tắt ads mà không cần Firebase thì đổi default
 * tại [com.mpcorporation.snapeffect.firebase.RemoteConfigManager.DEFAULTS].
 *
 * Toggle từng placement riêng lẻ: dùng các key `ad_*_enabled` của Remote Config.
 * Công tắc tổng này đè lên tất cả (tắt tổng = tắt hết, bất kể placement bật).
 */
object AdsMaster {

    var isEnabled: Boolean by mutableStateOf(true)
        internal set
}
