package com.abbyberkers.solarduino

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import com.abbyberkers.solarduino.ui.*

class MainActivity : AppCompatActivity() {

    val httpClient = PanelRequestSender()

    override fun onResume() {
        super.onResume()
        httpClient.requestUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val defaultMinAngle = 6
        val defaultMaxAngle = 56

        val frameLayout: FrameLayout = findViewById(R.id.frame)

        // Init components
        val changeAngleButton = ChangeAngleButton(findViewById(R.id.setAngle), resources, httpClient)
        changeAngleButton.initialise()
        AutoModeCheckBox(findViewById(R.id.autoBox)).initialise(httpClient)
        SolarPanelImage(findViewById(R.id.linePanel)).initialise()
        MoveUpButton(findViewById(R.id.upButton)).initialise(httpClient)
        MoveDownButton(findViewById(R.id.downButton)).initialise(httpClient)
        SelectAngleBar(findViewById(R.id.seekBar)).initialise(frameLayout, changeAngleButton, defaultMinAngle, defaultMaxAngle)

        httpClient.requestMinMaxAngle()
        httpClient.requestUpdate()
    }

    /**
     * Triggered by pressing/tapping the currentAngle TextView.
     * Updates the current angle.
     *
     * @param view currentAngle TextView
     */
    fun sendUpdateRequest(view: View) {
        httpClient.requestUpdate()
    }
}