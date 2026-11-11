package com.mpcorporation.snapeffect.core.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import com.mpcorporation.snapeffect.data.AppLinks

/**
 * Nút "Rate App": mở thẳng In-App Review (popup đánh giá của Google ngay trong app).
 *
 * Không đặt dialog chọn sao của app trước popup này: guideline In-App Review cấm hỏi ý kiến /
 * gợi ý số sao trước hoặc trong lúc hiện nút đánh giá. Popup của Google tự cho chọn sao.
 *
 * LƯU Ý về In-App Review:
 * - Google giới hạn quota hiển thị: gọi launchReviewFlow có thể "thành công" mà không
 *   show gì cả (by design, không có callback báo) - không được treo UX chờ nó.
 *   Đó là lý do các flow xong đều im lặng, không Toast "cảm ơn".
 * - request lỗi (thiết bị không có Play Store, quota...) -> fallback mở trang store.
 * - Chỉ test được đầy đủ khi app đã lên track nội bộ/production trên Play.
 */
object RateApp {

    private const val TAG = "RateApp"

    fun launch(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            } else {
                Log.w(TAG, "In-app review unavailable, opening store page", task.exception)
                activity.openUrl(AppLinks.playStoreUrl(activity))
            }
        }
    }
}
