package com.abbyberkers.solarduino;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.winsontan520.wversionmanager.library.WVersionManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    String urlString;
    String ipString = "http://192.168.8.42:8080/";
    String host = "192.168.8.42";

    String lastResult;           // String containing the result from the last http-request

    Toast unreachableToast;
    Toast updateToast;
    Toast updatedToast;
    Toast responseToast;

    int delay = 1000;        // arduino timeout is 2000 millis, so ask every second
    int handlerTimeout = 2000; //timeout for killing a request or ping

    boolean reachable;      // boolean to know whether the Arduino is reachable or not

    Timer downTimer;
    Timer upTimer;
    Timer autoTimer;

    TextView currentAngle;  // show current angle of solar panels

    ImageView imageView;    // image of solar panels

    SeekBar seekbar;        // seekbar to change angle of solar panels
    int seekbarProgress = 5;

    ImageButton upButton;   // button to make the solar panels move up
    ImageButton downButton; // button to make the solar panels move down
    Button setAngle;        // button to set angle of solar panels

    CheckBox autoBox;

    FrameLayout frameLayout;

    @Override
    public void onResume() {
        super.onResume();
        urlString = ipString + "?update";
        startHttpRequest();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForUpdates();

        currentAngle = findViewById(R.id.currentAngle);

        seekbar = findViewById(R.id.seekBar);

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

        imageView = findViewById(R.id.linePanel);
        // set the right point of the solar panel as turning axis
        imageView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        int finalWidth = imageView.getMeasuredWidth();
                        imageView.setPivotX(finalWidth);
                        return true;
                    }
                });

        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        setAngle = findViewById(R.id.setAngle);

        autoBox = findViewById(R.id.autoBox);
        //clicklistener instead of OnCheckedChange won't register sliding a switch
        //but this way when toggling the button from somewhere else the clicklistener isn't called
        autoBox.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoBox.isChecked()) {
                    urlString = ipString + "?panel=auto";
//                    Toast.makeText(getBaseContext(), "Auto mode switched on.", Toast.LENGTH_SHORT).show();
                    startHttpRequest();
                    autoTimer = new Timer(); // timer to schedule the update requests
                    //We assume that when timerTask runs the first time, the response with degrees is received
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            int current = getCurrentDegree();
                            //get degrees aimed at from lastResult
                            int next = current;
                            try {
                                next = getNextDegree(lastResult);
                            } catch (IndexOutOfBoundsException e) {
                                //this happens when lastResult is for example "panels above upper bound!"
                                Log.e("onCreate", "finding integer in " + lastResult + " failed");
                            }
                            if (next != current) {//get degrees displayed
                                sendUpdateRequest();
                            } else {
                                autoTimer.cancel();
                            }
                        }
                    };

                    // wait 1500ms with first task, then delay interval
                    autoTimer.schedule(timerTask, 1500, delay);
                } else {
                    urlString = ipString + "?panel=manual";
                    startHttpRequest();
//                    Toast.makeText(getBaseContext(), "Auto mode switched off.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        frameLayout = findViewById(R.id.frame);

        // seekbar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                // do something when the progress of the seekbar is changing ("live")
                String angle = "Set angle at " + (i + 5) + " \u00b0";
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
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        upButton.setPressed(true); // set pressed state true so colour changes
                        sendDirectionRequest("up");

                        upTimer = new Timer(); // timer to schedule the update requests
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                sendDirectionRequest("up"); // also send up before arduino times out
                                sendUpdateRequest();
                            }
                        };

                        // wait delay with first task, then delay interval
                        upTimer.schedule(timerTask, delay, delay);
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
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        downButton.setPressed(true); // set pressed state true to change colour
                        sendDirectionRequest("down");

                        downTimer = new Timer(); // timer to schedule the update requests
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                sendDirectionRequest("down"); //arduino expects request every [delay]ms
                                sendUpdateRequest();
                            }
                        };
                        // wait delay with first task, then delay interval
                        downTimer.schedule(timerTask, delay, delay);
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
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setPressed(true); // simulate onClick (pressed) event so colour changes

                        sendAngleRequest(seekbarProgress); // set the panels at angle
                        view.performClick();
                        break;

                    case MotionEvent.ACTION_UP:
                        view.setPressed(false); // simulate onClick (release) event so colour changes back

                        // a Timer and TimerTask to repeat the update request with interval 'delay',
                        // until the solar panels reached the angle that is on the seekbar at that moment
                        final Timer timer = new Timer();
                        final TimerTask task = new TimerTask() {

                            int angleInt = getCurrentDegree();

                            //                            int seekBarProgress = seekbar.getProgress(); // value seekbar
                            @Override
                            public void run() {
//                                Log.e("angles", "current " + angleInt + " seekbar " + seekBarProgress);

                                if (angleInt == seekbarProgress) {
//                                if(Math.abs(angleInt - seekBarProgress) == 1) {

                                    // cancel the timer when right angle is reached
//                                    Log.w("timer", "cancel");
                                    timer.cancel();
                                } else {
                                    // update the angle as long as the desired angle not reached
                                    angleInt = getCurrentDegree();
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
     * check for updates
     */
    public void checkForUpdates() {
        WVersionManager versionManager = new WVersionManager(this);
        versionManager.setVersionContentUrl("https://github.com/PHPirates/SolArduino/raw/master/solappduino/version.json");
        //v1.2:
//        versionManager.setVersionContentUrl("https://raw.githubusercontent.com/PHPirates/SolArduino/a3ca1759042450bff63827a04e01206947013922/solappduino/version.json");
        versionManager.setUpdateUrl("https://github.com/PHPirates/SolArduino/raw/master/SolAppduino/SolArduino/app/SolArduino-release1.3.apk");
        versionManager.checkVersion();
    }

    /**
     * Parses result string to find degree the panels go to
     *
     * @param result from http request, set by sendRequest
     * @return degrees
     */
    public int getNextDegree(String result) {
        String intString = result.substring(result.indexOf("_") + 1, result.lastIndexOf("_"));
        return Integer.parseInt(intString);
    }

    /**
     * @return degree currently displayed in the textview currentAngle
     */
    public int getCurrentDegree() {
        String angleString = currentAngle.getText().toString();

        if (angleString.length() < 3) {
            return Integer.parseInt(angleString.substring(0, 1)); //if only one digit
        } else if (angleString.length() == 3) {
            return Integer.parseInt(angleString.substring(0, 2));
        } else { //there must be a character like < or > in front
            return Integer.parseInt(angleString.substring(1, 3));
        }
    }

    /**
     * method to uncheck checkbox for automode, when any of the other buttons is pressed
     *
     * @param view checkbox
     */
    public void unCheck(View view) {
        if (autoBox.isChecked()) {
            autoBox.toggle();
        }
    }

    // fatal exception when this is removed...
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    /**
     * set the angle of the imageView
     *
     * @param i the angle at which to set the solar panel
     */
    public void rotate(int i) {
        imageView.setRotation(i);
    }

    /**
     * send a http request to the arduino, to move panels up and down
     *
     * @param direction up, down, or stop (and auto?)
     */
    public void sendDirectionRequest(String direction) {
        urlString = ipString + "?panel=" + direction;
        startHttpRequest(); //urlString will be used here

    }

    /**
     * Starts a http request
     */
    public void startHttpRequest() {
        final DataSender requestSender = new RequestSender();
        requestSender.execute(urlString);
        startHandler(requestSender);
    }

    /**
     * First the Arduino is pinged, if it doesn't succeed within two seconds
     * a toast will be shown, if it does, the http request is started.
     * The ping happens in a seperated thread, because if the Arduino is not
     * reachable, the code will hang there, and otherwise it would
     * hang the UI thread and crash the app
     */
    public void startPingRequest() {
        final DataSender sendPing = new PingSender();
        sendPing.execute(host);
        startHandler(sendPing);
    }

    /**
     * Starts handler to handle a dataSender and kill it after x seconds
     *
     * @param dataSender DataSender object, for example PingSender or RequestSender
     */
    public void startHandler(final DataSender dataSender) {
        //start a new handler that will cancel the AsyncTask after x seconds
        //in case the Arduino can't be reached
        Handler handler = new Handler(Looper.getMainLooper()); //make sure to start from UI thread
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dataSender.getStatus() == AsyncTask.Status.RUNNING) {
                    dataSender.cancel(true);
                    if (unreachableToast != null) unreachableToast.cancel();
                    unreachableToast = Toast.makeText(getBaseContext(), "The Arduino could not be reached, request terminated.", Toast.LENGTH_SHORT);
                    unreachableToast.show();
                    lastResult = "Arduino not reachable"; //update http request return string
                }

            }
        }, handlerTimeout);
    }

    /**
     * send a http request to the arduino, to set solar panels at specified angle
     *
     * @param angle to set solar panels on
     */
    public void sendAngleRequest(int angle) {
//        String urlString;
        if (angle < 10) { // make sure the url is always the same length, no matter what degree
            urlString = ipString + "?degrees=0" + String.valueOf(angle);
        } else {
            urlString = ipString + "?degrees=" + String.valueOf(angle);
        }
        startHttpRequest();
    }

    /**
     * Triggered by pressing/tapping the currentAngle TextView.
     * Send an update http-request to the Arduino
     *
     * @param view currentAngle TextView
     */
    public void sendUpdateRequest(View view) {
        if (updateToast == null) {
            if (updatedToast != null) {
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
     * Button up/down pressed.
     * When the angle is set at a certain degree, and moving towards there.
     * When the panels are moving after auto mode has been enabled
     * Send an update http-request to the Arduino.
     */
    public void sendUpdateRequest() {
        if (lastResult.contains("Un") || lastResult.contains("not")) {
            // if the previous request returns that the Arduino is not available or unreachable,
            // don't send another request
            return;
        }
        urlString = ipString + "?update";
        startHttpRequest();
    }

    /**
     * Both PingSender and RequestSender extend this task, to allow the handler
     * code to be put in a method and take a DataSender as parameter to include
     * both types of data senders.
     */
    private abstract class DataSender extends AsyncTask<String, Void, String> {
    }

    /**
     * Class to execute ping request to check the connection with the Arduino,
     * before http request is sent
     */

    private class PingSender extends DataSender {
        @Override
        /**
         * using varargs, String...url instead of (String[] url) could be useful when calling,
         * so you can use method("a","b") instead of method(new String{"a","b"})
         */
        protected String doInBackground(String... url) {
            try {
                // try to ping the Arduino first to find out if it's reachable or not.
                String s;
                ProcessBuilder processbuilder = new ProcessBuilder("/system/bin/ping", url[0]);
                Process process = processbuilder.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                reachable = false;
                while ((s = stdInput.readLine()) != null) {
                    if (s.contains("seq=")) {
//                        Log.e("ping", "first");
                        reachable = !s.contains("Host Unreachable");
                        break;
                    }
                }
                process.destroy(); //also terminate the process when ping didn't fail
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) { //needs to be a string to extend DataSender
            if (reachable) {
                super.onPostExecute(result);
                new RequestSender().execute(urlString);
            } else {
                if (unreachableToast != null) {
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
    private class RequestSender extends DataSender {

        @Override
        protected String doInBackground(String... url) {
            // try to send request when the Arduino is reachable.
            try {
                return sendRequest(url[0]);
            } catch (Exception e) {
                return "Arduino reachable, but unable to retrieve webpage.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            lastResult = result; //set result global as well

            if (result.contains("Un") || result.contains("not be")) {
                // Arduino could not be reached.

                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                // set seekbar at current angle, so update requests aren't sent anymore.
                seekbar.setProgress(getCurrentDegree());
            } else if (result.contains("Panel")) {
                if (responseToast != null)
                    responseToast.cancel(); //cancel any previous panel movement toast
                responseToast = Toast.makeText(getBaseContext(), result.trim(), Toast.LENGTH_SHORT);
                responseToast.show();
            } else {
                String urlString = MainActivity.this.urlString;
                if (urlString.contains("panel")) {
                    // Toast that the panels are going up or down so the user knows the Arduino
                    // received the request and knows what to do
                    //todo if this is just panels up or down, why are we converting it to an integer?
                    String string;
                    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        string = Html.fromHtml(result, Html.FROM_HTML_MODE_LEGACY) + "\u00b0";
                    } else {
                        //noinspection deprecation because we caught that in the if-statement above
                        string = Html.fromHtml(result) + "\u00b0";
                    }
                    Toast.makeText(getBaseContext(), string, Toast.LENGTH_SHORT).show();
                } else if (urlString.contains("update")) {
                    String[] updateString = result.split(" ");
                    if (updateString[0].trim().length() == 0) {
                        Toast.makeText(getBaseContext(), "Arduino did not return an angle", Toast.LENGTH_SHORT).show();
                    } else {
                        // string with current angle plus degree symbol
                        String angle;
                        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            angle = Html.fromHtml(updateString[0], Html.FROM_HTML_MODE_LEGACY) + "\u00b0";
                        } else {
                            //noinspection deprecation because we caught that in the if-statement above
                            angle = Html.fromHtml(updateString[0]) + "\u00b0";
                        }
                        // send "Updated." Toast, cancel the previous "Updated." Toast if that was still showing
                        if (updateToast != null) {
                            if (updatedToast != null) {
                                updatedToast.cancel();
                            }
                            updateToast.cancel();
                            updatedToast = Toast.makeText(getBaseContext(), "Updated.", Toast.LENGTH_SHORT);
                            updatedToast.show();
                        }

                        // convert string to integer, then rotate the image

                        int newAngle = 0;

                        //a new case of the Arduino returning either "panels above upper bound!"
                        // or "panels below lower bound!"
                        switch (updateString[1]) {
                            case "above": //"panels above upper bound!"
                                currentAngle.setText(">53°");
                                newAngle = 57;
                                break;
                            case "below": //"panels below lower bound!"
                                currentAngle.setText("<9°");
                                newAngle = 5;
                                break;
                            default:
                                currentAngle.setText(angle);
                                try {
                                    newAngle = Integer.valueOf(updateString[0]);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(getBaseContext(), "Arduino string not formatted correctly",
                                            Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                                break;
                        }

                        rotate(newAngle);

                        if (newAngle == 5) {
                            Toast.makeText(getBaseContext(), "Low end stop reached.", Toast.LENGTH_SHORT).show();
                        } else if (newAngle == 57) {
                            Toast.makeText(getBaseContext(), "High end stop reached.", Toast.LENGTH_SHORT).show();
                        }

                        updateString[1] = updateString[1].trim();
                    }
                    if (updateString[1].contains("auto")) {
                        if (!autoBox.isChecked()) {
                            autoBox.toggle();
                        }
                    } else if (updateString[1].contains("manual")) {
                        if (autoBox.isChecked()) {
                            autoBox.toggle();
                        }
                    }
                } else if (urlString.contains("Page")) {
                    Toast.makeText(getBaseContext(), "Page not found.", Toast.LENGTH_SHORT).show();
                } else if (urlString.contains("degrees")) {
                    //remove trailing newline
                    if (result.contains("\n")) {
                        //substring(0,3) means chars at index 0,1,2
                        result = result.substring(0, result.length() - 1);
                    }
                    //remove leading zeroes
                    if (result.charAt(0) == '0') {
                        result = result.substring(1, result.length());
                    }
                    Toast.makeText(getBaseContext(), "Angle set at " + result + "\u00b0", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Bad response received", Toast.LENGTH_SHORT).show();
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
//                int response = urlConnection.getResponseCode();

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

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return message;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_ping:
                startPingRequest();
                return true;
            case R.id.action_update:
                checkForUpdates();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

