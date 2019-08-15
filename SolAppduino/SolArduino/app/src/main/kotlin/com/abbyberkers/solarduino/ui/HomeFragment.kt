package com.abbyberkers.solarduino.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.abbyberkers.solarduino.R
import com.abbyberkers.solarduino.communication.PanelRequestSender
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment : Fragment() {

    private val httpClient = PanelRequestSender()

    /** Keep a reference to the current angle, to update it on resume. */
    private lateinit var currentAngleView: CurrentAngleView

    override fun onResume() {
        super.onResume()
        httpClient.requestUpdate(currentAngleView)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val defaultMinAngle = 6
        val defaultMaxAngle = 56

        val frameLayout: FrameLayout = frame

        // Init components
        val changeAngleButton = ChangeAngleButton(setAngle, resources, httpClient).apply { initialise() }
        AutoModeCheckBox(autoBox).initialise(httpClient)
        val panelImage = SolarPanelImage(linePanel).apply { initialise() }
        MoveUpButton(upButton).initialise(httpClient)
        MoveDownButton(downButton).initialise(httpClient)
        SelectAngleBar(seekBar).initialise(frameLayout, changeAngleButton, defaultMinAngle, defaultMaxAngle)
        currentAngleView = CurrentAngleView(currentAngle, panelImage, httpClient, resources).apply { initialise() }

        httpClient.requestAngleBounds()
    }
}