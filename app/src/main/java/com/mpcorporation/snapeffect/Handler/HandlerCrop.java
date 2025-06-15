package com.mpcorporation.snapeffect.Handler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.util.function.Consumer;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class HandlerCrop {
    public static void handleCropResult(Intent data, GPUImageView gpuImageView, Consumer<Uri> onImageReady) {
        final Uri resultUri = UCrop.getOutput(data);
        if (resultUri != null) {
            onImageReady.accept(resultUri);
            gpuImageView.setImage(resultUri);
            gpuImageView.requestRender();
        }
    }

    public static void handleCropError(Activity activity, Intent data) {
        final Throwable cropError = UCrop.getError(data);
        if (cropError != null) {
            Toast.makeText(activity, "Lỗi cắt ảnh: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("Lỗi HandlerCrop", "Lỗi cắt ảnh", cropError);
        }
    }
}
