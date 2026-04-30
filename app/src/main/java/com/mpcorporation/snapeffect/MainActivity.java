package com.mpcorporation.snapeffect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mpcorporation.snapeffect.Adapter.BottomNavAdapter;
import com.mpcorporation.snapeffect.Filters.AdjustEffectFactory;
import com.mpcorporation.snapeffect.Filters.ArtEffectFactory;
import com.mpcorporation.snapeffect.Filters.BottomNavItemFactory;
import com.mpcorporation.snapeffect.Filters.DistortEffectFactory;
import com.mpcorporation.snapeffect.Handler.ToolbarHandler;
import com.mpcorporation.snapeffect.Model.BottomNavItem;
import com.mpcorporation.snapeffect.Model.EffectItem;
import com.mpcorporation.snapeffect.Utils.HistoryManager;
import com.mpcorporation.snapeffect.Utils.PermissionUtils;
import com.mpcorporation.snapeffect.Utils.SliderUtils;
import com.mpcorporation.snapeffect.crop.CropActivity;
import com.mpcorporation.snapeffect.filter.BitmapIO;
import com.mpcorporation.snapeffect.filter.ImageRenderer;

import com.mpcorporation.snapeffect.View.EffectBottomSheet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 123;

    Toolbar layoutToolbar;
    private Uri photoUri;
    private ImageRenderer imageRenderer;
    private HistoryManager<Uri> manager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deleteTemporaryImages();

        layoutToolbar = findViewById(R.id.toolbar);
        imageRenderer = findViewById(R.id.content_edit);
        manager = new HistoryManager<>();

        ToolbarHandler.setupToolbar(this, layoutToolbar);

        RecyclerView bottomNavView = findViewById(R.id.bottom_navigation);
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int swDp = Resources.getSystem().getConfiguration().smallestScreenWidthDp;
        Log.e("Kích thước", "Smallest width dp: " + swDp);
        int sidePadding = (int) (screenWidth * 0.17f);

        bottomNavView.setPadding(sidePadding, 0, sidePadding, 0);
        bottomNavView.setClipToPadding(false);

        List<BottomNavItem> items = BottomNavItemFactory.create();
        BottomNavAdapter adapter = new BottomNavAdapter(items, position -> {
            EffectBottomSheet sheet;
            if (photoUri != null) {
                switch (position) {
                    case 0:
                        Intent cropIntent = new Intent(this, CropActivity.class);
                        cropIntent.putExtra(CropActivity.EXTRA_INPUT_URI, photoUri.toString());
                        cropLauncher.launch(cropIntent);
                        break;
                    case 1:
                        List<EffectItem> adjustEffects = AdjustEffectFactory.create();
                        sheet = EffectBottomSheet.getEffectBottomSheet(this, imageRenderer, adjustEffects, manager);
                        sheet.show(this.getSupportFragmentManager(), "blur_effects");
                        break;
                    case 2:
                        List<EffectItem> artEffect = ArtEffectFactory.create();
                        sheet = EffectBottomSheet.getEffectBottomSheet(this, imageRenderer, artEffect, manager);
                        sheet.show(this.getSupportFragmentManager(), "art_effects");
                        break;
                    case 3:
                        List<EffectItem> distorEffect = DistortEffectFactory.create();
                        sheet = EffectBottomSheet.getEffectBottomSheet(this, imageRenderer, distorEffect, manager);
                        sheet.show(this.getSupportFragmentManager(), "distor_effects");
                        break;
                }
            } else {
                Toast.makeText(this, "Cần thêm ảnh", Toast.LENGTH_SHORT).show();
            }
        });
        bottomNavView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bottomNavView.setAdapter(adapter);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                deleteTemporaryImages();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        deleteTemporaryImages();
    }

    // Launcher cho CropActivity
    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String uriStr = result.getData().getStringExtra(CropActivity.EXTRA_OUTPUT_URI);
                    if (uriStr != null) {
                        Uri croppedUri = Uri.parse(uriStr);
                        photoUri = croppedUri;
                        manager.add(croppedUri);
                        imageRenderer.setImage(this, croppedUri);
                        SliderUtils.hideSlider(this);
                    }
                } else if (result.getResultCode() == CropActivity.RESULT_CROP_ERROR) {
                    Toast.makeText(this, "Lỗi cắt ảnh", Toast.LENGTH_SHORT).show();
                }
            }
    );

    public void openCamera() {
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
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (photoUri != null) {
                        manager.clear();
                        manager.add(photoUri);
                        imageRenderer.setImage(this, photoUri);
                        imageRenderer.clearFilter();
                        deleteTemporaryImages();
                    } else {
                        Log.e("cameraLauncher", "Photo uri = null");
                    }
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                } else {
                    Log.e("cameraLauncher", "Hủy chụp ảnh");
                }
            }
    );

    public final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    photoUri = uri;
                    manager.clear();
                    manager.add(photoUri);
                    deleteTemporaryImages();
                    LinearLayout layout = findViewById(R.id.nav_host_fragment);
                    layout.setVisibility(ViewGroup.GONE);
                    SliderUtils.hideSlider(this);
                    imageRenderer.setImage(this, uri);
                    imageRenderer.clearFilter();
                } else {
                    Log.e("pickImageLauncher", "Không có ảnh nào được chọn");
                }
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        MenuItem cameraItem = menu.findItem(R.id.menu_camera);
        View actionView = cameraItem.getActionView();
        if (actionView != null) {
            actionView.setOnClickListener(v ->
                    Toast.makeText(this, "Clicked Camera", Toast.LENGTH_SHORT).show());
        }
        return true;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_camera) {
            PermissionUtils.openCameraWithCheck(this, PERMISSION_REQUEST_CAMERA);
            return true;
        } else if (id == R.id.menu_open) {
            pickImageLauncher.launch("image/*");
            return true;
        } else if (id == R.id.menu_save) {
            if (photoUri != null) {
                Bitmap toSave = imageRenderer.getCurrentBitmap();
                if (toSave == null) return true;
                String filename = "SnapEffect" + System.currentTimeMillis() + ".jpg";
                executor.execute(() -> {
                    try {
                        Uri saved = BitmapIO.saveToPictures(this, "Snap Effect", filename, toSave);
                        runOnUiThread(() -> {
                            manager.clear();
                            manager.add(saved);
                            imageRenderer.setImage(this, saved);
                            deleteTemporaryImages();
                            SliderUtils.hideSlider(this);
                            Toast.makeText(this, "Ảnh được lưu tại: /Pictures/Snap Effect/" + filename, Toast.LENGTH_SHORT).show();
                        });
                    } catch (IOException e) {
                        Log.e("menu_save", "Lỗi lưu ảnh", e);
                    }
                });
            } else {
                Toast.makeText(this, "Cần thêm ảnh", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_undo) {
            if (photoUri != null) {
                manager.undo();
                photoUri = manager.get();
                Log.e("undo", photoUri.toString());
                imageRenderer.setImage(this, photoUri);
                imageRenderer.clearFilter();
                SliderUtils.hideSlider(this);
                return true;
            } else {
                Toast.makeText(this, "Cần thêm ảnh", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_redo) {
            if (photoUri != null) {
                manager.redo();
                photoUri = manager.get();
                Log.e("redo", photoUri.toString());
                imageRenderer.setImage(this, photoUri);
                imageRenderer.clearFilter();
                SliderUtils.hideSlider(this);
            }
            return true;
        } else if (id == R.id.menu_more_vert) {
            String url = "https://www.freeprivacypolicy.com/live/619f632c-4ca6-41ff-9c7c-524fd0e9eacd";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void deleteTemporaryImages() {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Snap Effect Temporary");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.e("deleteTemp", "Không thể xóa: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        // Xóa cache files cũ
        File cache = getCacheDir();
        File[] cacheFiles = cache.listFiles(f -> f.getName().startsWith("snap_tmp_") || f.getName().startsWith("cropped_"));
        if (cacheFiles != null) {
            for (File f : cacheFiles) f.delete();
        }
    }
}
