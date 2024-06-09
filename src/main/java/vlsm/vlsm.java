package vlsm;// VlsmApp.java

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class vlsm extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Calculadora VLSM");
        var root = new VentanaVLSM();
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
