package com.abbyberkers.solarduino

import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar

/**
 * A seekbar (slider) to select an angle to which the panels should go.
 */
class SelectAngleBar(val seekbar: SeekBar) {

    val panelsMinAngle = 5 // todo magic minAngle

    fun initialise(frameLayout: FrameLayout, changeAngleButton: Button) {
        setHeight(frameLayout)
        setOnChangeListener(changeAngleButton)
    }

    /**
     * Set the height of the framelayout, depends on the height of the seekbar (in px).
     * It seems like the height of the seekbar can differ between devices.
     */
    private fun setHeight(frameLayout: FrameLayout) {
        seekbar.viewTreeObserver.addOnPreDrawListener {
            frameLayout.layoutParams.height = seekbar.measuredWidth + 15
            true
        }
    }

    /**
     * When the user changes the value, the button text will be updated.
     */
    private fun setOnChangeListener(changeAngleButton: Button) {
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, initiatedByUser: Boolean) {
                // Change button text when the progress of the seekbar is changing ("live")
                // todo if anywhere, this should be located in the ChangeAngleButton class
                val angleText = "Set angle at " + getAngle() + " \u00b0"
                changeAngleButton.text = angleText

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun getAngle() = seekbar.progress + panelsMinAngle
}