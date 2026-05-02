package com.mpcorporation.snapeffect.presentation.common

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.mpcorporation.snapeffect.R
import com.mpcorporation.snapeffect.domain.model.EffectItem
import com.mpcorporation.snapeffect.presentation.editor.EditorViewModel
import com.mpcorporation.snapeffect.presentation.editor.widget.ImageRendererView
import java.util.concurrent.atomic.AtomicBoolean

object SliderController {

    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun show(
        activity: Activity,
        item: EffectItem,
        renderer: ImageRendererView,
        viewModel: EditorViewModel
    ) {
        val seekBar = activity.findViewById<SeekBar>(R.id.parameterSeekBar)
        val labelView = activity.findViewById<TextView>(R.id.seekBarLabel)

        seekBar.visibility = View.VISIBLE
        labelView.visibility = View.VISIBLE
        seekBar.max = 100

        val initialProgress = ((item.defaultValue - item.min) / (item.max - item.min) * 100f).toInt()
        seekBar.progress = initialProgress
        labelView.text = "${item.label}: ${"%.2f".format(item.defaultValue)}"

        val isProcessing = AtomicBoolean(false)
        item.applyParameter(item.defaultValue)
        applyPreview(renderer, item, viewModel, isProcessing)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val value = item.min + (item.max - item.min) * (progress / 100f)
                labelView.text = "${item.label}: ${"%.2f".format(value)}"
                item.applyParameter(value)
                if (!isProcessing.get()) applyPreview(renderer, item, viewModel, isProcessing)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val source = renderer.getSourceBitmap() ?: return
                viewModel.applyFilterAndCommit(item.filter, source)
            }
        })
    }

    fun hide(activity: Activity) {
        activity.findViewById<SeekBar>(R.id.parameterSeekBar).visibility = View.GONE
        activity.findViewById<TextView>(R.id.seekBarLabel).visibility = View.GONE
    }

    private fun applyPreview(
        renderer: ImageRendererView,
        item: EffectItem,
        viewModel: EditorViewModel,
        isProcessing: AtomicBoolean
    ) {
        val preview = renderer.getPreviewBitmap() ?: return
        isProcessing.set(true)
        viewModel.previewFilter(item.filter, preview) { result ->
            renderer.post {
                renderer.showFilteredPreview(result)
                isProcessing.set(false)
            }
        }
    }
}
