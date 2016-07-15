package com.abbyberkers.solarduino;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    TextView textView;
    TextView responseTV;    // textView to show response from arduino

    ImageView imageView;    // image of solar panels

    SeekBar seekbar;        // seekbar to change angle of solar panels

    Button upButton;        // button to make the solar panels move up
    Button downButton;      // button to make the solar panels move down
    Button setAngle;        // button to set angle of solar panels

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        responseTV = (TextView) findViewById(R.id.response);

        seekbar = (SeekBar) findViewById(R.id.seekBar);

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

        // seekbar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // do something when the progress of the seekbar is changing ("live")
                String angle = "Set angle at " + i + " \u00b0";
                setAngle.setText(angle);
                rotate(i);

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
        String url = "http://192.168.2.10/?panel=" + direction;
//        String url = "http://www.google.com";
        new SendRequest().execute(url);

    }

    /**
     * send a http request to the arduino, to set solar panels at specified angle
     * @param view
     */
    public void sendAngleRequest(View view){
        int prog = seekbar.getProgress();
        String url = "http://192.168.2.10/?degree=" + String.valueOf(prog);
        new SendRequest().execute(url);
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
            responseTV.setText(Html.fromHtml(result));
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

