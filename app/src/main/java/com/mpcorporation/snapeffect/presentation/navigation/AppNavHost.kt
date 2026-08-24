package com.mpcorporation.snapeffect.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mpcorporation.snapeffect.data.prefs.OnboardingPrefs
import com.mpcorporation.snapeffect.presentation.camera.CameraScreen
import com.mpcorporation.snapeffect.presentation.crop.CropScreen
import com.mpcorporation.snapeffect.presentation.editor.EditorScreen
import com.mpcorporation.snapeffect.presentation.editor.EditorViewModel
import com.mpcorporation.snapeffect.presentation.onboarding.screen.LanguageScreen
import com.mpcorporation.snapeffect.presentation.onboarding.screen.LanguageStage
import com.mpcorporation.snapeffect.presentation.onboarding.screen.OnboardingScreen
import com.mpcorporation.snapeffect.presentation.onboarding.screen.SplashScreen
import com.mpcorporation.snapeffect.presentation.retouch.RetouchScreen
import com.mpcorporation.snapeffect.presentation.selective.SelectiveScreen
import com.mpcorporation.snapeffect.presentation.settings.SettingsScreen
import com.mpcorporation.snapeffect.presentation.sticker.StickerScreen
import com.mpcorporation.snapeffect.presentation.text.TextOverlayScreen

object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE = "language"
    const val LANGUAGE_CONFIRM = "language_confirm"
    const val ONBOARDING = "onboarding"
    const val EDITOR = "editor"
    const val CAMERA = "camera"
    const val SETTINGS = "settings"

    /** Màn Language mở từ Cài đặt (khác [LANGUAGE] ở chỗ có nút back, không đi tiếp onboarding). */
    const val SETTINGS_LANGUAGE = "settings_language"
    const val RETOUCH = "retouch"
    const val STICKER = "sticker"
    const val CROP = "crop"
    const val SELECTIVE = "selective"
    const val TEXT = "text"

    const val ARG_URI = "uri"

    fun retouch(uri: Uri) = "$RETOUCH/${Uri.encode(uri.toString())}"
    fun sticker(uri: Uri) = "$STICKER/${Uri.encode(uri.toString())}"
    fun crop(uri: Uri) = "$CROP/${Uri.encode(uri.toString())}"
    fun selective(uri: Uri) = "$SELECTIVE/${Uri.encode(uri.toString())}"
    fun text(uri: Uri) = "$TEXT/${Uri.encode(uri.toString())}"
}

/**
 * Uri ảnh mà màn Crop/Selective/Text trả về cho Editor (đặt trên savedStateHandle của
 * back stack entry Editor - thay cho startActivityForResult của bản Activity cũ).
 */
const val KEY_EDIT_RESULT = "edit_result_uri"

/** Uri ảnh vừa chụp từ màn Camera -> Editor mở như ảnh MỚI (reset history), khác [KEY_EDIT_RESULT]. */
const val KEY_CAMERA_RESULT = "camera_result_uri"

/**
 * Toàn bộ app: Splash -> (Language -> Language confirm -> Onboarding) -> Editor -> các công cụ.
 *
 * User cũ (đã qua onboarding) thì Splash đi thẳng ra Editor - nhưng vẫn phải qua Splash mỗi
 * lần mở app vì đó là nơi gather consent (UMP) và preload interstitial / app open.
 *
 * @param currentLanguageCode ngôn ngữ đang áp dụng (để pre-select trên màn Language).
 * @param onLanguageSelected  áp dụng ngôn ngữ mới (lưu + đổi state locale ở root -> relocalize).
 */
@Composable
fun AppNavHost(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateNext = {
                    // User cũ đi thẳng vào Editor, khỏi lặp lại Language + Onboarding
                    val next = if (OnboardingPrefs.isCompleted(context)) {
                        Routes.EDITOR
                    } else {
                        Routes.LANGUAGE
                    }
                    navController.navigate(next) {
                        // Không cho back về splash
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Giai đoạn 1: hiển thị theo ngôn ngữ hệ thống.
        // Chọn ngôn ngữ -> localize ngay + sang màn Language y hệt (đã đổi ngôn ngữ).
        composable(Routes.LANGUAGE) {
            LanguageScreen(
                selectedCode = currentLanguageCode,
                stage = LanguageStage.FIRST,
                onSelectLanguage = { code ->
                    onLanguageSelected(code)
                    navController.navigate(Routes.LANGUAGE_CONFIRM)
                },
                onConfirm = { navController.navigate(Routes.LANGUAGE_CONFIRM) }
            )
        }

        // Giai đoạn 2: bản y hệt nhưng đã localize. Continue -> onboarding.
        composable(Routes.LANGUAGE_CONFIRM) {
            LanguageScreen(
                selectedCode = currentLanguageCode,
                stage = LanguageStage.CONFIRM,
                // Đổi ngôn ngữ ở đây chỉ relocalize tại chỗ (không tạo thêm màn mới)
                onSelectLanguage = onLanguageSelected,
                onConfirm = {
                    navController.navigate(Routes.ONBOARDING) {
                        // Không cho back về màn language
                        popUpTo(Routes.LANGUAGE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    // Đánh dấu đã qua onboarding -> các lần mở app sau Splash đi thẳng Editor
                    OnboardingPrefs.setCompleted(context)
                    navController.navigate(Routes.EDITOR) {
                        // Không cho back về onboarding
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EDITOR) { entry ->
            // ViewModel scope theo entry Editor -> vẫn sống khi mở Crop/Selective/Text chồng lên
            val viewModel: EditorViewModel = hiltViewModel()
            val savedStateHandle = entry.savedStateHandle

            val editResult by savedStateHandle
                .getStateFlow<String?>(KEY_EDIT_RESULT, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(editResult) {
                val uri = editResult ?: return@LaunchedEffect
                viewModel.onEditResult(Uri.parse(uri))
                savedStateHandle[KEY_EDIT_RESULT] = null
            }

            // Ảnh chụp từ Camera -> mở như ảnh mới (setImage), không nối vào history.
            val cameraResult by savedStateHandle
                .getStateFlow<String?>(KEY_CAMERA_RESULT, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(cameraResult) {
                val uri = cameraResult ?: return@LaunchedEffect
                viewModel.setImage(Uri.parse(uri))
                savedStateHandle[KEY_CAMERA_RESULT] = null
            }

            EditorScreen(
                viewModel = viewModel,
                onOpenCamera = { navController.navigate(Routes.CAMERA) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenRetouch = { navController.navigate(Routes.retouch(it)) },
                onOpenSticker = { navController.navigate(Routes.sticker(it)) },
                onOpenCrop = { navController.navigate(Routes.crop(it)) },
                onOpenSelective = { navController.navigate(Routes.selective(it)) },
                onOpenText = { navController.navigate(Routes.text(it)) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onCaptured = { navController.returnCameraResult(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                currentLanguageCode = currentLanguageCode,
                onOpenLanguage = { navController.navigate(Routes.SETTINGS_LANGUAGE) },
                onBack = { navController.popBackStack() }
            )
        }

        // Bản màn Language của luồng onboarding + nút back; đổi ngôn ngữ áp dụng ngay tại chỗ.
        composable(Routes.SETTINGS_LANGUAGE) {
            LanguageScreen(
                selectedCode = currentLanguageCode,
                stage = LanguageStage.SETTINGS,
                onSelectLanguage = onLanguageSelected,
                onConfirm = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.RETOUCH}/{${Routes.ARG_URI}}",
            arguments = listOf(navArgument(Routes.ARG_URI) { type = NavType.StringType })
        ) { entry ->
            RetouchScreen(
                imageUri = entry.requireUriArg(),
                onDone = { navController.returnToEditor(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.STICKER}/{${Routes.ARG_URI}}",
            arguments = listOf(navArgument(Routes.ARG_URI) { type = NavType.StringType })
        ) { entry ->
            StickerScreen(
                imageUri = entry.requireUriArg(),
                onDone = { navController.returnToEditor(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.CROP}/{${Routes.ARG_URI}}",
            arguments = listOf(navArgument(Routes.ARG_URI) { type = NavType.StringType })
        ) { entry ->
            CropScreen(
                imageUri = entry.requireUriArg(),
                onDone = { navController.returnToEditor(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.SELECTIVE}/{${Routes.ARG_URI}}",
            arguments = listOf(navArgument(Routes.ARG_URI) { type = NavType.StringType })
        ) { entry ->
            SelectiveScreen(
                imageUri = entry.requireUriArg(),
                onDone = { navController.returnToEditor(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.TEXT}/{${Routes.ARG_URI}}",
            arguments = listOf(navArgument(Routes.ARG_URI) { type = NavType.StringType })
        ) { entry ->
            TextOverlayScreen(
                imageUri = entry.requireUriArg(),
                onDone = { navController.returnToEditor(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun androidx.navigation.NavBackStackEntry.requireUriArg(): Uri =
    Uri.parse(requireNotNull(arguments?.getString(Routes.ARG_URI)) { "Thiếu uri ảnh" })

/** Đưa ảnh vừa sửa về Editor rồi đóng màn công cụ. */
private fun NavHostController.returnToEditor(result: Uri) {
    previousBackStackEntry?.savedStateHandle?.set(KEY_EDIT_RESULT, result.toString())
    popBackStack()
}

/** Đưa ảnh vừa chụp về Editor (ảnh mới) rồi đóng màn Camera. */
private fun NavHostController.returnCameraResult(result: Uri) {
    previousBackStackEntry?.savedStateHandle?.set(KEY_CAMERA_RESULT, result.toString())
    popBackStack()
}
