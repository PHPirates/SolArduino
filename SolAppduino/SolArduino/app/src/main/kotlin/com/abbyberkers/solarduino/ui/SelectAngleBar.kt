package com.abbyberkers.solarduino.ui

import android.widget.FrameLayout
import android.widget.SeekBar

/**
 * A seekbar (slider) to select an angle to which the panels should go.
 */
class SelectAngleBar(private val seekbar: SeekBar) {


    fun initialise(frameLayout: FrameLayout,
                   changeAngleButton: ChangeAngleButton,
                   defaultMinAngle: Int,
                   defaultMaxAngle: Int) {
        setHeight(frameLayout)
        setOnChangeListener(changeAngleButton)
        seekbar.min = defaultMinAngle
        seekbar.max = defaultMaxAngle
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
    private fun setOnChangeListener(changeAngleButton: ChangeAngleButton) {
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, initiatedByUser: Boolean) {
                // Change button text when the progress of the seekbar is changing ("live")
                changeAngleButton.angle = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}