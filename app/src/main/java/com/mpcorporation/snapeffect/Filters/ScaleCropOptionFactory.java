package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.ScaleCrop;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

public class ScaleCropOptionFactory {
    public static List<ScaleCrop> create(){
        List<ScaleCrop> scaleCrops = new ArrayList<>();
        scaleCrops.add(new ScaleCrop(R.drawable.crop_portrait_24px, "Dọc", 0, 0));
        scaleCrops.add(new ScaleCrop(R.drawable.crop_1_1, "1:1", 1, 1));
        scaleCrops.add(new ScaleCrop(R.drawable.crop_3_2_24px, "3:2", 3, 2));
        scaleCrops.add(new ScaleCrop(R.drawable.crop_5_4_24px, "5:4", 5, 4));
        scaleCrops.add(new ScaleCrop(R.drawable.crop_7_5_24px, "7:5", 7, 5));
        scaleCrops.add(new ScaleCrop(R.drawable.crop_9_16_24px, "9:16", 9, 16));
        scaleCrops.add(new ScaleCrop(R.drawable.crop_16_9_24px, "16:9", 16, 9));
        return scaleCrops;
    }
}
