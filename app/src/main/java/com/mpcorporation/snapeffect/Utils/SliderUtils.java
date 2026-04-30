package com.mpcorporation.snapeffect.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mpcorporation.snapeffect.R;
import com.mpcorporation.snapeffect.filter.BitmapIO;
import com.mpcorporation.snapeffect.filter.ImageFilter;
import com.mpcorporation.snapeffect.filter.ImageRenderer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SliderUtils {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public static void showSlider(
            Activity activity,
            String label,
            float min,
            float max,
            float defaultValue,
            Consumer<Float> onParamChanged,   // chỉ cập nhật tham số filter, không apply
            ImageFilter filter,               // filter instance để apply
            ImageRenderer imageRenderer,
            HistoryManager<Uri> manager
    ) {
        SeekBar seekBar   = activity.findViewById(R.id.parameterSeekBar);
        TextView labelView = activity.findViewById(R.id.seekBarLabel);

        seekBar.setVisibility(View.VISIBLE);
        labelView.setVisibility(View.VISIBLE);
        seekBar.setMax(100);

        int initialProgress = (int) ((defaultValue - min) / (max - min) * 100f);
        seekBar.setProgress(initialProgress);
        labelView.setText(label + ": " + String.format("%.2f", defaultValue));

        // Debounce: bỏ qua event mới nếu filter trước chưa xong
        AtomicBoolean isProcessing = new AtomicBoolean(false);

        // Áp ngay giá trị mặc định lên preview
        onParamChanged.accept(defaultValue);
        applyPreview(filter, imageRenderer, isProcessing);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                float value = min + (max - min) * (progress / 100f);
                labelView.setText(label + ": " + String.format("%.2f", value));

                // Cập nhật tham số filter
                onParamChanged.accept(value);

                // Debounce: bỏ qua nếu đang xử lý
                if (isProcessing.get()) return;

                // Preview trên bitmap nhỏ, background thread
                applyPreview(filter, imageRenderer, isProcessing);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Khi nhả tay: apply filter lên ảnh gốc full-res, lưu cache, thêm history
                Bitmap source = imageRenderer.getSourceBitmap();
                if (source == null) return;

                // Đợi processing hiện tại xong (queue vào executor single-thread)
                executor.execute(() -> {
                    try {
                        // Apply lên ảnh gốc
                        Bitmap fullFiltered = filter.apply(source);

                        Uri uri = BitmapIO.saveToCache(activity, fullFiltered,
                                "snap_tmp_" + System.currentTimeMillis() + ".jpg");
                        Log.d("SliderUtils", "Saved: " + uri);

                        activity.runOnUiThread(() -> {
                            manager.add(uri);
                            imageRenderer.setImage(activity, uri);
                        });
                    } catch (IOException e) {
                        Log.e("SliderUtils", "Lỗi lưu ảnh", e);
                    }
                });
            }
        });
    }

    /**
     * Apply filter lên previewBitmap trên background thread,
     * rồi post kết quả lên UI thread qua showFilteredPreview().
     */
    private static void applyPreview(ImageFilter filter, ImageRenderer imageRenderer,
                                     AtomicBoolean isProcessing) {
        Bitmap preview = imageRenderer.getPreviewBitmap();
        if (preview == null) return;

        isProcessing.set(true);
        executor.execute(() -> {
            try {
                Bitmap result = filter.apply(preview);
                imageRenderer.post(() -> {
                    imageRenderer.showFilteredPreview(result);
                    isProcessing.set(false);
                });
            } catch (Exception e) {
                Log.e("SliderUtils", "Lỗi apply preview", e);
                isProcessing.set(false);
            }
        });
    }

    public static void hideSlider(Activity activity) {
        TextView labelView = activity.findViewById(R.id.seekBarLabel);
        SeekBar seekBar    = activity.findViewById(R.id.parameterSeekBar);
        seekBar.setVisibility(View.GONE);
        labelView.setVisibility(View.GONE);
    }
}
