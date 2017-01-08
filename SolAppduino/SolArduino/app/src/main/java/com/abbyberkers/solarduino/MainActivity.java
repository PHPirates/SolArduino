package com.abbyberkers.solarduino;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    String urlString;
    String ipString = "http://192.168.8.42/"; // IP Thomas
//    String ipString = "http://192.168.0.23/"; // IP Abby
    String host = "192.168.8.42"; // host Thomas
//    String host = "192.168.0.23"; // host Abby
//    String ipString = "http://192.168.2.107"; // IP test
//    String host = "192.168.2.107"; // host test

    String lastResult;           // String containing the result from the last http-request

    Toast unreachableToast;
    Toast updateToast;
    Toast updatedToast;

    int delay = 3365;        // delay for the Timer/TimerTask which asks for an update every (delay) millisecs
                            // 175 secs for 52 degrees -> 3.37 secs per degree, asking every fifth of a degree
    int handlerTimeout = 2000; //timeout for killing a request or ping

    boolean reachable;      // boolean to know whether the Arduino is reachable or not

    Timer downTimer;
    Timer upTimer;
    Timer autoTimer;

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

    // things for the storm mode
    CheckBox stormBox;
    Button stormStartButton;
    Button stormEndButton;
    TextView from;
    TextView to;
    FragmentManager fm = getSupportFragmentManager();
    int startYear, startMonth, startDay, startHour, startMinute;
    int endYear, endMonth, endDay, endHour, endMinute;
    boolean timeOnButton = false;
    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();
    long startInSeconds;
    long endInSeconds;

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

        WVersionManager versionManager = new WVersionManager(this);
        versionManager.setVersionContentUrl("https://github.com/PHPirates/SolArduino/raw/master/solappduino/version.json");
        versionManager.setUpdateUrl("https://github.com/PHPirates/SolArduino/raw/master/solappduino/solarduino/app/app-release.apk");
        versionManager.checkVersion();

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
        //clicklistener instead of OnCheckedChange won't register sliding a switch
        //but this way when toggling the button from somewhere else the clicklistener isn't called
        autoBox.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(autoBox.isChecked()) {
                    urlString = ipString + "?panel=auto";
//                    Toast.makeText(getBaseContext(), "Auto mode switched on.", Toast.LENGTH_SHORT).show();
                    startHttpRequest();
                    autoTimer = new Timer(); // timer to schedule the update requests
                    //We assume that when timerTask runs the first time, the response with degrees is received
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            //get degrees aimed at from lastResult
                            int next = getNextDegree(lastResult);
                            int current = getCurrentDegree();
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



        stormBox = (CheckBox) findViewById(R.id.stormBox);
        stormStartButton = (Button) findViewById(R.id.stormStartButton);
        stormEndButton = (Button) findViewById(R.id.stormEndButton);
        from = (TextView) findViewById(R.id.fromTV);
        to = (TextView) findViewById(R.id.toTV);

//        if(!stormBox.isChecked()){
//            changeStormVisibility(false); // make buttons and textviews invisible
//        } else {
//            changeStormVisibility(true); // make buttons and textview visible
//        }

        stormBox.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stormBox.isChecked()){
                    Log.e("Storm", "Storm mode activated.");
                    changeStormVisibility(true);
                    // putting the showStormPickers methods in the wrong order shows them in the right order
                    showStormPickers(false); // show date pickers for end date/time
                    showStormPickers(true); // show date pickers for start date/time
                    timeOnButton = true;
                } else {
                    Log.e("Storm", "Deactivated.");
                    changeStormVisibility(false);
                    timeOnButton = false;
                    stormStartButton.setText("start time");
                    stormEndButton.setText("end time");
//                    startCalendar.setTimeInMillis(System.currentTimeMillis());
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

                                if(angleInt == seekbarProgress) {
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
     * Parses result string to find degree the panels go to
     * @param result from http request, set by sendRequest
     * @return degrees
     */
    public int getNextDegree(String result) {
        String intString = result.substring(result.indexOf("_"),result.lastIndexOf("_"));
        return Integer.parseInt(intString);
    }

    /**
     * @return degree currently displayed in the textview currentAngle
     */
    public int getCurrentDegree() {
        String angleString = currentAngle.getText().toString();

        if (angleString.length()<3) {
            return Integer.parseInt(angleString.substring(0,1)); //if only one digit
        } else {
            return Integer.parseInt(angleString.substring(0,2));
        }
    }

    /**
     * method to uncheck checkbox for automode, when any of the other buttons is pressed
     * @param view checkbox
     */
    public void unCheck(View view){
        if(autoBox.isChecked()){
            autoBox.toggle();
        }
    }

    /**
     * method to change the visibility of the from and to textviews and the buttons with the start
     * and end time for the storm mode
     * @param active boolean, true if buttons should be made visible, false if invisible
     */
    public void changeStormVisibility(boolean active) {
        if(active){
            stormStartButton.setVisibility(View.VISIBLE);
            stormEndButton.setVisibility(View.VISIBLE);
            from.setVisibility(View.VISIBLE);
            to.setVisibility(View.VISIBLE);
        } else {
            stormStartButton.setVisibility(View.INVISIBLE);
            stormEndButton.setVisibility(View.INVISIBLE);
            from.setVisibility(View.INVISIBLE);
            to.setVisibility(View.INVISIBLE);
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

    public void datePass(boolean start, int year, int month, int day){
        if(start){
            this.startYear = year;
            this.startMonth = month;
            this.startDay = day;
        } else{
            this.endYear = year;
            this.endMonth = month;
            this.endDay = day;
        }

//        Log.e("Date", year + "-" + month + "-" + day);

    }

    public void timePass(boolean start, int hour, int minute) {
        if(start){
            this.startHour = hour;
            this.startMinute = minute;
        } else {
            this.endHour = hour;
            this.endMinute = minute;
        }

//        Log.e("Time", hour + ":" + minute);
//        Log.e("Start", String.valueOf(start));


        printTime(start);
    }

    public void printTime(boolean start) {
        /**
         * Print time and date on corresponding buttons
         */

        timeOnButton = true;

//        startCalendar.getInstance();
//        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy   HH:mm", Locale.getDefault());

        //sets time for alarm
        if(start) {
            startCalendar.set(Calendar.YEAR, startYear);
            startCalendar.set(Calendar.MONTH, startMonth);
            startCalendar.set(Calendar.DAY_OF_MONTH, startDay);
            startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
            startCalendar.set(Calendar.MINUTE, startMinute);
            startCalendar.set(Calendar.SECOND, 0);

            startInSeconds = startCalendar.getTimeInMillis()/1000 + 3600; // +2 hours because timezones?
            Log.e("start in seconds", String.valueOf(startInSeconds));

            Date startDate = new Date(startCalendar.getTimeInMillis());
            String startDateString = simpleDateFormat.format(startDate);
            stormStartButton.setText(startDateString);

        } else {
            endCalendar.set(Calendar.YEAR, endYear);
            endCalendar.set(Calendar.MONTH, endMonth);
            endCalendar.set(Calendar.DAY_OF_MONTH, endDay);
            endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
            endCalendar.set(Calendar.MINUTE, endMinute);
            endCalendar.set(Calendar.SECOND, 0);

            endInSeconds = endCalendar.getTimeInMillis()/1000 + 3600;
            Log.e("end in seconds", String.valueOf(endInSeconds));

            Date endDate = new Date(endCalendar.getTimeInMillis());
            String endDateString = simpleDateFormat.format(endDate);
            stormEndButton.setText(endDateString);
        }

        sendStormRequest(startInSeconds, endInSeconds);

    }

    public void showStormPickers(boolean start){
        if(start) {
            showTimeDialog("Start Time", start); // -- these are in the wrong order because they're opened the wrong way around... somehow...
            showDateDialog("Start Date", start); // -|
        } else {
            showTimeDialog("End Time", start);
            showDateDialog("End Date", start);
        }
    }

    public void showStormStart(View view){
        showStormPickers(true);
    }

    public void showStormEnd(View view){
        showStormPickers(false);
    }

    public void showDateDialog(String title, boolean start) {
        //date to be sent to fragment
        DateFragment dateFragment = new DateFragment();
        Bundle bundle = new Bundle(); // bundle to be sent

        if (timeOnButton) { // if there are times on the button already

                Calendar c = Calendar.getInstance();
            if(start) { // if the start button is clicked
                c.setTimeInMillis(startCalendar.getTimeInMillis());
            } else {
                c.setTimeInMillis(endCalendar.getTimeInMillis());
            }
                bundle.putInt("year", c.get(Calendar.YEAR));
                bundle.putInt("month", c.get(Calendar.MONTH));
                bundle.putInt("day", c.get(Calendar.DAY_OF_MONTH));


        } else {
            Calendar c = Calendar.getInstance();
            bundle.putInt("year", c.get(Calendar.YEAR));
            bundle.putInt("month", c.get(Calendar.MONTH));
            bundle.putInt("day", c.get(Calendar.DAY_OF_MONTH));

        }

        bundle.putString("title", title);
        bundle.putBoolean("start", start);

        dateFragment.setArguments(bundle);

        dateFragment.show(fm, "Dialog Fragment");

    }

    public void showTimeDialog(String title, boolean start) {
        TimeFragment timeFragment = new TimeFragment();
        Bundle bundle = new Bundle();

        Bundle extras = getIntent().getExtras();
        //get time from database to be default time if editing, current time if adding
        if (extras != null) { //if there's something in the bundle
            //int Value = extras.getInt("id"); //get the id to search

                //convert long to ints
                Calendar c = Calendar.getInstance();
//                c.setTimeInMillis(dataDate);
                bundle.putInt("hour", c.get(Calendar.HOUR_OF_DAY));
                bundle.putInt("minute", c.get(Calendar.MINUTE));

                //adding an alarm, current time is default
//                bundle.putInt("hour", hour);
//                bundle.putInt("minute", minute);
        } else {
            Calendar c = Calendar.getInstance();
            bundle.putInt("hour", c.get(Calendar.HOUR_OF_DAY));
            bundle.putInt("minute", c.get(Calendar.MINUTE));
        }

        bundle.putString("title", title);
        bundle.putBoolean("start", start);

        timeFragment.setArguments(bundle);

        timeFragment.show(fm, "Dialog Fragment");

    }


    // Request thingies start here -----------------------------------------------------------------------------------------
    /**
     * send a http request to the arduino, to move panels up and down
     * @param direction up, down, or stop (and auto?)
     */
    public void sendDirectionRequest(String direction) {
        urlString = ipString + "?panel=" + direction;
        startHttpRequest(); //urlString will be used here

    }

    public void sendStormRequest(long start, long end) {
        String startString = String.valueOf(start);
        String endString = String.valueOf(end);
        urlString = ipString + "?stormstart=" + startString + "stormend=" + endString;
        startHttpRequest();
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
                    unreachableToast = Toast.makeText(getBaseContext(),"The Arduino could not be reached, request terminated.",Toast.LENGTH_SHORT);
                    unreachableToast.show();
                    lastResult = "Arduino not reachable"; //update http request return string
                }

            }
        }, handlerTimeout);
    }

    /**
     * send a http request to the arduino, to set solar panels at specified angle
     * @param angle to set solar panels on
     */
    public void sendAngleRequest(int angle){
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
     * @param view currentAngle TextView
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
     *      When the panels are moving after auto mode has been enabled
     * Send an update http-request to the Arduino.
     */
    public void sendUpdateRequest() {
        if(lastResult.contains("Un") || lastResult.contains("not")) {
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
    private abstract class DataSender extends AsyncTask<String,Void,String> {}

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
        protected String doInBackground(String... url){
            try{
                // try to ping the Arduino first to find out if it's reachable or not.
                String s;
                ProcessBuilder processbuilder = new ProcessBuilder("/system/bin/ping", url[0]);
                Process process = processbuilder.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                reachable = false;
                while ((s = stdInput.readLine()) != null)
                {
                    if(s.contains("seq=")){
//                        Log.e("ping", "first");
                        reachable = !s.contains("Host Unreachable");
                        break;
                    }
                }
                process.destroy(); //also terminate the process when ping didn't fail
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) { //needs to be a string to extend DataSender
            if(reachable) {
                super.onPostExecute(result);
                new RequestSender().execute(urlString);
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
    private class RequestSender extends DataSender {

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
            lastResult = result; //set result global as well

            if (result.contains("Un") || result.contains("not be")) {
                // Arduino could not be reached.

                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
                // set seekbar at current angle, so update requests aren't sent anymore.
                seekbar.setProgress(getCurrentDegree());
            } else if (result.contains("Panel")) {
                Toast.makeText(getBaseContext(), result.trim(), Toast.LENGTH_SHORT).show();
            } else {
                String message = urlString;
                if (message.contains("panel")) {
                    // Toast that the panels are going up or down so the user knows the Arduino
                    // received the request and knows what to do
                    String string;
                    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        string = Html.fromHtml(result,Html.FROM_HTML_MODE_LEGACY)+ "\u00b0";
                    } else {
                        //noinspection deprecation because we caught that in the if-statement above
                        string = Html.fromHtml(result) + "\u00b0";
                    }
                    Toast.makeText(getBaseContext(), string, Toast.LENGTH_SHORT).show();
                } else if (message.contains("update")) { // received update request
                    String[] updateString = result.split(" ");
                    if (updateString[0].trim().length() == 0) {
                        Toast.makeText(getBaseContext(), "Arduino did not return an angle", Toast.LENGTH_SHORT).show();
                    } else { // received angle
                        String angle; // string with current angle plus degree symbol
                        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            angle = Html.fromHtml(updateString[0], Html.FROM_HTML_MODE_LEGACY) + "\u00b0";
                        } else {
                            //noinspection deprecation because we caught that in the if-statement above
                            angle = Html.fromHtml(updateString[0]) + "\u00b0";
                        }
                        currentAngle.setText(angle); // display the new angle in the textview
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
                        try {
                            newAngle = Integer.valueOf(updateString[0]);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getBaseContext(), "Arduino string not formatted correctly",
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        rotate(newAngle);

                        // toast for when the endstops are reached
                        if (newAngle == 5) {
                            Toast.makeText(getBaseContext(), "Low end stop reached.", Toast.LENGTH_SHORT).show();
                        } else if (newAngle == 57) {
                            Toast.makeText(getBaseContext(), "High end stop reached.", Toast.LENGTH_SHORT).show();
                        }
                            // TODO updateString[1] = updateString[1].trim(); was here???

                    }

                    updateString[1] = updateString[1].trim(); // TODO why was this inside the else above?

                    if (updateString[1].contains("auto")) {
                        if (!autoBox.isChecked()) {
                            autoBox.toggle();
                        }
                    } else if (updateString[1].contains("manual")) {
                        if (autoBox.isChecked()) {
                            autoBox.toggle();
                        }
                    }

                    updateString[2] = updateString[2].trim();
                    if(updateString[2].contains("storm")) {
                        if(!stormBox.isChecked()) {
                            stormBox.toggle();
                        }
                        changeStormVisibility(true);
//                        showStormPickers(true);
                    } else if(updateString[2].contains("calm")) {
                        if(stormBox.isChecked()) {
                            stormBox.toggle();
                        }
                        changeStormVisibility(false);
//                        showStormPickers(false);
                    }

                } else if (message.contains("Page")) {
                    Toast.makeText(getBaseContext(), "Page not found.", Toast.LENGTH_SHORT).show();
                } else if (message.contains("degrees")) {
                    //remove trailing newline
                    if (result.contains("\n")) {
                        //substring(0,3) means chars at index 0,1,2
                        result = result.substring(0,result.length()-1);
                    }
                    //remove leading zeroes
                    if (result.charAt(0)=='0') {
                        result = result.substring(1,result.length());
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
                if(urlConnection != null) {
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

