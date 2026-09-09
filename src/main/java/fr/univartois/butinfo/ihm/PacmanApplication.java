package fr.univartois.butinfo.ihm;

import java.io.IOException;

import controller.MenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PacmanApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Menu.fxml"));
        Parent root = loader.load();
        MenuController controller = loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root, 800, 950);

        stage.setScene(scene);
        stage.setTitle("Pacman");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
