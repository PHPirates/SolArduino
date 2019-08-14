package com.abbyberkers.solarduino.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.abbyberkers.solarduino.PanelRequestSender
import com.abbyberkers.solarduino.R
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment(private val httpClient: PanelRequestSender) : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultMinAngle = 6
        val defaultMaxAngle = 56

        val frameLayout: FrameLayout = frame

        // Init components
        val changeAngleButton = ChangeAngleButton(setAngle, resources, httpClient)
        changeAngleButton.initialise()
        AutoModeCheckBox(autoBox).initialise(httpClient)
        SolarPanelImage(linePanel).initialise()
        MoveUpButton(upButton).initialise(httpClient)
        MoveDownButton(downButton).initialise(httpClient)
        SelectAngleBar(seekBar).initialise(frameLayout, changeAngleButton, defaultMinAngle, defaultMaxAngle)

        // Request an update when tapping the current angle textview.
        currentAngle.setOnClickListener {
            httpClient.requestUpdate()
        }

        httpClient.requestMinMaxAngle()
    }
}