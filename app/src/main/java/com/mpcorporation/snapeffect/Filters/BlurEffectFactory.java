package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBilateralBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBoxBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDilationFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageRGBDilationFilter;

public class BlurEffectFactory {
    public static List<EffectItem> create (){
        List<EffectItem> blurEffect = new ArrayList<>();

        blurEffect.add(new EffectItem(
                "Làm mờ Gaussian",
                new GPUImageGaussianBlurFilter(),
                R.drawable.blur_gaussian,
                "Độ mờ", 0f, 10f,
                (filter, value) -> ((GPUImageGaussianBlurFilter) filter).setBlurSize(value)
        ));
        blurEffect.add(new EffectItem(
                "Làm mờ hộp",
                new GPUImageBoxBlurFilter(),
                R.drawable.blur_box,
                "Độ mờ", 0f, 10f,
                (filter, value) -> ((GPUImageBoxBlurFilter) filter).setBlurSize(value)
        ));
        blurEffect.add(new EffectItem(
                "Làm mờ giữ cạnh",
                new GPUImageBilateralBlurFilter(),
                R.drawable.blur_bilateral,
                "Độ mờ", 0f, 10f,
                (filter, value) -> ((GPUImageBilateralBlurFilter) filter).setDistanceNormalizationFactor(value)
        ));
        blurEffect.add(new EffectItem(
                "Khối pixel",
                new GPUImagePixelationFilter(),
                R.drawable.blur_pixel,
                "Pixel", 1f, 100f,
                (filter, value) -> ((GPUImagePixelationFilter) filter).setPixel(value)
        ));
        blurEffect.add(new EffectItem(
                "Dilation toàn ảnh",
                new GPUImageDilationFilter(),
                R.drawable.blur_dilation
        ));
        blurEffect.add(new EffectItem(
                "Dilation kênh RGB",
                new GPUImageRGBDilationFilter(),
                R.drawable.blur_dilation_rbg
        ));
        return blurEffect;
    }
}
