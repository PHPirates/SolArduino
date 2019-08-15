package com.abbyberkers.solarduino.ui.schedule

import android.content.Context
import com.abbyberkers.solarduino.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

/**
 * A chart with the schedule for today's angles.
 */
class ScheduleChart(private val data: List<SchedulePoint>,
                    private val chart: LineChart,
                    private val context: Context) {

    init {
        val entries = this.data.map { Entry(it.time, it.angle) }
        val dataSet = LineDataSet(entries, "label")
        // Style the chart with the apps colors.
        dataSet.apply {
            color = context.getColor(R.color.colorAccent)
            setCircleColor(context.getColor(R.color.colorAccent))
        }
        val lineData = LineData(dataSet)

        this.chart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.valueFormatter = AxisTimeFormatter()
            invalidate()  // Refresh the chart.
        }
    }
}

