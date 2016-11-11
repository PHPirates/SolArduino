package SolArduino;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("interface.fxml"));
        primaryStage.setTitle("SolArduino");
        Scene scene = new Scene(root, 650, 350);
        // read the css this way so we don't get problems when creating an executable JAR
        scene.getStylesheets().add(getClass().getResource("resources/stylesheet.css").toExternalForm());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("ic_solarduino.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
