package fr.univartois.butinfo.ihm;

import java.util.Random;

public final class GameMapFactory {

    private static final Random RANDOM = new Random();

    private GameMapFactory() {
        // Empêche l'instanciation de la fabrique.
    }

    public static GameMap createEmptyMap(int height, int width) {
        GameMap map = new GameMap(height, width);
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (row == 0 || row == height - 1 || column == 0 || column == width - 1) {
                    map.set(row, column, new Tile(row, column, TileContent.WALL));
                } else {
                    Tile tile = new Tile(row, column, TileContent.PATH);
                    tile.setGum(Gum.PACGUM);
                    map.set(row, column, tile);
                }
            }
        }
        return map;
    }

    public static GameMap createMapWithRegularIntermediateWall(int height, int width) {
        GameMap map = createEmptyMap(height, width);
        for (int row = 2; row < height - 2; row += 2) {
            for (int column = 2; column < width - 2; column += 2) {
                map.set(row, column, new Tile(row, column, TileContent.WALL));
            }
        }
        return map;
    }

    public static GameMap createMapWithRandomWalls(int height, int width) {
        GameMap map = createEmptyMap(height, width);
        for (int row = 1; row < height - 1; row++) {
            for (int column = 1; column < width - 1; column++) {
                if (RANDOM.nextInt(4) == 0) {
                    map.set(row, column, new Tile(row, column, TileContent.WALL));
                }
            }
        }
        return map;
    }
}
