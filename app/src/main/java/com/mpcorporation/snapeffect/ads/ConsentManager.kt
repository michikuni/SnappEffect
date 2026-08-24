package com.mpcorporation.snapeffect.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.mpcorporation.snapeffect.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Quản lý consent GDPR/EEA qua Google UMP + init Google Mobile Ads SDK.
 *
 * Flow chuẩn (gọi [gatherConsent] từ Splash mỗi lần mở app):
 * 1. Nếu đã có consent từ phiên trước -> init MobileAds ngay (song song với bước 2).
 * 2. requestConsentInfoUpdate -> show consent form nếu khu vực bắt buộc (EEA/UK).
 * 3. Xong form (hoặc lỗi) -> callback với [ConsentInformation.canRequestAds]:
 *    - true  -> init MobileAds (nếu chưa), bắt đầu load ad được.
 *    - false -> KHÔNG request ad, nhưng app vẫn chạy bình thường.
 *
 * Callback LUÔN được gọi (kể cả lỗi mạng / lỗi form) để flow không bao giờ kẹt,
 * cùng triết lý với [InterstitialAdManager.show].
 *
 * User EEA đổi ý sau này: nếu [isPrivacyOptionsRequired] thì phải có entry point
 * (VD nút trong Settings) gọi [showPrivacyOptionsForm].
 */
object ConsentManager {

    private const val TAG = "ConsentManager"

    // Setting debug "force EEA" lưu prefs để bật/tắt ngay trong app
    private const val DEBUG_PREFS = "consent_debug_prefs"
    private const val KEY_FORCE_EEA = "force_eea"

    private val isMobileAdsInitCalled = AtomicBoolean(false)
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun consentInformation(context: Context): ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /** Đã đủ điều kiện request ad chưa (consent OK hoặc khu vực không yêu cầu consent). */
    fun canRequestAds(context: Context): Boolean =
        consentInformation(context).canRequestAds()

    /** Khu vực yêu cầu phải có entry point "Privacy options" cho user đổi lựa chọn consent. */
    fun isPrivacyOptionsRequired(context: Context): Boolean =
        consentInformation(context).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Cập nhật consent info + show form nếu bắt buộc, sau đó init MobileAds nếu được phép.
     *
     * @param onConsentResolved luôn được gọi đúng 1 lần trên main thread;
     *   `canRequestAds` = true nghĩa là bắt đầu load ad được.
     */
    fun gatherConsent(activity: Activity, onConsentResolved: (canRequestAds: Boolean) -> Unit) {
        // Công tắc tổng tắt -> không có ad nào chạy, nên không cần hỏi consent
        // và cũng KHÔNG init MobileAds SDK. Flow app đi tiếp ngay.
        if (!AdsMaster.isEnabled) {
            Log.i(TAG, "Ads tắt (ads_enabled=false) - bỏ qua consent, không init MobileAds")
            onConsentResolved(false)
            return
        }

        val consentInfo = consentInformation(activity)

        // Có consent từ phiên trước -> init sớm cho ad load nhanh,
        // trong lúc đó vẫn requestConsentInfoUpdate như thường (Google yêu cầu gọi mỗi lần mở app).
        if (consentInfo.canRequestAds()) {
            initMobileAdsOnce(activity)
        }

        val params = ConsentRequestParameters.Builder()
            .apply {
                if (isDebugEeaForced(activity)) {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                            .build()
                    )
                }
            }
            .build()

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Show form nếu status = REQUIRED; nếu không, callback được gọi ngay.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError: FormError? ->
                    formError?.let { Log.w(TAG, "Consent form error: ${it.errorCode} - ${it.message}") }
                    finishGathering(activity, onConsentResolved)
                }
            },
            { requestError: FormError ->
                // Lỗi mạng/cấu hình -> không chặn app; vẫn có thể request ad
                // nếu consent phiên trước còn hiệu lực.
                Log.w(TAG, "Consent info update error: ${requestError.errorCode} - ${requestError.message}")
                finishGathering(activity, onConsentResolved)
            }
        )
    }

    private fun finishGathering(context: Context, onConsentResolved: (Boolean) -> Unit) {
        val canRequest = canRequestAds(context)
        if (canRequest) initMobileAdsOnce(context)
        onConsentResolved(canRequest)
    }

    /**
     * Show form "Privacy options" cho user xem lại / đổi consent.
     * Gắn vào Settings khi [isPrivacyOptionsRequired] = true.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (FormError?) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let { Log.w(TAG, "Privacy options form error: ${it.errorCode} - ${it.message}") }
            onDismissed(formError)
        }
    }

    // ---- Công cụ test consent ----

    /**
     * Giả lập thiết bị đang ở EEA để UMP bắt buộc show form. CHỈ có tác dụng ở build
     * debug ([BuildConfig.DEBUG]) - release luôn dùng geography thật.
     *
     * Emulator mặc định là test device; thiết bị thật cần thêm hashed device id vào
     * [ConsentDebugSettings.Builder.addTestDeviceHashedId] (xem logcat tag "UserMessagingPlatform").
     */
    fun isDebugEeaForced(context: Context): Boolean =
        BuildConfig.DEBUG &&
            context.getSharedPreferences(DEBUG_PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_FORCE_EEA, false)

    fun setDebugEeaForced(context: Context, forced: Boolean) {
        context.getSharedPreferences(DEBUG_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_FORCE_EEA, forced).apply()
    }

    /**
     * Xóa toàn bộ trạng thái consent đã lưu (API test của UMP) - lần [gatherConsent]
     * kế tiếp sẽ đi lại flow từ đầu (show form nếu khu vực yêu cầu).
     */
    fun reset(context: Context) {
        consentInformation(context).reset()
        Log.d(TAG, "Consent state reset")
    }

    /** Init Google Mobile Ads đúng 1 lần, trên background thread (khuyến nghị của Google). */
    private fun initMobileAdsOnce(context: Context) {
        if (!isMobileAdsInitCalled.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        initScope.launch {
            MobileAds.initialize(appContext) {
                Log.d(TAG, "MobileAds initialized")
            }
        }
    }
}
