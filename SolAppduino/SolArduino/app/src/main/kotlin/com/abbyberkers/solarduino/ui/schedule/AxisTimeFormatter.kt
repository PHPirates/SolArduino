package com.abbyberkers.solarduino.ui.schedule

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formatter for axis values.
 */
class AxisTimeFormatter : ValueFormatter() {
    /**
     * Formats a value given in seconds since 01-01-1970 as HH:mm.
     */
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(value * 1000)
    }
}