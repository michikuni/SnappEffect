package com.mpcorporation.snapeffect.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

private const val TAG = "Intents"

/** Mở [url] bằng browser (hoặc app bắt được link). Không crash khi thiết bị thiếu browser. */
fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "No activity to open url: $url")
    }
}

/** Share [text] qua system share sheet. */
fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, null))
}

/** Mở app email soạn sẵn thư gửi [to]. Không crash khi thiết bị không có app email. */
fun Context.sendEmail(to: String, subject: String, body: String = "") {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "No email app installed")
    }
}
