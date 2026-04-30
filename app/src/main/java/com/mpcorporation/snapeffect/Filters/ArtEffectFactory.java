package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;
import com.mpcorporation.snapeffect.filter.filters.CrosshatchFilter;
import com.mpcorporation.snapeffect.filter.filters.EmbossFilter;
import com.mpcorporation.snapeffect.filter.filters.GrayscaleFilter;
import com.mpcorporation.snapeffect.filter.filters.HalftoneFilter;
import com.mpcorporation.snapeffect.filter.filters.SepiaFilter;
import com.mpcorporation.snapeffect.filter.filters.SmoothToonFilter;
import com.mpcorporation.snapeffect.filter.filters.SobelFilter;
import com.mpcorporation.snapeffect.filter.filters.SolarizeFilter;

import java.util.ArrayList;
import java.util.List;

public class ArtEffectFactory {
    public static List<EffectItem> create() {
        List<EffectItem> artEffect = new ArrayList<>();

        artEffect.add(new EffectItem(
                "Âm bản",
                new GrayscaleFilter(),
                R.drawable.art_gray_scale
        ));
        artEffect.add(new EffectItem(
                "Vintage",
                new SepiaFilter(0.5f),
                R.drawable.art_vintage,
                "Cường độ", 0f, 1f, 0.5f,
                (filter, value) -> ((SepiaFilter) filter).setIntensity(value)
        ));
        artEffect.add(new EffectItem(
                "Hoạt hình",
                new SmoothToonFilter(0.6f),
                R.drawable.art_smooth_toon,
                "Độ mượt", 0f, 1f, 0.6f,
                (filter, value) -> ((SmoothToonFilter) filter).setThreshold(value)
        ));
        artEffect.add(new EffectItem(
                "Chấm tròn nửa tông",
                new HalftoneFilter(0.01f),
                R.drawable.art_half_tone,
                "Pixel", 0.001f, 0.05f, 0.01f,
                (filter, value) -> ((HalftoneFilter) filter).setFractionalWidthOfAPixel(value)
        ));
        artEffect.add(new EffectItem(
                "Gạch chéo",
                new CrosshatchFilter(0.01f),
                R.drawable.art_cross_hatch,
                "Khoảng cách", 0.01f, 0.1f, 0.01f,
                (filter, value) -> ((CrosshatchFilter) filter).setCrossHatchSpacing(value)
        ));
        artEffect.add(new EffectItem(
                "Nổi 3D",
                new EmbossFilter(1f),
                R.drawable.art_emboss,
                "Cường độ", 0f, 5f, 1f,
                (filter, value) -> ((EmbossFilter) filter).setIntensity(value)
        ));
        artEffect.add(new EffectItem(
                "Đảo ngược vùng sáng",
                new SolarizeFilter(0.2f),
                R.drawable.art_solarize,
                "Cường độ", 0f, 1f, 0.2f,
                (filter, value) -> ((SolarizeFilter) filter).setThreshold(value)
        ));
        artEffect.add(new EffectItem(
                "Sketch",
                new SobelFilter(0.8f),
                R.drawable.art_sobel,
                "Biên", 0f, 1f, 0.8f,
                (filter, value) -> ((SobelFilter) filter).setThreshold(value)
        ));
        return artEffect;
    }
}
