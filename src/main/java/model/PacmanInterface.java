package model;

import javafx.scene.Scene;
import javafx.stage.Stage;


public interface PacmanInterface {

    void setPartie(Partie partie);


    void setStage(Stage stage);


    void setScene(Scene scene);


    void prepare(GameMap map);


    void bindPlayer(Player player);


    void bindGhost(Ghost ghost);
}