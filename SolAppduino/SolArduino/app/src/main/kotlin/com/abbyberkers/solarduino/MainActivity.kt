package com.abbyberkers.solarduino

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.abbyberkers.solarduino.ui.HomeFragment
import com.abbyberkers.solarduino.ui.schedule.ScheduleFragment
import com.abbyberkers.solarduino.ui.replace
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(top_toolbar)

        val homeFragment = HomeFragment(stop_button)
        val scheduleFragment = ScheduleFragment()

        supportFragmentManager.replace(R.id.fragment_container, homeFragment)

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home_button -> supportFragmentManager.replace(R.id.fragment_container, homeFragment)
                R.id.schedule_button -> supportFragmentManager.replace(R.id.fragment_container, scheduleFragment)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return true
    }
}