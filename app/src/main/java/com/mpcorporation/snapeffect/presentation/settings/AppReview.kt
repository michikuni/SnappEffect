package com.mpcorporation.snapeffect.presentation.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Gửi đánh giá lên Play Store bằng **In-App Review API** (dialog của Google hiện ngay trong app,
 * không rời app).
 *
 * Lưu ý về API của Google:
 * - Không truyền được số sao lên: SDK chỉ cho phép *mở* dialog, người dùng chọn sao ngay trong
 *   dialog đó. Số sao user chọn ở [RateAppDialog] chỉ dùng để quyết định có mở dialog hay không.
 * - Google có quota riêng: dialog có thể KHÔNG hiện (đã đánh giá rồi, mới đánh giá gần đây...)
 *   và SDK vẫn báo thành công - đây là hành vi bình thường, không phải lỗi.
 * - Bản debug / cài không qua Play sẽ fail -> fallback mở thẳng trang Play Store.
 */
fun launchAppReview(activity: Activity, onFinished: () -> Unit = {}) {
    val manager = ReviewManagerFactory.create(activity)
    manager.requestReviewFlow()
        .addOnCompleteListener { request ->
            if (request.isSuccessful) {
                manager.launchReviewFlow(activity, request.result)
                    .addOnCompleteListener { onFinished() }
            } else {
                openPlayStorePage(activity)
                onFinished()
            }
        }
}

/** Mở trang app trên Play Store (ưu tiên app Play Store, không có thì mở trình duyệt). */
fun openPlayStorePage(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
