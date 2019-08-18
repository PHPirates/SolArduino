package com.abbyberkers.solarduino.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abbyberkers.solarduino.R
import com.abbyberkers.solarduino.communication.PanelRequestSender
import com.abbyberkers.solarduino.ui.components.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment(private val stopButton: FloatingActionButton) : Fragment() {

    private lateinit var httpClient: PanelRequestSender

    override fun onResume() {
        super.onResume()
        httpClient.requestUpdate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.home_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // Init components
        val panelImage = SolarPanelImage(linePanel).apply { initialise() }
        val currentAngleView = CurrentAngleView(currentAngle, panelImage, resources)
        httpClient = PanelRequestSender(progressBar, currentAngleView, autoBox)
        currentAngleView.initialise(httpClient)
        val changeAngleButton = ChangeAngleButton(setAngle, resources, httpClient).apply { initialise() }
        AutoModeCheckBox(autoBox).initialise(httpClient)
        MoveUpButton(upButton).initialise(httpClient)
        MoveDownButton(downButton).initialise(httpClient)
        val selectAngleBar = SelectAngleBar(seekBar).apply { initialise(frame, changeAngleButton) }
        stopButton.setOnClickListener { httpClient.stopPanels() }

        httpClient.requestAngleBounds(selectAngleBar)
    }
}