package com.abbyberkers.solarduino.communication

/**
 * Response from the webserver.
 */
data class HttpResponse(
        val emergency: Boolean,
        val angle: Float,
        val auto_mode: Boolean,
        val message: String,
        val min_angle: Float,
        val max_angle: Float
)