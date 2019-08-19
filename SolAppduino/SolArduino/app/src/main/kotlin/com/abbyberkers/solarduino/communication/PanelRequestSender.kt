package com.abbyberkers.solarduino.communication

import android.widget.CheckBox
import android.widget.ProgressBar
import com.abbyberkers.solarduino.ui.components.AutoModeCheckBox
import com.abbyberkers.solarduino.ui.components.CurrentAngleView
import com.abbyberkers.solarduino.ui.components.SelectAngleBar

/**
 * Send requests to the panels.
 * Only one instance of this class should exist.
 */
class PanelRequestSender(progressBar: ProgressBar, currentAngleView: CurrentAngleView, autoCheckBox: CheckBox) {

    private val handler = HttpRequestHandler(progressBar, currentAngleView, autoCheckBox)

    fun requestUpdate() {
        handler.sendRequest(RequestType.UPDATE)
    }

    /**
     * Update (soft bound) min/max angle.
     */
    fun requestAngleBounds(selectAngleBar: SelectAngleBar) {
        val updateFunction: (response: HttpResponse) -> Unit = {
            selectAngleBar.seekbar.min = it.min_angle.toInt()
            selectAngleBar.seekbar.max = it.max_angle.toInt()
        }
        handler.sendRequest(RequestType.UPDATE, updateFunction = updateFunction)
    }

    fun enableAutoMode(checkBox: AutoModeCheckBox) {
        handler.sendRequest(RequestType.AUTO_ENABLE,
                parameters = "?panel=auto",
                updateFunction = { checkBox.checkBox.isChecked = it.auto_mode })
    }

    fun disableAutoMode(checkBox: AutoModeCheckBox) {
        handler.sendRequest(RequestType.AUTO_ENABLE,
                parameters = "?panel=manual",
                updateFunction = { checkBox.checkBox.isChecked = it.auto_mode })
    }

    fun movePanelsUp() {
        handler.sendRequest(RequestType.PANELS_UP, "?panel=up")
    }

    fun movePanelsDown() {
        handler.sendRequest(RequestType.PANELS_DOWN, "?panel=down")
    }

    fun stopPanels() {
        handler.sendRequest(RequestType.PANELS_STOP, "?panel=stop")
    }

    fun movePanelsToAngle(angle: Int) {
        handler.sendRequest(RequestType.PANELS_TO_ANGLE, "?degrees=$angle")
    }

}