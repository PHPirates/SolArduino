package com.abbyberkers.solarduino.ui

import android.widget.Button

/**
 * A button to make the panels go to a certain angle.
 */
class ChangeAngleButton(private val button: Button) {

    /**
     * Set button text given an angle.
     */
    fun setText(angle: Int) {
        button.text = "Set angle at $angle\u00b0"
    }

}