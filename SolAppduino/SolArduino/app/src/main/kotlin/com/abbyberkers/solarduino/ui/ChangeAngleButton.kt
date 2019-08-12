package com.abbyberkers.solarduino.ui

import android.content.res.Resources
import android.widget.Button
import com.abbyberkers.solarduino.HttpClient
import com.abbyberkers.solarduino.R

/**
 * A button to make the panels go to a certain angle.
 */
class ChangeAngleButton(private val button: Button,
                        private val resources: Resources,
                        private val httpClient: HttpClient
) {

    var angle = 42
    set(value) {
        setText(angle)
        field = value
    }

    fun initialise() {
        button.setOnClickListener {
            httpClient.movePanelsToAngle(angle)
        }
    }

    /**
     * Set button text given an angle.
     */
    private fun setText(angle: Int) {
        button.text = resources.getString(R.string.change_angle_button_description, angle)
    }

}