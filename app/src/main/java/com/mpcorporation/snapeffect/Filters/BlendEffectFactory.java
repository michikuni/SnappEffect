package com.mpcorporation.snapeffect.Filters;

import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageAddBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageChromaKeyBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorBurnBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorDodgeBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDarkenBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDifferenceBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDissolveBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageDivideBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExclusionBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHardLightBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLightenBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLinearBurnBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLuminosityBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageMixBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageMultiplyBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageNormalBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageScreenBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSoftLightBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSourceOverBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSubtractBlendFilter;

public class BlendEffectFactory {
    public static List<EffectItem> create() {
        List<EffectItem> blendEffect = new ArrayList<>();
        blendEffect.add(new EffectItem(
                "Tan mờ ảnh",
                new GPUImageDissolveBlendFilter(),
                R.drawable.hive_24px,
                "Blend",
                0f, 1f,
                (filter, value) -> ((GPUImageDissolveBlendFilter) filter).setMix(value)));
        blendEffect.add(new EffectItem(
                "Trộn 2 ảnh theo tỉ lệ",
                new GPUImageMixBlendFilter("0.5"),
                R.drawable.hive_24px,
                "Blend",
                0f, 1f,
                (filter, value) -> ((GPUImageMixBlendFilter) filter).setMix(value)));
        blendEffect.add(new EffectItem(
                "Cộng pixel ảnh",
                new GPUImageAddBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trộn alpha ảnh",
                new GPUImageAlphaBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Xóa màu nền",
                new GPUImageChromaKeyBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trộn màu",
                new GPUImageColorBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Làm tối mạnh",
                new GPUImageColorBurnBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Làm sáng mạnh",
                new GPUImageColorDodgeBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Giữ màu tối hơn",
                new GPUImageDarkenBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Hiệu số pixel",
                new GPUImageDifferenceBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Chia pixel",
                new GPUImageDivideBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Khác biệt mềm",
                new GPUImageExclusionBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Nhấn mạnh màu",
                new GPUImageHardLightBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trộn màu theo Hue",
                new GPUImageHueBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Giữ màu sáng hơn",
                new GPUImageLightenBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Làm tối tuyến tính",
                new GPUImageLinearBurnBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trộn theo độ sáng",
                new GPUImageLuminosityBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Nhân pixel ảnh",
                new GPUImageMultiplyBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trộn bình thường",
                new GPUImageNormalBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Hiệu ứng overlay",
                new GPUImageOverlayBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trộng độ bão hòa",
                new GPUImageSaturationBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Làm sáng ảnh",
                new GPUImageScreenBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trộn nhẹ nhàng",
                new GPUImageSoftLightBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Ghi đè ảnh gốc",
                new GPUImageSourceOverBlendFilter(),
                R.drawable.hive_24px));
        blendEffect.add(new EffectItem(
                "Trừ pixel",
                new GPUImageSubtractBlendFilter(),
                R.drawable.hive_24px));
        return blendEffect;
    }
}
