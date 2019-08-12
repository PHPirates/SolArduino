package com.abbyberkers.solarduino.ui

import com.abbyberkers.solarduino.HttpClient

class MoveDownButton(private val downButton: AccessibilityImageButton) {

    fun initialise(httpClient: HttpClient) {
        // When touched, start moving panels.
        downButton.downAction = { httpClient.movePanelsDown() }
        // When released, stop them.
        downButton.upAction = { httpClient.stopPanels() }
    }
}