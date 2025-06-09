package com.example.snapeffect.Utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import com.example.snapeffect.R;

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
}
