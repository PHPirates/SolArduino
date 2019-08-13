package com.abbyberkers.solarduino.ui

import com.abbyberkers.solarduino.PanelRequestSender

class MoveDownButton(private val downButton: AccessibilityImageButton) {

    fun initialise(panelRequestSender: PanelRequestSender) {
        // When touched, start moving panels.
        downButton.downAction = { panelRequestSender.movePanelsDown() }
        // When released, stop them.
        downButton.upAction = { panelRequestSender.stopPanels() }
    }
}