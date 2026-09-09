package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.univartois.butinfo.ihm.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;


/**
 * Cette classe joue le rôle de façade
 */
public class Partie {

    private static final int NB_LIGNES = 31;
    private static final int NB_COLONNES = 28;
    private static final int PACMAN_DEPART_LIGNE = 23;
    private static final int PACMAN_DEPART_COLONNE = 13;
    private static final int TICKS_AFRAID_MAX = 40;
    private static final int POINTS_PACGUM = 10;
    private static final int POINTS_MEGAGUM = 30;
    private static final int POINTS_FANTOME = 200;

    private PacmanInterface controller;
    private GameMap map;
    private Player player;
    private final List<Ghost> ghosts = new ArrayList<>();
    private final Map<Ghost, int[]> ghostSpawns = new HashMap<>();

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty lives = new SimpleIntegerProperty(3);
    private final BooleanProperty afraid = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOver = new SimpleBooleanProperty(false);
    private final BooleanProperty victory = new SimpleBooleanProperty(false);

    public BooleanProperty victoryProperty() {
        return victory;
    }

    private int ticksAfraidRestants = 0;
    private int currentDirRow = 0;
    private int currentDirCol = 0;
    private int desiredDirRow = 0;
    private int desiredDirCol = 0;
    private int gommesRestantes = 0;

    private Timeline playerTimeline;
    private final Random random = new Random();
    private final SoundManager soundManager = new SoundManager();

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public void placeOnMap(AbstractCharacter character) {
        List<Tile> empties = map.getEmptyTiles();
        if (empties.isEmpty()) return;
        Tile choisie = empties.get(random.nextInt(empties.size()));
        character.setPosition(choisie.getRow(), choisie.getColumn());
    }

    public void setController(PacmanInterface controller) {
        this.controller = controller;
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public BooleanProperty afraidProperty() {
        return afraid;
    }

    public BooleanProperty gameOverProperty() {
        return gameOver;
    }

    public int getScore() {
        return score.get();
    }

    public GameMap getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    public void start() {
        map = GameMapFactory.createPacmanMap(NB_LIGNES, NB_COLONNES);
        gommesRestantes = 0;
        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < NB_COLONNES; colonne++) {
                if (map.get(ligne, colonne).getGum() != null) {
                    gommesRestantes++;
                }
            }
        }

        controller.prepare(map);

        player = new Player(this);
        player.setPosition(PACMAN_DEPART_LIGNE, PACMAN_DEPART_COLONNE);
        Tile tileDepart = map.get(PACMAN_DEPART_LIGNE, PACMAN_DEPART_COLONNE);
        if (tileDepart.getGum() != null) {
            gommesRestantes--;
        }
        tileDepart.setGum(null);
        controller.bindPlayer(player);

        ghosts.clear();
        ghostSpawns.clear();
        ajouterFantome("red",    14, 13);
        ajouterFantome("blue",   14, 14);
        ajouterFantome("pink",   14, 12);
        ajouterFantome("orange", 14, 15);

        score.set(0);
        lives.set(3);
        afraid.set(false);
        gameOver.set(false);
        victory.set(false);
        ticksAfraidRestants = 0;
        currentDirRow = 0;
        currentDirCol = 0;
        desiredDirRow = 0;
        desiredDirCol = 0;

        for (Ghost g : ghosts) {
            g.animate();
        }

        if (playerTimeline != null) {
            playerTimeline.stop();
        }
        playerTimeline = new Timeline(new KeyFrame(Duration.millis(125), e -> {
            if ((desiredDirRow != 0 || desiredDirCol != 0)
                    && peutBouger(player, desiredDirRow, desiredDirCol)) {
                currentDirRow = desiredDirRow;
                currentDirCol = desiredDirCol;
            }
            if (currentDirRow != 0 || currentDirCol != 0) {
                deplacer(player, currentDirRow, currentDirCol);
            }
            if (ticksAfraidRestants > 0) {
                ticksAfraidRestants--;
                if (ticksAfraidRestants == 0) {
                    afraid.set(false);
                }
            }
        }));
        playerTimeline.setCycleCount(Animation.INDEFINITE);
        playerTimeline.play();

        soundManager.play("Voicy_Pacman-music");
    }

    private void ajouterFantome(String couleur, int ligne, int colonne) {
        Ghost g = new Ghost(couleur, this);
        g.setPosition(ligne, colonne);
        ghosts.add(g);
        ghostSpawns.put(g, new int[]{ligne, colonne});
        controller.bindGhost(g);
    }

    public void setPlayerDirection(int dirRow, int dirCol) {
        this.desiredDirRow= dirRow;
        this.desiredDirCol = dirCol;
    }

    public void movePlayerUp() {
        setPlayerDirection(-1, 0);
    }

    public void movePlayerDown() {
        setPlayerDirection(1, 0);
    }

    public void movePlayerLeft() {
        setPlayerDirection(0, -1);
    }

    public void movePlayerRight() {
        setPlayerDirection(0, 1);
    }

    public void moveUp(AbstractCharacter c) {
        deplacer(c, -1, 0);
    }

    public void moveDown(AbstractCharacter c) {
        deplacer(c, 1, 0);
    }

    public void moveLeft(AbstractCharacter c) {
        deplacer(c, 0, -1);
    }

    public void moveRight(AbstractCharacter c) {
        deplacer(c, 0, 1);
    }

    private boolean peutBouger(AbstractCharacter c, int dRow, int dCol) {
        int newRow = c.getRow() + dRow;
        int newCol = c.getColumn() + dCol;

        if (newCol < 0) newCol = map.getWidth() - 1;
        else if (newCol >= map.getWidth()) newCol = 0;

        if (!map.isOnMap(newRow, newCol)) return false;
        return map.get(newRow, newCol).getContent() != TileContent.WALL;
    }

    private void deplacer(AbstractCharacter c, int dRow, int dCol) {
        int newRow = c.getRow() + dRow;
        int newCol = c.getColumn() + dCol;

        if (newCol < 0) newCol = map.getWidth() - 1;
        else if (newCol >= map.getWidth()) newCol = 0;

        if (!map.isOnMap(newRow, newCol)) return;
        Tile cible = map.get(newRow, newCol);
        if (cible.getContent() == TileContent.WALL) return;

        if (c == player) {
            if (cible.getGum() == Gum.PACGUM) {
                cible.setGum(null);
                gommesRestantes--;
                score.set(score.get() + POINTS_PACGUM);
                soundManager.play("Pacman-chomp");
            } else if (cible.getGum() == Gum.MEGAGUM) {
                cible.setGum(null);
                gommesRestantes--;
                score.set(score.get() + POINTS_MEGAGUM);
                ticksAfraidRestants = TICKS_AFRAID_MAX;
                afraid.set(true);
                soundManager.play("megagum");
            }
        }

        c.setPosition(newRow, newCol);
        verifierCollisions();

        // Victoire : toutes les gommes ont été mangées
        if (c == player && gommesRestantes <= 0 && !gameOver.get()) {
            victory();
        }
    }

    private void verifierCollisions() {
        if (player == null) return;
        for (Ghost g : ghosts) {
            if (g.getRow() == player.getRow() && g.getColumn() == player.getColumn()) {
                if (afraid.get()) {
                    int[] spawn = ghostSpawns.get(g);
                    g.setPosition(spawn[0], spawn[1]);
                    g.resetDirection();
                    score.set(score.get() + POINTS_FANTOME);
                    soundManager.play("Voicy_Hitmarker");
                } else {
                    soundManager.play("Voicy_Pacman-Death");
                    player.decHealth();
                    lives.set(player.getHealth());
                    currentDirRow = 0;
                    currentDirCol = 0;
                    if (player.getHealth() > 0) {
                        relancer();
                    } else {
                        gameOver();
                    }
                    return;
                }
            }
        }
    }

    private void gameOver() {
        stop();
        gameOver.set(true);
    }

    private void victory() {
        stop();
        soundManager.play("victory");
        victory.set(true);
    }

    private void relancer() {
        player.setPosition(PACMAN_DEPART_LIGNE, PACMAN_DEPART_COLONNE);
        for (Ghost g : ghosts) {
            int[] spawn = ghostSpawns.get(g);
            g.setPosition(spawn[0], spawn[1]);
            g.resetDirection();
        }
        afraid.set(false);
        ticksAfraidRestants = 0;
    }

    public void restart() {
        stop();
        start();
    }

    public void stop() {
        for (Ghost g : ghosts) {
            g.stop();
        }
        if (playerTimeline != null) {
            playerTimeline.stop();
        }
        soundManager.stopAll();
    }
}
