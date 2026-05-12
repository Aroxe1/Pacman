/**
 * Ce logiciel est distribué à des fins éducatives.
 *
 * Il est fourni "tel quel", sans garantie d'aucune sorte, explicite
 * ou implicite, notamment sans garantie de qualité marchande, d'adéquation
 * à un usage particulier et d'absence de contrefaçon.
 * En aucun cas, les auteurs ou titulaires du droit d'auteur ne seront
 * responsables de tout dommage, réclamation ou autre responsabilité, que ce
 * soit dans le cadre d'un contrat, d'un délit ou autre, en provenance de,
 * consécutif à ou en relation avec le logiciel ou son utilisation, ou avec
 * d'autres éléments du logiciel.
 *
 * (c) 2022-2026 Romain Wallon - Université d'Artois.
 * Tous droits réservés.
 */

package fr.univartois.butinfo.ihm;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import javafx.event.ActionEvent;

import java.net.URL;
import java.util.NoSuchElementException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PacmanController {

// Changer pour repasser en gridpane totalement probleme collision
    @FXML
    private StackPane gameArea;

    @FXML
    private Label title;

    @FXML
    private Label livesLabel;

    @FXML
    private Label scoreLabel;

    @FXML
    private GridPane gameBoard;

    @FXML
    private Pane entitiesPane;

    private static final int NB_LIGNES = 10;
    private static final int NB_COLONNES = 15;
    private static final int TAILLE_CASE = 32;

    private ImageView pacmanView;

    // Remplace les anciens deltaX/deltaY et VITESSE
    private double deltaX = 0;
    private double deltaY = 0;
    private double prochainDeltaX = 0;
    private double prochainDeltaY = 0;
    private final double VITESSE = 2;

    // Seuil d'alignement : on accepte un écart <= VITESSE
    private boolean estAligne(double valeur) {
        double reste = valeur % TAILLE_CASE;
        return reste <= VITESSE || reste >= (TAILLE_CASE - VITESSE);
    }

    private double snapValeur(double valeur) {
        return Math.round(valeur / TAILLE_CASE) * TAILLE_CASE;
    }

    private final int[][] carteNiveau = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    @FXML
    public void initialize() {
        System.out.println("Le jeu s'initialise !");
        // C'est ici qu'on commencera à dessiner la carte et placer les personnages

        double largeur = NB_COLONNES * TAILLE_CASE;
        double hauteur = NB_LIGNES * TAILLE_CASE;

        entitiesPane.setPrefSize(largeur, hauteur);
        entitiesPane.setMaxSize(largeur, hauteur);

        dessinerCarteNiveau();
        placerEntitesInitiales();

        initialiserClavier();
        initialiserBoucleJeu();
    }

    private void initialiserClavier() {
        gameArea.setFocusTraversable(true);
        javafx.application.Platform.runLater(() -> gameArea.requestFocus());

        gameArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    deltaX = 0;
                    deltaY = -VITESSE;
                    break;
                case DOWN:
                    deltaX = 0;
                    deltaY = VITESSE;
                    break;
                case LEFT:
                    deltaX = -VITESSE;
                    deltaY = 0;
                    break;
                case RIGHT:
                    deltaX = VITESSE;
                    deltaY = 0;
                    break;
                default:
                    break;
            }
        });
    }

    private void initialiserBoucleJeu() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double currentX = pacmanView.getX();
                double currentY = pacmanView.getY();

                // --- Tentative d'appliquer la direction en attente ---
                boolean changementHV = (prochainDeltaX != 0 && prochainDeltaY == 0); // demande horizontale
                boolean changementVH = (prochainDeltaY != 0 && prochainDeltaX == 0); // demande verticale

                if (changementHV && estAligne(currentY)) {
                    // Vérifie que la direction demandée n'est pas bloquée par un mur
                    double testX = pacmanView.getX() + prochainDeltaX;
                    double testMaxX = testX + TAILLE_CASE - 1;
                    double snapY = snapValeur(currentY);
                    double testMaxY = snapY + TAILLE_CASE - 1;

                    int tLigneHaut     = (int) (snapY    / TAILLE_CASE);
                    int tLigneBas      = (int) (testMaxY  / TAILLE_CASE);
                    int tColonneGauche = (int) (testX     / TAILLE_CASE);
                    int tColonneDroite = (int) (testMaxX   / TAILLE_CASE);

                    boolean murDevant = tLigneHaut < 0 || tLigneBas >= NB_LIGNES
                            || tColonneGauche < 0 || tColonneDroite >= NB_COLONNES
                            || carteNiveau[tLigneHaut][tColonneGauche] == 1
                            || carteNiveau[tLigneHaut][tColonneDroite] == 1
                            || carteNiveau[tLigneBas][tColonneGauche]  == 1
                            || carteNiveau[tLigneBas][tColonneDroite]  == 1;

                    if (!murDevant) {
                        // La voie est libre : on snap et on applique
                        pacmanView.setY(snapY);
                        deltaX = prochainDeltaX;
                        deltaY = prochainDeltaY;
                        // On efface la demande seulement quand elle est appliquée
                        prochainDeltaX = 0;
                        prochainDeltaY = 0;
                    }
                    // Sinon : on garde la demande en attente, elle sera retestée la prochaine frame

                } else if (changementVH && estAligne(currentX)) {
                    double snapX = snapValeur(currentX);
                    double testMaxX = snapX + TAILLE_CASE - 1;
                    double testY = pacmanView.getY() + prochainDeltaY;
                    double testMaxY = testY + TAILLE_CASE - 1;

                    int tLigneHaut     = (int) (testY     / TAILLE_CASE);
                    int tLigneBas      = (int) (testMaxY   / TAILLE_CASE);
                    int tColonneGauche = (int) (snapX     / TAILLE_CASE);
                    int tColonneDroite = (int) (testMaxX   / TAILLE_CASE);

                    boolean murDevant = tLigneHaut < 0 || tLigneBas >= NB_LIGNES
                            || tColonneGauche < 0 || tColonneDroite >= NB_COLONNES
                            || carteNiveau[tLigneHaut][tColonneGauche] == 1
                            || carteNiveau[tLigneHaut][tColonneDroite] == 1
                            || carteNiveau[tLigneBas][tColonneGauche]  == 1
                            || carteNiveau[tLigneBas][tColonneDroite]  == 1;

                    if (!murDevant) {
                        pacmanView.setX(snapX);
                        deltaX = prochainDeltaX;
                        deltaY = prochainDeltaY;
                        prochainDeltaX = 0;
                        prochainDeltaY = 0;
                    }
                }

                // --- Déplacement dans la direction courante ---
                double futurX    = pacmanView.getX() + deltaX;
                double futurY    = pacmanView.getY() + deltaY;
                double futurMaxX = futurX + TAILLE_CASE - 1;
                double futurMaxY = futurY + TAILLE_CASE - 1;

                int ligneHaut     = (int) (futurY    / TAILLE_CASE);
                int ligneBas      = (int) (futurMaxY  / TAILLE_CASE);
                int colonneGauche = (int) (futurX    / TAILLE_CASE);
                int colonneDroite = (int) (futurMaxX  / TAILLE_CASE);

                if (ligneHaut >= 0 && ligneBas < NB_LIGNES
                        && colonneGauche >= 0 && colonneDroite < NB_COLONNES) {

                    boolean mur = carteNiveau[ligneHaut][colonneGauche] == 1
                            || carteNiveau[ligneHaut][colonneDroite] == 1
                            || carteNiveau[ligneBas][colonneGauche]  == 1
                            || carteNiveau[ligneBas][colonneDroite]  == 1;

                    if (!mur) {
                        pacmanView.setX(futurX);
                        pacmanView.setY(futurY);
                    } else {
                        deltaX = 0;
                        deltaY = 0;
                    }
                }
            }
        };
        timer.start();
    }

    private Image loadImage(String nomImage) {
        try {
            URL url = getClass().getResource("assets/images/" + nomImage);
            return new Image(url.toExternalForm(), TAILLE_CASE, TAILLE_CASE, true, true);

        } catch (NullPointerException | IllegalArgumentException e) {
            throw new NoSuchElementException("Could not load image", e);
        }
    }

    @FXML
    public void dessinerCarteNiveau() {
        // 1. Nettoyer la grille
        gameBoard.getChildren().clear();
        gameBoard.getColumnConstraints().clear();
        gameBoard.getRowConstraints().clear();

        gameBoard.setAlignment(javafx.geometry.Pos.CENTER);

        for (int c = 0; c < NB_COLONNES; c++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPrefWidth(TAILLE_CASE); // On impose la taille de 32px
            gameBoard.getColumnConstraints().add(colConst);
        }

        for (int l = 0; l < NB_LIGNES; l++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(TAILLE_CASE); // On impose la taille de 32px
            gameBoard.getRowConstraints().add(rowConst);
        }



        Image imageMur = loadImage("wall.png");
        Image imageChemin = loadImage("path.png");



        // Remplissage de la grille avec des ImageView
        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < NB_COLONNES; colonne++) {
                ImageView imageView = new ImageView();

                if (carteNiveau[ligne][colonne] == 1) {
                    imageView.setImage(imageMur);
                }

                else if (carteNiveau[ligne][colonne] == 2) {}

                else{
                    imageView.setImage(imageChemin);
                }

                gameBoard.add(imageView, colonne, ligne);
            }
        }
    }

    private void placerEntitesInitiales() {
        // --- PAC-MAN ---
        Image imagePacman = loadImage("pacman.png");
        pacmanView = new ImageView(imagePacman);
        pacmanView.setX(7 * TAILLE_CASE);
        pacmanView.setY(5 * TAILLE_CASE);

        // --- PACGUM ---
        Image imagePacGum = loadImage("pacgum.png");

        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < NB_COLONNES; colonne++) {
                if (carteNiveau[ligne][colonne] == 0) {
                    ImageView uneGomme = new ImageView(imagePacGum);

                    uneGomme.setX(colonne * TAILLE_CASE);
                    uneGomme.setY(ligne * TAILLE_CASE);

                    entitiesPane.getChildren().add(uneGomme);
                }
            }
        }

        // --- FANTÔME ROUGE ---
        Image imageRed = loadImage("red.png");
        ImageView redView = new ImageView(imageRed);
        redView.setX(3 * TAILLE_CASE);
        redView.setY(1 * TAILLE_CASE);

        // --- FANTÔME BLEU ---
        Image imageBlue = loadImage("blue.png");
        ImageView blueView = new ImageView(imageBlue);
        blueView.setX(11 * TAILLE_CASE);
        blueView.setY(1 * TAILLE_CASE);

        // --- FANTÔME ORANGE ---
        Image imageOrange = loadImage("orange.png");
        ImageView orangeView = new ImageView(imageOrange);
        orangeView.setX(3 * TAILLE_CASE);
        orangeView.setY(8 * TAILLE_CASE);

        // --- FANTÔME ROSE ---
        Image imagePink = loadImage("pink.png");
        ImageView pinkView = new ImageView(imagePink);
        pinkView.setX(11 * TAILLE_CASE);
        pinkView.setY(8 * TAILLE_CASE);

        // On ajoute tout le monde dans le panneau des entités (au-dessus de la carte)
        entitiesPane.getChildren().addAll(pacmanView, redView, blueView, orangeView, pinkView);
    }


    @FXML
    public void restartButton(ActionEvent event) {
        System.out.println("Le bouton Rejouer a été cliqué !");
        initialize();
    }

}
