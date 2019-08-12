package com.abbyberkers.solarduino

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import com.abbyberkers.solarduino.ui.AutoModeCheckBox
import com.abbyberkers.solarduino.ui.ChangeAngleButton
import com.abbyberkers.solarduino.ui.SelectAngleBar
import com.abbyberkers.solarduino.ui.SolarPanelImage

class MainActivity2 : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        HttpClient().requestUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val defaultMinAngle = 6
        val defaultMaxAngle = 56

        val httpClient = HttpClient()

        val frameLayout: FrameLayout = findViewById(R.id.frame)
        val changeAngleButton = ChangeAngleButton(findViewById(R.id.setAngle))

        // Init components
        SelectAngleBar(findViewById(R.id.seekBar)).initialise(frameLayout, changeAngleButton, defaultMinAngle, defaultMaxAngle)
        AutoModeCheckBox(findViewById(R.id.autoBox)).initialise(httpClient)
        SolarPanelImage(findViewById(R.id.linePanel)).initialise()

        HttpClient().requestMinMaxAngle()
        HttpClient().requestUpdate()
    }

}