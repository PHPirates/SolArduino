package com.abbyberkers.solarduino.ui.components

import android.widget.CheckBox
import com.abbyberkers.solarduino.communication.PanelRequestSender

/**
 * Checkbox to set/unset panels on auto mode.
 */
class AutoModeCheckBox(val checkBox: CheckBox) {

    fun initialise(panelRequestSender: PanelRequestSender) {
        // This makes sure that when the button is toggled programmatically, the clicklistener isn't called
        checkBox.setOnClickListener {
            if (checkBox.isChecked) {
                panelRequestSender.enableAutoMode(this)
            } else {
                panelRequestSender.disableAutoMode(this)
            }
        }
    }
}