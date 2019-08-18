package com.abbyberkers.solarduino.ui.components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * A class with custom touch events, the down touch event will be repeated with the given timeout until the button is released.
 */
class TimerImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, var timeout: Long = 5000) : ImageButton(context, attrs) {

    var upAction: () -> Unit = {}
    var downAction: () -> Unit = {}
    var cancelled = false
    var timerTask: TimerTask.() -> Unit = {
        if (cancelled) {
            cancel()
        }
        downAction()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                return true
            }

            MotionEvent.ACTION_UP -> {
                cancelled = true
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
        cancelled = false
        fixedRateTimer("upTimer", false, period = timeout, action = timerTask)
        return true
    }
}