package com.example.snapeffect.Model;

import java.util.function.BiConsumer;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class AdjustableFilterConfig<T extends GPUImageFilter> {
    public final Class<T> filterClass;
    public final String label;
    public final float minValue, maxValue, defaultValue;
    public final BiConsumer<GPUImageFilter, Float> setter;

    public AdjustableFilterConfig(Class<T> filterClass,
                                  String label,
                                  float minValue,
                                  float maxValue,
                                  float defaultValue,
                                  BiConsumer<GPUImageFilter, Float> setter) {
        this.filterClass = filterClass;
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
        this.setter = setter;
    }
}
