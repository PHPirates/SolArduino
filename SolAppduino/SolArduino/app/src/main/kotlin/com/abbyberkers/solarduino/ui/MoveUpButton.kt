package com.abbyberkers.solarduino.ui

import android.view.MotionEvent
import android.widget.ImageButton
import com.abbyberkers.solarduino.HttpClient

class MoveUpButton(private val upButton: ImageButton) {

    fun initialise(httpClient: HttpClient) {
        // set OnTouchListener for the up button, send http request when button pressed and released
        upButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {

                    upButton.isPressed = true // set pressed state true so colour changes
                    httpClient.moveUp()
                    view.performClick()
                }
                MotionEvent.ACTION_UP -> {
                    //                        textView.setText("Up button released.");
                    upButton.isPressed = false // set pressed state false so colour changes back to default
                    httpClient.moveDown()
                }
            }
            false
        }

    }

}