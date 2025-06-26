package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLuminanceThresholdFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageWhiteBalanceFilter;

public class AdjustEffectFactory {
    public static List<EffectItem> create() {
        List<EffectItem> adjustEffects = new ArrayList<>();

        adjustEffects.add(new EffectItem(
                "Điều chỉnh độ sáng",
                new GPUImageBrightnessFilter(),
                R.drawable.adjust_brightness,
                "Độ sáng", -1f, 1f, 0f,
                (filter, value) -> ((GPUImageBrightnessFilter) filter).setBrightness(value)
        ));
        adjustEffects.add(new EffectItem(
                "Điều chỉnh tương phản",
                new GPUImageContrastFilter(),
                R.drawable.adjust_contrast,
                "Tương phản", 0f, 4f, 1f,
                (filter, value) -> ((GPUImageContrastFilter) filter).setContrast(value)
        ));
        adjustEffects.add(new EffectItem(
                "Điều chỉnh độ bão hòa",
                new GPUImageSaturationFilter(),
                R.drawable.adjust_saturation,
                "Bão hòa", 0f, 2f, 1f,
                (filter, value) -> ((GPUImageSaturationFilter) filter).setSaturation(value)
        ));
        adjustEffects.add(new EffectItem(
                "Điều chỉnh màu Hue",
                new GPUImageHueFilter(),
                R.drawable.adjust_hue,
                "Hue", 0f, 360f, 360f,
                (filter, value) -> ((GPUImageHueFilter) filter).setHue(value)
        ));
        adjustEffects.add(new EffectItem(
                "Điều chỉnh gamma",
                new GPUImageGammaFilter(),
                R.drawable.adjust_gamma,
                "Gamma", 0f, 3f, 1f,
                (filter, value) -> ((GPUImageGammaFilter) filter).setGamma(value)
        ));
        adjustEffects.add(new EffectItem(
                "Điều chỉnh phơi sáng",
                new GPUImageExposureFilter(),
                R.drawable.adjust_exposure,
                "Phơi sáng", -10f, 10f, 0f,
                (filter, value) -> ((GPUImageExposureFilter) filter).setExposure(value)
        ));
        adjustEffects.add(new EffectItem(
                "Cân bằng trắng",
                new GPUImageWhiteBalanceFilter(),
                R.drawable.adjust_white_balance,
                "Cân bằng trắng", 200f, 800f, 500f,
                (filter, value) -> ((GPUImageWhiteBalanceFilter) filter).setTemperature(value)
        ));
        adjustEffects.add(new EffectItem(
                "Ngưỡng hóa sáng",
                new GPUImageLuminanceThresholdFilter(),
                R.drawable.edge_luminance,
                "Ngưỡng", 0f, 1f, 0.5f,
                (filter, value) -> ((GPUImageLuminanceThresholdFilter) filter).setThreshold(value)
        ));
//        adjustEffects.add(new EffectItem(
//                "Curve chỉnh màu",
//                new GPUImageToneCurveFilter(),
//                R.drawable.adjust_tone_curve
//        ));
//        adjustEffects.add(new EffectItem(
//                "Đảo màu",
//                new GPUImageColorInvertFilter(),
//                R.drawable.adjust_invert_colors
//        ));
//        adjustEffects.add(new EffectItem(
//                "Giảm màu sắc",
//                new GPUImagePosterizeFilter(),
//                R.drawable.adjust_colors,
//                "Giảm màu", 1, 256,
//                (filter, value) -> ((GPUImagePosterizeFilter) filter).setColorLevels((int)value.floatValue())
//        ));
//        adjustEffects.add(new EffectItem(
//                "Màu nâu cổ điển",
//                new GPUImageSepiaToneFilter(),
//                R.drawable.adjust_vintage,
//                "Cường độ", 0f, 1f,
//                (filter, value) -> ((GPUImageSepiaToneFilter) filter).setIntensity(value)
//        ));
//        adjustEffects.add(new EffectItem(
//                "Chuyển sang trắng đen",
//                new GPUImageGrayscaleFilter(),
//                R.drawable.adjust_gray_scale
//        ));

        /*
        adjustEffects.add(new EffectItem("Điều chỉnh RGB riêng lẻ", new GPUImageRGBFilter(), 0f, 1f, (filter, value) -> ((GPUImageRGBFilter) filter).setRed(value)));
        adjustEffects.add(new EffectItem("Điều chỉnh RGB riêng lẻ", new GPUImageRGBFilter(), 0f, 1f, (filter, value) -> ((GPUImageRGBFilter) filter).setGreen(value)));
        adjustEffects.add(new EffectItem("Điều chỉnh RGB riêng lẻ", new GPUImageRGBFilter(), 0f, 1f, (filter, value) -> ((GPUImageRGBFilter) filter).setBlue(value)));
        adjustEffects.add(new EffectItem("Đổ bóng", new GPUImageHighlightShadowFilter(), 0f, 1f, (filter, value) -> ((GPUImageHighlightShadowFilter) filter).setShadows(value)));
        adjustEffects.add(new EffectItem("Đổ bóng", new GPUImageHighlightShadowFilter(), 0f, 1f, (filter, value) -> ((GPUImageHighlightShadowFilter) filter).setHighlights(value)));
        adjustEffects.add(new EffectItem("Điều chỉnh cấp", new GPUImageLevelsFilter(), 0f, 1f, (filter, value) -> ((GPUImageLevelsFilter) filter).setBlueMin(value)));
        adjustEffects.add(new EffectItem("Điều chỉnh cấp", new GPUImageLevelsFilter(), 0f, 1f, (filter, value) -> ((GPUImageLevelsFilter) filter).setGreenMin(value)));
        adjustEffects.add(new EffectItem("Điều chỉnh cấp", new GPUImageLevelsFilter(), 0f, 1f, (filter, value) -> ((GPUImageLevelsFilter) filter).setRedMin(value)));
        adjustEffects.add(new EffectItem("Áp dụng ma trận màu", new GPUImageColorMatrixFilter(), 0f, 1f, (filter, value) -> ((GPUImageColorMatrixFilter) filter).setIntensity(value)));
        adjustEffects.add(new EffectItem("Lọc đơn sắc", new GPUImageMonochromeFilter(), 0f, 1f, (filter, value) -> ((GPUImageMonochromeFilter) filter).setIntensity(value)));
        adjustEffects.add(new EffectItem("Lọc đơn sắc", new GPUImageMonochromeFilter(), 0f, 1f, (filter, value) -> ((GPUImageMonochromeFilter) filter).setColor(0,1,1)));
        adjustEffects.add(new EffectItem("Đổi sáng tối thành màu", new GPUImageFalseColorFilter(), 0f, 1f, (filter, value) -> ((GPUImageFalseColorFilter) filter).setFirstColor(1f)));
        adjustEffects.add(new EffectItem("Đổi sáng tối thành màu", new GPUImageFalseColorFilter(), 0f, 1f, (filter, value) -> ((GPUImageFalseColorFilter) filter).setSecondColor(1f)));
        */
        return adjustEffects;
    }
}
