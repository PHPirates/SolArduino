package com.abbyberkers.solarduino.ui.components

import com.abbyberkers.solarduino.communication.PanelRequestSender

class MoveUpButton(private val upButton: AccessibilityImageButton) {

    fun initialise(panelRequestSender: PanelRequestSender) {
        // When touched, start moving panels.
        upButton.downAction = { panelRequestSender.movePanelsUp() }
        // When released, stop them.
        upButton.upAction = { panelRequestSender.stopPanels() }
    }
}