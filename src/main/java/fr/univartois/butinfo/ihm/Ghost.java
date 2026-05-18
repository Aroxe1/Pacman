package fr.univartois.butinfo.ihm;

public class Ghost extends AbstractCharacter {

    private String color;

    public Ghost(int row, int column, int health, String color) {
        super(row, column, health);
        this.color = color;
    }

    @Override
    public String getName() {
        return color;
    }

    public String getColor() {
        return color;
    }
}
