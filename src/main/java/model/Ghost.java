/**
 * Ce logiciel est distribué à des fins éducatives.
 *
 * (c) 2022-2026 Romain Wallon - Université d'Artois.
 * Tous droits réservés.
 */

package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.univartois.butinfo.ihm.AbstractCharacter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * La classe Ghost représente les fantômes adversaires du joueur dans le jeu Pacman.
 *
 * À chaque tick (toutes les 300 ms), le fantôme :
 * 1. Liste toutes les directions valides (pas de mur, pas hors carte).
 * 2. Exclut le demi-tour (comme dans le vrai Pacman).
 * 3. Tire au sort parmi les options restantes.
 * 4. Si aucune option (impasse), le demi-tour est autorisé.
 *
 * Cette ré-évaluation permanente évite que les fantômes restent coincés.
 *
 * @author Romain Wallon
 *
 * @version 0.1.0
 */
public class Ghost extends AbstractCharacter {

    private static final Random RANDOM = new Random();


    private final String color;


    private Timeline timeline;

    private int dirRow = 0;
    private int dirCol = 0;

    public Ghost(String color, Partie partie) {
        super(Integer.MAX_VALUE, partie);
        this.color = color;
    }

    @Override
    public String getName() {
        return color;
    }

    public void moveRandomly() {
        GameMap map = getPartie().getMap();
        if (map == null) return;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};


        List<int[]> valides = new ArrayList<>();
        for (int[] d : directions) {
            int newRow = getRow() + d[0];
            int newCol = getColumn() + d[1];

            // Tunnel horizontal
            if (newCol < 0) newCol = map.getWidth() - 1;
            else if (newCol >= map.getWidth()) newCol = 0;

            if (!map.isOnMap(newRow, newCol)) continue;
            if (map.get(newRow, newCol).getContent() == TileContent.WALL) continue;

            valides.add(d);
        }

        if (valides.isEmpty()) return; // Bloqué partout


        List<int[]> sansDemiTour = new ArrayList<>();
        for (int[] d : valides) {
            if (dirRow == 0 && dirCol == 0) {
                sansDemiTour.add(d);
            } else if (d[0] != -dirRow || d[1] != -dirCol) {
                sansDemiTour.add(d);
            }
        }


        int[] choix;
        if (!sansDemiTour.isEmpty()) {
            choix = sansDemiTour.get(RANDOM.nextInt(sansDemiTour.size()));
        } else {

            choix = valides.get(0);
        }

        dirRow = choix[0];
        dirCol = choix[1];

        if (dirRow == -1)      getPartie().moveUp(this);
        else if (dirRow == 1)  getPartie().moveDown(this);
        else if (dirCol == -1) getPartie().moveLeft(this);
        else if (dirCol == 1)  getPartie().moveRight(this);
    }

    /**
     * Démarre l'animation : déplacement aléatoire toutes les 300 ms.
     */
    public void animate() {
        this.timeline = new Timeline(
                new KeyFrame(Duration.millis(225), e -> moveRandomly()));
        this.timeline.setCycleCount(Animation.INDEFINITE);
        this.timeline.play();
    }

    /**
     * Arrête l'animation de ce fantôme.
     */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    /**
     * Réinitialise la direction
     */
    public void resetDirection() {
        dirRow = 0;
        dirCol = 0;
    }
}
