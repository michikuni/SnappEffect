package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;
import com.mpcorporation.snapeffect.filter.filters.GaussianBlurFilter;
import com.mpcorporation.snapeffect.filter.filters.PixelateFilter;
import com.mpcorporation.snapeffect.filter.filters.SwirlFilter;
import com.mpcorporation.snapeffect.filter.filters.ZoomBlurFilter;

import java.util.ArrayList;
import java.util.List;

public class DistortEffectFactory {
    public static List<EffectItem> create() {
        List<EffectItem> distorEffect = new ArrayList<>();

        distorEffect.add(new EffectItem(
                "Khối pixel",
                new PixelateFilter(7f),
                R.drawable.distort_pixel,
                "Pixel", 1f, 100f, 7f,
                (filter, value) -> ((PixelateFilter) filter).setPixel(value)
        ));
        distorEffect.add(new EffectItem(
                "Làm mờ",
                new GaussianBlurFilter(0f),
                R.drawable.distort_gaussian,
                "Độ mờ", 0f, 10f, 0f,
                (filter, value) -> ((GaussianBlurFilter) filter).setBlurSize(value)
        ));
        distorEffect.add(new EffectItem(
                "Xoáy hình ảnh",
                new SwirlFilter(0.1f),
                R.drawable.distort_swirl,
                "Độ xoáy", 0f, 2f, 0.1f,
                (filter, value) -> ((SwirlFilter) filter).setAngle(value)
        ));
        distorEffect.add(new EffectItem(
                "Mờ zoom trung tâm",
                new ZoomBlurFilter(2f),
                R.drawable.distort_zoom_blur,
                "Kích thước mờ", 0f, 2f, 2f,
                (filter, value) -> ((ZoomBlurFilter) filter).setBlurSize(value)
        ));
        return distorEffect;
    }
}
