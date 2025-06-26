package com.mpcorporation.snapeffect.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mpcorporation.snapeffect.MainActivity;

public class PermissionUtils {
    public static void openCameraWithCheck(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    requestCode);
        } else {
            ((MainActivity) activity).openCamera();
        }
    }
}
