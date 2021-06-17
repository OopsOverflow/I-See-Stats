package app;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EarthTest extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/gui/gui.fxml"));
		Scene scene = new Scene(root);
        scene.getStylesheets().add("/gui/style.css");
		primaryStage.setScene(scene);
		primaryStage.setTitle("tuto ihm");
		primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
