package com.abbyberkers.solarduino

/**
 * todo In case any action is done which is manual control, uncheck auto checkbox
 */
class HttpClient {

    fun requestUpdate() {}

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