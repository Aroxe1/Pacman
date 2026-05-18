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
    private GameMap gameMap;
    private Player player;
    private int score = 0;

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
        int nouvelleLigne = player.getRow() + changementLigne;
        int nouvelleColonne = player.getColumn() + changementColonne;

        // On gère le tunnel de téléportation sur les côtés
        if (nouvelleColonne < 0) {
            nouvelleColonne = NB_COLONNES - 1;
        } else if (nouvelleColonne >= NB_COLONNES) {
            nouvelleColonne = 0;
        }

        // On vérifie les murs et les limites
        if (gameMap.isOnMap(nouvelleLigne, nouvelleColonne)) {
            Tile cible = gameMap.get(nouvelleLigne, nouvelleColonne);
            if (cible.getContent() != TileContent.WALL) { // Si c'est pas un mur

                if (cible.getGum() == Gum.PACGUM) {
                    // On retire le gum et on incrémente le score
                    cible.setGum(null);
                    score += 10;
                    scoreLabel.setText("Score : " + score);
                }

                grilleImages[player.getRow()][player.getColumn()].setImage(null);

                // On met à jour les coordonnées
                player.setPosition(nouvelleLigne, nouvelleColonne);

                grilleImages[player.getRow()][player.getColumn()].setImage(imagePacman);
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
        gameMap = new GameMap(NB_LIGNES, NB_COLONNES);

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

        // Construction de la GameMap à partir du tableau d'initialisation
        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < NB_COLONNES; colonne++) {
                TileContent contenu = (carteNiveau[ligne][colonne] == 1)
                        ? TileContent.WALL
                        : TileContent.PATH;
                Tile tile = new Tile(ligne, colonne, contenu);
                if (carteNiveau[ligne][colonne] == 0) {
                    tile.setGum(Gum.PACGUM);
                }
                gameMap.set(ligne, colonne, tile);
            }
        }

        // On place Pacman au centre de la carte (Ligne 8, Colonne 10)
        player = new Player(8, 10, 3);
        gameMap.get(player.getRow(), player.getColumn()).setGum(null);

        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < NB_COLONNES; colonne++) {
                Tile tile = gameMap.get(ligne, colonne);

                if (tile.getContent() != TileContent.WALL) {
                    ImageView fond = new ImageView(imageChemin);
                    gameBoard.add(fond, colonne, ligne);
                }

                ImageView premierPlan = new ImageView();
                grilleImages[ligne][colonne] = premierPlan; // mise en place du premier plan pour empiler les images

                if (ligne == player.getRow() && colonne == player.getColumn()) {
                    premierPlan.setImage(imagePacman);
                }
                else if (tile.getContent() == TileContent.WALL) {
                    premierPlan.setImage(imageMur);
                }
                else if (tile.getGum() == Gum.PACGUM) {
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