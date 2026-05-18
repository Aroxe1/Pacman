package fr.univartois.butinfo.ihm;

public class Player extends AbstractCharacter {

    public Player(int row, int column, int health) {
        super(row, column, health);
    }

    @Override
    public String getName() {
        return "pacman";
    }
}
