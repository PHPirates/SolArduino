package com.abbyberkers.solarduino.communication

/**
 * Response from the webserver.
 */
data class HttpResponse(
        val emergency: Boolean,
        val angle: Float,
        val mode: String,
        val message: String
)