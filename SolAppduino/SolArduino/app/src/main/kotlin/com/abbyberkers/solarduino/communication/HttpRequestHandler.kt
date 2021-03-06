package com.abbyberkers.solarduino.communication

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import com.abbyberkers.solarduino.ui.components.CurrentAngleView
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.*
import java.net.ConnectException
import java.net.SocketTimeoutException


class HttpRequestHandler(private val progressBar: ProgressBar, private val currentAngleView: CurrentAngleView, private val autoCheckBox: CheckBox) {


    /** Remember currently executing job and type. */
    private var currentJob: Job? = null
    private var currentJobType: RequestType? = null

    /** Keep track of whether a toast is shown or not. */
    private var currentToast: Toast? = null


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
        Handler(Looper.getMainLooper()).post(Runnable {
            progressBar.visibility = View.VISIBLE
        })

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
     * todo #98:
     * todo timeouts
     * todo retry in case of certain failures
     * todo error throwing/handling
     * todo https://www.twilio.com/engineering/2013/11/04/http-client
     * todo https://github.com/kevinburke/hamms
     *
     * @return The coroutine job.
     */
    private fun startGetRequest(parameters: String = "", updateFunction: (response: HttpResponse) -> Unit = {}): Job {
        Log.w("request", "$parameters ")
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

            var resultString: String? = null
            run {
                // Try to get a response a couple of times
                repeat(3) {
                    resultString = doRequest(client, parameters)
                    if (resultString != null) {
                        return@run
                    }
                }
            }
            if (resultString == null) {
                // Give up.
                Handler(Looper.getMainLooper()).post(Runnable {
                    progressBar.visibility = View.INVISIBLE
                    currentToast?.cancel()
                    currentToast = Toast.makeText(progressBar.context, "Could not connect to Pi", Toast.LENGTH_SHORT)
                    currentToast!!.show()
                })
                return@launch
            } else {
                val response: HttpResponse = Gson().fromJson(resultString!!, HttpResponse::class.java)
                client.close()
                // Run in ui thread
                Handler(Looper.getMainLooper()).post(Runnable {
                    // Always update angle, for any request
                    currentAngleView.angle = response.angle
                    autoCheckBox.isChecked = response.auto_mode
                    if (response.emergency) {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(progressBar.context, response.message, Toast.LENGTH_SHORT)
                        currentToast!!.show()
                    }
                    updateFunction(response)
                    progressBar.visibility = View.INVISIBLE
                })
            }
        }
    }

    /**
     * Try a http request.
     *
     * @return Response or null in case request didn't reach the server.
     */
    private suspend fun doRequest(client: HttpClient, parameters: String): String? {
        return try {
            client.get<String>("http://192.168.8.42:8080/$parameters")
        } catch (e: SocketTimeoutException) {
            return null
        } catch (e: ConnectException) {
            return null
        }
    }

}