package SolArduino;

import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.Scanner;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class Controller {

    String username = System.getProperty("user.name");

    String ip = "http://192.168.178.106/?";

    @FXML private Text text;
    @FXML private TextField inputDegrees;
    @FXML private ImageView buttonDownImage;

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
        if (inputDegrees.getText() != null) {
            sendHttpRequest("degrees=" + inputDegrees.getText());
        }
    }

    private void sendHttpRequest(String urlparam) {
        final String url = ip+urlparam;

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                System.out.println("sending request to "+url);
                try {
                    InputStream response = new URL(url).openStream();
                    try (Scanner scanner = new Scanner(response)) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        text.setText(responseBody);
                    }
                } catch (IOException e) {
                    System.out.println("Request to "+url+" failed.");
                    printStackTrace();
                }
                System.out.println("thread exiting");
            }

        });
        thread.start();


    }
}
