package com.mpcorporation.snapeffect.presentation.common.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mpcorporation.snapeffect.domain.model.UserPreset
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserPresetBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: UserPresetViewModel by viewModels()
    private var saveMode: Boolean = false
    private var onApplyPreset: ((UserPreset) -> Unit)? = null

    fun configure(saveMode: Boolean, onApplyPreset: (UserPreset) -> Unit) {
        this.saveMode = saveMode
        this.onApplyPreset = onApplyPreset
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = if (saveMode) buildSaveView() else buildLoadView()

    private fun buildSaveView(): View {
        val ctx = requireContext()
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        root.addView(TextView(ctx).apply {
            text = "Lưu Preset hiện tại"
            textSize = 16f
        })
        val nameField = EditText(ctx).apply { hint = "Tên preset..." }
        root.addView(nameField)
        val btnSave = Button(ctx).apply {
            text = "Lưu"
            setOnClickListener {
                val name = nameField.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(ctx, "Nhập tên preset", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.add(UserPreset(name = name))
                Toast.makeText(ctx, "Đã lưu preset \"$name\"", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
        root.addView(btnSave)
        return root
    }

    private fun buildLoadView(): View {
        val ctx = requireContext()
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 40)
        }
        root.addView(TextView(ctx).apply {
            text = "Preset của tôi"
            textSize = 16f
            setPadding(0, 0, 0, 24)
        })

        lifecycleScope.launch {
            val presets = viewModel.load()
            if (presets.isEmpty()) {
                root.addView(TextView(ctx).apply {
                    text = "Chưa có preset nào. Hãy lưu preset từ menu."
                })
            } else {
                presets.forEachIndexed { index, preset ->
                    val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
                    row.addView(TextView(ctx).apply {
                        text = preset.name
                        textSize = 14f
                        layoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                        )
                    })
                    row.addView(Button(ctx).apply {
                        text = "Áp dụng"
                        setOnClickListener {
                            onApplyPreset?.invoke(preset)
                            dismiss()
                        }
                    })
                    row.addView(Button(ctx).apply {
                        text = "Xóa"
                        setOnClickListener {
                            viewModel.delete(index)
                            dismiss()
                        }
                    })
                    root.addView(row)
                }
            }
        }
        return root
    }
}
