package com.abbyberkers.solarduino.ui.components

import android.widget.ImageView

/**
 * Schematic image of the solar panels to show the angle.
 */
class SolarPanelImage(private val imageView: ImageView) {

    fun initialise() {
        imageView.viewTreeObserver.addOnPreDrawListener {
            imageView.pivotX = imageView.measuredWidth.toFloat()
            true
        }
    }

    fun setAngle(angle: Float) {
        imageView.rotation = angle
    }
}