package fr.univartois.butinfo.ihm;

public class Tile {

    private int row;
    private int column;
    private TileContent content;
    private Gum gum;

    public Tile(int row, int column) {
        this.row = row;
        this.column = column;
        this.content = TileContent.PATH;
        this.gum = null;
    }

    public Tile(int row, int column, TileContent content) {
        this.row = row;
        this.column = column;
        this.content = content;
        this.gum = null;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public TileContent getContent() {
        return content;
    }

    public Gum getGum() {
        return gum;
    }

    public void setGum(Gum gum) {
        this.gum = gum;
    }

    public boolean isEmpty() {
        return content.isEmpty() && gum == null;
    }
}
