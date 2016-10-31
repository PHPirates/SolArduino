package SolArduino;

import com.sun.corba.se.spi.orbutil.fsm.Action;
import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.text.Text;

import javax.annotation.Resources;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class Controller implements Initializable{

    String username = System.getProperty("user.name");
    String timesFileString = "src/SolArduino/resources/times.csv";
    String anglesFileString = "src/SolArduino/resources/angles.csv";
    String separator = ",";

    long[][] data;

    File timesFile;
    File anglesFile;

    BufferedReader timesReader;
    BufferedReader anglesReader;

    ArrayList<long[]> graphData;

    int angle = 42;

    @FXML private Slider slider;
    @FXML private Button buttonSetAngle;
    @FXML private LineChart graph;

//    double[][] graphData = new double[24][2];

    /**
     * Initialization method for the controller.
     */
    @FXML public void initialize(URL location, ResourceBundle resourceBundle){
        slider.valueProperty().addListener((observable, oldValue, newValue)-> {
            angle = newValue.intValue();
            if(angle < 10){
                buttonSetAngle.setText("set panels at  " + angle + "  degrees" );

            } else {
                buttonSetAngle.setText("set panels at " + angle + " degrees");
            }

        });

//        graph.setData(getGraphData());

        try{
            timesFile = new File(timesFileString);
            anglesFile = new File(anglesFileString);

            timesReader = new BufferedReader(new FileReader(timesFile));
            anglesReader = new BufferedReader(new FileReader(anglesFile));

            String timeLine;
            while((timeLine = timesReader.readLine()) != null){
                String[] timesString = timeLine.split(separator);
                data = new long[timesString.length][2];
                for (int i = 0; i < timesString.length; i++) {
                    data[i][0] = Long.valueOf(timesString[i]);
                }

                String angleLine;
                while((angleLine = anglesReader.readLine()) != null){
                    String[] anglesString = angleLine.split(separator);
                    for (int i = 0; i < timesString.length; i++) {
                        data[i][1] = Long.valueOf(anglesString[i]);
                    }
                }
            }

            for (int i = 0; i < data.length; i++) {
                System.out.println("time: " + data[i][0] + " angle: " + data[i][1]);
            }


        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML protected void generateGraph(ActionEvent event) {
//        graph.setData(getGraphData());
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2016,9,31,0,0,0);
        long start = startCalendar.getTimeInMillis();

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(2016,9,31,23,59,59);
        long end = endCalendar.getTimeInMillis();

        System.out.println(start);
        System.out.println(end);

        XYChart.Series series = new XYChart.Series();
        ObservableList<XYChart.Series<Long,Long>> list = FXCollections.observableArrayList();
        series.setName("angles");
        for (int i = 0; i < data.length; i++) {

            if(start/1000 < data[i][0] && data[i][0] < end/1000) {
                long daySeconds = data[i][0] % 86400;
                long hour = daySeconds / 3600;
                series.getData().add(new XYChart.Data<>(hour, data[i][1]));
            }
        }

//        series.getData().clear();
        list.add(series);
        graph.setData(list);
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

//    private ObservableList<XYChart.Series<Double, Double>> getGraphData(){
//        for (int i = 0; i < graphData.length; i++) {
//            graphData[i][0] = i+1;
//            graphData[i][1] = Math.random()*6000;
//        }
//        XYChart.Series series = new XYChart.Series();
//        ObservableList<XYChart.Series<Double, Double>> list = FXCollections.observableArrayList();
//        series.setName("power");
//        series.getData().clear();
//        for (double[] aGraphData : graphData) {
//            series.getData().add(new XYChart.Data<>(aGraphData[0], aGraphData[1]));
//
//        }
//        list.add(series);
//        return list;
//    }

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
