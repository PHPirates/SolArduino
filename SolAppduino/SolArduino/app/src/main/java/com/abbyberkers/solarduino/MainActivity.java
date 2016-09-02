package com.abbyberkers.solarduino;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.AbstractSequentialList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    String urlString;
    String ipString = "http://192.168.2.106/"; // IP Thomas
//    String ipString = "http://192.168.0.23/"; // IP Abby
    String host = "192.168.2.106"; // host Thomas
//    String host = "192.168.0.23"; // host Abby
//    String ipString = "http://192.168.2.107"; // IP test
//    String host = "192.168.2.107"; // host test

    String toast;           // String containing the result from the last http-request
    String autoMode;        // String with auto if auto mode on, manual if auto mode off

    Toast unreachableToast;
    Toast updateToast;
    Toast updatedToast;

    int delay = 900;        // delay for the Timer/TimerTask

    boolean reachable;      // boolean to know whether the Arduino is reachable or not

    Timer downTimer;
    Timer upTimer;

//    TextView textView;
    TextView currentAngle;  // show current angle of solar panels
//    TextView responseTV;    // show response from arduino

    ImageView imageView;    // image of solar panels

    SeekBar seekbar;        // seekbar to change angle of solar panels
    int seekbarProgress = 5;

    ImageButton upButton;   // button to make the solar panels move up
    ImageButton downButton; // button to make the solar panels move down
    Button setAngle;        // button to set angle of solar panels

    CheckBox autoBox;

    FrameLayout frameLayout;

    @Override
    public void onResume(){
        super.onResume();
        urlString = ipString + "?update";
        startHttpRequest();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        textView = (TextView) findViewById(R.id.textView);
        currentAngle = (TextView) findViewById(R.id.currentAngle);
//        responseTV = (TextView) findViewById(R.id.response);

        seekbar = (SeekBar) findViewById(R.id.seekBar);

        seekbar.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        // set the height of the framelayout, depends on the height of the seekbar (in px)
                        // height of the seekbar is different for every device(/API?)...
                        int seekBarHeight = seekbar.getMeasuredWidth(); // get height of seekbar
                        // get layout parameters of framelayout
                        ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                        params.height = seekBarHeight + 15; // set height of framelayout
                        return true;
                    }
                });

        imageView = (ImageView) findViewById(R.id.linePanel);
        // set the right point of the solar panel as turning axis
        imageView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        int finalWidth = imageView.getMeasuredWidth();
                        imageView.setPivotX(finalWidth);
                        return true;
                    }
                });

        upButton = (ImageButton) findViewById(R.id.upButton);
        downButton = (ImageButton) findViewById(R.id.downButton);
        setAngle = (Button) findViewById(R.id.setAngle);

        autoBox = (CheckBox) findViewById(R.id.autoBox);
        autoBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    urlString = ipString + "?panel=auto";
//                    Toast.makeText(getBaseContext(), "Auto mode switched on.", Toast.LENGTH_SHORT).show();
                    startHttpRequest();
                } else {
                    urlString = ipString + "?panel=manual";
                    startHttpRequest();
//                    Toast.makeText(getBaseContext(), "Auto mode switched off.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        frameLayout = (FrameLayout) findViewById(R.id.frame);
//        int height = frameLayout.getLayoutParams().height;
//        Log.e("height", String.valueOf(height));

        // seekbar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                // do something when the progress of the seekbar is changing ("live")
                String angle = "Set angle at " + (i+5) + " \u00b0";
                seekbarProgress = seekbar.getProgress() + 5;
                setAngle.setText(angle); // set the text "Set angle at xx" on the button

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do something when the user starts changing the progress of the seekbar

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do something when the user stops changing the progress of the seekbar

            }
        });

        // set OnTouchListener for the up button, send http request when button pressed and released
        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        upButton.setPressed(true); // set pressed state true so colour changes
                        sendDirectionRequest("up");

                        upTimer = new Timer(); // timer to schedule the update requests
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                sendUpdateRequest();
                            }
                        };

                        // wait 1500ms with first task, then delay interval
                        upTimer.schedule(timerTask, 1500, delay);
                        view.performClick();
                        break;
                    case MotionEvent.ACTION_UP:
//                        textView.setText("Up button released.");
                        upButton.setPressed(false); // set pressed state false so colour changes back to default
                        upTimer.cancel(); // stop the update requests being sent
                        sendDirectionRequest("stop");
                        break;
                }
                return false;
            }
        });

        // set OnTouchListener for the down button, send http request when button pressed and released
        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        downButton.setPressed(true); // set pressed state true to change colour
                        sendDirectionRequest("down");

                        downTimer = new Timer(); // timer to schedule the update requests
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                sendUpdateRequest();
                            }
                        };
                        // wait 1500ms with first task, then delay interval
                        downTimer.schedule(timerTask, 1500, delay);
                        view.performClick();
                        break;

                    case MotionEvent.ACTION_UP:
//                        textView.setText("Down button released.");
                        downButton.setPressed(false); // set pressed state false to change colour to default
                        downTimer.cancel(); // stop the update request being sent
                        sendDirectionRequest("stop");
                        break;
                }
                return false;
            }
        });

        setAngle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        view.setPressed(true); // simulate onClick (pressed) event so colour changes

                        Log.e("setAngle", "pressed");
//                        int prog = seekbar.getProgress(); // get the value from the seekbar
                        sendAngleRequest(seekbarProgress); // set the panels at angle
                        view.performClick();
                        break;

                    case MotionEvent.ACTION_UP:
                        view.setPressed(false); // simulate onClick (release) event so colour changes back

                        Log.e("setAngle", "released");

                        // a Timer and TimerTask to repeat the update request with interval 'delay',
                        // until the solar panels reached the angle that is on the seekbar at that moment
                        final Timer timer = new Timer();
                        final TimerTask task = new TimerTask() {

                            // String containing the current angle, taken from
                            // (TextView) currentAngle, minus the degree symbol, trimmed to remove
                            // possible spaces ("9 \u00b0" -> "9")
                            String angleString = (currentAngle.getText().toString())
                                    .substring(0,2)
                                    .trim();

                            int angleInt = Integer.valueOf(angleString);
//                            int seekBarProgress = seekbar.getProgress(); // value seekbar
                            @Override
                            public void run() {
//                                Log.e("angles", "current " + angleInt + " seekbar " + seekBarProgress);

                                if(angleInt == seekbarProgress) {
//                                if(Math.abs(angleInt - seekBarProgress) == 1) {

                                    // cancel the timer when right angle is reached
//                                    Log.w("timer", "cancel");
                                    timer.cancel();
                                } else {
                                    // update the angle as long as the desired angle not reached

                                    // get current angle from TextView
                                    angleString = (currentAngle.getText().toString())
                                            .substring(0, 2) // get the first 2 chars of string
                                            .trim(); // trim spaces off

                                    angleInt = Integer.valueOf(angleString);
                                    seekbarProgress = seekbar.getProgress() + 5;

                                    sendUpdateRequest();
                                }

                            }
                        };

                        timer.schedule(task, delay, delay); // send update request if necessary
                        // send first after 'delay' ms, then with 'delay' interval
                        break;
                }
                return false;
            }
        });
    }

    /**
     * method to uncheck checkbox for automode, when any of the other buttons is pressed
     * @param view
     */
    public void unCheck(View view){
        if(autoBox.isChecked()){
            autoBox.toggle();
        }
    }

    // fatal exception when this is removed...
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){
        return false;
    }

    /**
     * set the angle of the imageView
     *
     * @param i the angle at which to set the solar panel
     */
    public void rotate(int i){
        imageView.setRotation(i);
    }

    /**
     * send a http request to the arduino, to move panels up and down
     * @param direction up, down, or stop (and auto?)
     */
    public void sendDirectionRequest(String direction) {
        urlString = ipString + "?panel=" + direction;
//        String urlString = "http://www.google.com";
//        urlString = "http://pannenkoekenwagen.nl/pkw/test.html";
        startHttpRequest(); //urlString will be used here

    }

    /**
     * First the Arduino is pinged, if it doesn't succeed within two seconds
     * a toast will be shown, if it does, the http request is started.
     * The ping happens in a seperated thread, because if the Arduino is not
     * reachable, the code will hang there, and otherwise it would
     * hang the UI thread and crash the app
     */
    public void startHttpRequest() {
        final SendPingTask sendPing = new SendPingTask();
        sendPing.execute(host);
        //start a new handler that will cancel the AsyncTask after 2 seconds
        //in case the Arduino can't be reached
        Log.e("handler","starting up handler");
        Handler handler = new Handler(Looper.getMainLooper()); //make sure to start from UI thread
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sendPing.getStatus() == AsyncTask.Status.RUNNING) {
                    sendPing.cancel(true);
                    unreachableToast = Toast.makeText(getBaseContext(),"The Arduino could not be reached.",Toast.LENGTH_SHORT);
                    unreachableToast.show();
                    toast = "Arduino not reachable"; //update http request return string
                }

            }
        }, 2000);
    }

    /**
     * send a http request to the arduino, to set solar panels at specified angle
     * @param angle
     */
    public void sendAngleRequest(int angle){
        Toast.makeText(getBaseContext(), "Angle set at " + angle + "\u00b0", Toast.LENGTH_SHORT).show();
//        String urlString;
        if(angle < 10){ // make sure the url is always the same length, no matter what degree
            urlString = ipString + "?degrees=0" + String.valueOf(angle);
        } else {
            urlString = ipString + "?degrees=" + String.valueOf(angle);
        }
//        urlString = "http://pannenkoekenwagen.nl/pkw/test.html";
        startHttpRequest();
    }

    /**
     * Triggered by pressing/tapping the currentAngle TextView.
     * Send an update http-request to the Arduino
     * @param view
     */
    public void sendUpdateRequest(View view) {
        if(updateToast == null) {
            if(updatedToast != null) {
                updatedToast.cancel();
            }
            updateToast = Toast.makeText(getBaseContext(), "Updating...", Toast.LENGTH_SHORT);
        }
        updateToast.show();
        urlString = ipString + "?update";
        startHttpRequest();
    }

    /**
     * Triggered by loops in which the angle needs to be frequently updated.
     *      Button up/down pressed.
     *      When the angle is set at a certain degree, and moving towards there.
     *      TODO when the panels are moving after auto mode has been enabled?
     * Send an update http-request to the Arduino.
     */
    public void sendUpdateRequest() {
        if(toast.contains("Un") || toast.contains("not")) {
            // if the previous request returns that the Arduino is not available or unreachable,
            // don't send another request
            return;
        }
        urlString = ipString + "?update";
        startHttpRequest();
    }

    /**
     * Class to execute ping request to check the connection with the Arduino,
     * before http request is sent
     */

    private class SendPingTask extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... url){ //TODO why does this need varargs?
            try{
                Log.e("sendreq","starting ping");
                // try to ping the Arduino first to find out if it's reachable or not.
                String s;
                ProcessBuilder processbuilder = new ProcessBuilder("/system/bin/ping", url[0]);
                Process process = processbuilder.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Log.e("sr","before loop");
                reachable = false;
                while ((s = stdInput.readLine()) != null)
                {
                    Log.e("output", s);
                    if(s.contains("seq=")){
//                        Log.e("ping", "first");
                        if (s.contains("Host Unreachable")) {
                            Log.e("sendreq","ping failed"); //TODO is this ever reached?
                        } else {
                            reachable = true;
                        }
                        break;
                    }
                }
                Log.e("sr","after loop ");


            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(reachable) {
                super.onPostExecute(result);
                new SendRequest().execute(urlString);
            } else {
                if(unreachableToast != null) {
                    unreachableToast.setText("Arduino could not be reached.");
                } else {
                    unreachableToast = Toast.makeText(getBaseContext(), "Arduino could not be reached.", Toast.LENGTH_SHORT);
                }
                unreachableToast.show();
            }

        }
    }

    /**
     * http requests happen here in a seperate thread
     */
    private class SendRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url){
                // try to send request when the Arduino is reachable.
                try {
                    return sendRequest(url[0]);
                } catch (Exception e) {
                    return "Arduino reachable, but unable to retrieve webpage.";
                }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("result", result);
            toast = result.substring(0, 2).trim();
//            Log.w("result", toast);
//            toast = result; // set toast to be the latest result.
//            String color = "#" + String.valueOf(Integer.toHexString(defaultColor));
//            currentAngle.setTextColor(Color.parseColor(color));
            if(result.contains("Un") || result.contains("not be")){
                // Arduino could not be reached.

                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                // set seekbar at current angle, so update requests aren't sent anymore.
                seekbar.setProgress(Integer.valueOf((currentAngle.getText().toString())
                        .substring(0, 2)
                        .trim()));
                Log.w("internet", "not connected");
            } else if(result.contains("Panel")){
                Toast.makeText(getBaseContext(), result.trim(), Toast.LENGTH_SHORT).show();
            } else {
                String message = urlString;
                if (message.equals(ipString)) {
                } else if (message.contains("panel")) {
                    // Toast that the panels are going up or down so the user knows the Arduino
                    // received the request and knows what to do
                    Toast.makeText(getBaseContext(), Html.fromHtml(result), Toast.LENGTH_SHORT).show();
                } else if (message.contains("degrees")) {
//                    Log.e("result", result);
                } else if (message.contains("update")) {
                    String[] updateString = result.split(" ");
                    // string with current angle plus degree symbol
                    String angle = Html.fromHtml(updateString[0]) + "\u00b0";
                    currentAngle.setText(angle);
                    if(updateToast != null) {
                        if(updatedToast != null) {
                            updatedToast.cancel();
                        }
                        updateToast.cancel();
                        updatedToast = Toast.makeText(getBaseContext(), "Updated.", Toast.LENGTH_SHORT);
                        updatedToast.show();
                    }

                    // get the current angle from the result, without spaces
                    result = result.substring(0, 2).trim();
                    // convert string to integer, then rotate the image
                    rotate(Integer.valueOf(updateString[0]));

                    updateString[1].trim();
                    Log.e("update", updateString[1]);
                    Log.e("check", String.valueOf(autoBox.isChecked()));
                    Log.e("update", String.valueOf(updateString[1].contains("manual")));

                    if(updateString[1].contains("auto")) {
                        if(!autoBox.isChecked()) {
                            autoBox.toggle();
                        }
                    } else if(updateString[1].contains("manual")) {
                        if(autoBox.isChecked()) {
                            autoBox.toggle();
                        }
                    }
                } else if (message.contains("Page")) {
                    Toast.makeText(getBaseContext(), "Page not found.", Toast.LENGTH_SHORT).show();
                } else {
                    // Back up, don't know what happened when we arrive here, should never happen.
                    // But has happened in the past.
//                    Log.e("message", message);
                    Toast.makeText(getBaseContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private String sendRequest(String myURL) {
            String message = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(myURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();
                int response = urlConnection.getResponseCode();
                Log.e("url", "The response is: " + response);

                InputStream in = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(in));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                message = stringBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
//                Toast.makeText(getBaseContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return message;
        }


    }
}

