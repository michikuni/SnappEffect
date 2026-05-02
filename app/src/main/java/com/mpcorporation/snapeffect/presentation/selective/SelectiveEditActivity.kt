package com.mpcorporation.snapeffect.presentation.selective

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.presentation.selective.widget.BrushMaskView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectiveEditActivity : AppCompatActivity() {

    private val viewModel: SelectiveEditViewModel by viewModels()
    private lateinit var brushMaskView: BrushMaskView

    private var brightness = 0f
    private var contrast = 1f
    private var saturation = 1f
    private var brushSize = 80

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selective_edit)

        setSupportActionBar(findViewById<Toolbar>(R.id.selective_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        brushMaskView = findViewById(R.id.brush_mask_view)
        val brushSizeBar = findViewById<SeekBar>(R.id.seekbar_brush_size)
        val brightnessBar = findViewById<SeekBar>(R.id.seekbar_sel_brightness)
        val contrastBar = findViewById<SeekBar>(R.id.seekbar_sel_contrast)
        val saturationBar = findViewById<SeekBar>(R.id.seekbar_sel_saturation)
        val labelBrush = findViewById<TextView>(R.id.label_brush_size)
        val btnEraser = findViewById<Button>(R.id.btn_eraser)
        val btnClearMask = findViewById<Button>(R.id.btn_clear_mask)
        val btnConfirm = findViewById<Button>(R.id.btn_selective_confirm)

        val uriStr = intent.getStringExtra(EXTRA_INPUT_URI)
        if (uriStr == null) {
            setResult(RESULT_SELECTIVE_ERROR)
            finish()
            return
        }
        viewModel.load(Uri.parse(uriStr))

        viewModel.source.observe(this) { bmp -> bmp?.let { brushMaskView.setSourceBitmap(it) } }
        viewModel.result.observe(this) { res ->
            when (res) {
                is SelectiveResult.Success -> {
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(EXTRA_OUTPUT_URI, res.uri.toString())
                    )
                    finish()
                }
                is SelectiveResult.Error -> {
                    Toast.makeText(this, res.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        brushSizeBar.progress = 40
        brushSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brushSize = 20 + progress * 2
                brushMaskView.setBrushRadius(brushSize)
                labelBrush.text = "Cọ: ${brushSize}px"
            }
        })

        brightnessBar.progress = 50
        brightnessBar.setOnSeekBarChangeListener(simpleListener { p -> brightness = (p - 50) / 50f })
        contrastBar.progress = 33
        contrastBar.setOnSeekBarChangeListener(simpleListener { p -> contrast = 0.5f + p / 100f * 3.5f })
        saturationBar.progress = 50
        saturationBar.setOnSeekBarChangeListener(simpleListener { p -> saturation = p / 50f })

        btnEraser.setOnClickListener {
            brushMaskView.toggleEraser()
            btnEraser.alpha = if (brushMaskView.isEraserMode) 1f else 0.5f
        }
        btnClearMask.setOnClickListener { brushMaskView.clearMask() }
        btnConfirm.setOnClickListener { applySelective() }
    }

    private fun applySelective() {
        val source = viewModel.source.value ?: return
        val mask = brushMaskView.getMaskBitmap()
        if (mask == null) {
            Toast.makeText(this, "Vẽ vùng cần chỉnh sửa", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.apply(source, mask, brightness, contrast, saturation)
    }

    private fun simpleListener(onChange: (Int) -> Unit): SeekBar.OnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onChange(progress)
            }
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
        const val EXTRA_INPUT_URI = "selective_input_uri"
        const val EXTRA_OUTPUT_URI = "selective_output_uri"
        const val RESULT_SELECTIVE_ERROR = RESULT_FIRST_USER + 20
    }
}
