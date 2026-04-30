package com.mpcorporation.snapeffect.Model;

import com.mpcorporation.snapeffect.filter.ImageFilter;

import java.util.function.BiConsumer;

public class EffectItem {
    private final String name;
    private final int filterIconRes;
    private final String label;
    private final ImageFilter filter;
    private final boolean hasParameter;
    private final float min;
    private final float max;
    private final float defaultValue;
    private final BiConsumer<ImageFilter, Float> parameterApplier;

    // Filter không có tham số
    public EffectItem(String name, ImageFilter filter, int iconRes) {
        this(name, iconRes, filter, "", false, 0f, 0f, 0f, null);
    }

    // Filter có tham số
    public EffectItem(String name, ImageFilter filter, int iconRes, String label, float min, float max, float defaultValue, BiConsumer<ImageFilter, Float> applier) {
        this(name, iconRes, filter, label, true, min, max, defaultValue, applier);
    }

    private EffectItem(String name, int filterIcon, ImageFilter filter, String label, boolean hasParameter, float min, float max, float defaultValue, BiConsumer<ImageFilter, Float> applier) {
        this.name = name;
        this.filterIconRes = filterIcon;
        this.label = label;
        this.filter = filter;
        this.hasParameter = hasParameter;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.parameterApplier = applier;
    }

    public String getName() { return name; }
    public int getFilterIconRes() { return filterIconRes; }
    public ImageFilter getFilter() { return filter; }
    public boolean hasParameter() { return hasParameter; }
    public float getMin() { return min; }
    public float getDefaultValue() { return defaultValue; }
    public float getMax() { return max; }
    public String getLabel() { return label; }

    public void applyParameter(float value) {
        if (parameterApplier != null) {
            parameterApplier.accept(filter, value);
        }
    }
}
