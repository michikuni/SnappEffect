package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLuminanceThresholdFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter;
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
                "Cân bằng trắng", 2000f, 8000f, 5000f,
                (filter, value) -> ((GPUImageWhiteBalanceFilter) filter).setTemperature(value)
        ));
        adjustEffects.add(new EffectItem(
                "Ngưỡng hóa sáng",
                new GPUImageLuminanceThresholdFilter(),
                R.drawable.edge_luminance,
                "Ngưỡng", 0f, 1f, 0.5f,
                (filter, value) -> ((GPUImageLuminanceThresholdFilter) filter).setThreshold(value)
        ));
        return adjustEffects;
    }
}
