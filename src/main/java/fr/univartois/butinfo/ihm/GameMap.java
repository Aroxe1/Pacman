package fr.univartois.butinfo.ihm;

import java.util.ArrayList;
import java.util.List;

public class GameMap {

    private int height;
    private int width;
    private Tile[][] tiles;

    public GameMap(int height, int width) {
        this.height = height;
        this.width = width;
        this.tiles = new Tile[height][width];
        init();
    }

    private void init() {
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                tiles[row][column] = new Tile(row, column);
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isOnMap(int row, int column) {
        return row >= 0 && row < height && column >= 0 && column < width;
    }

    public Tile get(int row, int column) {
        return tiles[row][column];
    }

    public void set(int row, int column, Tile tile) {
        tiles[row][column] = tile;
    }

    public List<Tile> getEmptyTiles() {
        List<Tile> empties = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (tiles[row][column].isEmpty()) {
                    empties.add(tiles[row][column]);
                }
            }
        }
        return empties;
    }
}
