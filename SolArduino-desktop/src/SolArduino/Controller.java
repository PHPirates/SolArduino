package SolArduino;

import com.sun.javafx.tk.Toolkit;
import com.sun.corba.se.spi.orbutil.fsm.Action;
import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.*;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class Controller implements Initializable{

    String username = System.getProperty("user.name");
    String timesFileString = "resources/times.csv"; // path to the times.csv file
    String anglesFileString = "resources/angles.csv"; // path to the angles.csv file
    String separator = ",";
    String ip = "http://192.168.2.42/?"; // ip address from the Arduino

    long[][] data; // contains the times and angles from the csv files

    Calendar chosenDate; // calendar with chosen date from the DatePicker

    int angle = 42; // angle at which the solar panels are set
    int threadTimeout = 2; //seconds

    XYChart.Series lower = new XYChart.Series();
    XYChart.Series upper = new XYChart.Series();

    @FXML private GridPane controlGridPane;
    @FXML private Slider slider;
    @FXML private Text responseTextView;
    @FXML private Button buttonUp;
    @FXML private Button buttonDown;
    @FXML private Button buttonUpdate;
    @FXML private Button getButtonSetAngle;
    @FXML private Button buttonAuto;
    @FXML private Button buttonSetAngle;
    @FXML private LineChart graph;
    @FXML private DatePicker datePicker;
    @FXML private TableView table;

    /**
     * Initialization method for the controller.
     */
    @FXML public void initialize(URL location, ResourceBundle resourceBundle){

        // make the buttons with the images resize by adding a listener to the GridPane size - Don't see why this works
        controlGridPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                buttonUp.setPrefHeight(newValue.intValue());
                buttonDown.setPrefHeight(newValue.intValue());
                buttonUpdate.setStyle("-fx-font-size: " + (Double) newValue/20 + "px;");
                buttonSetAngle.setStyle("-fx-font-size: " + (Double) newValue/25 + "px;");
                buttonAuto.setStyle("-fx-font-size: " + (Double) newValue/20 + "px;");
            }
        });

        controlGridPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                buttonUp.setPrefWidth(newValue.intValue());
                buttonDown.setPrefWidth(newValue.intValue());
            }
        });

        // listener to detect changes for the slider
        slider.valueProperty().addListener((observable, oldValue, newValue)-> {
            angle = newValue.intValue();
            // set text at the setAngle button
            if(angle < 10){
                // little bit alignment, because of 1-digit numbers
                buttonSetAngle.setText("set panels at  " + angle + "  degrees" );
            } else {
                buttonSetAngle.setText("set panels at " + angle + " degrees");
            }

        });

        chosenDate = Calendar.getInstance(); // create calendar object
        // listener to detect changes for the DatePicker
        datePicker.valueProperty().addListener((observable, oldDay, newDay)-> {
            // create Date object from the start of the newDay
            Date date = Date.from(newDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
            chosenDate.setTime(date); // set the date of chosenDate to newDay

            graph.setData(getGraphData(chosenDate)); // change the drawing for the graph

        });

        try {
            // read the times.csv and angles.csv files
            // times.csv contains the UNIX times in seconds (since January 1, 00:00:00:00)

            InputStream timesIS = getClass().getResourceAsStream(timesFileString);
            InputStream anglesIS = getClass().getResourceAsStream(anglesFileString);

            InputStreamReader timesISReader = new InputStreamReader(timesIS);
            InputStreamReader anglesISReader = new InputStreamReader(anglesIS);

            BufferedReader timesReader = new BufferedReader(timesISReader); // BufferedReader to read times file
            BufferedReader anglesReader = new BufferedReader(anglesISReader); // BufferedReader to read angles file

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
                anglesReader.close();
            }
            timesReader.close();

        } catch (Exception e){ // catch Exception (FileNotFoundException?)
            e.printStackTrace();
        }

        // initialize angle graph with today's angles
        Calendar calendar = Calendar.getInstance();
        Date time = new Date(System.currentTimeMillis()); // date with current time
        calendar.setTime(time); // set calendar object with current time to pass to getGraphData

        // lower and upper contain data to draw the lines to clarify the range of the solar panels in the graph
        lower.getData().add(new XYChart.Data<>(5,5));
        lower.getData().add(new XYChart.Data<>(23,5));
        upper.getData().add(new XYChart.Data<>(5,57));
        upper.getData().add(new XYChart.Data<>(23,57));

        graph.setData(getGraphData(getToday()));

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
        sendHttpRequest("degrees=" + angle);
    }

    @FXML protected void todayGraph(ActionEvent event) {
        graph.setData(getGraphData(getToday()));
    }

    /**
     * @param day
     * @return list that contains data for graph
     */
    private ObservableList<XYChart.Series<Double,Double>> getGraphData(Calendar day){

        int year = day.get(Calendar.YEAR); // get year of chosen date
        int month = day.get(Calendar.MONTH); // get month of chosen date
        int dayOfMonth = day.get(Calendar.DAY_OF_MONTH); // get day of the month of chosen date
        TimeZone timeZone = TimeZone.getDefault(); // get local timezone (with DST)
        Calendar startCalendar = Calendar.getInstance(); // calendar with first second of day we want the graph for
        startCalendar.set(year, month, dayOfMonth,0,0,0); // months start at 0
        long start = startCalendar.getTimeInMillis();
        long offset = timeZone.getOffset(start); // calculate the offset because of time zone using the start time
        start = start + offset; // set the actual start time, with correct time zone

        Calendar endCalendar = Calendar.getInstance(); // calendar with last second of day we want the graph for
        endCalendar.set(year, month, dayOfMonth,23,59,59);
        long end = endCalendar.getTimeInMillis() + offset; // set end time with correct time zone

        XYChart.Series series = new XYChart.Series();
        ObservableList<XYChart.Series<Double,Double>> list = FXCollections.observableArrayList();
        series.setName("angles");
        series.getData().clear();

        ObservableList<TableData> tableList = FXCollections.observableArrayList();

        for (int i = 0; i < data.length; i++) {

            int length = String.valueOf(data[i][0]).length();
            if(length == 10) {
                // time * 1000 to convert to milliseconds, then add offset for correct time zone
                data[i][0] = data[i][0] * 1000 + offset;
            }

            if(start < data[i][0] && data[i][0] < end) { // find times during the given day
                Date date = new Date(data[i][0]); // create DateObject with times in milliseconds
                DateFormat hourFormat = new SimpleDateFormat("HH"); // DateFormat to get the hour of day
                double hour = Double.valueOf(hourFormat.format(date)); // double so we can add the minutes to it

                DateFormat minuteFormat = new SimpleDateFormat("mm"); // DateFormat to get the minute of hour
                double minute = Double.valueOf(minuteFormat.format(date))/60; // convert minutes to part of hour (decimal)

                double time = hour + minute; // double with the time, e.g.: 12:45 = 12.75, so it's displayed neatly in the graph
                double angle = Double.valueOf(data[i][1])/10;
                series.getData().add(new XYChart.Data<>(time, angle)); // add time and angle to the data of the graph

                DateFormat timeFormat = new SimpleDateFormat("HH:mm"); // format to show time in the table
                String timeString = String.valueOf(timeFormat.format(date));
                TableData tableData = new TableData(timeString, angle);
                tableList.add(tableData);
            }
        }

        table.setItems(tableList);
        table.setMaxHeight((table.getItems().size()+1) * 30); // set height of the table so we don't have empty rows

        datePicker.setValue(day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()); // set default date in textbox DatePicker

        list.addAll(lower, upper, series);
        return list;
    }

    /**
     * @return calendar with current date and time
     */
    private Calendar getToday(){
        Calendar calendar = Calendar.getInstance();
        Date time = new Date(System.currentTimeMillis()); // date with current time
        calendar.setTime(time); // set calendar object with current time to pass to getGraphData
        return calendar;
    }

    private void sendHttpRequest(String urlparam) {
        final String url = ip+urlparam;
        //start up a single thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> {
                String responseBody = "";
                System.out.println("sending request to "+url);
                try {
                    InputStream response = new URL(url).openStream();
                    try (Scanner scanner = new Scanner(response)) {
                        responseBody = scanner.useDelimiter("\\A").next();
                        responseTextView.setText(responseBody);
                    }
                } catch (IOException e) {
                    //the thread will finally exit after executor.shutdownNow() has tried to stop it
                }
                return responseBody;
            }).get(threadTimeout,TimeUnit.SECONDS); //timeout of x seconds
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            responseTextView.setText("Arduino not reachable!");
            System.out.println("Request timed out.");
            //remove text again after two seconds by executing a delayed task
            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    responseTextView.setText("");
                    return null;
                }
            };
            service.schedule(task,2,TimeUnit.SECONDS);

            executor.shutdownNow();
        }
    }
}
