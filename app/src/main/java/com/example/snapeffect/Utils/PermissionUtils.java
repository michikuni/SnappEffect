package com.example.snapeffect.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.snapeffect.MainActivity;

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
    public static void checkPermission(Activity activity){
        String[] permission = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean needRequest = false;
        for (String permis : permission){
            if (ContextCompat.checkSelfPermission(activity, permis) != PackageManager.PERMISSION_GRANTED){
                needRequest = true;
                break;
            }
        }
        if (needRequest){
            ActivityCompat.requestPermissions(activity, permission, 200);
        }
    }
}
