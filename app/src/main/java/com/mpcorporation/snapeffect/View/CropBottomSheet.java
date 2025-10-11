package com.mpcorporation.snapeffect.View;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mpcorporation.snapeffect.Adapter.ScaleCropAdapter;
import com.mpcorporation.snapeffect.Model.ScaleCrop;
import com.mpcorporation.snapeffect.R;

import java.util.List;

public class CropBottomSheet extends BottomSheetDialogFragment {

    private List<ScaleCrop> cropOptions;
    private ScaleCropAdapter.OnCropClickListener listener;

    private Uri inputUri;
    private Uri outputUri;
    private Activity activity;

    private ActivityResultLauncher<CropImageContractOptions> cropLauncher;

    public void setScaleCrop(List<ScaleCrop> scaleCrops) {
        this.cropOptions = scaleCrops;
    }

    public void setData(Activity activity, Uri input, Uri output) {
        this.activity = activity;
        this.inputUri = input;
        this.outputUri = output;
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
            startCrop(option);
            dismiss();
        }));

        return view;
    }

    private void startCrop(ScaleCrop crop) {
        if (activity == null || inputUri == null) return;

        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        options.fixAspectRatio = true;
        options.aspectRatioX = (int) crop.ratioX;
        options.aspectRatioY = (int) crop.ratioY;

        CropImageContractOptions contractOptions =
                new CropImageContractOptions(inputUri, options);

        cropLauncher = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri croppedUri = result.getUriContent();
                // TODO: xử lý ảnh đã cắt
            } else {
                Exception error = result.getError();
                error.printStackTrace();
            }
        });

        cropLauncher.launch(contractOptions);
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
        sheet.setData(activity, input, output);
        return sheet;
    }
}
