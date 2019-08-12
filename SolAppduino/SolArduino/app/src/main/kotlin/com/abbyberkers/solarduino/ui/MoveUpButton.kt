package com.abbyberkers.solarduino.ui

import com.abbyberkers.solarduino.HttpClient

class MoveUpButton(private val upButton: AccessibilityImageButton) {

    fun initialise(httpClient: HttpClient) {
        // When touched, start moving panels.
        upButton.downAction = { httpClient.movePanelsUp() }
        // When released, stop them.
        upButton.upAction = { httpClient.stopPanels() }
    }
}