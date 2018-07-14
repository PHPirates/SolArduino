package nl.deltadak.solarduino;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Class to create data for the table
 */
@SuppressWarnings({"WeakerAccess", "unused"}) //weird incorrect warnings
public class TableData {

    private final SimpleStringProperty time;
    private final SimpleDoubleProperty angle;
    public TableData(String time, double angle){
        this.time = new SimpleStringProperty(time);
        this.angle = new SimpleDoubleProperty(angle);
    }

    public SimpleStringProperty timeProperty(){
        return time;
    }

    public SimpleDoubleProperty angleProperty(){
        return angle;
    }

    public String getTime(){
        return time.get();
    }

    public double getAngle(){
        return angle.get();
    }
}
