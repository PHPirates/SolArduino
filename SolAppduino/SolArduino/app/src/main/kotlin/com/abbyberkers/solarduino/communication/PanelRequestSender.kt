package com.abbyberkers.solarduino.communication

import android.widget.ProgressBar
import com.abbyberkers.solarduino.ui.CurrentAngleView

/**
 *
 * only one instance of this class should exist
 * todo progressindicator
 * todo In case any action is done which is manual control, uncheck auto checkbox
 * todo send update periodically
 * todo option to enable logging to see all http requests and responses?
 */
class PanelRequestSender(private val progressBar: ProgressBar) {

    private val handler = HttpRequestHandler(progressBar)

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