package SolArduino;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("interface.fxml"));
        primaryStage.setTitle("SolArduino");
        Scene scene = new Scene(root, 650, 350);
        File file = new File("src/SolArduino/resources/stylesheet.css");
        scene.getStylesheets().add(file.toURI().toString());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("ic_solarduino.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
