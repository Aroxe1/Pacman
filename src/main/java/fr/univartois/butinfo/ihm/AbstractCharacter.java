package fr.univartois.butinfo.ihm;

public abstract class AbstractCharacter {

    private int row;
    private int column;
    private int health;

    protected AbstractCharacter(int row, int column, int health) {
        this.row = row;
        this.column = column;
        this.health = health;
    }

    public abstract String getName();

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getHealth() {
        return health;
    }

    public void incHealth() {
        health++;
    }

    public void decHealth() {
        health--;
    }
}
