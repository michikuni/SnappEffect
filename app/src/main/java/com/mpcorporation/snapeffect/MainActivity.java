package com.mpcorporation.snapeffect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Handler.BottomNavHandler;
import com.mpcorporation.snapeffect.Handler.HandlerCrop;
import com.mpcorporation.snapeffect.Handler.ToolbarHandler;
import com.mpcorporation.snapeffect.Utils.PermissionUtils;
import com.mpcorporation.snapeffect.Utils.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.*;

import com.yalantis.ucrop.UCrop;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 123;
    Toolbar layoutToolbar;
    private Uri photoUri;
    private GPUImageView gpuImageView;
    List<GPUImageFilter> activeFilters = new ArrayList<>();
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutToolbar = findViewById(R.id.toolbar);
        gpuImageView = findViewById(R.id.content_edit);
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);

        ToolbarHandler.setupToolbar(this, layoutToolbar);

        RecyclerView bottomNavView = findViewById(R.id.bottom_navigation);

        BottomNavHandler.setupBottomNav(this, bottomNavView, gpuImageView, activeFilters, photoUri);

        // Ẩn SeekBar
        UIUtils.setupSeekBarDismissOnClick(findViewById(R.id.frame_gpu), findViewById(R.id.parameterSeekBar), this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            HandlerCrop.handleCropResult(data,gpuImageView, uri -> photoUri = uri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            HandlerCrop.handleCropError(this, data);
        }
    }
    public void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From camera");
        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(cameraIntent);
    }
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Toast.makeText(this, "Ảnh đã chụp xong", Toast.LENGTH_SHORT).show();
                    Log.d("Main Activity", "Chụp ảnh thành công");
                    gpuImageView.setImage(photoUri);
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    gpuImageView.requestRender();
                    gpuImageView.setFilter(new GPUImageFilter());
                } else {
                    Toast.makeText(this, "Bạn đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                    Log.d("Main Activity", "Hủy chụp ảnh");
                }
            }
    );
    public final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d("Main Activity", "Chọn ảnh từ thư viện: " + uri);
                    photoUri = uri;
                    gpuImageView.setImage(uri);
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    gpuImageView.requestRender();
                    gpuImageView.setFilter(new GPUImageFilter());
                }
                else {
                    Log.w("Main Actitvity", "Không có ảnh nào được chọn");
                    Toast.makeText(this, "Không có ảnh nào đưược chọn", Toast.LENGTH_SHORT).show();
                }
            });
    //Hiển thị menu top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }
    //Xử lý các component của menu top
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_open) {
            View anchorView = layoutToolbar;
            for (int i = 0; i < layoutToolbar.getChildCount(); i++) {
                View child = layoutToolbar.getChildAt(i);
                if (child instanceof ActionMenuView) {
                    ActionMenuView actionMenuView = (ActionMenuView) child;
                    for (int j = 0; j < actionMenuView.getChildCount(); j++) {
                        View menuItemView = actionMenuView.getChildAt(j);
                        if (actionMenuView.getMenu().getItem(j).getItemId() == R.id.menu_open) {
                            anchorView = menuItemView;
                            break;
                        }
                    }
                }
            }
            UIUtils.showPopupMenu(this,
                    anchorView,
                    () -> pickImageLauncher.launch("image/*"),
                    () -> PermissionUtils.openCameraWithCheck(this, PERMISSION_REQUEST_CAMERA)
            );
            return true;
        } else if (id == R.id.menu_history) {
            return true;
        } else if (id == R.id.menu_save) {
            Random random = new Random();
            int fileName = random.nextInt(100000);
            gpuImageView.saveToPictures("Snap Effect", "SnapEffect" + fileName + ".jpg", null);
            Toast.makeText(this, "Ảnh được lưu tại: /Pictures/Snap Effect/"+fileName+".jpg", Toast.LENGTH_SHORT).show();
            Log.d("Main Activity", "Lưu ảnh tại: /Pictures/Snap Effect/"+fileName+".jpg");
            return true;
        } else if (id == R.id.menu_more_vert) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}