package com.mpcorporation.snapeffect.presentation.navigation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.AppOpenAdManager
import com.mpcorporation.snapeffect.core.util.findActivity
import com.mpcorporation.snapeffect.data.prefs.OnboardingPrefs
import com.mpcorporation.snapeffect.presentation.camera.CameraScreen
import com.mpcorporation.snapeffect.presentation.crop.CropScreen
import com.mpcorporation.snapeffect.presentation.editor.EditorScreen
import com.mpcorporation.snapeffect.presentation.editor.EditorViewModel
import com.mpcorporation.snapeffect.presentation.editor.ExitConfirmSheet
import com.mpcorporation.snapeffect.presentation.language.ChooseLanguageScreen
import com.mpcorporation.snapeffect.presentation.language.LanguageScreen
import com.mpcorporation.snapeffect.presentation.nativefull.NativeFullScreen
import com.mpcorporation.snapeffect.presentation.onboarding.OnboardingScreen
import com.mpcorporation.snapeffect.presentation.retouch.RetouchScreen
import com.mpcorporation.snapeffect.presentation.selective.SelectiveScreen
import com.mpcorporation.snapeffect.presentation.settings.SettingsScreen
import com.mpcorporation.snapeffect.presentation.splash.SplashScreen
import com.mpcorporation.snapeffect.presentation.start.StartScreen
import com.mpcorporation.snapeffect.presentation.sticker.StickerScreen
import com.mpcorporation.snapeffect.presentation.text.TextOverlayScreen

object Routes {
    const val SPLASH = "splash"
    const val CHOOSE_LANGUAGE = "choose_language"
    const val NATIVE_FULL_LANGUAGE = "native_full_language"
    const val ONBOARDING = "onboarding"
    const val START = "start"
    const val EDITOR = "editor"
    const val CAMERA = "camera"
    const val SETTINGS = "settings"

    /** Màn Language mở từ Cài đặt: chọn là áp dụng ngay, có nút back, không ad. */
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

/** Luồng mở app đầu tiên (Splash dùng cho cả user cũ). App Open không show đè các màn này. */
private val FIRST_FLOW_ROUTES = setOf(
    Routes.SPLASH,
    Routes.CHOOSE_LANGUAGE,
    Routes.NATIVE_FULL_LANGUAGE,
    Routes.ONBOARDING,
    Routes.START,
)

/**
 * Uri ảnh mà màn Crop/Selective/Text trả về cho Editor (đặt trên savedStateHandle của
 * back stack entry Editor - thay cho startActivityForResult của bản Activity cũ).
 */
const val KEY_EDIT_RESULT = "edit_result_uri"

/** Uri ảnh vừa chụp từ màn Camera -> Editor mở như ảnh MỚI (reset history), khác [KEY_EDIT_RESULT]. */
const val KEY_CAMERA_RESULT = "camera_result_uri"

/**
 * Toàn bộ app, luồng mở app theo AdmobBase:
 * Splash -> (Choose Language -> Native Full -> Onboarding -> Start) -> Editor -> các công cụ.
 *
 * User cũ (đã bấm Start ở lần đầu) thì Splash đi thẳng ra Editor - nhưng vẫn phải qua Splash mỗi
 * lần mở app vì đó là nơi gather consent (UMP) và preload interstitial / app open.
 *
 * @param currentLanguageCode ngôn ngữ đang áp dụng (để pre-select trên màn Language ở Cài đặt).
 * @param onLanguageSelected  áp dụng ngôn ngữ mới (lưu + đổi state locale ở root -> relocalize).
 */
@Composable
fun AppNavHost(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current

    // Google khuyến nghị user dùng app vài lần rồi mới show App Open, nên không show đè luồng này.
    // route = null là lúc NavHost chưa dựng xong graph, vẫn đang ở Splash.
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    SideEffect {
        AppOpenAdManager.isBlocked = currentRoute == null || currentRoute in FIRST_FLOW_ROUTES
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        firstFlowScreen(Routes.SPLASH) {
            SplashScreen(
                onNavigateNext = {
                    // User cũ (đã qua hết luồng mở app) đi thẳng vào Editor
                    val next = if (OnboardingPrefs.isCompleted(context)) Routes.EDITOR else Routes.CHOOSE_LANGUAGE
                    navController.navigate(next) {
                        // Không cho back về splash
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        firstFlowScreen(Routes.CHOOSE_LANGUAGE) {
            ChooseLanguageScreen(
                onApplyLanguage = onLanguageSelected,
                onFinished = {
                    // Native Full chỉ khi placement bật + có consent + có ad unit (release chưa
                    // điền unit thì AdConfig.nativeFullScreen = null -> bỏ qua màn này).
                    val next = if (
                        AdConfig.canShowPlacement(context, AdConfig.KEY_NATIVE_FULL_LANGUAGE_ENABLED) &&
                        AdConfig.nativeFullScreen != null
                    ) {
                        Routes.NATIVE_FULL_LANGUAGE
                    } else {
                        Routes.ONBOARDING
                    }
                    navController.navigate(next) {
                        // Không cho back về màn language
                        popUpTo(Routes.CHOOSE_LANGUAGE) { inclusive = true }
                    }
                }
            )
        }

        firstFlowScreen(Routes.NATIVE_FULL_LANGUAGE) {
            NativeFullScreen(
                onClose = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.NATIVE_FULL_LANGUAGE) { inclusive = true }
                    }
                }
            )
        }

        firstFlowScreen(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.START) {
                        // Không cho back về onboarding
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        firstFlowScreen(Routes.START) {
            StartScreen(
                onStart = {
                    // Đánh dấu đã qua hết luồng -> các lần mở app sau Splash đi thẳng Editor
                    OnboardingPrefs.setCompleted(context)
                    navController.navigate(Routes.EDITOR) {
                        popUpTo(Routes.START) { inclusive = true }
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

            // Editor là màn gốc: back -> sheet xác nhận thoát (theo AdmobBase)
            var showExitSheet by remember { mutableStateOf(false) }
            BackHandler { showExitSheet = true }
            if (showExitSheet) {
                ExitConfirmSheet(
                    onDismiss = { showExitSheet = false },
                    onExit = {
                        showExitSheet = false
                        context.findActivity()?.finish()
                    }
                )
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

        // Màn Language mở từ Cài đặt: chọn là áp dụng ngay, back để quay lại
        composable(Routes.SETTINGS_LANGUAGE) {
            LanguageScreen(
                selectedCode = currentLanguageCode,
                onSelectLanguage = onLanguageSelected,
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

/**
 * Màn của luồng mở app chuyển tức thì, không crossfade: gần như màn nào cũng có native, chồng 2 màn
 * lên nhau lúc fade thì ad của màn cũ vẫn nhận touch, dễ click nhầm.
 */
private fun NavGraphBuilder.firstFlowScreen(route: String, content: @Composable () -> Unit) {
    composable(
        route = route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        content()
    }
}

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
