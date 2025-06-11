package com.example.snapeffect.Utils;

import static com.example.snapeffect.Utils.SliderUtils.hideSlider;
import static com.example.snapeffect.Utils.SliderUtils.showSlider;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.snapeffect.Model.AdjustableFilterConfig;
import com.example.snapeffect.Model.EffectItem;
import com.example.snapeffect.View.EffectBottomSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;

public class FilterUtils {
    public static void applyFilter(GPUImageView gpuImageView, List<GPUImageFilter> activeFilters, GPUImageFilter filter){
        if (!activeFilters.contains(filter)){
            activeFilters.add(filter);
            GPUImageFilterGroup filterGroup = new GPUImageFilterGroup(activeFilters);
            gpuImageView.setFilter(filterGroup);
            gpuImageView.requestRender();
        }
    }
    @NonNull
    public static EffectBottomSheet getEffectBottomSheet(
            Activity context, GPUImageView gpuImageView,
            List<EffectItem> effectItems, List<GPUImageFilter> activeFilters,
            List<AdjustableFilterConfig<?>> configs
    ) {
        EffectBottomSheet sheet = new EffectBottomSheet();
        sheet.setEffectItems(effectItems);
        sheet.setOnEffectClickListener(filter -> {
            boolean isRemoved = false;
            for (int i = 0; i < activeFilters.size(); i++) {
                if (activeFilters.get(i).getClass().equals(filter.getClass())) {
                    activeFilters.remove(i);
                    if (activeFilters.isEmpty()){
                        activeFilters.add(new GPUImageFilter());
                    }
                    isRemoved = true;
                    break;
                }
            }
            if (isRemoved) {
                for (EffectItem item : effectItems) {
                    if (item.getFilter().getClass().equals(filter.getClass())) {
                        Toast.makeText(context, "Gỡ bỏ: " + item.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
                applyAllFilters(gpuImageView, activeFilters);
            } else {
                applyFilter(gpuImageView, activeFilters, filter);
                for (AdjustableFilterConfig<?> config : configs){
                    if (config.filterClass.isInstance(filter)) {
                        config.setter.accept(filter, config.defaultValue);
                        gpuImageView.requestRender();
                        showSlider(context, config.label, config.minValue, config.maxValue, config.defaultValue, value -> {
                            config.setter.accept(filter, value);
                            gpuImageView.requestRender();
                        });
                        break;
                    }
                }

            }
        });
        return sheet;
    }
    public static void applyAllFilters(GPUImageView view, List<GPUImageFilter> filters) {
        GPUImageFilterGroup group = new GPUImageFilterGroup(filters);
        view.setFilter(group);
    }

}
