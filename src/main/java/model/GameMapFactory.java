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

package model;

import java.util.Random;

/**
 * La classe GameMapFactory est une classe utilitaire qui permet de créer
 * différentes cartes pour le jeu Pacman.
 *
 * @author Romain Wallon
 *
 * @version 0.1.0
 */
public class GameMapFactory {

    /**
     * Le générateur de nombres pseudo-aléatoires permettant de générer des cartes aléatoires.
     */
    private static final Random RANDOM = new Random();

    /**
     * La matrice décrivant la carte du niveau de Pacman.
     * <ul>
     *   <li>{@code 0} : un chemin contenant une PACGUM ;</li>
     *   <li>{@code 1} : un mur ;</li>
     *   <li>{@code 2} : un chemin vide ;</li>
     *   <li>{@code 3} : un chemin contenant une MEGAGUM.</li>
     * </ul>
     */
    private static final int[][] CARTE_NIVEAU = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
            {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,0,1,1,1,1,1,2,1,1,2,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,1,1,1,2,1,1,2,1,1,1,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,2,2,2,2,2,2,2,2,2,2,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,2,1,1,1,2,2,1,1,1,2,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,2,1,2,2,2,2,2,2,1,2,1,1,0,1,1,1,1,1,1},
            {2,2,2,2,2,2,0,2,2,2,1,2,2,2,2,2,2,1,2,2,2,0,2,2,2,2,2,2},
            {1,1,1,1,1,1,0,1,1,2,1,2,2,2,2,2,2,1,2,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,2,1,1,1,1,1,1,1,1,2,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,2,2,2,2,2,2,2,2,2,2,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,2,1,1,1,1,1,1,1,1,2,1,1,0,1,1,1,1,1,1},
            {1,1,1,1,1,1,0,1,1,2,1,1,1,1,1,1,1,1,2,1,1,0,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
            {1,0,0,0,1,1,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,1,1,0,0,0,1},
            {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
            {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
            {1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1},
            {1,3,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,3,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    /**
     * Empêche la création d'instances de cette classe.
     */
    private GameMapFactory() {
        throw new AssertionError("No GameMapFactory instances for you!");
    }

    /**
     * Crée la carte du niveau de Pacman à partir de la matrice {@link #CARTE_NIVEAU}.
     * Les murs, les chemins et les gommes (PACGUM et MEGAGUM) sont positionnés selon
     * la matrice.
     *
     * @param height Le nombre de lignes dans la carte.
     * @param width Le nombre de colonnes dans la carte.
     *
     * @return La carte qui a été créée.
     */
    public static GameMap createPacmanMap(int height, int width) {
        GameMap map = new GameMap(height, width);

        for (int ligne = 0; ligne < height; ligne++) {
            for (int colonne = 0; colonne < width; colonne++) {
                Tile tile = map.get(ligne, colonne);
                int v = CARTE_NIVEAU[ligne][colonne];
                tile.setContent(v == 1 ? TileContent.WALL : TileContent.PATH);
                if (v == 0) {
                    tile.setGum(Gum.PACGUM);
                } else if (v == 3) {
                    tile.setGum(Gum.MEGAGUM);
                } else {
                    tile.setGum(null);
                }
            }
        }

        return map;
    }

    /**
     * Crée une carte vide, sur laquelle les murs se trouvent uniquement sur les côtés.
     *
     * @param height Le nombre de lignes dans la carte.
     * @param width Le nombre de colonnes dans la carte.
     *
     * @return La carte qui a été créée.
     */
    public static GameMap createEmptyMap(int height, int width) {
        GameMap map = new GameMap(height, width);

        // On place les murs en haut et en bas de la carte.
        for (int i = 0; i < width; i++) {
            map.get(0, i).setContent(TileContent.WALL);
            map.get(height - 1, i).setContent(TileContent.WALL);
        }

        // On place les murs à gauche et à droite de la carte.
        for (int i = 0; i < height; i++) {
            map.get(i, 0).setContent(TileContent.WALL);
            map.get(i, width - 1).setContent(TileContent.WALL);
        }

        // Entre les murs, il y a des chemins partout.
        for (int i = 1; i < (height - 1); i++) {
            for (int j = 1; j < (width - 1); j++) {
                map.get(i, j).setContent(TileContent.PATH);
            }
        }

        return map;
    }

    /**
     * Crée une carte avec des murs positionnés à intervalle régulier à l'intérieur
     * de la carte.
     *
     * @param height Le nombre de lignes dans la carte.
     * @param width Le nombre de colonnes dans la carte.
     *
     * @return La carte qui a été créée.
     */
    public static GameMap createMapWithRegularIntermediateWall(int height, int width) {
        GameMap map = createEmptyMap(height, width);

        // On rajoute les murs à intervalle régulier.
        for (int i = 2; i < (height - 1); i += 2) {
            for (int j = 2; j < (width - 2); j++) {
                map.get(i, j).setContent(TileContent.WALL);
            }
        }

        return map;
    }

    /**
     * Crée une carte avec des murs solides positionnés à intervalle régulier à l'intérieur
     * de la carte, et un certain nombre de murs de briques répartis aléatoirement sur la
     * carte.
     *
     * @param height Le nombre de lignes dans la carte.
     * @param width Le nombre de colonnes dans la carte.
     *
     * @return La carte qui a été créée.
     */
    public static GameMap createMapWithRandomWalls(int height, int width) {
        GameMap map = createMapWithRegularIntermediateWall(height, width);

        // On place un mur aléatoire sur chaque ligne sans murs.
        for (int i = 3; i < height - 2; i += 2) {
            map.get(i, RANDOM.nextInt(1, width - 1)).setContent(TileContent.WALL);
        }

        return map;
    }

}
