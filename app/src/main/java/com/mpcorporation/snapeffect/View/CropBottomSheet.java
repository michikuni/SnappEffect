package com.mpcorporation.snapeffect.View;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mpcorporation.snapeffect.Adapter.ScaleCropAdapter;
import com.mpcorporation.snapeffect.Model.ScaleCrop;
import com.mpcorporation.snapeffect.R;
import com.yalantis.ucrop.UCrop;

import java.util.List;


public class CropBottomSheet extends BottomSheetDialogFragment {
    private List<ScaleCrop> cropOptions;
    private ScaleCropAdapter.OnCropClickListener listener;
    public void setScaleCrop(List<ScaleCrop> scaleCrops) {
        this.cropOptions = scaleCrops;
    }

    public void setOnCropClickListener(ScaleCropAdapter.OnCropClickListener listener) {
        this.listener = listener;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_effects, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_effects);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        recyclerView.setAdapter(new ScaleCropAdapter(cropOptions, option -> {
            if (listener != null) listener.onCropClick(option);
            dismiss();
        }));
        return view;
    }
    @NonNull
    public static CropBottomSheet getCropBottomSheet(
            Activity activity,
            List<ScaleCrop> scaleCrops,
            Uri input,
            Uri output
    ) {
        CropBottomSheet sheet = new CropBottomSheet();
        sheet.setScaleCrop(scaleCrops);
        sheet.setOnCropClickListener(crop -> UCrop.of(input, output).withAspectRatio(crop.ratioX, crop.ratioY).start(activity));
        return sheet;
    }
}