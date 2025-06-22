package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageCrosshatchFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHalftoneFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSmoothToonFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSolarizeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToonFilter;

public class ArtEffectFactory {
    public static List<EffectItem> create(){
        List<EffectItem> artEffect = new ArrayList<>();

        artEffect.add(new EffectItem(
                "Vẽ phác thảo",
                new GPUImageSketchFilter(),
                R.drawable.widgets_24px
        ));
        artEffect.add(new EffectItem(
                "Hiệu ứng hoạt hình",
                new GPUImageToonFilter(),
                R.drawable.widgets_24px
        ));
        artEffect.add(new EffectItem(
                "Hoạt hình mượt",
                new GPUImageSmoothToonFilter(),
                R.drawable.widgets_24px,
                "Độ mượt", 0f, 1f,
                (filter, value) -> ((GPUImageSmoothToonFilter) filter).setThreshold(value)
        ));
        artEffect.add(new EffectItem(
                "Chấm tròn nửa tông",
                new GPUImageHalftoneFilter(),
                R.drawable.widgets_24px,
                "Pixel", 0.001f, 0.05f,
                (filter, value) -> ((GPUImageHalftoneFilter) filter).setFractionalWidthOfAPixel(value)
        ));
        artEffect.add(new EffectItem(
                "Gạch chéo",
                new GPUImageCrosshatchFilter(),
                R.drawable.widgets_24px,
                "Khoảng cách", 0.01f, 0.1f,
                (filter, value) -> ((GPUImageCrosshatchFilter) filter).setCrossHatchSpacing(value)
        ));
        artEffect.add(new EffectItem(
                "Nổi 3D",
                new GPUImageEmbossFilter(),
                R.drawable.widgets_24px,
                "Cường độ", 0f, 5f,
                (filter, value) -> ((GPUImageEmbossFilter) filter).setIntensity(value)
        ));
        artEffect.add(new EffectItem(
                "Đảo ngược vùng sáng",
                new GPUImageSolarizeFilter(),
                R.drawable.widgets_24px,
                "Cường độ", 0f, 1f,
                (filter, value) -> ((GPUImageSolarizeFilter) filter).setThreshold(value)
        ));
        artEffect.add(new EffectItem(
                "LUT màu",
                new GPUImageLookupFilter(),
                R.drawable.widgets_24px
        ));
        return artEffect;
    }
}
