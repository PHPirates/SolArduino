package com.abbyberkers.solarduino.ui.components

import com.abbyberkers.solarduino.communication.PanelRequestSender

class MoveDownButton(private val downButton: AccessibilityImageButton) {

    fun initialise(panelRequestSender: PanelRequestSender) {
        // When touched, start moving panels.
        downButton.downAction = { panelRequestSender.movePanelsDown() }
        // When released, stop them.
        downButton.upAction = { panelRequestSender.stopPanels() }
    }
}