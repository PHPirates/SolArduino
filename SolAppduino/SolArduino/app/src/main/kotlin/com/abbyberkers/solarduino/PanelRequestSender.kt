package com.abbyberkers.solarduino

/**
 * todo In case any action is done which is manual control, uncheck auto checkbox
 * todo option to enable logging to see all http requests and responses
 */
class PanelRequestSender {

    val handler = HttpRequestHandler()

    fun requestUpdate() {
        handler.sendRequest()
    }

    /**
     * todo update all places where defaultMinAngle/MaxAngle is used
     */
    fun requestMinMaxAngle() {}

    fun enableAutoMode() {}

    fun disableAutoMode() {}

    fun movePanelsUp() {}

    fun movePanelsDown() {}

    fun stopPanels() {}

    fun movePanelsToAngle(angle: Int) {}

}