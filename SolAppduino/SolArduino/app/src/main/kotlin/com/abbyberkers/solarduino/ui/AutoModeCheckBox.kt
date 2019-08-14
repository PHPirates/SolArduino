package com.abbyberkers.solarduino.ui

import android.widget.CheckBox
import com.abbyberkers.solarduino.PanelRequestSender

/**
 * Checkbox to set/unset panels on auto mode.
 */
class AutoModeCheckBox(private val autoBox: CheckBox) {

    fun initialise(panelRequestSender: PanelRequestSender) {
        // This makes sure that when the button is toggled programmatically, the clicklistener isn't called
        autoBox.setOnClickListener {
            if (autoBox.isChecked) {
                panelRequestSender.enableAutoMode()
            } else {
                panelRequestSender.disableAutoMode()
            }
        }
    }
}