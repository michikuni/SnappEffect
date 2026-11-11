package com.mpcorporation.snapeffect.data

import android.content.Context

/**
 * Link / địa chỉ liên hệ của app (màn Cài đặt).
 */
object AppLinks {

    const val TERMS_OF_SERVICE = "https://sites.google.com/view/beaty-camera/term-of-service"
    const val PRIVACY_POLICY = "https://sites.google.com/view/beaty-camera/privacy-policy"

    /**
     * Email nhận góp ý (mục "Feedback" ở Cài đặt). Để trống -> ẩn mục Feedback.
     * TODO: điền email hỗ trợ của app trước khi release.
     */
    const val SUPPORT_EMAIL = ""

    /**
     * Link Play Store của app. Bỏ đuôi ".debug" để link đúng cả khi chạy build debug
     * (phòng khi debug có applicationIdSuffix riêng, không tồn tại trên store).
     */
    fun playStoreUrl(context: Context): String {
        val packageName = context.packageName.removeSuffix(".debug")
        return "https://play.google.com/store/apps/details?id=$packageName"
    }
}
