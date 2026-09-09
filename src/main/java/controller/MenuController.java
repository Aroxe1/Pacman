package controller;

import java.io.IOException;

import model.PacmanInterface;
import model.Partie;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuController {

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onPlay() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Pacman.fxml"));
        Parent root = loader.load();
        PacmanInterface controller = loader.getController();

        Partie partie = new Partie();
        partie.setController(controller);
        controller.setPartie(partie);
        controller.setStage(stage);

        Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
        controller.setScene(scene);

        partie.start();

        stage.setScene(scene);
    }

    @FXML
    private void onQuit() {
        Platform.exit();
    }
}
