package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageOpacityFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageTransformFilter;

public class TransformEffectFactory {
    public static List<EffectItem> create(){
        List<EffectItem> transEffect = new ArrayList<>();

        transEffect.add(new EffectItem(
                "Thay đổi độ mờ",
                new GPUImageOpacityFilter(),
                R.drawable.trans_opacity,
                "Độ mờ", 0f, 1f,
                (filter, value) -> ((GPUImageOpacityFilter) filter).setOpacity(value)
        ));
        transEffect.add(new EffectItem(
                "Bộ lọc cơ bản",
                new GPUImageFilter(),
                R.drawable.widgets_24px
        ));
        transEffect.add(new EffectItem(
                "Biến đổi affine",
                new GPUImageTransformFilter(),
                R.drawable.trans_affine
        ));
        return transEffect;
    }
}
