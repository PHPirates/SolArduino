package com.abbyberkers.solarduino.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageButton
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * A class with custom touch events, the down touch event will be repeated with the given timeout until the button is released.
 */
class TimerImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, private var timeout: Long = 5000) : ImageButton(context, attrs) {

    var upAction: () -> Unit = {}
    var downAction: () -> Unit = {}
    private var timer: Timer? = null
    // Provide the action direction to performClick()
    private var action = MotionEvent.ACTION_DOWN

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                this.action = MotionEvent.ACTION_DOWN
                // Recommended by Android to use the same function for both up and down
                performClick()
                return true
            }

            MotionEvent.ACTION_UP -> {
                this.action = MotionEvent.ACTION_UP
                return true
            }
        }

        return false
    }

    // Because we call this from onTouchEvent, this code will be executed on both
    // touch actions or when the system calls this using Accessibility
    override fun performClick(): Boolean {
        super.performClick()

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                timer = fixedRateTimer("upTimer", false, period = timeout) {
                    downAction()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                timer?.cancel()
                upAction()
                return true
            }
        }

        return false
    }
}