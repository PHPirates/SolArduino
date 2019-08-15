package com.abbyberkers.solarduino.communication

/**
 * Possible http request types.
 */
enum class RequestType {
    UPDATE,
    ANGLE_BOUNDS,
    AUTO_ENABLE,
    AUTO_DISABLE,
    PANELS_UP,
    PANELS_DOWN,
    PANELS_STOP,
    PANELS_TO_ANGLE,
}