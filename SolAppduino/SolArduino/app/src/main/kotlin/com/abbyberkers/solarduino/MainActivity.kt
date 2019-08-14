package com.abbyberkers.solarduino

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abbyberkers.solarduino.ui.HomeFragment
import com.abbyberkers.solarduino.ui.ScheduleFragment
import com.abbyberkers.solarduino.ui.replace
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val httpClient = PanelRequestSender()

    override fun onResume() {
        super.onResume()
        httpClient.requestUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.replace(R.id.fragment_container, HomeFragment(httpClient))

        bottom_navigation.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.home_button -> toast("home button clicked")
                R.id.schedule_button -> toast("schedule button clicked")
            }
            true
        }
    }

}