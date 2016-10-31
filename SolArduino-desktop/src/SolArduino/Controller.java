package SolArduino;

import com.sun.javafx.tk.Toolkit;
import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import javax.annotation.Resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.*;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class Controller implements Initializable{

    String username = System.getProperty("user.name");

    int angle = 42;

    @FXML private Slider slider;
    @FXML private Text responseTextView;
    @FXML private Button buttonSetAngle;
    String ip = "http://192.168.8.42/?";

    @FXML public void initialize(URL location, ResourceBundle resourceBundle){
        slider.valueProperty().addListener((observable, oldValue, newValue)-> {
            angle = newValue.intValue();
            if(angle < 10){
                buttonSetAngle.setText("set panels at  " + angle + "  degrees" );

            } else {
                buttonSetAngle.setText("set panels at " + angle + " degrees");
            }

        });
    }

    @FXML protected void buttonUp(MouseEvent event) {
        if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            sendHttpRequest("panel=up");
        } else { // mouse released
            sendHttpRequest("panel=stop");
        }
    }

    @FXML protected void buttonDown(MouseEvent event) {
        if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            sendHttpRequest("panel=down");
        } else { // mouse released
            sendHttpRequest("panel=stop");
        }
    }

    @FXML protected void buttonAuto(ActionEvent event) {
        sendHttpRequest("panel=auto");
    }

    @FXML protected void buttonUpdate(ActionEvent event) {
        sendHttpRequest("update");
    }

    @FXML protected void setAngle(ActionEvent event) {
//        if (inputDegrees.getText() != null) {
//            sendHttpRequest("degrees=" + inputDegrees.getText());
//        }
        sendHttpRequest("degrees=" + angle);
    }

    private void sendHttpRequest(String urlparam) {
        final String url = ip+urlparam;

        //currently the 'arduino not reachable' message is given if the thread still runs
        //after the timeout, but the thread only kills itself after like ten seconds
        //for some reasons the requests are queued though, which is a nice feature
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(new Callable<String>() {
                @Override
                public String call() {
                    String responseBody = null;
                    System.out.println("sending request to "+url);
                    try {
                        InputStream response = new URL(url).openStream();
                        try (Scanner scanner = new Scanner(response)) {
                            responseBody = scanner.useDelimiter("\\A").next();
                            responseTextView.setText(responseBody);
                        }
                    } catch (IOException e) {
                        System.out.println("Request to "+url+" failed.");
//                        printStackTrace();
                    }
                    System.out.println("thread exiting");
                    return responseBody;
                }

            }).get(2,TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            responseTextView.setText("Arduino not reachable!");
            System.out.println("Request timed out.");
            executor.shutdown(); //todo doesn't shutdown thread?
//            e.printStackTrace();
        }


    }
}
