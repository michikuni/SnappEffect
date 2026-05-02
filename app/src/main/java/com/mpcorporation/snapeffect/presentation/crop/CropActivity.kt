package com.mpcorporation.snapeffect.presentation.crop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.presentation.crop.widget.CropImageView
import com.mpcorporation.snapeffect.presentation.crop.widget.CropTouchHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CropActivity : AppCompatActivity() {

    private val viewModel: CropViewModel by viewModels()

    private lateinit var cropImageView: CropImageView
    private lateinit var touchHandler: CropTouchHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        setSupportActionBar(findViewById<Toolbar>(R.id.crop_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cropImageView = findViewById(R.id.crop_image_view)
        touchHandler = CropTouchHandler(cropImageView)
        cropImageView.setOnTouchListener { _, event -> touchHandler.onTouchEvent(event) }

        val ratioX = intent.getFloatExtra(EXTRA_RATIO_X, 1f)
        val ratioY = intent.getFloatExtra(EXTRA_RATIO_Y, 1f)
        cropImageView.setAspectRatio(ratioX, ratioY)

        val uriStr = intent.getStringExtra(EXTRA_INPUT_URI)
        if (uriStr == null) {
            setResult(RESULT_CROP_ERROR)
            finish()
            return
        }
        cropImageView.setImageUri(this, Uri.parse(uriStr))

        findViewById<Button>(R.id.btn_rotate).setOnClickListener { cropImageView.rotateImage(90) }
        findViewById<Button>(R.id.btn_crop_confirm).setOnClickListener { performCrop() }

        viewModel.result.observe(this) { res ->
            when (res) {
                is CropResult.Success -> {
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_OUTPUT_URI, res.uri.toString()))
                    finish()
                }
                is CropResult.Error -> {
                    Toast.makeText(this, "Lỗi cắt ảnh: ${res.message}", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CROP_ERROR)
                    finish()
                }
            }
        }
    }

    private fun performCrop() {
        val source = cropImageView.source() ?: return
        viewModel.crop(
            source,
            cropImageView.cropTransform(),
            cropImageView.cropBoxRect(),
            cropImageView.cropWidthPx(),
            cropImageView.cropHeightPx()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(RESULT_CANCELED)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_INPUT_URI = "input_uri"
        const val EXTRA_OUTPUT_URI = "output_uri"
        const val EXTRA_RATIO_X = "ratio_x"
        const val EXTRA_RATIO_Y = "ratio_y"
        const val RESULT_CROP_ERROR = RESULT_FIRST_USER
    }
}
