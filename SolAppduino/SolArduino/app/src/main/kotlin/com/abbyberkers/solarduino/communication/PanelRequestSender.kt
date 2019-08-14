package com.abbyberkers.solarduino.communication

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

/**
 *
 * only one instance of this class should exist
 * todo In case any action is done which is manual control, uncheck auto checkbox
 * todo option to enable logging to see all http requests and responses
 * todo be smart about when to retry: always send latest request only, except when the same one already running etc.
 */
class PanelRequestSender {

    /** Remember currently executing job and type. */
    private var currentJob: Job? = null
    private var currentJobType: RequestType? = null

    val handler = HttpRequestHandler()

    fun requestUpdate() {
        val jobType = RequestType.UPDATE
        // If there already is a job of the same type running, do not submit a second one
        if (currentJobType != jobType || currentJob?.isActive == false) {
            // Cancel active job
            if (currentJob != null && currentJob!!.isActive) {
                currentJob!!.cancelAndJoin()
            }
            currentJob = handler.sendRequest()
            currentJobType = jobType
        }
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