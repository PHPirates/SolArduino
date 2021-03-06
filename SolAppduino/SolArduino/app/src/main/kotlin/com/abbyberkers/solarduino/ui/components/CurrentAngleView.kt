package com.abbyberkers.solarduino.ui.components

import android.content.res.Resources
import android.widget.TextView
import com.abbyberkers.solarduino.R
import com.abbyberkers.solarduino.communication.PanelRequestSender

class CurrentAngleView(private val currentAngle: TextView,
                       private val panelImage: SolarPanelImage,
                       private val resources: Resources) {
    var angle: Float = (-42).toFloat()
    set(value) {
        currentAngle.text = resources.getString(R.string.angle, value.toInt())
        panelImage.setAngle(value)
        field = value
    }

    fun initialise(httpClient: PanelRequestSender) {
        // Request an update when tapping the current angle textview.
        currentAngle.setOnClickListener {
            httpClient.requestUpdate()
        }
    }

}