package com.abbyberkers.solarduino

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class httpRequestHandler {

    /**
     *
     */
    fun sendRequest(parameters: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = HttpClient(Android) {
                engine {
                    connectTimeout = 100_000
                    socketTimeout = 100_000
                }
            }
            client.get<String>("http://192.168.8.42:8080/").apply {
                Log.i("?", this)
            }
            client.close()
        }
    }

}