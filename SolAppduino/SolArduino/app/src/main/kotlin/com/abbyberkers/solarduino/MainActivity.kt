package com.abbyberkers.solarduino

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abbyberkers.solarduino.ui.HomeFragment
import com.abbyberkers.solarduino.ui.ScheduleFragment
import com.abbyberkers.solarduino.ui.replace
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.replace(R.id.fragment_container, HomeFragment())

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home_button -> supportFragmentManager.replace(R.id.fragment_container, HomeFragment())
                R.id.schedule_button -> supportFragmentManager.replace(R.id.fragment_container, ScheduleFragment())
            }
            true
        }
    }

    /**
     * When pressing back on the home page, close the app.
     * When pressing back on any other page, go to the home page.
     */
    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) is HomeFragment) {
            finish()
        } else {
            bottom_navigation.selectedItemId = R.id.home_button
        }
    }
}