package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageLuminanceFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLuminanceThresholdFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSobelEdgeDetectionFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSobelThresholdFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageThresholdEdgeDetectionFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageWeakPixelInclusionFilter;

public class EdgeEffectFactory {
    public static List<EffectItem> create(){
        List<EffectItem> edgeEffect = new ArrayList<>();

        edgeEffect.add(new EffectItem(
                "Biên Sobel có ngưỡng",
                new GPUImageSobelThresholdFilter(),
                R.drawable.adjust_24px,
                "Biên", 0f, 1f,
                (filter, value) -> ((GPUImageSobelThresholdFilter) filter).setThreshold(value)
        ));
        edgeEffect.add(new EffectItem(
                "Biên theo ngưỡng",
                new GPUImageThresholdEdgeDetectionFilter(),
                R.drawable.adjust_24px,
                "Biên", 0f, 1f,
                (filter, value) -> ((GPUImageThresholdEdgeDetectionFilter) filter).setThreshold(value)
        ));
        edgeEffect.add(new EffectItem(
                "Ngưỡng hóa sáng",
                new GPUImageLuminanceThresholdFilter(),
                R.drawable.adjust_24px,
                "Ngưỡng", 0f, 1f,
                (filter, value) -> ((GPUImageLuminanceThresholdFilter) filter).setThreshold(value)
        ));
        edgeEffect.add(new EffectItem(
                "Phát hiện biên Sobel",
                new GPUImageSobelEdgeDetectionFilter(),
                R.drawable.adjust_24px
        ));
        edgeEffect.add(new EffectItem(
                "Lọc theo độ sáng",
                new GPUImageLuminanceFilter(),
                R.drawable.adjust_24px
        ));
        edgeEffect.add(new EffectItem(
                "Lọc điểm yếu",
                new GPUImageWeakPixelInclusionFilter(),
                R.drawable.adjust_24px
        ));
        return edgeEffect;
    }
}
