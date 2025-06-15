package com.mpcorporation.snapeffect.Filters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class FilterManager {
    private static final Map<Class<?>, GPUImageFilter> filters = new HashMap<>();

    public static GPUImageFilter getFilter(Class<? extends GPUImageFilter> filterClass) {
        return filters.get(filterClass);
    }

    public static void addFilter(GPUImageFilter filter) {
        filters.put(filter.getClass(), filter);
    }

    public static boolean contains(Class<? extends GPUImageFilter> filterClass) {
        return filters.containsKey(filterClass);
    }

    public static void removeFilter(Class<? extends GPUImageFilter> filterClass) {
        filters.remove(filterClass);
    }

    public static Collection<GPUImageFilter> getAllFilters() {
        return filters.values();
    }

    public static void clear() {
        filters.clear();
    }
}

