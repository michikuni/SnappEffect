package com.mpcorporation.snapeffect.firebase

import android.content.Context
import android.util.Log
import com.mpcorporation.snapeffect.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Wrapper an toàn cho Firebase Remote Config - CHỈ lo fetch/activate và đọc giá trị thô.
 *
 * File này KHÔNG chứa key hay default nào của app. Key + default + logic áp giá trị
 * (ad unit id, toggle, capping...) nằm ở [com.mpcorporation.snapeffect.ads.AdConfig].
 *
 * Chưa cấu hình Firebase (thiếu google-services.json) -> mọi getter trả về `default`
 * caller truyền vào, app hoạt động y hệt như không có Remote Config.
 *
 * Gọi [init] 1 lần từ Application.onCreate (SAU khi các nơi cần nghe đã đăng ký qua
 * [addOnActivatedListener]). Giá trị fetch về persist cho các phiên sau
 * (chuẩn "activate ngay khi fetch xong" của Remote Config).
 */
object RemoteConfigManager {

    private const val TAG = "RemoteConfigManager"

    private var remoteConfig: FirebaseRemoteConfig? = null
    private val onActivatedListeners = mutableListOf<() -> Unit>()

    /**
     * Đăng ký callback chạy mỗi khi có bộ giá trị mới sẵn sàng để đọc:
     * lúc [init] (giá trị persist từ phiên trước) và sau mỗi lần fetch thành công.
     * Đăng ký TRƯỚC khi gọi [init] để không lỡ đợt notify đầu tiên.
     */
    fun addOnActivatedListener(listener: () -> Unit) {
        onActivatedListeners += listener
    }

    fun init(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.i(TAG, "Firebase chưa cấu hình - Remote Config dùng defaults trong code")
            return
        }

        val config = FirebaseRemoteConfig.getInstance()
        config.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                // Debug fetch không giới hạn để test; release theo khuyến nghị 1 giờ
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
                .build()
        )
        remoteConfig = config

        // Giá trị đã activate từ phiên trước (nếu có) dùng được ngay khi khởi động
        notifyActivated()

        config.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Remote config fetched, updated=${task.result}")
                notifyActivated()
            } else {
                Log.w(TAG, "Remote config fetch failed", task.exception)
            }
        }
    }

    private fun notifyActivated() {
        onActivatedListeners.forEach { it() }
    }

    // Getter trả về [default] khi: Firebase chưa cấu hình, hoặc key chưa có giá trị
    // fetch về (không khai báo trên console) - tức source STATIC.

    fun getString(key: String, default: String): String {
        val value = remoteConfig?.getValue(key) ?: return default
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) default
        else value.asString()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        val value = remoteConfig?.getValue(key) ?: return default
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) default
        else value.asBoolean()
    }

    fun getLong(key: String, default: Long): Long {
        val value = remoteConfig?.getValue(key) ?: return default
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) default
        else value.asLong()
    }
}
