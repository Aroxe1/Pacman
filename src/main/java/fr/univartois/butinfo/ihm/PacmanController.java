package fr.univartois.butinfo.ihm;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.NoSuchElementException;
import javafx.scene.image.ImageView;

public class PacmanController {

    @FXML
    public Button restartButton;
    @FXML
    private StackPane gameArea;
    @FXML
    private Label livesLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private GridPane gameBoard;

    private static final int NB_LIGNES = 15;
    private static final int NB_COLONNES = 21;
    private static final int TAILLE_CASE = 32;

    private final int[][] carteNiveau = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,1,0,1},
            {1,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,0,1},
            {1,1,1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,1,1,1},
            {1,1,1,1,1,0,1,0,0,0,0,0,0,0,1,0,1,1,1,1,1},
            {0,0,0,0,0,0,0,0,1,1,0,1,1,0,0,0,0,0,0,0,0},
            {1,1,1,1,1,0,1,0,1,0,0,0,1,0,1,0,1,1,1,1,1},
            {1,1,1,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    // --- VARIABLES DU JEU ---
    private ImageView[][] grilleImages;
    private int pacmanLigne;
    private int pacmanColonne;

    private Image imagePacman;
    private Image imageMur;
    private Image imageChemin;
    private Image imagePacGum;

    @FXML
    public void initialize() {
        System.out.println("Le jeu s'initialise (Version Grille) !");


        imagePacman = loadImage("pacman.png");
        imageMur = loadImage("wall.png");
        imageChemin = loadImage("path.png");
        imagePacGum = loadImage("pacgum.png");

        dessinerCarteNiveau();
        initialiserClavier();
    }

    private void initialiserClavier() {
        gameArea.setFocusTraversable(true);
        javafx.application.Platform.runLater(() -> gameArea.requestFocus());

        gameArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Z:    deplacerPacman(-1, 0); break; // -1 ligne (on monte)
                case S:  deplacerPacman(1, 0);  break; // +1 ligne (on descend)
                case Q:  deplacerPacman(0, -1); break; // -1 colonne (gauche)
                case D: deplacerPacman(0, 1);  break; // +1 colonne (droite)
                default: break;
            }
        });
    }

    private void deplacerPacman(int changementLigne, int changementColonne) {
        int nouvelleLigne = pacmanLigne + changementLigne;
        int nouvelleColonne = pacmanColonne + changementColonne;

        // On gère le tunnel de téléportation sur les côtés
        if (nouvelleColonne < 0) {
            nouvelleColonne = NB_COLONNES - 1;
        } else if (nouvelleColonne >= NB_COLONNES) {
            nouvelleColonne = 0;
        }

        // On vérifie les murs et les limites
        if (nouvelleLigne >= 0 && nouvelleLigne < NB_LIGNES) {
            if (carteNiveau[nouvelleLigne][nouvelleColonne] != 1) { // Si c'est pas un mur

                if (carteNiveau[nouvelleLigne][nouvelleColonne] == 0) {
                    // On dit à la carte que la case est maintenant vide (2)
                    carteNiveau[nouvelleLigne][nouvelleColonne] = 2;
                }

                grilleImages[pacmanLigne][pacmanColonne].setImage(null);

                // On met à jour les coordonnées
                pacmanLigne = nouvelleLigne;
                pacmanColonne = nouvelleColonne;


                grilleImages[pacmanLigne][pacmanColonne].setImage(imagePacman);
            }
        }
    }

    @FXML
    public void dessinerCarteNiveau() {
        gameBoard.getChildren().clear();
        gameBoard.getColumnConstraints().clear();
        gameBoard.getRowConstraints().clear();
        gameBoard.setAlignment(javafx.geometry.Pos.CENTER);

        grilleImages = new ImageView[NB_LIGNES][NB_COLONNES];

        for (int c = 0; c < NB_COLONNES; c++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPrefWidth(TAILLE_CASE);
            gameBoard.getColumnConstraints().add(colConst);
        }

        for (int l = 0; l < NB_LIGNES; l++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(TAILLE_CASE);
            gameBoard.getRowConstraints().add(rowConst);
        }

        // On place Pacman au centre de la carte (Ligne 8, Colonne 10)
        pacmanLigne = 8;
        pacmanColonne = 10;
        carteNiveau[pacmanLigne][pacmanColonne] = 2;

        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < NB_COLONNES; colonne++) {

                if (carteNiveau[ligne][colonne] != 1) {
                    ImageView fond = new ImageView(imageChemin);
                    gameBoard.add(fond, colonne, ligne);
                }

                ImageView premierPlan = new ImageView();
                grilleImages[ligne][colonne] = premierPlan; // mise en place du premier plan pour empiler les images

                if (ligne == pacmanLigne && colonne == pacmanColonne) {
                    premierPlan.setImage(imagePacman);
                }
                else if (carteNiveau[ligne][colonne] == 1) {
                    premierPlan.setImage(imageMur);
                }
                else if (carteNiveau[ligne][colonne] == 0) {
                    premierPlan.setImage(imagePacGum);
                }

                gameBoard.add(premierPlan, colonne, ligne);
            }
        }
    }

    private Image loadImage(String nomImage) {
        try {
            URL url = getClass().getResource("assets/images/" + nomImage);
            return new Image(url.toExternalForm(), TAILLE_CASE, TAILLE_CASE, true, true);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new NoSuchElementException("Could not load image", e);
        }
    }

    //private int implementScore(){
        //int compteur = 0;

        //scoreLabel.setText("compteur");
    //}

    @FXML
    public void restartButton(ActionEvent event) {
        System.out.println("Le bouton Rejouer a été cliqué !");
        dessinerCarteNiveau();
    }
}