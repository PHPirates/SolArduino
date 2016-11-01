package SolArduino;

import com.sun.corba.se.spi.orbutil.fsm.Action;
import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import javax.annotation.Resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class Controller implements Initializable{

    String username = System.getProperty("user.name");

    int angle = 42;

    @FXML private GridPane controlGridPane;
    @FXML private Slider slider;
    @FXML private Button buttonUp;
    @FXML private Button buttonDown;
    @FXML private Button buttonSetAngle;
    @FXML private LineChart graph;
    @FXML private TextField inputDegrees;

    double[][] graphData = new double[24][2];

    @FXML public void initialize(URL location, ResourceBundle resourceBundle){

        controlGridPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                buttonUp.setPrefHeight(newValue.intValue());
                buttonDown.setPrefHeight(newValue.intValue());
            }
        });

        controlGridPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                buttonUp.setPrefWidth(newValue.intValue());
                buttonDown.setPrefWidth(newValue.intValue());
            }
        });

        slider.valueProperty().addListener((observable, oldValue, newValue)-> {
            angle = newValue.intValue();
            if(angle < 10){
                buttonSetAngle.setText("set panels at  " + angle + "  degrees" );

            } else {
                buttonSetAngle.setText("set panels at " + angle + " degrees");
            }

        });

        graph.setData(getGraphData());

    }

    @FXML protected void buttonUp(MouseEvent event) {
        if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            sendHttpRequest("http://192.168.178.106/?panel=up");
        } else { // mouse released
            sendHttpRequest("http://192.168.178.106/?panel=stop");
        }
    }

    @FXML protected void buttonDown(MouseEvent event) {
        if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            sendHttpRequest("http://192.168.178.106/?panel=down");
        } else { // mouse released
            sendHttpRequest("http://192.168.178.106/?panel=stop");
        }
    }

    @FXML protected void buttonAuto(ActionEvent event) {
        sendHttpRequest("http://192.168.178.106/?panel=auto");
    }

    @FXML protected void buttonUpdate(ActionEvent event) {
        sendHttpRequest("http://192.168.178.106/?update");
    }

    @FXML protected void setAngle(ActionEvent event) {
        sendHttpRequest("http://192.168.178.106/?degrees="+angle);
    }

    @FXML protected void generateGraph(ActionEvent event) {
        graph.setData(getGraphData());
    }

    private ObservableList<XYChart.Series<Double, Double>> getGraphData(){
        for (int i = 0; i < graphData.length; i++) {
            graphData[i][0] = i+1;
            graphData[i][1] = Math.random()*6000;
        }
        XYChart.Series series = new XYChart.Series();
        ObservableList<XYChart.Series<Double, Double>> list = FXCollections.observableArrayList();
        series.setName("power");
        series.getData().clear();
        for (double[] aGraphData : graphData) {
            series.getData().add(new XYChart.Data<>(aGraphData[0], aGraphData[1]));

        }
        list.add(series);
        return list;
    }

    private void sendHttpRequest(String url) {
        System.out.println("sending request to"+url);
//        try {
//            InputStream response = new URL(url).openStream();
//            try (Scanner scanner = new Scanner(response)) {
//                String responseBody = scanner.useDelimiter("\\A").next();
//                text.setText(responseBody);
//            }
//        } catch (IOException e) {
//            printStackTrace();
//        }

    }
}
