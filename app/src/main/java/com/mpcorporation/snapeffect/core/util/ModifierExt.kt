package com.mpcorporation.snapeffect.core.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Báo mỗi lần có ngón tay chạm xuống bất kỳ đâu trong vùng này (kể cả trên native ad), không ăn
 * sự kiện nên view bên dưới vẫn nhận bình thường. Bản Compose của dispatchTouchEvent bên BaseXml.
 */
fun Modifier.onAnyTouchDown(onTouch: () -> Unit): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.changes.any { it.changedToDown() }) onTouch()
        }
    }
}
