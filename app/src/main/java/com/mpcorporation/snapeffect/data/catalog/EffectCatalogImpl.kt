package com.mpcorporation.snapeffect.data.catalog

import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.data.filter.filters.BrightnessFilter
import com.mpcorporation.snapeffect.data.filter.filters.ContrastFilter
import com.mpcorporation.snapeffect.data.filter.filters.CrosshatchFilter
import com.mpcorporation.snapeffect.data.filter.filters.EmbossFilter
import com.mpcorporation.snapeffect.data.filter.filters.ExposureFilter
import com.mpcorporation.snapeffect.data.filter.filters.GammaFilter
import com.mpcorporation.snapeffect.data.filter.filters.GaussianBlurFilter
import com.mpcorporation.snapeffect.data.filter.filters.GrayscaleFilter
import com.mpcorporation.snapeffect.data.filter.filters.HalftoneFilter
import com.mpcorporation.snapeffect.data.filter.filters.HueFilter
import com.mpcorporation.snapeffect.data.filter.filters.LuminanceThresholdFilter
import com.mpcorporation.snapeffect.data.filter.filters.PixelateFilter
import com.mpcorporation.snapeffect.data.filter.filters.PresetFilter
import com.mpcorporation.snapeffect.data.filter.filters.SaturationFilter
import com.mpcorporation.snapeffect.data.filter.filters.SepiaFilter
import com.mpcorporation.snapeffect.data.filter.filters.SmoothToonFilter
import com.mpcorporation.snapeffect.data.filter.filters.SobelFilter
import com.mpcorporation.snapeffect.data.filter.filters.SolarizeFilter
import com.mpcorporation.snapeffect.data.filter.filters.SwirlFilter
import com.mpcorporation.snapeffect.data.filter.filters.WhiteBalanceFilter
import com.mpcorporation.snapeffect.data.filter.filters.ZoomBlurFilter
import com.mpcorporation.snapeffect.domain.filter.ImageFilter
import com.mpcorporation.snapeffect.domain.model.BottomNavItem
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.domain.model.ScaleCrop
import com.mpcorporation.snapeffect.domain.model.UserPreset
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EffectCatalogImpl @Inject constructor() : EffectCatalog {

    override fun bottomNavItems(): List<BottomNavItem> = listOf(
        BottomNavItem(R.drawable.bot_nav_crop, "Cắt"),
        BottomNavItem(R.drawable.bot_nav_adjust, "Chỉnh ảnh"),
        BottomNavItem(R.drawable.bot_nav_art, "Nghệ thuật"),
        BottomNavItem(R.drawable.bot_nav_distor, "Biến dạng")
    )

    override fun adjustEffects(): List<EffectItem> = listOf(
        EffectItem(
            name = "Độ sáng",
            filterIconRes = R.drawable.adjust_brightness,
            filter = BrightnessFilter(0f),
            label = "Độ sáng",
            hasParameter = true,
            min = -1f, max = 1f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as BrightnessFilter).brightness = v }
        ),
        EffectItem(
            name = "Tương phản",
            filterIconRes = R.drawable.adjust_contrast,
            filter = ContrastFilter(1f),
            label = "Tương phản",
            hasParameter = true,
            min = 0f, max = 4f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as ContrastFilter).contrast = v }
        ),
        EffectItem(
            name = "Bão hòa",
            filterIconRes = R.drawable.adjust_saturation,
            filter = SaturationFilter(1f),
            label = "Bão hòa",
            hasParameter = true,
            min = 0f, max = 2f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as SaturationFilter).saturation = v }
        ),
        EffectItem(
            name = "Màu Hue",
            filterIconRes = R.drawable.adjust_hue,
            filter = HueFilter(0f),
            label = "Hue",
            hasParameter = true,
            min = 0f, max = 360f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as HueFilter).hue = v }
        ),
        EffectItem(
            name = "Gamma",
            filterIconRes = R.drawable.adjust_gamma,
            filter = GammaFilter(1f),
            label = "Gamma",
            hasParameter = true,
            min = 0.1f, max = 3f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as GammaFilter).gamma = v }
        ),
        EffectItem(
            name = "Phơi sáng",
            filterIconRes = R.drawable.adjust_exposure,
            filter = ExposureFilter(0f),
            label = "Phơi sáng",
            hasParameter = true,
            min = -10f, max = 10f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as ExposureFilter).exposure = v }
        ),
        EffectItem(
            name = "Cân bằng trắng",
            filterIconRes = R.drawable.adjust_white_balance,
            filter = WhiteBalanceFilter(5000f),
            label = "Cân bằng trắng",
            hasParameter = true,
            min = 2000f, max = 8000f, defaultValue = 5000f,
            parameterApplier = { f, v -> (f as WhiteBalanceFilter).temperature = v }
        ),
        EffectItem(
            name = "Ngưỡng hóa sáng",
            filterIconRes = R.drawable.adjust_luminance,
            filter = LuminanceThresholdFilter(0.5f),
            label = "Ngưỡng",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.5f,
            parameterApplier = { f, v -> (f as LuminanceThresholdFilter).threshold = v }
        )
    )

    override fun artEffects(): List<EffectItem> = listOf(
        EffectItem(
            name = "Âm bản",
            filterIconRes = R.drawable.art_gray_scale,
            filter = GrayscaleFilter()
        ),
        EffectItem(
            name = "Vintage",
            filterIconRes = R.drawable.art_vintage,
            filter = SepiaFilter(0.5f),
            label = "Cường độ",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.5f,
            parameterApplier = { f, v -> (f as SepiaFilter).intensity = v }
        ),
        EffectItem(
            name = "Hoạt hình",
            filterIconRes = R.drawable.art_smooth_toon,
            filter = SmoothToonFilter(0.6f),
            label = "Độ mượt",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.6f,
            parameterApplier = { f, v -> (f as SmoothToonFilter).threshold = v }
        ),
        EffectItem(
            name = "Chấm tròn nửa tông",
            filterIconRes = R.drawable.art_half_tone,
            filter = HalftoneFilter(0.01f),
            label = "Pixel",
            hasParameter = true,
            min = 0.001f, max = 0.05f, defaultValue = 0.01f,
            parameterApplier = { f, v -> (f as HalftoneFilter).fractionalWidth = v }
        ),
        EffectItem(
            name = "Gạch chéo",
            filterIconRes = R.drawable.art_cross_hatch,
            filter = CrosshatchFilter(0.01f),
            label = "Khoảng cách",
            hasParameter = true,
            min = 0.01f, max = 0.1f, defaultValue = 0.01f,
            parameterApplier = { f, v -> (f as CrosshatchFilter).spacing = v }
        ),
        EffectItem(
            name = "Nổi 3D",
            filterIconRes = R.drawable.art_emboss,
            filter = EmbossFilter(1f),
            label = "Cường độ",
            hasParameter = true,
            min = 0f, max = 5f, defaultValue = 1f,
            parameterApplier = { f, v -> (f as EmbossFilter).intensity = v }
        ),
        EffectItem(
            name = "Đảo ngược vùng sáng",
            filterIconRes = R.drawable.art_solarize,
            filter = SolarizeFilter(0.2f),
            label = "Cường độ",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.2f,
            parameterApplier = { f, v -> (f as SolarizeFilter).threshold = v }
        ),
        EffectItem(
            name = "Sketch",
            filterIconRes = R.drawable.art_sobel,
            filter = SobelFilter(0.8f),
            label = "Biên",
            hasParameter = true,
            min = 0f, max = 1f, defaultValue = 0.8f,
            parameterApplier = { f, v -> (f as SobelFilter).threshold = v }
        )
    )

    override fun distortEffects(): List<EffectItem> = listOf(
        EffectItem(
            name = "Khối pixel",
            filterIconRes = R.drawable.distort_pixel,
            filter = PixelateFilter(7f),
            label = "Pixel",
            hasParameter = true,
            min = 1f, max = 100f, defaultValue = 7f,
            parameterApplier = { f, v -> (f as PixelateFilter).pixelSize = v }
        ),
        EffectItem(
            name = "Làm mờ",
            filterIconRes = R.drawable.distort_gaussian,
            filter = GaussianBlurFilter(0f),
            label = "Độ mờ",
            hasParameter = true,
            min = 0f, max = 10f, defaultValue = 0f,
            parameterApplier = { f, v -> (f as GaussianBlurFilter).blurSize = v }
        ),
        EffectItem(
            name = "Xoáy hình ảnh",
            filterIconRes = R.drawable.distort_swirl,
            filter = SwirlFilter(0.1f),
            label = "Độ xoáy",
            hasParameter = true,
            min = 0f, max = 2f, defaultValue = 0.1f,
            parameterApplier = { f, v -> (f as SwirlFilter).angle = v }
        ),
        EffectItem(
            name = "Mờ zoom trung tâm",
            filterIconRes = R.drawable.distort_zoom_blur,
            filter = ZoomBlurFilter(2f),
            label = "Kích thước mờ",
            hasParameter = true,
            min = 0f, max = 2f, defaultValue = 2f,
            parameterApplier = { f, v -> (f as ZoomBlurFilter).blurSize = v }
        )
    )

    override fun presetEffects(): List<EffectItem> = listOf(
        EffectItem("Vintage", R.drawable.preset_vintage, PresetFilter(
            SepiaFilter(0.35f), BrightnessFilter(-0.03f), ContrastFilter(1.05f)
        )),
        EffectItem("Cool", R.drawable.preset_cool, PresetFilter(
            WhiteBalanceFilter(7500f), SaturationFilter(1.2f)
        )),
        EffectItem("Warm", R.drawable.preset_warm, PresetFilter(
            WhiteBalanceFilter(2800f), SaturationFilter(1.3f), BrightnessFilter(0.05f)
        )),
        EffectItem("Fade", R.drawable.preset_fade, PresetFilter(
            SaturationFilter(0.6f), BrightnessFilter(0.1f), ContrastFilter(0.85f)
        )),
        EffectItem("Drama", R.drawable.preset_drama, PresetFilter(
            ContrastFilter(1.5f), SaturationFilter(1.4f), BrightnessFilter(-0.05f)
        )),
        EffectItem("Matte", R.drawable.preset_matte, PresetFilter(
            ContrastFilter(0.9f), SaturationFilter(0.8f), BrightnessFilter(0.08f)
        )),
        EffectItem("Trắng đen", R.drawable.preset_bw, PresetFilter(
            GrayscaleFilter(), ContrastFilter(1.4f)
        )),
        EffectItem("Golden", R.drawable.preset_golden, PresetFilter(
            WhiteBalanceFilter(3000f), SaturationFilter(1.5f),
            BrightnessFilter(0.08f), ContrastFilter(1.1f)
        ))
    )

    override fun cropRatios(): List<ScaleCrop> = listOf(
        ScaleCrop(R.drawable.crop_1_1, "1:1", 1f, 1f),
        ScaleCrop(R.drawable.crop_3_2_24px, "3:2", 3f, 2f),
        ScaleCrop(R.drawable.crop_5_4_24px, "5:4", 5f, 4f),
        ScaleCrop(R.drawable.crop_7_5_24px, "7:5", 7f, 5f),
        ScaleCrop(R.drawable.crop_9_16_24px, "9:16", 9f, 16f),
        ScaleCrop(R.drawable.crop_16_9_24px, "16:9", 16f, 9f)
    )

    override fun buildFilterFromUserPreset(preset: UserPreset): ImageFilter = PresetFilter(
        BrightnessFilter(preset.brightness),
        ContrastFilter(preset.contrast),
        SaturationFilter(preset.saturation),
        HueFilter(preset.hue),
        ExposureFilter(preset.exposure),
        WhiteBalanceFilter(preset.whiteBalance),
        GammaFilter(preset.gamma)
    )

    override fun adjustmentFilter(
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): ImageFilter = PresetFilter(
        BrightnessFilter(brightness),
        ContrastFilter(contrast),
        SaturationFilter(saturation)
    )
}
