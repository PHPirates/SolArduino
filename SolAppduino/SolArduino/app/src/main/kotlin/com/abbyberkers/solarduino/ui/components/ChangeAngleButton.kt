package com.abbyberkers.solarduino.ui.components

import android.content.res.Resources
import android.widget.Button
import com.abbyberkers.solarduino.communication.PanelRequestSender
import com.abbyberkers.solarduino.R

/**
 * A button to make the panels go to a certain angle.
 */
class ChangeAngleButton(private val button: Button,
                        private val resources: Resources,
                        private val panelRequestSender: PanelRequestSender
) {

    var angle = 42
    set(value) {
        field = value
        setText(angle)
    }

    fun initialise() {
        button.setOnClickListener {
            panelRequestSender.movePanelsToAngle(angle)
        }
    }

    /**
     * Set button text given an angle.
     */
    private fun setText(angle: Int) {
        button.text = resources.getString(R.string.change_angle_button_description, angle)
    }

}