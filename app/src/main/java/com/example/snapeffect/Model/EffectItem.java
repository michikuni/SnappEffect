package com.example.snapeffect.Model;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class EffectItem {
    public final String name;
    public final GPUImageFilter filter;

    public EffectItem(String name, GPUImageFilter filter){
        this.name = name;
        this.filter = filter;
    }
    public GPUImageFilter getFilter(){
        return filter;
    }
    public String getName(){
        return name;
    }
}
