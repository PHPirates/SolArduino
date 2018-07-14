package nl.deltadak.solarduino;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main class.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/interface.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("SolArduino");
        Scene scene = new Scene(root, 750, 400);
        // read the css this way so we don't get problems when creating an executable JAR
        scene.getStylesheets().add(getClass().getResource("/stylesheet.css").toExternalForm());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/ic_solarduino.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main method.
     * @param args System args.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
