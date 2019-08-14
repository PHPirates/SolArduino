package com.abbyberkers.solarduino.communication

import android.util.Log
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HttpRequestHandler {

    /**
     * Send a http request with the given parameters, in the form of ?key1=value1&key2=value2
     * todo timeouts
     * todo retry in case of certain failures
     * todo error throwing/handling
     * todo https://www.twilio.com/engineering/2013/11/04/http-client
     * todo https://github.com/kevinburke/hamms
     * todo make this cancellable https://kotlinlang.org/docs/reference/coroutines/cancellation-and-timeouts.html
     *
     * @return The coroutine job.
     */
    fun sendRequest(parameters: String = ""): Job {
        // Send http requests in a coroutine
        return CoroutineScope(Dispatchers.IO).launch {
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