package com.mpcorporation.snapeffect.presentation.language

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.ads.AdConfig
import com.mpcorporation.snapeffect.ads.AdWaitState
import com.mpcorporation.snapeffect.ads.NativeAdHorizontal
import com.mpcorporation.snapeffect.ads.NativeAdHorizontalHeight
import com.mpcorporation.snapeffect.ads.NativeAdSlot
import com.mpcorporation.snapeffect.ads.rememberAdWait
import com.mpcorporation.snapeffect.core.util.onAnyTouchDown
import com.mpcorporation.snapeffect.domain.model.AppLanguage
import com.mpcorporation.snapeffect.locale.LocaleManager
import com.mpcorporation.snapeffect.presentation.components.PrimaryButton
import com.mpcorporation.snapeffect.presentation.theme.SnapTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Các bước của màn Choose Language (theo AdmobBase):
 * - [LOADING]  : Language Loading. List khóa + progress; hết giờ -> [PICK].
 * - [PICK]     : Language 1. Chưa có Continue; tap 1 ngôn ngữ -> [PICKED].
 * - [PICKED]   : Language 2. Có impression xong mới hiện Continue; đổi ngôn ngữ không đổi ad.
 * - [APPLYING] : Language Applying. Layout loading; có impression xong -> áp dụng ngôn ngữ -> [APPLIED].
 * - [APPLIED]  : Language Next. Báo đã áp dụng; có impression xong -> sang màn sau.
 */
private enum class ChoosePhase { LOADING, PICK, PICKED, APPLYING, APPLIED }

/** Giữ ad thêm chừng này sau impression: dưới 1s không được tính là viewable. */
private const val AD_VIEWABLE_MS = 1_000L

/** Thời gian dừng ở Applying / Applied. */
private const val AUTO_NEXT_DELAY_MS = 1_500L

/** Vào [ChoosePhase.PICK] chừng này mới hiện bàn tay gợi ý. */
private const val TAP_HINT_DELAY_MS = 1_000L

/** Chừa chỗ cuối list cho nút Continue nổi phía trên ad. */
private val ContinueAreaHeight = 74.dp

private val TapHintSize = 100.dp

/**
 * Gộp Language Loading + Language 1 + 2 + Applying + Next vào 1 màn: layout giữ nguyên,
 * mỗi [ChoosePhase] 1 native riêng trong cùng 1 chỗ ở đáy màn (cố định, không nhảy).
 *
 * Mọi bước chờ ad đều có lối ra (fail / hết giờ, xem [rememberAdWait]) nên không bao giờ kẹt user;
 * các timer chỉ chạy khi màn đang resumed, user click ad rồi quay lại thì đếm lại.
 *
 * @param onApplyLanguage áp dụng ngôn ngữ đã chọn (lưu + relocalize), gọi ngay trước [ChoosePhase.APPLIED].
 * @param onFinished      xong toàn bộ, đi tiếp sang màn sau.
 */
@Composable
fun ChooseLanguageScreen(
    onApplyLanguage: (String) -> Unit,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val languages = remember { LocaleManager.languagesForDisplay() }
    val adEnabled = remember {
        AdConfig.canShowPlacement(context, AdConfig.KEY_NATIVE_LANGUAGE_ENABLED)
    }

    // Không có ad thì Loading chỉ là chờ suông -> bỏ qua
    var phase by rememberSaveable {
        mutableStateOf(
            if (adEnabled && AdConfig.isLanguageLoadingEnabled) ChoosePhase.LOADING else ChoosePhase.PICK
        )
    }
    var selectedCode by rememberSaveable { mutableStateOf<String?>(null) }
    var canContinue by remember(phase) { mutableStateOf(false) }
    var isTapHintVisible by remember { mutableStateOf(false) }
    var touchCount by remember { mutableIntStateOf(0) }
    val loadingProgress = remember { Animatable(0f) }
    val adWait = rememberAdWait(hasAd = adEnabled, key = phase)

    LaunchedEffect(phase) {
        when (phase) {
            ChoosePhase.LOADING -> lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                loadingProgress.snapTo(0f)
                loadingProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = AdConfig.languageLoadingTimeMs.toInt(),
                        easing = LinearEasing
                    )
                )
            }

            ChoosePhase.PICK -> {
                delay(TAP_HINT_DELAY_MS)
                isTapHintVisible = true
            }

            else -> Unit
        }
    }

    // [ChoosePhase.APPLIED] tự chuyển sang màn sau (có thể là native full): mỗi lần user chạm thì
    // đếm lại, để ad màn sau không hiện đúng lúc đang tap.
    val autoNextKey = if (phase == ChoosePhase.APPLIED) touchCount else 0

    LaunchedEffect(phase, adWait.state, autoNextKey) {
        if (adWait.state == AdWaitState.WAITING) return@LaunchedEffect
        val adHoldMs = if (adWait.state == AdWaitState.IMPRESSION) AD_VIEWABLE_MS else 0L
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            when (phase) {
                ChoosePhase.LOADING -> {
                    delay(adHoldMs)
                    snapshotFlow { loadingProgress.value }.first { it >= 1f }
                    phase = ChoosePhase.PICK
                }

                ChoosePhase.PICK -> Unit

                ChoosePhase.PICKED -> {
                    delay(adHoldMs)
                    canContinue = true
                }

                ChoosePhase.APPLYING -> {
                    delay(AUTO_NEXT_DELAY_MS)
                    selectedCode?.let(onApplyLanguage)
                    phase = ChoosePhase.APPLIED
                }

                ChoosePhase.APPLIED -> {
                    delay(AUTO_NEXT_DELAY_MS)
                    onFinished()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapTheme.colors.page)
            .onAnyTouchDown { touchCount++ }
    ) {
        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SnapTheme.colors.textPrimary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
        )
        if (phase != ChoosePhase.APPLIED) {
            Text(
                text = stringResource(
                    if (phase == ChoosePhase.LOADING) R.string.language_loading else R.string.language_subtitle
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = SnapTheme.colors.textSecondary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
            )
        }
        // Luôn giữ chỗ cho progress để list không nhảy lên khi hết Loading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                .height(4.dp)
        ) {
            if (phase == ChoosePhase.LOADING) {
                LinearProgressIndicator(
                    progress = { loadingProgress.value },
                    color = SnapTheme.colors.brand,
                    trackColor = SnapTheme.colors.brandSoft,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (phase == ChoosePhase.APPLIED) {
                AppliedContent(language = languages.firstOrNull { it.code == selectedCode })
            } else {
                LanguagePicker(
                    languages = languages,
                    selectedCode = selectedCode,
                    tapHintCode = if (phase == ChoosePhase.PICK && isTapHintVisible) {
                        LocaleManager.suggestedLanguageCode()
                    } else {
                        null
                    },
                    isPickable = phase == ChoosePhase.PICK || phase == ChoosePhase.PICKED,
                    isLoading = phase == ChoosePhase.LOADING,
                    isApplying = phase == ChoosePhase.APPLYING,
                    showContinue = phase == ChoosePhase.PICKED && canContinue,
                    onSelect = { code ->
                        selectedCode = code
                        phase = ChoosePhase.PICKED
                    },
                    onContinue = { phase = ChoosePhase.APPLYING },
                )
            }
        }

        if (adEnabled) {
            NativeAdSlot(minHeight = NativeAdHorizontalHeight) {
                // Đổi phase -> bỏ ad cũ, lấy ad mới ngay trong đúng chỗ đó
                key(phase) {
                    NativeAdHorizontal(
                        adUnitId = AdConfig.nativeLanguage,
                        onAdFailedToLoad = adWait::onFailed,
                        onAdImpression = adWait::onImpression,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguagePicker(
    languages: List<AppLanguage>,
    selectedCode: String?,
    tapHintCode: String?,
    isPickable: Boolean,
    isLoading: Boolean,
    isApplying: Boolean,
    showContinue: Boolean,
    onSelect: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = ContinueAreaHeight)
        ) {
            items(languages, key = { it.code }) { language ->
                val showTapHint = language.code == tapHintCode
                Box(modifier = Modifier.zIndex(if (showTapHint) 1f else 0f)) {
                    LanguageRow(
                        language = language,
                        selected = language.code == selectedCode,
                        onClick = { onSelect(language.code) },
                        enabled = isPickable,
                        grayscaleFlag = isLoading,
                    )
                    if (showTapHint) TapHint(modifier = Modifier.matchParentSize())
                }
            }
        }

        // Nút nổi trên list, không chiếm chỗ của list hay ad: hiện ra không làm ad bị xê dịch
        AnimatedVisibility(
            visible = showContinue,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PrimaryButton(
                text = stringResource(R.string.btn_continue),
                onClick = onContinue,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        if (isApplying) ApplyingOverlay(modifier = Modifier.matchParentSize())
    }
}

/**
 * Bàn tay gợi ý tap, thò ra ngoài dòng và không tính vào chiều cao dòng để list không giật.
 * Đầu ngón tay và vòng sóng nằm đúng tâm khung animation, nên chỉ cần đưa tâm khung về tâm radio.
 */
@Composable
private fun TapHint(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.click_language))
    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .offset(x = TapHintSize / 2 - LanguageRowRadioCenterFromEnd)
                .requiredSize(TapHintSize)
        )
    }
}

@Composable
private fun ApplyingOverlay(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_basic_light))
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.75f))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.language_applying),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppliedContent(language: AppLanguage?) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (language != null) {
            Spacer(modifier = Modifier.height(8.dp))
            LanguageRow(
                language = language,
                selected = true,
                onClick = {},
                enabled = false,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_language_applied),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.language_applied),
                style = MaterialTheme.typography.bodyLarge,
                color = SnapTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
