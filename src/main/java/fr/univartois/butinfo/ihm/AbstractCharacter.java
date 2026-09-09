/**
 * Ce logiciel est distribué à des fins éducatives.
 *
 * (c) 2022-2026 Romain Wallon - Université d'Artois.
 * Tous droits réservés.
 */

package fr.univartois.butinfo.ihm;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.Partie;

/**
 * La classe AbstractCharacter est la classe parente des différents personnages
 * pouvant se déplacer dans le jeu Pacman. Sa position est exposée via des
 * propriétés observables permettant la liaison de données.
 *
 * @author Romain Wallon
 *
 * @version 0.1.0
 */
public abstract class AbstractCharacter {

    /**
     * La ligne où se trouve ce personnage (propriété observable).
     */
    private final IntegerProperty row = new SimpleIntegerProperty();

    /**
     * La colonne où se trouve ce personnage (propriété observable).
     */
    private final IntegerProperty column = new SimpleIntegerProperty();

    /**
     * Les points de vie restants pour ce personnage.
     */
    private int health;

    /**
     * La partie en cours à laquelle ce personnage est rattaché.
     */
    private final Partie partie;

    /**
     * Crée une nouvelle instance de AbstractCharacter.
     *
     * @param initialHealth Les points de vie initiaux du personnage.
     * @param partie La partie en cours à laquelle ce personnage est rattaché.
     */
    protected AbstractCharacter(int initialHealth, Partie partie) {
        this.health = initialHealth;
        this.partie = partie;
    }

    public abstract String getName();

    public int getRow() {
        return row.get();
    }

    public IntegerProperty getRowProperty() {
        return row;
    }

    public int getColumn() {
        return column.get();
    }

    public IntegerProperty getColumnProperty() {
        return column;
    }

    public void setPosition(int row, int column) {
        this.row.set(row);
        this.column.set(column);
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

    public Partie getPartie() {
        return partie;
    }
}
