/**
 * Ce logiciel est distribué à des fins éducatives.
 *
 * (c) 2022-2026 Romain Wallon - Université d'Artois.
 * Tous droits réservés.
 */

package model;

import fr.univartois.butinfo.ihm.AbstractCharacter;

/**
 * La classe Player représente le personnage du joueur (Pacman).
 *
 * @author Romain Wallon
 *
 * @version 0.1.0
 */
public class Player extends AbstractCharacter {

    public Player(Partie partie) {
        super(3, partie);
    }

    @Override
    public String getName() {
        return "pacman";
    }
}
