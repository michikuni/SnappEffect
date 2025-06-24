package com.mpcorporation.snapeffect.Model;

import android.util.Log;

import java.util.function.BiConsumer;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class EffectItem {
    private final String name;
    private final int filterIconRes;
    private final String label;
    private final GPUImageFilter filter;
    private final boolean hasParameter;
    private final float min;
    private final float max;
    private float value;
    private final BiConsumer<GPUImageFilter, Float> parameterApplier;
    private float currentValue;

    // Filter không có tham số
    public EffectItem(String name, GPUImageFilter filter, int iconRes) {
        this(name, iconRes, filter, "", false, 0f, 0f, null);
    }

    // Filter có tham số
    public EffectItem(String name, GPUImageFilter filter, int iconRes, String label, float min, float max, BiConsumer<GPUImageFilter, Float> applier) {
        this(name, iconRes, filter, label, true, min, max, applier);
    }

    // Constructor chính
    private EffectItem(String name, int filterIcon, GPUImageFilter filter, String label, boolean hasParameter, float min, float max, BiConsumer<GPUImageFilter, Float> applier) {
        this.name = name;
        this.filterIconRes = filterIcon;
        this.label = label;
        this.filter = filter;
        this.hasParameter = hasParameter;
        this.min = min;
        this.max = max;
        this.parameterApplier = applier;
    }

    public String getName() {
        return name;
    }

    public int getFilterIconRes() {
        return filterIconRes;
    }

    public GPUImageFilter getFilter() {
        return filter;
    }

    public boolean hasParameter() {
        return hasParameter;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public String getLabel() {
        return label;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public void applyParameter(float value) {
        this.currentValue = value;
        if (parameterApplier != null) {
            parameterApplier.accept(filter, value);
            Log.e("Parameter", String.format("%.2f", value));
            Log.e("Filter", filter.getClass().getSimpleName());
        }
    }
}
