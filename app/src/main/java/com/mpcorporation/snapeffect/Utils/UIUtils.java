package com.mpcorporation.snapeffect.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

public class UIUtils {
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
