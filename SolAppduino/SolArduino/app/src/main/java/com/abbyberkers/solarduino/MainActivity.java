package com.abbyberkers.solarduino;

import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    String urlString;
    String ipString = "http://192.168.0.23/";

    TextView textView;
    TextView currentAngle;  // show current angle of solar panels
    TextView responseTV;    // show response from arduino

    ImageView imageView;    // image of solar panels

    SeekBar seekbar;        // seekbar to change angle of solar panels

    Button upButton;        // button to make the solar panels move up
    Button downButton;      // button to make the solar panels move down
    Button setAngle;        // button to set angle of solar panels

    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        currentAngle = (TextView) findViewById(R.id.currentAngle);
        responseTV = (TextView) findViewById(R.id.response);

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

        upButton = (Button) findViewById(R.id.upButton);
        downButton = (Button) findViewById(R.id.downButton);
        setAngle = (Button) findViewById(R.id.setAngle);

        frameLayout = (FrameLayout) findViewById(R.id.frame);
//        int height = frameLayout.getLayoutParams().height;
//        Log.e("height", String.valueOf(height));

        // seekbar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // do something when the progress of the seekbar is changing ("live")
                String angle = "Set angle at " + i + " \u00b0";
                setAngle.setText(angle);
//                rotate(i);

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

        // set OnTouchListener for the up button, send http request when putton pressed and released
        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        textView.setText("Up button pressed.");
                        sendDirectionRequest("up");
                        break;
                    case MotionEvent.ACTION_UP:
                        textView.setText("Up button released.");
                        sendDirectionRequest("stop");
                        break;
                }
                return true;
            }
        });

        // set OnTouchListener for the down button, send http request when putton pressed and released
        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        textView.setText("Down button pressed.");
                        sendDirectionRequest("down");
                        break;
                    case MotionEvent.ACTION_UP:
                        textView.setText("Down button released.");
                        sendDirectionRequest("stop");
                        break;
                }
                return true;
            }
        });

        setAngle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e("setAngle", "pressed");
                        int prog = seekbar.getProgress();
                        sendAngleRequest(prog);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e("setAngle", "released");
//                        String angleString = (currentAngle.getText().toString()).substring(0,2);
//                        int angleInt = Integer.valueOf(angleString);
//                        int seekBarProgress = seekbar.getProgress();
//                        Log.e("angles", "current " + angleInt + " seekbar " + seekBarProgress);
//
//                        sendUpdateRequest();

                        final Timer timer = new Timer();

                        final TimerTask task = new TimerTask() {
                            String angleString = (currentAngle.getText().toString())
                                    .substring(0,2)
                                    .trim();

                            int angleInt = Integer.valueOf(angleString);
                            int seekBarProgress = seekbar.getProgress();
                            @Override
                            public void run() {
                                Log.e("angles", "current " + angleInt + " seekbar " + seekBarProgress);

                                if(Math.abs(angleInt - seekBarProgress) == 1) {
                                    Log.w("timer", "cancel");
                                    timer.cancel();
                                } else {

                                    angleString = (currentAngle.getText().toString())
                                            .substring(0, 2)
                                            .trim();

                                    angleInt = Integer.valueOf(angleString);
                                    seekBarProgress = seekbar.getProgress();

                                    sendUpdateRequest();
                                }

                            }
                        };

                        timer.schedule(task, 800, 800);

                        break;
                }
                return true;
            }
        });
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
        new SendRequest().execute(urlString);

    }

    /**
     * send a http request to the arduino, to set solar panels at specified angle
     * @param angle
     */
    public void sendAngleRequest(int angle){
//        String urlString;
        if(angle < 10){
            urlString = ipString + "?degrees=0" + String.valueOf(angle);
        } else {
            urlString = ipString + "?degrees=" + String.valueOf(angle);
        }
//        urlString = "http://pannenkoekenwagen.nl/pkw/test.html";
        new SendRequest().execute(urlString);
    }

    public void sendUpdateRequest() {
        urlString = ipString + "?update";
        new SendRequest().execute(urlString);
    }


    /**
     * class to do html stuff...
     */
    private class SendRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url){
            try{
                return sendRequest(url[0]);
            } catch (Exception e){
                return "Unable to retrieve web page.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String message = urlString.substring(20);
            Log.e("urlString", urlString);
            Log.e("message", message);
            if(message.contains("panel")){
                responseTV.setText(Html.fromHtml(result));
                Toast.makeText(getBaseContext(), Html.fromHtml(result), Toast.LENGTH_SHORT).show();
            } else if(message.contains("degrees")) {
                Log.e("result", result);
            } else if(message.contains("update")) {
                Log.e("result", result);
                currentAngle.setText(Html.fromHtml(result));
                result = result.substring(0,2).trim();
                rotate(Integer.valueOf(result));
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

