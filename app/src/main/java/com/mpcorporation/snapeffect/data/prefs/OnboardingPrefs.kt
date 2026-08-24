package com.mpcorporation.snapeffect.data.prefs

import android.content.Context

/**
 * Lưu trạng thái "đã hoàn thành onboarding" (SharedPreferences).
 *
 * Chỉ set 1 lần khi user bấm Continue ở màn Onboarding. Các lần mở app sau,
 * Splash sẽ đi thẳng vào Editor thay vì lặp lại Language + Onboarding.
 */
object OnboardingPrefs {

    private const val PREFS = "onboarding_prefs"
    private const val KEY_COMPLETED = "onboarding_completed"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isCompleted(context: Context): Boolean =
        prefs(context).getBoolean(KEY_COMPLETED, false)

    fun setCompleted(context: Context) {
        prefs(context).edit().putBoolean(KEY_COMPLETED, true).apply()
    }
}
