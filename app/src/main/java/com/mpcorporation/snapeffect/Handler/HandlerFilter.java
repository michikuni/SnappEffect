package com.mpcorporation.snapeffect.Handler;

import com.mpcorporation.snapeffect.Model.EffectItem;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;

public class HandlerFilter {
    public static void applyFilter(GPUImageView gpuImageView, List<GPUImageFilter> activeFilters, GPUImageFilter filter){
        gpuImageView.getGPUImage().setBackgroundColor(1.0f, 1.0f, 1.0f);
        if (!activeFilters.contains(filter)){
            activeFilters.add(filter);
        }
        GPUImageFilterGroup filterGroup = new GPUImageFilterGroup(activeFilters);
        gpuImageView.setFilter(filterGroup);
        gpuImageView.requestRender();
    }
    public static void applyAllFilters(GPUImageView view, List<GPUImageFilter> filters) {
        view.getGPUImage().setBackgroundColor(1.0f, 1.0f, 1.0f);
        GPUImageFilterGroup group = new GPUImageFilterGroup(filters);
        view.setFilter(group);
        view.requestRender();
    }

}
