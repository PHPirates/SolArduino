package com.abbyberkers.solarduino.communication

import android.widget.Toast
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.*

class HttpRequestHandler {


    /** Remember currently executing job and type. */
    private var currentJob: Job? = null
    private var currentJobType: RequestType? = null


    /**
     * Send a http request with the given parameters.
     *
     * Will cancel a running request if it is of a different type.
     *
     * @param jobType: Type of the request.
     * @param parameters: Http parameters in the form of ?key1=value1&key2=value2
     * @param updateFunction: Function to execute when the http response is received.
     */
    fun sendRequest(jobType: RequestType, parameters: String = "", updateFunction: (response: HttpResponse) -> Unit = {}) {
        // If there already is a job of the same type running, do not submit a second one
        if (currentJobType != jobType || currentJob?.isActive == false) {
            // Canceling any active job will be done in a coroutine as well to avoid hanging
            CoroutineScope(Dispatchers.IO).launch {
                // Cancel active job
                if (currentJob != null && currentJob!!.isActive) {
                    currentJob!!.cancelAndJoin()
                }
                currentJob = startGetRequest(parameters, updateFunction)
                currentJobType = jobType
            }
        }
    }

    /**
     * Start a get request with the given parameters.
     * todo timeouts
     * todo retry in case of certain failures
     * todo error throwing/handling
     * todo https://www.twilio.com/engineering/2013/11/04/http-client
     * todo https://github.com/kevinburke/hamms
     *
     * @return The coroutine job.
     */
    private fun startGetRequest(parameters: String = "", updateFunction: (response: HttpResponse) -> Unit = {}): Job {
        // Send http requests in a coroutine
        return CoroutineScope(Dispatchers.IO).launch {
            val client = HttpClient(Android) {
                install(JsonFeature) {
                    serializer = GsonSerializer()
                }
                engine {
                    connectTimeout = 10_000  // milliseconds
                    socketTimeout = 10_000
                }
            }
            // Tests have shown that this call is cancellable with job.cancelAndJoin()
            val resultString = client.get<String>("http://192.168.8.42:8080/$parameters")
            val response: HttpResponse = Gson().fromJson(resultString, HttpResponse::class.java)
            updateFunction(response)
            client.close()
        }
    }

}