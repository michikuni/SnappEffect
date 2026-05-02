package com.mpcorporation.snapeffect.presentation.editor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.core.util.PermissionUtils
import com.mpcorporation.snapeffect.domain.repository.EffectCatalog
import com.mpcorporation.snapeffect.presentation.common.SliderController
import com.mpcorporation.snapeffect.presentation.common.adapter.BottomNavAdapter
import com.mpcorporation.snapeffect.presentation.common.bottomsheet.EffectBottomSheet
import com.mpcorporation.snapeffect.presentation.crop.CropActivity
import com.mpcorporation.snapeffect.presentation.editor.widget.ImageRendererView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditorActivity : AppCompatActivity() {

    @Inject lateinit var effectCatalog: EffectCatalog

    private val viewModel: EditorViewModel by viewModels()

    private lateinit var renderer: ImageRendererView
    private var photoUri: Uri? = null
    private var cameraOutputUri: Uri? = null

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uriStr = result.data!!.getStringExtra(CropActivity.EXTRA_OUTPUT_URI)
                if (uriStr != null) {
                    val uri = Uri.parse(uriStr)
                    photoUri = uri
                    viewModel.loadImage(uri, resetHistory = false)
                    SliderController.hide(this)
                }
            } else if (result.resultCode == CropActivity.RESULT_CROP_ERROR) {
                Toast.makeText(this, "Lỗi cắt ảnh", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraOutputUri?.let { uri ->
                    photoUri = uri
                    viewModel.setExternal(uri)
                    findViewById<LinearLayout>(R.id.nav_host_fragment).visibility = ViewGroup.GONE
                    renderer.clearFilter()
                    viewModel.clearTemporaryImages()
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            photoUri = uri
            viewModel.setExternal(uri)
            findViewById<LinearLayout>(R.id.nav_host_fragment).visibility = ViewGroup.GONE
            SliderController.hide(this)
            renderer.clearFilter()
            viewModel.clearTemporaryImages()
        }

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.clearTemporaryImages()

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        renderer = findViewById(R.id.content_edit)

        observe()
        setupBottomNav()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.clearTemporaryImages()
                finish()
            }
        })
    }

    private fun observe() {
        viewModel.bitmap.observe(this) { bmp -> bmp?.let { renderer.setBitmap(it) } }
        viewModel.events.observe(this) { event ->
            when (event) {
                is EditorEvent.Saved -> Toast.makeText(
                    this,
                    "Ảnh được lưu tại: /Pictures/Snap Effect/${event.filename}",
                    Toast.LENGTH_SHORT
                ).show()
                is EditorEvent.Error -> Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                is EditorEvent.FilterApplied -> { photoUri = event.uri }
            }
        }
    }

    private fun setupBottomNav() {
        val bottomNavView = findViewById<RecyclerView>(R.id.bottom_navigation)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val sidePadding = (screenWidth * 0.17f).toInt()
        bottomNavView.setPadding(sidePadding, 0, sidePadding, 0)
        bottomNavView.clipToPadding = false

        val items = effectCatalog.bottomNavItems()
        bottomNavView.adapter = BottomNavAdapter(items) { position -> onNavClicked(position) }
        bottomNavView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun onNavClicked(position: Int) {
        val photo = photoUri
        if (photo == null) {
            Toast.makeText(this, "Cần thêm ảnh", Toast.LENGTH_SHORT).show()
            return
        }
        when (position) {
            0 -> {
                val intent = Intent(this, CropActivity::class.java)
                    .putExtra(CropActivity.EXTRA_INPUT_URI, photo.toString())
                cropLauncher.launch(intent)
            }
            1 -> showEffectSheet(effectCatalog.adjustEffects(), "adjust_effects")
            2 -> showEffectSheet(effectCatalog.artEffects(), "art_effects")
            3 -> showEffectSheet(effectCatalog.distortEffects(), "distort_effects")
        }
    }

    private fun showEffectSheet(items: List<com.mpcorporation.snapeffect.domain.model.EffectItem>, tag: String) {
        val sheet = EffectBottomSheet()
        sheet.configure(items) { selected ->
            if (selected.hasParameter) {
                SliderController.show(this, selected, renderer, viewModel)
            } else {
                SliderController.hide(this)
                renderer.getSourceBitmap()?.let { src ->
                    viewModel.applyFilterAndCommit(selected.filter, src)
                }
            }
        }
        sheet.show(supportFragmentManager, tag)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_camera -> {
                if (PermissionUtils.hasCameraPermission(this)) openCamera()
                else PermissionUtils.requestCameraPermission(this, REQUEST_CAMERA_PERMISSION)
                true
            }
            R.id.menu_open -> {
                pickImageLauncher.launch("image/*")
                true
            }
            R.id.menu_save -> {
                if (photoUri != null) {
                    renderer.getCurrentBitmap()?.let { viewModel.saveToGallery(it) }
                } else {
                    Toast.makeText(this, "Cần thêm ảnh", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_undo -> {
                if (photoUri != null) {
                    viewModel.undo()
                    SliderController.hide(this)
                } else Toast.makeText(this, "Cần thêm ảnh", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_redo -> {
                if (photoUri != null) {
                    viewModel.redo()
                    SliderController.hide(this)
                }
                true
            }
            R.id.menu_more_vert -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.freeprivacypolicy.com/live/619f632c-4ca6-41ff-9c7c-524fd0e9eacd")
                    )
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openCamera() {
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From camera")
        }
        cameraOutputUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            cv
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri)
        cameraLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearTemporaryImages()
    }

    private companion object {
        const val REQUEST_CAMERA_PERMISSION = 123
    }
}
