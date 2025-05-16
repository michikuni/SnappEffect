package com.example.snapeffect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snapeffect.Adapter.BottomNavAdapter;
import com.example.snapeffect.Model.BottomNavItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int DEFAULT_SIZE = 150;
//    private static final int REQUEST_CODE_GALLERY = 100;
    private static final int REQUEST_CODE_CAMERA = 101;
    private static final int REQUEST_PERMISSION = 200;
    private static final int PERMISSION_REQUEST_CAMERA = 123;
    Toolbar layoutToolbar;
    private ImageView imageView;
    private TextView textView;
    private Uri photoUri;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private void openCameraWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            openCamera(); // Gọi như bình thường
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showToast(String message) {
        mainHandler.post(() -> {
            long startTime = System.nanoTime();
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            long endTime = System.nanoTime();
            Log.d("Main Activity", "Toast display time: " + (endTime - startTime) / 1_000_000.0 + "ms");
        });
    }
    private void showPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.open_option_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.option1){
              pickImageLauncher.launch("image/*");
//                openGallery();
            }
            if (item.getItemId() == R.id.option2){
                openCameraWithPermissionCheck();
            }
            checkPermission();
            showToast("Selected: " + item.getTitle());
            return true;
        });
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popup.show();
    }

    //hết date
//    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, REQUEST_CODE_GALLERY);
//    }
//    private void openCamera() {
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "New Picture");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
//
//        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
//    }
    private void openCamera(){
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
                    Glide.with(this)
                            .load(photoUri)
                            .fitCenter()
                            .into(imageView);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0
                    );
                    params.weight = 1;
                    params.gravity = Gravity.CENTER;
                    imageView.setLayoutParams(params);
                    imageView.requestLayout();
                    textView.setVisibility(ViewGroup.GONE);
                } else {
                    Toast.makeText(this, "Bạn đã hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            }
    );
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d("Main Activity", "Image selected: " + uri.toString());
                    // Dùng Glide để tải ảnh
                    Glide.with(this)
                            .load(uri)
                            .fitCenter()
                            .into(imageView);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    // Thay đổi kích thước ImageView
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0
                    );
                    params.weight = 1;
                    params.gravity = Gravity.CENTER;
                    imageView.setLayoutParams(params);
                    imageView.requestLayout();
                    textView.setVisibility(ViewGroup.GONE);
                }
                else {
                    Log.w("Main Actitvity", "Không có ảnh nào được chọn");
                    Toast.makeText(this, "Không có ảnh nào đưược chọn", Toast.LENGTH_SHORT).show();
                }
            });
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnItemSelectedListener(item ->{
//            if (item.getItemId() == R.id.nav_filters){
//                Toast.makeText(this, "Filters clicked", Toast.LENGTH_SHORT).show();
//                return true;
//            } else if(item.getItemId() == R.id.nav_tools){
//                Toast.makeText(this, "Tools clicked", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//            return false;
//        });


        textView = findViewById(R.id.text_unknow_photo);
        imageView = findViewById(R.id.photo_edit_content);
        imageView.setImageResource(R.drawable.image_24px);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams defaultParams = new LinearLayout.LayoutParams(
                dpToPx(DEFAULT_SIZE),
                dpToPx(DEFAULT_SIZE)
        );
        defaultParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(defaultParams);

        //Tạo Toolbar để hiển thị menu top
        layoutToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(layoutToolbar);

        //Hiển thị menu bot bằng RecyclerView
        RecyclerView bottomNavView = findViewById(R.id.bottom_navigation);
        bottomNavView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Các thành phần của menu bot
        List<BottomNavItem> items = new ArrayList<>();
        items.add(new BottomNavItem(R.drawable.crop_24px, "Crop"));
        items.add(new BottomNavItem(R.drawable.download_24px, "Download"));
        items.add(new BottomNavItem(R.drawable.filter_alt_24px, "Filter"));
        items.add(new BottomNavItem(R.drawable.folder_open_24px, "Open"));
        items.add(new BottomNavItem(R.drawable.history_24px, "History"));
        items.add(new BottomNavItem(R.drawable.photo_camera_24px, "Camera"));
        items.add(new BottomNavItem(R.drawable.smartphone_24px, "Phone"));
        items.add(new BottomNavItem(R.drawable.tune_24px, "Tune"));
        items.add(new BottomNavItem(R.drawable.widgets_24px, "Widget"));
        items.add(new BottomNavItem(R.drawable.more_vert_24px, "More Vert"));

        //Xử lý click
        BottomNavAdapter adapter = new BottomNavAdapter(items, position -> {
            String label = items.get(position).label;
            Log.d("Main Activity", "BottomNav clicked: " + label);
            showToast("Clicked: " + label);
        });

        bottomNavView.setAdapter(adapter);
    }

    private void checkPermission(){
        String[] permission = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean needRequest = false;
        for (String permis : permission){
            if (ContextCompat.checkSelfPermission(this, permis) != PackageManager.PERMISSION_GRANTED){
                needRequest = true;
                break;
            }
        }
        if (needRequest){
            ActivityCompat.requestPermissions(this, permission, REQUEST_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                imageView.setImageURI(photoUri);
            }
        }
    }
    //Đổi từ dp sang pixel
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

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
                if (child instanceof androidx.appcompat.widget.ActionMenuView) {
                    androidx.appcompat.widget.ActionMenuView actionMenuView = (androidx.appcompat.widget.ActionMenuView) child;
                    for (int j = 0; j < actionMenuView.getChildCount(); j++) {
                        View menuItemView = actionMenuView.getChildAt(j);
                        // Kiểm tra MenuItem tương ứng
                        if (actionMenuView.getMenu().getItem(j).getItemId() == R.id.menu_open) {
                            anchorView = menuItemView;
                            break;
                        }
                    }
                }
            }
            showPopupMenu(anchorView);
            Toast.makeText(this, "Open file", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_history) {
            Toast.makeText(this, "History clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_save) {
            Toast.makeText(this, "Save clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_more_vert) {
            Toast.makeText(this, "More Vert clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}