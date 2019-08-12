package com.abbyberkers.solarduino.ui

import android.content.Context
import android.view.MotionEvent
import android.widget.ImageButton

/**
 * A class with custom touch events, which takes a certain Android accessibility warning into account.
 */
class AccessibilityImageButton(context: Context) : ImageButton(context) {

    var upAction: () -> Unit = {}
    var downAction: () -> Unit = {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                return true
            }

            MotionEvent.ACTION_UP -> {
                upAction()
                return true
            }
        }
        return false
    }

    // Because we call this from onTouchEvent, this code will be executed for both
    // normal touch events and for when the system calls this using Accessibility
    override fun performClick(): Boolean {
        super.performClick()
        downAction()
        return true
    }
}