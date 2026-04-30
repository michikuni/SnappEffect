package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;
import com.mpcorporation.snapeffect.filter.filters.BrightnessFilter;
import com.mpcorporation.snapeffect.filter.filters.ContrastFilter;
import com.mpcorporation.snapeffect.filter.filters.ExposureFilter;
import com.mpcorporation.snapeffect.filter.filters.GammaFilter;
import com.mpcorporation.snapeffect.filter.filters.HueFilter;
import com.mpcorporation.snapeffect.filter.filters.LuminanceThresholdFilter;
import com.mpcorporation.snapeffect.filter.filters.SaturationFilter;
import com.mpcorporation.snapeffect.filter.filters.WhiteBalanceFilter;

import java.util.ArrayList;
import java.util.List;

public class AdjustEffectFactory {
    public static List<EffectItem> create() {
        List<EffectItem> adjustEffects = new ArrayList<>();

        adjustEffects.add(new EffectItem(
                "Độ sáng",
                new BrightnessFilter(0f),
                R.drawable.adjust_brightness,
                "Độ sáng", -1f, 1f, 0f,
                (filter, value) -> ((BrightnessFilter) filter).setBrightness(value)
        ));
        adjustEffects.add(new EffectItem(
                "Tương phản",
                new ContrastFilter(1f),
                R.drawable.adjust_contrast,
                "Tương phản", 0f, 4f, 1f,
                (filter, value) -> ((ContrastFilter) filter).setContrast(value)
        ));
        adjustEffects.add(new EffectItem(
                "Bão hòa",
                new SaturationFilter(1f),
                R.drawable.adjust_saturation,
                "Bão hòa", 0f, 2f, 1f,
                (filter, value) -> ((SaturationFilter) filter).setSaturation(value)
        ));
        adjustEffects.add(new EffectItem(
                "Màu Hue",
                new HueFilter(0f),
                R.drawable.adjust_hue,
                "Hue", 0f, 360f, 0f,
                (filter, value) -> ((HueFilter) filter).setHue(value)
        ));
        adjustEffects.add(new EffectItem(
                "Gamma",
                new GammaFilter(1f),
                R.drawable.adjust_gamma,
                "Gamma", 0.1f, 3f, 1f,
                (filter, value) -> ((GammaFilter) filter).setGamma(value)
        ));
        adjustEffects.add(new EffectItem(
                "Phơi sáng",
                new ExposureFilter(0f),
                R.drawable.adjust_exposure,
                "Phơi sáng", -10f, 10f, 0f,
                (filter, value) -> ((ExposureFilter) filter).setExposure(value)
        ));
        adjustEffects.add(new EffectItem(
                "Cân bằng trắng",
                new WhiteBalanceFilter(5000f),
                R.drawable.adjust_white_balance,
                "Cân bằng trắng", 2000f, 8000f, 5000f,
                (filter, value) -> ((WhiteBalanceFilter) filter).setTemperature(value)
        ));
        adjustEffects.add(new EffectItem(
                "Ngưỡng hóa sáng",
                new LuminanceThresholdFilter(0.5f),
                R.drawable.adjust_luminance,
                "Ngưỡng", 0f, 1f, 0.5f,
                (filter, value) -> ((LuminanceThresholdFilter) filter).setThreshold(value)
        ));
        return adjustEffects;
    }
}
