package com.mpcorporation.snapeffect.presentation.crop.widget

import android.view.MotionEvent
import kotlin.math.sqrt

class CropTouchHandler(private val cropView: CropImageView) {

    private var lastPanX = 0f
    private var lastPanY = 0f
    private var lastSpan = 0f
    private var lastPivotX = 0f
    private var lastPivotY = 0f
    private var isPinching = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (cropView.handleFreeformTouch(event)) return true
        val pointerCount = event.pointerCount

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastPanX = event.x
                lastPanY = event.y
                isPinching = false
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (pointerCount == 2) {
                    lastSpan = span(event)
                    lastPivotX = midX(event)
                    lastPivotY = midY(event)
                    isPinching = true
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPinching && pointerCount >= 2) {
                    val s = span(event)
                    val px = midX(event)
                    val py = midY(event)
                    if (lastSpan > 0) cropView.zoom(s / lastSpan, px, py)
                    cropView.pan(px - lastPivotX, py - lastPivotY)
                    lastSpan = s
                    lastPivotX = px
                    lastPivotY = py
                } else if (!isPinching && pointerCount == 1) {
                    cropView.pan(event.x - lastPanX, event.y - lastPanY)
                    lastPanX = event.x
                    lastPanY = event.y
                }
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (pointerCount == 2) {
                    isPinching = false
                    val remaining = if (event.actionIndex == 0) 1 else 0
                    lastPanX = event.getX(remaining)
                    lastPanY = event.getY(remaining)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPinching = false
                return true
            }
        }
        return false
    }

    private fun span(e: MotionEvent): Float {
        val dx = e.getX(0) - e.getX(1)
        val dy = e.getY(0) - e.getY(1)
        return sqrt(dx * dx + dy * dy)
    }

    private fun midX(e: MotionEvent) = (e.getX(0) + e.getX(1)) / 2f
    private fun midY(e: MotionEvent) = (e.getY(0) + e.getY(1)) / 2f
}
