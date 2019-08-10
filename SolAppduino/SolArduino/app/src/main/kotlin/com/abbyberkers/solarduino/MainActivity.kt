package com.abbyberkers.solarduino

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.FrameLayout

class MainActivity2 : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        HttpClient().requestUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val frameLayout: FrameLayout = findViewById(R.id.frame)
        val changeAngleButton: Button = findViewById(R.id.setAngle)

        SelectAngleBar(findViewById(R.id.seekBar)).initialise(frameLayout, changeAngleButton)


        HttpClient().requestUpdate()
    }

}