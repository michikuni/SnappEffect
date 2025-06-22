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
                R.drawable.filter_alt_24px,
                "Độ phồng", 0f, 1f,
                (filter, value) -> ((GPUImageBulgeDistortionFilter) filter).setScale(value)
        ));
        distorEffect.add(new EffectItem(
                "Hiệu ứng cầu kính",
                new GPUImageGlassSphereFilter(),
                R.drawable.filter_alt_24px
        ));
        distorEffect.add(new EffectItem(
                "Xoáy hình ảnh",
                new GPUImageSwirlFilter(),
                R.drawable.filter_alt_24px,
                "Độ xoáy", 0f, 2f,
                (filter, value) -> ((GPUImageSwirlFilter) filter).setAngle(value)
        ));
        distorEffect.add(new EffectItem(
                "Khúc xạ cầu",
                new GPUImageSphereRefractionFilter(),
                R.drawable.filter_alt_24px,
                "Độ khúc xạ", 0f, 1f,
                (filter, value) -> ((GPUImageSphereRefractionFilter) filter).setRadius(value)
        ));
        distorEffect.add(new EffectItem(
                "Mờ zoom trung tâm",
                new GPUImageZoomBlurFilter(),
                R.drawable.filter_alt_24px,
                "Kích thước mờ", 0f, 2f,
                (filter, value) -> ((GPUImageZoomBlurFilter) filter).setBlurSize(value)
        ));
        return distorEffect;
    }
}
