package com.mpcorporation.snapeffect.core.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Unwrap Context (Compose LocalContext) để lấy ra Activity - cần cho việc show fullscreen ad. */
fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
