package com.abbyberkers.solarduino.communication

import com.abbyberkers.solarduino.ui.CurrentAngleView

/**
 *
 * only one instance of this class should exist
 * todo In case any action is done which is manual control, uncheck auto checkbox
 * todo option to enable logging to see all http requests and responses
 * todo be smart about when to retry: always send latest request only, except when the same one already running etc.
 * todo progressindicator
 */
class PanelRequestSender {

    private val handler = HttpRequestHandler()

    fun requestUpdate(currentAngleView: CurrentAngleView) {
        handler.sendRequest(RequestType.UPDATE, updateFunction = { currentAngleView.angle = it.angle })
    }

    /**
     * todo update all places where defaultMinAngle/MaxAngle is used
     *
     * update (soft) min/max angle
     */
    fun requestAngleBounds() {}

    fun enableAutoMode() {}

    fun disableAutoMode() {}

    fun movePanelsUp() {}

    fun movePanelsDown() {}

    fun stopPanels() {}

    fun movePanelsToAngle(angle: Int) {}

}