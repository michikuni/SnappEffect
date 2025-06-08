package com.example.snapeffect.Utils;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;

public class FilterUtils {
    public static void applyFilter(GPUImageView gpuImageView, List<GPUImageFilter> activeFilters, GPUImageFilter filter){
        if (!activeFilters.contains(filter)){
            activeFilters.add(filter);
            GPUImageFilterGroup filterGroup = new GPUImageFilterGroup(activeFilters);
            gpuImageView.setFilter(filterGroup);
            gpuImageView.requestRender();
        }
    }
}
