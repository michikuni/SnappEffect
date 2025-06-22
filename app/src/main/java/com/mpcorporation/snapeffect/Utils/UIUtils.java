package com.mpcorporation.snapeffect.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import com.mpcorporation.snapeffect.R;

public class UIUtils {
    public static void showToast(Handler handler, Activity activity, String message) {
        handler.post(() -> {
            long startTime = System.nanoTime();
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            long endTime = System.nanoTime();
            Log.d("Toast UIUtils", "Toast display time: " + (endTime - startTime) / 1_000_000.0 + "ms");
        });
    }

    public static void showPopupMenu(Activity activity, View anchorView, Runnable onOption1, Runnable onOption2) {
        PopupMenu popup = new PopupMenu(activity, anchorView, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.open_option_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.option1) onOption1.run();
            if (item.getItemId() == R.id.option2) onOption2.run();
            PermissionUtils.checkPermission(activity);
            showToast(new Handler(Looper.getMainLooper()), activity, "Chọn ảnh: " + item.getTitle());
            return true;
        });
        popup.show();
    }
    @SuppressLint("ClickableViewAccessibility")
    public static void setupSeekBarDismissOnClick(View rootLayout, SeekBar seekBar, Activity activity) {
        if (seekBar == null) {
            Log.e("Slider", "SeekBar or LabelView not found in layout.");
            return;
        }
        // Click ra ngoài sẽ ẩn SeekBar
        rootLayout.setOnClickListener(v -> {
            if (seekBar.getVisibility() == View.VISIBLE) {
                SliderUtils.hideSlider(activity);
            }
        });
        // Ngăn sự kiện click khi đang kéo SeekBar
        seekBar.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }
}
