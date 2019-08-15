package com.abbyberkers.solarduino

import android.content.Context
import android.widget.Toast

/**
 * Display a toast.
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}