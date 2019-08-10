package com.abbyberkers.solarduino

import android.os.AsyncTask


/**
 * Both PingSender and RequestSender extend this task, to allow the handler
 * code to be put in a method and take a DataSender as parameter to include
 * both types of data senders.
 */
abstract class DataSender : AsyncTask<String, Void, String>()