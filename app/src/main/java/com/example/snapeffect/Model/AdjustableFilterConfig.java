package com.example.snapeffect.Model;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class AdjustableFilterConfig<T extends GPUImageFilter> {
    public Class<T> filterClass;
    public String label;
    public float minValue, maxValue, defaultValue;
    public BiConsumer<GPUImageFilter, Float> setter;

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
