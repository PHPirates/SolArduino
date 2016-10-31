package SolArduino;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class Controller implements Initializable{

    String username = System.getProperty("user.name");
    String timesFileString = "src/SolArduino/resources/times.csv"; // path to the times.csv file
    String anglesFileString = "src/SolArduino/resources/angles.csv"; // path to the angles.csv file
    String separator = ",";

    long[][] data; // contains the times and angles from the csv files

    File timesFile;
    File anglesFile;

    BufferedReader timesReader;
    BufferedReader anglesReader;

    int angle = 42;

    @FXML private Slider slider;
    @FXML private Button buttonSetAngle;
    @FXML private LineChart graph;

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

        try { // read the times.csv and angles.csv files
            // times.csv contains the UNIX times in seconds (since January 1, 00:00:00:00)
            // if we convert these times to a date, we get the time in GMT +0, so we have to add one or two hours still
            timesFile = new File(timesFileString);
            anglesFile = new File(anglesFileString);

            timesReader = new BufferedReader(new FileReader(timesFile)); // BufferedReader to read times file
            anglesReader = new BufferedReader(new FileReader(anglesFile)); // BufferedReader to read angles file

            String timeLine; // line with the times
            while((timeLine = timesReader.readLine()) != null){
                // runs only once, since times.csv consists of only one line
                String[] timesString = timeLine.split(separator); // split the times at the commas
                data = new long[timesString.length][2]; // create data[][] based on the number of times/angles
                for (int i = 0; i < timesString.length; i++) {
                    data[i][0] = Long.valueOf(timesString[i]); // fill data[][] with the times
                }

                // we can safely read the angles file here, since this only runs once
                String angleLine; // line with the angles
                while((angleLine = anglesReader.readLine()) != null){
                    String[] anglesString = angleLine.split(separator); // split angles at the commas
                    for (int i = 0; i < timesString.length; i++) {
                        data[i][1] = Long.valueOf(anglesString[i]); // fill data[][] with the angles
                    }
                }
            }

        } catch (Exception e){ // catch Exception (FileNotFoundException?)
            e.printStackTrace();
        }

    }

    @FXML protected void generateGraph(ActionEvent event) {
//        graph.setData(getGraphData());
        TimeZone timeZone = TimeZone.getDefault(); // get default timezone TODO is this the current timezone?
        Calendar startCalendar = Calendar.getInstance(); // calendar with first second of day we want the graph for
        startCalendar.set(2016,9,31,0,0,0);
        long start = startCalendar.getTimeInMillis();
        long offset = timeZone.getOffset(start); // calculate the offset because of time zone using the start time
        start = start + offset; // set the actual start time, with correct time zone


        Calendar endCalendar = Calendar.getInstance(); // calendar with last second of day we want the graph for
        endCalendar.set(2016,9,31,23,59,59);
        long end = endCalendar.getTimeInMillis() + offset; // set end time with correct time zone

        XYChart.Series series = new XYChart.Series();
        ObservableList<XYChart.Series<Double,Long>> list = FXCollections.observableArrayList();
        series.setName("angles");
        for (int i = 0; i < data.length; i++) {
            // time * 1000 to convert to milliseconds, then add offset for correct time zone
            data[i][0] = data[i][0] * 1000 + offset;

            if(start < data[i][0] && data[i][0] < end) { // find times during the given day
                Date date = new Date(data[i][0]); // create DateObject with times in milliseconds
                DateFormat hourFormat = new SimpleDateFormat("HH"); // DateFormat to get the hour of day
                double hour = Double.valueOf(hourFormat.format(date)); // double so we can add the minutes to it

                DateFormat minuteFormat = new SimpleDateFormat("mm"); // DateFormat to get the minute of hour
                double minute = Double.valueOf(minuteFormat.format(date))/60; // convert minutes to part of hour (decimal)

                System.out.println(hour + minute);
                double time = hour + minute; // double with the time, e.g.: 12:45 = 12.75, so it's displayed neatly in the graph
                series.getData().add(new XYChart.Data<>(time, data[i][1])); // add time and angle to the data of the graph
            }
        }

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
