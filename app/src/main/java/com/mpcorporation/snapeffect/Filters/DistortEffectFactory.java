package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSwirlFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageZoomBlurFilter;

public class DistortEffectFactory {
    public static List<EffectItem> create(){
        List<EffectItem> distorEffect = new ArrayList<>();
        distorEffect.add(new EffectItem(
                "Khối pixel",
                new GPUImagePixelationFilter(),
                R.drawable.distort_pixel,
                "Pixel", 1f, 100f, 7f,
                (filter, value) -> ((GPUImagePixelationFilter) filter).setPixel(value)
        ));
        distorEffect.add(new EffectItem(
                "Làm mờ",
                new GPUImageGaussianBlurFilter(),
                R.drawable.distort_gaussian,
                "Độ mờ", 0f, 10f, 0f,
                (filter, value) -> ((GPUImageGaussianBlurFilter) filter).setBlurSize(value)
        ));
        distorEffect.add(new EffectItem(
                "Xoáy hình ảnh",
                new GPUImageSwirlFilter(),
                R.drawable.distort_swirl,
                "Độ xoáy", 0f, 2f, 0.1f,
                (filter, value) -> ((GPUImageSwirlFilter) filter).setAngle(value)
        ));
        distorEffect.add(new EffectItem(
                "Mờ zoom trung tâm",
                new GPUImageZoomBlurFilter(),
                R.drawable.distort_zoom_blur,
                "Kích thước mờ", 0f, 2f, 2f,
                (filter, value) -> ((GPUImageZoomBlurFilter) filter).setBlurSize(value)
        ));
        return distorEffect;
    }
}
