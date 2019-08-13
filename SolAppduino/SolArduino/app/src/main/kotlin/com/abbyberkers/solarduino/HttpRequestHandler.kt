package com.abbyberkers.solarduino

import android.util.Log
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HttpRequestHandler {

    /**
     * Send a http request with the given parameters, in the form of ?key1=value1&key2=value2
     */
    fun sendRequest(parameters: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            val client = HttpClient(Android) {
                install(JsonFeature) {
                    serializer = GsonSerializer()
                }
                engine {
                    connectTimeout = 100_000
                    socketTimeout = 100_000
                }
            }
            val resultString = client.get<String>("http://192.168.8.42:8080/$parameters")
            val response = Gson().fromJson(resultString, HttpResponse::class.java)
            Log.i("?", "${response.angle} ${response.mode}")
            client.close()
        }
    }

}