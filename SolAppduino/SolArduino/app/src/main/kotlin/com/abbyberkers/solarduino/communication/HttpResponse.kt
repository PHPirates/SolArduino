package com.abbyberkers.solarduino.communication

/**
 * Response from the webserver.
 */
data class HttpResponse(
        val emergency: Boolean,
        val angle: Float,
        val autoMode: Boolean,
        val message: String,
        val minAngle: Float,
        val maxAngle: Float
)