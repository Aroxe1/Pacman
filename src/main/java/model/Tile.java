/**
 * Ce logiciel est distribué à des fins éducatives.
 *
 * (c) 2022-2026 Romain Wallon - Université d'Artois.
 * Tous droits réservés.
 */

package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * La classe Tile représente une tuile composant la carte du jeu Pacman.
 * Son contenu est une propriété observable, et elle peut contenir une gomme.
 *
 * @author Romain Wallon
 *
 * @version 0.1.0
 */
public class Tile {

    /**
     * La ligne où cette tuile est positionnée sur la carte.
     */
    private final int row;

    /**
     * La colonne où cette tuile est positionnée sur la carte.
     */
    private final int column;

    /**
     * Le contenu de cette tuile, sous forme de propriété observable.
     */
    private final ObjectProperty<TileContent> content = new SimpleObjectProperty<>();

    /**
     * La gomme éventuellement présente sur cette tuile.
     */
    private final ObjectProperty<Gum> gum = new SimpleObjectProperty<>();

    /**
     * Construit une nouvelle instance de Tile.
     *
     * @param row La ligne où la tuile est positionnée sur la carte.
     * @param column La colonne où la tuile est positionnée sur la carte.
     */
    public Tile(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public TileContent getContent() {
        return content.get();
    }

    public void setContent(TileContent content) {
        this.content.set(content);
    }

    /**
     * Donne la propriété observable représentant le contenu de cette tuile.
     *
     * @return La propriété observable du contenu.
     */
    public ObjectProperty<TileContent> getContentProperty() {
        return content;
    }

    public Gum getGum() {
        return gum.get();
    }

    public void setGum(Gum gum) {
        this.gum.set(gum);
    }

    /**
     * Donne la propriété observable représentant la gomme présente sur cette tuile.
     *
     * @return La propriété observable de la gomme.
     */
    public ObjectProperty<Gum> getGumProperty() {
        return gum;
    }

    /**
     * Vérifie si cette tuile est vide (un personnage peut s'y déplacer).
     *
     * @return Si cette tuile est vide.
     */
    public boolean isEmpty() {
        TileContent c = content.get();
        return c != null && c.isEmpty();
    }
}
