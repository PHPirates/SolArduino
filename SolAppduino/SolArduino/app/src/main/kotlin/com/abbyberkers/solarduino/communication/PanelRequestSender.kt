package com.abbyberkers.solarduino.communication

import android.widget.ProgressBar
import com.abbyberkers.solarduino.ui.AutoModeCheckBox
import com.abbyberkers.solarduino.ui.CurrentAngleView
import com.abbyberkers.solarduino.ui.SelectAngleBar

/**
 *
 * only one instance of this class should exist
 * todo In case any action is done which is manual control, uncheck auto checkbox
 * todo send update periodically
 * todo option to enable logging to see all http requests and responses?
 */
class PanelRequestSender(progressBar: ProgressBar, currentAngleView: CurrentAngleView) {

    private val handler = HttpRequestHandler(progressBar, currentAngleView)

    fun requestUpdate() {
        handler.sendRequest(RequestType.UPDATE)
    }

    /**
     * Update (soft bound) min/max angle.
     */
    fun requestAngleBounds(selectAngleBar: SelectAngleBar) {
        val updateFunction: (response: HttpResponse) -> Unit = {
            selectAngleBar.seekbar.min = it.minAngle.toInt()
            selectAngleBar.seekbar.max = it.maxAngle.toInt()
        }
        handler.sendRequest(RequestType.UPDATE, updateFunction = updateFunction)
    }

    fun enableAutoMode(checkBox: AutoModeCheckBox) {
        handler.sendRequest(RequestType.AUTO_ENABLE,
                parameters = "?panel=auto",
                updateFunction = { checkBox.checkBox.isSelected = it.autoMode })
    }

    fun disableAutoMode(checkBox: AutoModeCheckBox) {
        handler.sendRequest(RequestType.AUTO_ENABLE,
                parameters = "?panel=manual",
                updateFunction = { checkBox.checkBox.isSelected = it.autoMode })
    }

    fun movePanelsUp() {
        // todo timer
    }

    fun movePanelsDown() {}

    fun stopPanels() {
        handler.sendRequest(RequestType.PANELS_STOP, "?panel=stop")
    }

    fun movePanelsToAngle(angle: Int) {
        handler.sendRequest(RequestType.PANELS_TO_ANGLE, "?degrees=$angle")
    }

}