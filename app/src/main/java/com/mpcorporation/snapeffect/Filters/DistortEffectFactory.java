package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBulgeDistortionFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGlassSphereFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSphereRefractionFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSwirlFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageZoomBlurFilter;

public class DistortEffectFactory {
    public static List<EffectItem> create(){
        List<EffectItem> distorEffect = new ArrayList<>();

        distorEffect.add(new EffectItem(
                "Phồng trung tâm",
                new GPUImageBulgeDistortionFilter(),
                R.drawable.distor_bulge,
                "Độ phồng", 0f, 1f,
                (filter, value) -> ((GPUImageBulgeDistortionFilter) filter).setScale(value)
        ));
        distorEffect.add(new EffectItem(
                "Hiệu ứng cầu kính",
                new GPUImageGlassSphereFilter(),
                R.drawable.distor_glass
        ));
        distorEffect.add(new EffectItem(
                "Xoáy hình ảnh",
                new GPUImageSwirlFilter(),
                R.drawable.distor_swirl,
                "Độ xoáy", 0f, 2f,
                (filter, value) -> ((GPUImageSwirlFilter) filter).setAngle(value)
        ));
        distorEffect.add(new EffectItem(
                "Khúc xạ cầu",
                new GPUImageSphereRefractionFilter(),
                R.drawable.distor_refraction,
                "Độ khúc xạ", 0f, 1f,
                (filter, value) -> ((GPUImageSphereRefractionFilter) filter).setRadius(value)
        ));
        distorEffect.add(new EffectItem(
                "Mờ zoom trung tâm",
                new GPUImageZoomBlurFilter(),
                R.drawable.distor_zoom_blur,
                "Kích thước mờ", 0f, 2f,
                (filter, value) -> ((GPUImageZoomBlurFilter) filter).setBlurSize(value)
        ));
        return distorEffect;
    }
}
