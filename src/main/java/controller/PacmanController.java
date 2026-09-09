package controller;

import java.io.IOException;

import fr.univartois.butinfo.ihm.AbstractCharacter;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.*;

public class PacmanController implements PacmanInterface{

    private static final int TAILLE_CASE = 28;

    @FXML
    private Button restartButton;
    @FXML
    private Button menuButton;
    @FXML
    private Button muteButton;
    @FXML
    private StackPane gameArea;
    @FXML
    private Label scoreLabel;
    @FXML
    private GridPane gameBoard;
    @FXML
    private HBox livesBox;

    private Partie partie;
    private Scene scene;
    private Stage stage;

    private double anglePacman = 0;
    private ImageView pacmanView;

    @Override
    public void setPartie(Partie partie) {
        this.partie = partie;
        scoreLabel.textProperty().bind(
                Bindings.concat("SCORE : ", partie.scoreProperty().asString()));
        partie.livesProperty().addListener((obs, ov, nv) -> mettreAJourVies(nv.intValue()));
        mettreAJourVies(partie.livesProperty().get());

        if (muteButton != null) {
            partie.getSoundManager().mutedProperty().addListener((obs, ov, nv) ->
                    muteButton.setText(nv ? "SON : OFF" : "SON : ON"));
            muteButton.setText(partie.getSoundManager().isMuted() ? "SON : OFF" : "SON : ON");
        }

        partie.gameOverProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                afficherGameOver();
            }
        });

        partie.victoryProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                afficherVictory();
            }
        });
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setScene(Scene scene) {
        this.scene = scene;
        scene.setOnKeyPressed(event -> {
            if (partie == null) return;
            switch (event.getCode()) {
                case Z: partie.movePlayerUp();    anglePacman = -90; appliquerRotation(); break;
                case S: partie.movePlayerDown();  anglePacman =  90; appliquerRotation(); break;
                case Q: partie.movePlayerLeft();  anglePacman = 180; appliquerRotation(); break;
                case D: partie.movePlayerRight(); anglePacman =   0; appliquerRotation(); break;
                default: break;
            }
        });
    }

    @Override
    public void prepare(GameMap map) {
        gameBoard.getChildren().clear();
        gameBoard.getColumnConstraints().clear();
        gameBoard.getRowConstraints().clear();
        gameBoard.setAlignment(javafx.geometry.Pos.CENTER);

        int height = map.getHeight();
        int width = map.getWidth();

        for (int c = 0; c < width; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(TAILLE_CASE);
            gameBoard.getColumnConstraints().add(cc);
        }
        for (int l = 0; l < height; l++) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(TAILLE_CASE);
            gameBoard.getRowConstraints().add(rc);
        }

        for (int ligne = 0; ligne < height; ligne++) {
            for (int colonne = 0; colonne < width; colonne++) {
                Tile tile = map.get(ligne, colonne);
                ImageView fond = new ImageView();
                fond.setFitWidth(TAILLE_CASE);
                fond.setFitHeight(TAILLE_CASE);
                fond.imageProperty().bind(Bindings.createObjectBinding(
                        () -> tile.getContent() == null ? null : loadImage(tile.getContent().getName()),
                        tile.getContentProperty()));
                gameBoard.add(fond, colonne, ligne);
            }
        }

        for (int ligne = 0; ligne < height; ligne++) {
            for (int colonne = 0; colonne < width; colonne++) {
                Tile tile = map.get(ligne, colonne);
                ImageView gomme = new ImageView();
                gomme.setFitWidth(TAILLE_CASE);
                gomme.setFitHeight(TAILLE_CASE);
                gomme.imageProperty().bind(Bindings.createObjectBinding(
                        () -> tile.getGum() == null ? null : loadImage(tile.getGum().getName()),
                        tile.getGumProperty()));
                gameBoard.add(gomme, colonne, ligne);
            }
        }
    }

    @Override
    public void bindPlayer(Player player) {
        pacmanView = new ImageView(loadImage(player.getName()));
        pacmanView.setFitWidth(TAILLE_CASE);
        pacmanView.setFitHeight(TAILLE_CASE);
        installerListenersPosition(pacmanView, player);
        gameBoard.add(pacmanView, player.getColumn(), player.getRow());
    }

    @Override
    public void bindGhost(Ghost ghost) {
        ImageView view = new ImageView();
        view.setFitWidth(TAILLE_CASE);
        view.setFitHeight(TAILLE_CASE);
        view.imageProperty().bind(Bindings.createObjectBinding(
                () -> partie.afraidProperty().get()
                        ? loadImage("hurt")
                        : loadImage(ghost.getName()),
                partie.afraidProperty()));
        installerListenersPosition(view, ghost);
        gameBoard.add(view, ghost.getColumn(), ghost.getRow());
    }

    private Image loadImage(String name) {
        return new Image(getClass().getResource(
                "/fr/univartois/butinfo/ihm/assets/images/" + name + ".png").toExternalForm(),
                TAILLE_CASE, TAILLE_CASE, true, true);
    }

    private void installerListenersPosition(ImageView view, AbstractCharacter character) {
        character.getRowProperty().addListener((obs, ov, nv) ->
                GridPane.setRowIndex(view, nv.intValue()));
        character.getColumnProperty().addListener((obs, ov, nv) ->
                GridPane.setColumnIndex(view, nv.intValue()));
    }

    private void appliquerRotation() {
        if (pacmanView != null) {
            pacmanView.setRotate(anglePacman);
        }
    }

    private void mettreAJourVies(int vies) {
        livesBox.getChildren().clear();
        for (int i = 0; i < vies; i++) {
            ImageView iv = new ImageView(loadImage("pacman"));
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            livesBox.getChildren().add(iv);
        }
    }

    private void afficherGameOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GameOver.fxml"));
            Parent root = loader.load();
            GameOverController gameOverCtrl = loader.getController();
            gameOverCtrl.setStage(stage);
            gameOverCtrl.setScore(partie.scoreProperty().get());
            Scene gameOverScene = new Scene(root, scene.getWidth(), scene.getHeight());
            Platform.runLater(() -> stage.setScene(gameOverScene));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherVictory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Victory.fxml"));
            Parent root = loader.load();
            VictoryController victoryCtrl = loader.getController();
            victoryCtrl.setStage(stage);
            victoryCtrl.setScore(partie.scoreProperty().get());
            Scene victoryScene = new Scene(root, scene.getWidth(), scene.getHeight());
            Platform.runLater(() -> stage.setScene(victoryScene));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMenu() {
        try {
            partie.stop();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Menu.fxml"));
            Parent root = loader.load();
            MenuController menuCtrl = loader.getController();
            menuCtrl.setStage(stage);
            stage.setScene(new Scene(root, scene.getWidth(), scene.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRestart() {
        partie.restart();
    }

    @FXML
    private void onMute() {
        partie.getSoundManager().toggleMute();
    }

}
