package com.abbyberkers.solarduino.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abbyberkers.solarduino.R

class ScheduleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.schedule_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ScheduleChart(getChartData(), view.findViewById(R.id.schedule_chart), context!!)
    }

    /**
     * TODO #95: get the data from the Pi instead of our own little file located in /res/raw.
     * TODO move to PanelRequestSender?
     *
     * Gets the data for the schedule chart.
     */
    private fun getChartData(): List<SchedulePoint> {
        val rawId = resources.getIdentifier("angles", "raw", "com.abbyberkers.solarduino")
        return resources.openRawResource(rawId).bufferedReader().useLines { lines ->
            lines.map {
                val angleTime = it.split(" ")
                SchedulePoint(angle = angleTime.first().toFloat(),
                        time = angleTime.last().toFloat())
            }.toList()
        }
    }
}