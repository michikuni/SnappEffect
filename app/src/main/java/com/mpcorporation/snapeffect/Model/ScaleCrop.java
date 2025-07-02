package com.mpcorporation.snapeffect.Model;

public class ScaleCrop{
    public final int iconResId;
    public final String label;
    public final float ratioX;
    public final float ratioY;

    public ScaleCrop(int iconResId, String label, float ratioX, float ratioY) {
        this.iconResId = iconResId;
        this.label = label;
        this.ratioX = ratioX;
        this.ratioY = ratioY;
    }
}
