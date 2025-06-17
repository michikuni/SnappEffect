package com.mpcorporation.snapeffect.Model;

import android.util.Log;

import java.util.function.BiConsumer;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class EffectItem {
    private final String name;
    private final String label;
    private final GPUImageFilter filter;
    private final boolean hasParameter;
    private final float min;
    private final float max;
    private final BiConsumer<GPUImageFilter, Float> parameterApplier;

    // Filter không có tham số
    public EffectItem(String name, GPUImageFilter filter) {
        this(name, filter, "",false, 0f, 0f, null);
    }

    // Filter có tham số
    public EffectItem(String name, GPUImageFilter filter, String label, float min, float max, BiConsumer<GPUImageFilter, Float> applier) {
        this(name, filter, label, true, min, max, applier);
    }

    private EffectItem(String name, GPUImageFilter filter, String label, boolean hasParameter, float min, float max, BiConsumer<GPUImageFilter, Float> applier) {
        this.name = name;
        this.label = label;
        this.filter = filter;
        this.hasParameter = hasParameter;
        this.min = min;
        this.max = max;
        this.parameterApplier = applier;
    }

    public String getParameterApplier(){ return parameterApplier.toString(); }
    public String getName() { return name; }
    public GPUImageFilter getFilter() { return filter; }
    public boolean hasParameter() { return hasParameter; }
    public float getMin() { return min; }
    public float getMax() { return max; }
    public String getLabel() { return label; }
    private float currentValue;
    public void applyParameter(float value) {
        this.currentValue = value;
        if (parameterApplier != null) {
            parameterApplier.accept(filter, value);
            Log.e("Parameter", String.format("%.2f", value));
            Log.e("Filter", filter.getClass().getSimpleName());
        }
    }
    public float getCurrentValue() {
        return currentValue;
    }
}