package com.mpcorporation.snapeffect.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mpcorporation.snapeffect.locale.LocaleManager
import com.mpcorporation.snapeffect.locale.ProvideAppLocale
import com.mpcorporation.snapeffect.presentation.navigation.AppNavHost
import com.mpcorporation.snapeffect.presentation.theme.SnapEffectTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity duy nhất của app - toàn bộ màn hình là Compose (xem AppNavHost).
 *
 * Ngôn ngữ áp bằng [ProvideAppLocale] cho cả cây Compose, nên không cần override
 * attachBaseContext như các Activity XML trước đây.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapEffectTheme {
                // context ở đây là Activity (chưa bị override locale) -> dùng để đọc/ghi pref.
                val context = LocalContext.current

                // Ngôn ngữ đang áp dụng cho cả app. Khởi tạo = đã lưu hoặc ngôn ngữ hệ thống.
                var languageCode by rememberSaveable {
                    mutableStateOf(LocaleManager.resolveInitialCode(context))
                }

                // Đổi languageCode -> relocalize toàn app (chỉ recompose, không recreate Activity).
                ProvideAppLocale(languageCode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.safeDrawingPadding()) {
                            AppNavHost(
                                currentLanguageCode = languageCode,
                                onLanguageSelected = { code ->
                                    LocaleManager.saveCode(context, code)
                                    languageCode = code
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
