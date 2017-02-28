package SolArduino;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.*;

//lots of variables that we really want to be easily found
@SuppressWarnings("FieldCanBeLocal")
public class Controller implements Initializable {
    
    //Global variables for easy change/lookup
    private String timesFileString = "resources/times.csv";
    // path to the times.csv file
    private String anglesFileString = "resources/angles.csv";
    // path to the angles.csv file
    private String separator = ",";
    
    private Timer timerUp; // access is global to cancel it on button release
    private Timer timerDown; // access is global to cancel it on button release
    private Timer animationTimer; // access is global to be able to cancel it
    
    private int timeout = 1000; // timeout for sending up/down requests
    private String ip = "http://192.168.8.42/?"; // ip address from the Arduino
    private String currentVersionString = "Version 1.2";
    // current version of the desktop app
    private String lastVersionString;
    private String checkVersionLink =
            "https://raw.githubusercontent.com/PHPirates/SolArduino/"
                    + "master/SolArduino-desktop/version.txt";
    private String jarLink =
            "https://github.com/PHPirates/SolArduino/raw/master/SolArduino"
                    + "-desktop/"
                    + "out/artifacts/SolArduino_desktop_jar/SolArduino"
                    + "-desktop.jar";
    
    private boolean animationRunning = false;
    // boolean that keeps track of whether an animation is going on
    
    // array with animation speeds.
    // first half are for backwards, second half for forwards
    private final int[] animationSpeed = {
            100,
            200,
            300,
            500,
            750,
            750,
            500,
            300,
            200,
            100
    };
    private int middle = animationSpeed.length / 2; // default for count
    // int to keep track of where in the animationSpeed we are, and thus
    // whether animation goes forwards or backwards
    private int count = middle;
    
    private long[][] data; // contains the times and angles from the csv files
    
    private Calendar chosenDate;// calendar with chosen date from the DatePicker
    
    private int angle = 42; // angle at which the solar panels are set
    @SuppressWarnings("FieldCanBeLocal") private int threadTimeout = 2;//seconds
    @SuppressWarnings("FieldCanBeLocal") private int textTimeout = 2;
    // seconds after which the text 'arduino not reachable' disappears
    
    private XYChart.Series<Double, Double> lower = new XYChart.Series<>();
    private XYChart.Series<Double, Double> upper = new XYChart.Series<>();
    
    @FXML private GridPane controlGridPane;
    @FXML private Slider slider;
    @FXML private Text angleTextView;
    @FXML private Text responseTextView;
    @FXML private Text currentVersion;
    @FXML private Text lastVersion;
    @FXML private Button buttonUp;
    @FXML private Button buttonDown;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonAuto;
    @FXML private Button buttonSetAngle;
    @FXML private Button versionUpdate;
    @FXML private LineChart<Double, Double> graph;
    @FXML private DatePicker datePicker;
    @FXML private TableView<TableData> table;
    
    /**
     * Initialization method for the controller.
     */
    @FXML
    public void initialize(final URL location,
                           final ResourceBundle resourceBundle) {
        
        currentVersion.setText(currentVersionString);
        
        // make the buttons with the images resize by adding a listener to
        // the GridPane size - Don't see why this works
        controlGridPane.heightProperty()
                .addListener((observable, oldValue, newValue) -> {
                    buttonUp.setPrefHeight(newValue.intValue());
                    buttonDown.setPrefHeight(newValue.intValue());
                    buttonUpdate.setStyle(
                            "-fx-font-size: " + (Double)newValue / 20 + "px;");
                    buttonSetAngle.setStyle(
                            "-fx-font-size: " + (Double)newValue / 25 + "px;");
                    buttonAuto.setStyle(
                            "-fx-font-size: " + (Double)newValue / 20 + "px;");
                });
        
        controlGridPane.widthProperty()
                .addListener((observable, oldValue, newValue) -> {
                    buttonUp.setPrefWidth(newValue.intValue());
                    buttonDown.setPrefWidth(newValue.intValue());
                });
        
        // listener to detect changes for the slider
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            angle = newValue.intValue();
            // set text at the setAngle button
            if (angle < 10) {
                // little bit alignment, because of 1-digit numbers
                buttonSetAngle.setText("set panels at  " + angle + "  degrees");
            } else {
                buttonSetAngle.setText("set panels at " + angle + " degrees");
            }
            
        });
        
        chosenDate = Calendar.getInstance(); // create calendar object
        // listener to detect changes for the DatePicker
        datePicker.valueProperty().addListener((observable, oldDay, newDay) -> {
            // create Date object from the start of the newDay
            Date date = Date.from(newDay.atStartOfDay(ZoneId.systemDefault())
                                          .toInstant());
            chosenDate.setTime(date); // set the date of chosenDate to newDay
            
            graph.setData(getGraphData(
                    chosenDate)); // change the drawing for the graph
            
        });
        
        try {
            // read the times.csv and angles.csv files
            // times.csv contains the UNIX times in seconds (since January 1,
            // 00:00:00:00)
            
            InputStream timesIS = getClass()
                    .getResourceAsStream(timesFileString);
            InputStream anglesIS = getClass()
                    .getResourceAsStream(anglesFileString);
            
            InputStreamReader timesISReader = new InputStreamReader(timesIS);
            InputStreamReader anglesISReader = new InputStreamReader(anglesIS);
            
            BufferedReader timesReader = new BufferedReader(
                    timesISReader); // BufferedReader to read times file
            BufferedReader anglesReader = new BufferedReader(
                    anglesISReader); // BufferedReader to read angles file
            
            String timeLine; // line with the times
            while ((timeLine = timesReader.readLine()) != null) {
                // runs only once, since times.csv consists of only one line
                String[] timesString = timeLine
                        .split(separator); // split the times at the commas
                data = new long[timesString.length][2]; // create data[][]
                // based on the number of times/angles
                for (int i = 0; i < timesString.length; i++) {
                    data[i][0] = Long.valueOf(
                            timesString[i]); // fill data[][] with the times
                }
                
                // we can safely read the angles file here, since this only
                // runs once
                String angleLine; // line with the angles
                while ((angleLine = anglesReader.readLine()) != null) {
                    String[] anglesString = angleLine
                            .split(separator); // split angles at the commas
                    for (int i = 0; i < timesString.length; i++) {
                        data[i][1] = Long.valueOf(
                                anglesString[i]); // fill data[][] with the
                        // angles
                    }
                }
                anglesReader.close();
            }
            timesReader.close();
            
        } catch (Exception e) { // catch Exception (FileNotFoundException?)
            e.printStackTrace();
        }
        
        // initialize angle graph with today's angles
        Calendar calendar = Calendar.getInstance();
        Date time = new Date(
                System.currentTimeMillis()); // date with current time
        calendar.setTime(
                time); // set calendar object with current time to pass to
        // getGraphData
        
        // lower and upper contain data to draw the lines to clarify the
        // range of the solar panels in the graph
        lower.getData().add(new XYChart.Data<>((double)5, (double)5));
        lower.getData().add(new XYChart.Data<>((double)23, (double)5));
        upper.getData().add(new XYChart.Data<>((double)5, (double)57));
        upper.getData().add(new XYChart.Data<>((double)23, (double)57));
        
        graph.setData(getGraphData(getToday()));
        
    }
    
    @FXML
    protected void buttonUp(final MouseEvent event) {
        if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            sendHttpRequest("panel=up");
            timerUp = new Timer();
            TimerTask sendUpTask = new TimerTask() {
                @Override
                public void run() {
                    sendHttpRequest("panel=up");
                    sendUpdateRequest();
                }
            };
            timerUp.schedule(sendUpTask, timeout, timeout);
            
        } else { // mouse released
            sendHttpRequest("panel=stop");
            timerUp.cancel();
        }
    }
    
    @FXML
    protected void buttonDown(final MouseEvent event) {
        if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            sendHttpRequest("panel=down");
            timerDown = new Timer();
            TimerTask sendDownTask = new TimerTask() {
                @Override
                public void run() {
                    sendHttpRequest("panel=down");
                    sendUpdateRequest();
                }
            };
            timerDown.schedule(sendDownTask, timeout, timeout);
        } else { // mouse released
            sendHttpRequest("panel=stop");
            timerDown.cancel();
        }
    }
    
    @FXML
    protected void buttonAuto() {
        sendHttpRequest("panel=auto");
    }
    
    @FXML
    protected void buttonUpdate() {
        sendUpdateRequest();
    }
    
    @FXML
    protected void setAngle() {
        sendHttpRequest("degrees=" + angle);
    }
    
    @FXML
    protected void todayGraph() {
        if (animationRunning) {
            stopAnimation();
        }
        graph.setData(getGraphData(getToday()));
    }
    
    @FXML
    protected void nextDayGraph() {
        LocalDate nextDay = datePicker
                .getValue(); // get date that's on DatePicker
        nextDay = nextDay.plusDays(1); // add one day
        
        graph.setData(getGraphData(
                localDateToCalendar(nextDay))); // display new graph
    }
    
    @FXML
    protected void previousDayGraph() {
        LocalDate previousDay = datePicker.getValue();
        previousDay = previousDay.minusDays(1);
        
        graph.setData(getGraphData(localDateToCalendar(previousDay)));
    }
    
    @FXML
    protected void playAnimationForward() {
        if (count < (animationSpeed.length
                - 1)) { // to avoid IndexOutOfBound exceptions
            if (animationRunning) { // check if there is an animation going on
                animationTimer.cancel();
            }
            
            // increase count before getting the speed, if we increase count
            // afterwards,
            // the animation would first go faster if we press back and only
            // go slower/backwards the second time
            count++;
            int speed = animationSpeed[count];
            playAnimation(speed);
        }
        
    }
    
    @FXML
    protected void playAnimationBackward() {
        if (count > 0) {
            if (animationRunning) {
                animationTimer.cancel();
            }
            
            count--;
            int speed = animationSpeed[count];
            playAnimation(speed);
            
        }
        
    }
    
    @FXML
    protected void stopAnimation() {
        if (animationRunning) { // check if animation is running
            animationTimer.cancel();
            animationRunning = false;
            count = middle; // set count to the default value
        }
    }
    
    @FXML
    protected void checkVersion() {
        System.out.println("checking version");
        checkVersionOnline(
                checkVersionLink); // check what the last version is from the
        // txt file online
        if (currentVersionString
                .equals(lastVersionString)) { // if up to date show text to
            // say so (and hide update button)
            lastVersion.setText("Up to date.");
            versionUpdate.setVisible(false);
        } else { // if not up to date, show
            // button with download link
            versionUpdate.setText("Update to " + lastVersionString);
            versionUpdate.setVisible(true);
        }
    }
    
    @FXML
    protected void getDownloadLink() {
        try {
            Desktop.getDesktop().browse(new URI(
                    jarLink)); // open link with the last jar file in a browser
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }
    
    /**
     * play animation
     *
     * @param speed
     *         to play with
     */
    private void playAnimation(final int speed) {
        animationTimer = new Timer(); // create new timer
        animationRunning = true;
        TimerTask animationTimerTask = new TimerTask() {
            
            @Override
            public void run() {
                Platform.runLater(() -> { // runLater to avoid not being on
                    // fx-application thread
                    // if count is higher than the default, the
                    // animation goes forward. Backwards when it's
                    // lower.
                    if (count >= middle) {
                        nextDayGraph();
                    } else {
                        previousDayGraph();
                    }
                });
            }
        };
        animationTimer.schedule(animationTimerTask, 10, speed);
    }
    
    /**
     * convert date to calendar
     *
     * @param localDate
     *         to be converted
     *
     * @return converted calendar
     */
    private Calendar localDateToCalendar(final LocalDate localDate) {
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault())
                                      .toInstant()); // first convert
        // LocalDate to Date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date); // convert Date to Calendar
        return cal;
    }
    
    /**
     * @param day
     *         for which data is returned
     *
     * @return list that contains data for graph
     */
    private ObservableList<XYChart.Series<Double, Double>> getGraphData(
            Calendar day) {
        
        int year = day.get(Calendar.YEAR); // get year of chosen date
        int month = day.get(Calendar.MONTH); // get month of chosen date
        int dayOfMonth = day
                .get(Calendar.DAY_OF_MONTH); // get day of the month of
        // chosen date
        TimeZone timeZone = TimeZone
                .getDefault(); // get local timezone (with DST)
        Calendar startCalendar = Calendar
                .getInstance(); // calendar with first second of day we want
        // the graph for
        startCalendar
                .set(year, month, dayOfMonth, 0, 0, 0); // months start at 0
        long start = startCalendar.getTimeInMillis();
        long offset = timeZone.getOffset(
                start); // calculate the offset because of time zone using
        // the start time
        start += offset; // set the actual start time, with correct time zone
        
        Calendar endCalendar = Calendar
                .getInstance(); // calendar with last second of day we want
        // the graph for
        endCalendar.set(year, month, dayOfMonth, 23, 59, 59);
        long end = endCalendar.getTimeInMillis()
                + offset; // set end time with correct time zone
        
        XYChart.Series<Double, Double> series = new XYChart.Series<>();
        ObservableList<XYChart.Series<Double, Double>> list = FXCollections
                .observableArrayList();
        series.setName("angles");
        series.getData().clear();
        
        ObservableList<TableData> tableList = FXCollections
                .observableArrayList();
        
        for (int i = 0; i < data.length; i++) {
            
            int length = String.valueOf(data[i][0]).length();
            if (length == 10) {
                // time * 1000 to convert to milliseconds, then add offset
                // for correct time zone
                data[i][0] = (data[i][0] * 1000) + offset;
            }
            
            if ((start < data[i][0]) && (data[i][0]
                    < end)) { // find times during the given day
                Date date = new Date(
                        data[i][0]); // create DateObject with times in
                // milliseconds
                DateFormat hourFormat = new SimpleDateFormat(
                        "HH"); // DateFormat to get the hour of day
                double hour = Double.valueOf(hourFormat.format(date)); //
                // double so we can add the minutes to it
                
                DateFormat minuteFormat = new SimpleDateFormat(
                        "mm"); // DateFormat to get the minute of hour
                double minute = Double.valueOf(minuteFormat.format(date))
                        / 60; // convert minutes to part of hour (decimal)
                
                double time = hour
                        + minute; // double with the time, e.g.: 12:45 =
                // 12.75, so it's displayed neatly in the graph
                double angle = (double)data[i][1] / 10;
                series.getData()
                        .add(new XYChart.Data<>(time, angle)); // add time
                // and angle to the data of the graph
                
                DateFormat timeFormat = new SimpleDateFormat(
                        "HH:mm"); // format to show time in the table
                String timeString = String.valueOf(timeFormat.format(date));
                TableData tableData = new TableData(timeString, angle);
                tableList.add(tableData);
            }
        }
        
        table.setItems(tableList);
        table.setMaxHeight((table.getItems().size() + 1)
                                   * 30); // set height of the table so we
        // don't have empty rows
        
        datePicker.setValue(day.toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate()); // set default date in
        // textbox DatePicker
        
        //noinspection unchecked because this is not to be fixed at call site
        list.addAll(lower, upper, series);
        return list;
    }
    
    /**
     * @return calendar with current date and time
     */
    private Calendar getToday() {
        Calendar calendar = Calendar.getInstance();
        Date time = new Date(
                System.currentTimeMillis()); // date with current time
        calendar.setTime(
                time); // set calendar object with current time to pass to
        // getGraphData
        return calendar;
    }
    
    private void checkVersionOnline(final String url) {
        //start up a single thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> {
                String line = "";
                System.out.println("sending request to " + url);
                try {
                    InputStream response = new URL(url).openStream();
                    try (Scanner scanner = new Scanner(response)) {
                        line = scanner.nextLine();
                        lastVersion.setText("Last version: " + line);
                        lastVersionString = line;
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    //the thread will finally exit after executor.shutdownNow
                    // () has tried to stop it
                }
                return line;
            }).get(threadTimeout, TimeUnit.SECONDS); //timeout of x seconds
        } catch (InterruptedException | ExecutionException | TimeoutException
                e) {
            System.out.println("Request timed out.");
            executor.shutdownNow();
        }
    }
    
    //normal http request and update request put response in a different
    // textview
    private void sendHttpRequest(final String urlparam) {
        sendHttpRequest(urlparam, responseTextView);
    }
    
    private void sendUpdateRequest() {
        sendHttpRequest("update", angleTextView);
    }
    
    private void sendHttpRequest(final String urlparam,
                                 final Text responseTextView) {
        final String url = ip + urlparam;
        //start up a single thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> {
                String responseBody = "";
                System.out.println("sending request to " + url);
                try {
                    InputStream response = new URL(url).openStream();
                    try (Scanner scanner = new Scanner(response)) {
                        responseBody = scanner.useDelimiter("\\A").next();
                        responseTextView.setText(responseBody);
                    }
                } catch (IOException e) {
                    //the thread will finally exit after executor.shutdownNow
                    // () has tried to stop it
                }
                return responseBody;
            }).get(threadTimeout, TimeUnit.SECONDS); //timeout of x seconds
        } catch (InterruptedException | ExecutionException | TimeoutException
                e) {
            responseTextView.setText("Arduino not reachable!");
            System.out.println("Request timed out.");
            //remove text again after two seconds by executing a delayed task
            ScheduledExecutorService service = Executors
                    .newScheduledThreadPool(1);
            Runnable task = new Task() {
                @Override
                protected Object call() throws Exception {
                    responseTextView.setText("");
                    return null;
                }
            };
            service.schedule(task, textTimeout, TimeUnit.SECONDS);
            
            executor.shutdownNow();
        }
    }
}
