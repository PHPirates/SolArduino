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
import android.widget.TextView;

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
    TextView responseTV;
    TextView touchx;
    TextView touchy;

    ImageView imageView;

    Button upButton;
    Button downButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        responseTV = (TextView) findViewById(R.id.response);
//        touchx = (TextView) findViewById(R.id.touchx);
//        touchy = (TextView) findViewById(R.id.touchy);

//        imageView = (ImageView) findViewById(R.id.imageView);
//        imageView.getViewTreeObserver().addOnPreDrawListener(
//                new ViewTreeObserver.OnPreDrawListener() {
//                    public boolean onPreDraw() {
//                        int finalHeight = imageView.getMeasuredHeight();
//                        int finalWidth = imageView.getMeasuredWidth();
//                        Log.e("height", String.valueOf(finalHeight));
//                        Log.e("width", String.valueOf(finalWidth));
//                        return true;
//                    }
//                });

        upButton = (Button) findViewById(R.id.upButton);
        downButton = (Button) findViewById(R.id.downButton);

//        imageView.setOnTouchListener(new View.OnTouchListener() {
//            float touchX;
//            float touchY;
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                touchX = motionEvent.getX();
//                touchY = motionEvent.getY();
//
//                String xtext = "x = " + String.valueOf(touchX);
//                String ytext = "y = " +String.valueOf(touchY);
//
//                touchx.setText(xtext);
//                touchy.setText(ytext);
//                switch (motionEvent.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        imageView.setImageResource(R.drawable.line_touched);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        imageView.setImageResource(R.drawable.line);
//                        break;
//                }
//                return true;
//            }
//        });

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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){
        return false;
    }

    public void sendDirectionRequest(String direction) {
        String url = "http://192.168.2.10/?panel=" + direction;
//        String url = "http://www.google.com";
        new SendRequest().execute(url);


    }

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

