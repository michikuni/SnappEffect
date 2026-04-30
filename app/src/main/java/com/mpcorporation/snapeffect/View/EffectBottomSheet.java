package com.mpcorporation.snapeffect.View;

import static com.mpcorporation.snapeffect.Utils.SliderUtils.hideSlider;
import static com.mpcorporation.snapeffect.Utils.SliderUtils.showSlider;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.EffectAdapter;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mpcorporation.snapeffect.Utils.HistoryManager;
import com.mpcorporation.snapeffect.filter.BitmapIO;
import com.mpcorporation.snapeffect.filter.ImageRenderer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EffectBottomSheet extends BottomSheetDialogFragment {

    private List<EffectItem> effectItems;
    private EffectAdapter.OnEffectClickListener listener;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void setEffectItems(List<EffectItem> effectItems) { this.effectItems = effectItems; }
    public void setOnEffectClickListener(EffectAdapter.OnEffectClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_effects, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_effects);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(new EffectAdapter(effectItems, filter -> {
            if (listener != null) listener.onClick(filter);
            dismiss();
        }));
        return view;
    }

    @NonNull
    public static EffectBottomSheet getEffectBottomSheet(
            Activity context,
            ImageRenderer imageRenderer,
            List<EffectItem> effectItems,
            HistoryManager<Uri> manager
    ) {
        EffectBottomSheet sheet = new EffectBottomSheet();
        sheet.setEffectItems(effectItems);
        sheet.setOnEffectClickListener(clickedFilter -> {
            for (EffectItem config : effectItems) {
                if (!config.getFilter().getClass().equals(clickedFilter.getClass())) continue;

                if (config.hasParameter()) {
                    // Filter có slider: preview trên bitmap nhỏ, apply gốc khi nhả
                    showSlider(
                            context,
                            config.getLabel(),
                            config.getMin(),
                            config.getMax(),
                            config.getDefaultValue(),
                            value -> config.applyParameter(value),  // chỉ set tham số
                            config.getFilter(),
                            imageRenderer,
                            manager
                    );
                } else {
                    // Filter không có slider: apply trên background thread, không preview
                    hideSlider(context);
                    Bitmap source = imageRenderer.getSourceBitmap();
                    if (source == null) break;

                    executor.execute(() -> {
                        try {
                            Bitmap filtered = config.getFilter().apply(source);
                            Uri uri = BitmapIO.saveToCache(context, filtered,
                                    "snap_tmp_" + System.currentTimeMillis() + ".jpg");
                            Log.d("EffectSheet", "Saved: " + uri);
                            context.runOnUiThread(() -> {
                                manager.add(uri);
                                imageRenderer.setImage(context, uri);
                            });
                        } catch (IOException e) {
                            Log.e("EffectSheet", "Lỗi lưu ảnh", e);
                        }
                    });
                }
                break;
            }
        });
        return sheet;
    }
}
