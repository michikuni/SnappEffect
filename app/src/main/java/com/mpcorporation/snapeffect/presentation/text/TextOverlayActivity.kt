package com.mpcorporation.snapeffect.presentation.text

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.domain.usecase.BurnTextUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class TextOverlayActivity : AppCompatActivity() {

    private val viewModel: TextOverlayViewModel by viewModels()

    private lateinit var previewImage: ImageView
    private lateinit var overlayText: TextView
    private lateinit var inputField: EditText

    private var textXNorm = 0.5f
    private var textYNorm = 0.5f
    private var textColor = Color.WHITE
    private var textSizeNorm = 0.08f
    private var isBold = false
    private var isItalic = false

    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_overlay)

        setSupportActionBar(findViewById<Toolbar>(R.id.text_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        previewImage = findViewById(R.id.text_preview_image)
        overlayText = findViewById(R.id.text_overlay_label)
        inputField = findViewById(R.id.text_input_field)
        val sizeBar = findViewById<SeekBar>(R.id.text_size_seekbar)
        val btnConfirm = findViewById<Button>(R.id.btn_text_confirm)
        val btnColor = findViewById<Button>(R.id.btn_text_color)
        val btnBold = findViewById<Button>(R.id.btn_text_bold)
        val btnItalic = findViewById<Button>(R.id.btn_text_italic)

        val uriStr = intent.getStringExtra(EXTRA_INPUT_URI)
        if (uriStr == null) {
            setResult(RESULT_TEXT_ERROR)
            finish()
            return
        }
        viewModel.load(Uri.parse(uriStr))

        viewModel.source.observe(this) { bmp -> bmp?.let { previewImage.setImageBitmap(it) } }
        viewModel.result.observe(this) { res ->
            when (res) {
                is TextResult.Success -> {
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_OUTPUT_URI, res.uri.toString()))
                    finish()
                }
                is TextResult.Error -> Toast.makeText(this, res.message, Toast.LENGTH_SHORT).show()
            }
        }

        overlayText.setOnTouchListener { v, event ->
            val container = findViewById<FrameLayout>(R.id.text_preview_container)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragOffsetX = event.x
                    dragOffsetY = event.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    var newX = v.x + event.x - dragOffsetX
                    var newY = v.y + event.y - dragOffsetY
                    newX = max(0f, min((container.width - v.width).toFloat(), newX))
                    newY = max(0f, min((container.height - v.height).toFloat(), newY))
                    v.x = newX
                    v.y = newY
                    textXNorm = (newX + v.width / 2f) / container.width
                    textYNorm = (newY + v.height / 2f) / container.height
                    true
                }
                else -> false
            }
        }

        inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                overlayText.text = s?.toString().takeIf { !it.isNullOrEmpty() } ?: "Text"
            }
        })
        overlayText.text = "Text"

        sizeBar.progress = 40
        sizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSizeNorm = 0.02f + progress / 100f * 0.18f
                updateOverlayTextSize()
            }
        })

        val colors = intArrayOf(
            Color.WHITE, Color.BLACK, Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN
        )
        var colorIdx = 0
        btnColor.setOnClickListener {
            colorIdx = (colorIdx + 1) % colors.size
            textColor = colors[colorIdx]
            overlayText.setTextColor(textColor)
        }

        btnBold.setOnClickListener {
            isBold = !isBold
            updateOverlayTypeface()
            btnBold.alpha = if (isBold) 1f else 0.5f
        }

        btnItalic.setOnClickListener {
            isItalic = !isItalic
            updateOverlayTypeface()
            btnItalic.alpha = if (isItalic) 1f else 0.5f
        }

        btnConfirm.setOnClickListener { burn() }
    }

    private fun updateOverlayTextSize() {
        val container = findViewById<FrameLayout>(R.id.text_preview_container)
        val pxSize = container.width * textSizeNorm
        overlayText.setTextSize(TypedValue.COMPLEX_UNIT_PX, max(12f, pxSize))
    }

    private fun updateOverlayTypeface() {
        val style = (if (isBold) Typeface.BOLD else 0) or (if (isItalic) Typeface.ITALIC else 0)
        overlayText.setTypeface(null, if (style == 0) Typeface.NORMAL else style)
    }

    private fun burn() {
        val source = viewModel.source.value
        val text = inputField.text.toString()
        if (source == null || text.isEmpty()) {
            Toast.makeText(this, "Nhập nội dung text", Toast.LENGTH_SHORT).show()
            return
        }
        val container = findViewById<FrameLayout>(R.id.text_preview_container)
        viewModel.burn(
            source,
            BurnTextUseCase.TextOptions(
                text = text,
                xNorm = textXNorm,
                yNorm = textYNorm,
                color = textColor,
                sizeNorm = textSizeNorm,
                bold = isBold,
                italic = isItalic,
                containerWidth = container.width,
                containerHeight = container.height
            )
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
        const val EXTRA_INPUT_URI = "text_input_uri"
        const val EXTRA_OUTPUT_URI = "text_output_uri"
        const val RESULT_TEXT_ERROR = RESULT_FIRST_USER + 10
    }
}
