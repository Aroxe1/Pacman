# Fiche Récapitulative — Pacman TP3

---

## AbstractCharacter

```java
public abstract class AbstractCharacter {

    // Propriétés JavaFX observables : quand on appelle .set(), tous les listeners sont notifiés
    // → permet au GridPane de déplacer automatiquement le sprite sans code supplémentaire
    private final IntegerProperty row    = new SimpleIntegerProperty();
    private final IntegerProperty column = new SimpleIntegerProperty();

    private int health;         // points de vie du personnage
    private final Partie partie; // référence à la partie pour appeler deplacer(), getMap()...

    protected AbstractCharacter(int initialHealth, Partie partie) {
        this.health = initialHealth; // Player = 3, Ghost = Integer.MAX_VALUE
        this.partie = partie;
    }

    // Chaque sous-classe retourne le nom de son image PNG (sans extension)
    // Player → "pacman", Ghost → "red" / "blue" / "pink" / "orange"
    public abstract String getName();

    // Lecture directe de la valeur (int) de la propriété
    public int getRow()    { return row.get(); }
    public int getColumn() { return column.get(); }

    // Retourne la propriété elle-même → utilisée pour le binding et les listeners
    public IntegerProperty getRowProperty()    { return row; }
    public IntegerProperty getColumnProperty() { return column; }

    // Met à jour les deux propriétés d'un coup
    // → déclenche automatiquement les listeners dans installerListenersPosition()
    // → le GridPane déplace le sprite immédiatement
    public void setPosition(int row, int column) {
        this.row.set(row);
        this.column.set(column);
    }

    public int  getHealth()  { return health; }
    public void incHealth()  { health++; }
    public void decHealth()  { health--; } // appelée par Partie quand Pacman touche un fantôme
    public Partie getPartie(){ return partie; }
}
```

---

## Player

```java
public class Player extends AbstractCharacter {

    // Initialise le joueur avec 3 vies
    public Player(Partie partie) { super(3, partie); }

    // "pacman" → chargera pacman.png dans loadImage()
    @Override
    public String getName() { return "pacman"; }
}
```

---

## Ghost

```java
public class Ghost extends AbstractCharacter {

    private static final Random RANDOM = new Random();
    private final String color;   // "red", "blue", "pink" ou "orange"
    private Timeline timeline;    // boucle d'animation du fantôme
    private int dirRow = 0, dirCol = 0; // direction courante (0,0 au départ = pas encore bougé)

    // health = Integer.MAX_VALUE → le fantôme ne peut pas "mourir" normalement
    public Ghost(String color, Partie partie) { super(Integer.MAX_VALUE, partie); this.color = color; }

    // Retourne la couleur → chargera red.png, blue.png... dans loadImage()
    @Override
    public String getName() { return color; }

    public void moveRandomly() {
        GameMap map = getPartie().getMap();
        if (map == null) return; // sécurité si appelée avant start()

        // Les 4 directions possibles : haut, bas, gauche, droite
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};

        // --- Étape 1 : filtrer les directions accessibles ---
        List<int[]> valides = new ArrayList<>();
        for (int[] d : directions) {
            int newRow = getRow() + d[0];
            int newCol = getColumn() + d[1];

            // Gestion du tunnel horizontal : sortir à gauche → réapparaître à droite et vice versa
            if (newCol < 0)               newCol = map.getWidth() - 1;
            else if (newCol >= map.getWidth()) newCol = 0;

            if (!map.isOnMap(newRow, newCol)) continue;                          // hors carte → skip
            if (map.get(newRow, newCol).getContent() == TileContent.WALL) continue; // mur → skip
            valides.add(d);
        }
        if (valides.isEmpty()) return; // bloqué de tous côtés (ne devrait pas arriver)

        // --- Étape 2 : exclure le demi-tour ---
        // Le demi-tour c'est exactement l'opposé de la direction actuelle : (-dirRow, -dirCol)
        List<int[]> sansDemiTour = new ArrayList<>();
        for (int[] d : valides) {
            if (dirRow == 0 && dirCol == 0)               sansDemiTour.add(d); // premier mouvement → tout est ok
            else if (d[0] != -dirRow || d[1] != -dirCol)  sansDemiTour.add(d); // pas l'opposé → ok
        }

        // --- Étape 3 : choisir aléatoirement ---
        // Si sansDemiTour est vide (impasse) → on autorise quand même le demi-tour
        int[] choix = sansDemiTour.isEmpty()
                ? valides.get(0)
                : sansDemiTour.get(RANDOM.nextInt(sansDemiTour.size()));

        // Mémoriser la direction choisie pour le prochain tick
        dirRow = choix[0];
        dirCol = choix[1];

        // --- Étape 4 : déléguer le mouvement à Partie ---
        // Partie.deplacer() gère le tunnel, les murs, et verifierCollisions()
        if      (dirRow == -1) getPartie().moveUp(this);
        else if (dirRow ==  1) getPartie().moveDown(this);
        else if (dirCol == -1) getPartie().moveLeft(this);
        else if (dirCol ==  1) getPartie().moveRight(this);
    }

    public void animate() {
        // KeyFrame = une action à déclencher après un délai
        // Duration.millis(225) = toutes les 225 ms
        // Animation.INDEFINITE = boucle infinie jusqu'à timeline.stop()
        timeline = new Timeline(new KeyFrame(Duration.millis(225), e -> moveRandomly()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    // Appelée par Partie.stop() et Partie.gameOver()
    public void stop() { if (timeline != null) timeline.stop(); }

    // Appelée après un respawn : remet le fantôme en position "pas encore bougé"
    public void resetDirection() { dirRow = 0; dirCol = 0; }
}
```

---

## Partie

```java
public class Partie {

    private static final int NB_LIGNES = 31, NB_COLONNES = 28;
    private static final int PACMAN_DEPART_LIGNE = 23, PACMAN_DEPART_COLONNE = 13;
    private static final int TICKS_AFRAID_MAX = 40; // 40 ticks × 125 ms = 5 secondes de mode peur
    private static final int POINTS_PACGUM = 10, POINTS_MEGAGUM = 30, POINTS_FANTOME = 200;

    // Encodage de la carte : 1=mur, 0=chemin+pacgum, 2=chemin seul, 3=chemin+megagum
    private final int[][] carteNiveau = { ... };

    private PacmanInterface controller; // la vue (PacmanController)
    private GameMap map;
    private Player player;
    private final List<Ghost> ghosts = new ArrayList<>();
    private final Map<Ghost, int[]> ghostSpawns = new HashMap<>(); // position de respawn de chaque fantôme

    // Propriétés observables : la vue s'y abonne et se met à jour automatiquement
    private final IntegerProperty score   = new SimpleIntegerProperty(0);
    private final IntegerProperty lives   = new SimpleIntegerProperty(3);
    private final BooleanProperty afraid  = new SimpleBooleanProperty(false);  // fantômes en mode peur ?
    private final BooleanProperty gameOver= new SimpleBooleanProperty(false);
    private final BooleanProperty victory = new SimpleBooleanProperty(false);  // ⚠️ jamais mise à true

    private int ticksAfraidRestants = 0;          // décompte du mode peur en ticks
    private int currentDirRow = 0, currentDirCol = 0; // direction actuelle du joueur (mise à jour par clavier)
    private Timeline playerTimeline;
    private final Random random = new Random();
    private final SoundManager soundManager = new SoundManager();

    // Retournent les propriétés → la vue peut s'y abonner avec .addListener() ou .bind()
    public IntegerProperty scoreProperty()    { return score; }
    public IntegerProperty livesProperty()    { return lives; }
    public BooleanProperty afraidProperty()   { return afraid; }
    public BooleanProperty gameOverProperty() { return gameOver; }
    public BooleanProperty victoryProperty()  { return victory; }
    public GameMap      getMap()          { return map; }
    public Player       getPlayer()       { return player; }
    public List<Ghost>  getGhosts()       { return ghosts; }
    public SoundManager getSoundManager() { return soundManager; }

    // Injecte le contrôleur (appelé par MenuController avant start())
    public void setController(PacmanInterface controller) { this.controller = controller; }

    public void start() {
        // --- 1. Construire la carte ---
        map = new GameMap(NB_LIGNES, NB_COLONNES);
        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int col = 0; col < NB_COLONNES; col++) {
                Tile tile = map.get(ligne, col);
                int v = carteNiveau[ligne][col];
                tile.setContent(v == 1 ? TileContent.WALL : TileContent.PATH);
                // Placer la gomme selon la valeur : 0=pacgum, 3=megagum, sinon rien
                if      (v == 0) tile.setGum(Gum.PACGUM);
                else if (v == 3) tile.setGum(Gum.MEGAGUM);
                else             tile.setGum(null);
            }
        }

        // --- 2. Préparer la vue (construit le GridPane graphique) ---
        controller.prepare(map);

        // --- 3. Créer et placer le joueur ---
        player = new Player(this);
        player.setPosition(PACMAN_DEPART_LIGNE, PACMAN_DEPART_COLONNE);
        map.get(PACMAN_DEPART_LIGNE, PACMAN_DEPART_COLONNE).setGum(null); // pas de gomme sous Pacman
        controller.bindPlayer(player); // ajoute le sprite à la grille

        // --- 4. Créer les 4 fantômes ---
        ghosts.clear(); ghostSpawns.clear();
        ajouterFantome("red",    14, 13);
        ajouterFantome("blue",   14, 14);
        ajouterFantome("pink",   14, 12);
        ajouterFantome("orange", 14, 15);

        // --- 5. Réinitialiser toutes les valeurs ---
        score.set(0); lives.set(3); afraid.set(false);
        gameOver.set(false); ticksAfraidRestants = 0;
        currentDirRow = 0; currentDirCol = 0;

        // --- 6. Démarrer les animations ---
        for (Ghost g : ghosts) g.animate(); // chaque fantôme lance sa propre Timeline

        // Timeline du joueur : tick toutes les 125 ms
        if (playerTimeline != null) playerTimeline.stop(); // arrêter l'ancienne si restart
        playerTimeline = new Timeline(new KeyFrame(Duration.millis(125), e -> {
            // Ne bouger que si une direction est définie (touche enfoncée)
            if (currentDirRow != 0 || currentDirCol != 0)
                deplacer(player, currentDirRow, currentDirCol);
            // Décompte du mode peur : quand ça atteint 0, désactiver afraid
            if (ticksAfraidRestants > 0 && --ticksAfraidRestants == 0)
                afraid.set(false);
        }));
        playerTimeline.setCycleCount(Animation.INDEFINITE);
        playerTimeline.play();

        soundManager.play("Voicy_Pacman-music");
    }

    public void stop() {
        ghosts.forEach(Ghost::stop);           // arrêter chaque Timeline de fantôme
        if (playerTimeline != null) playerTimeline.stop();
        soundManager.stopAll();
    }

    // Redémarre proprement : stop() remet tout à zéro, start() reconstruit tout
    public void restart() { stop(); start(); }

    // Mémorise la direction voulue par le joueur → sera appliquée au prochain tick
    public void setPlayerDirection(int dirRow, int dirCol) {
        this.currentDirRow = dirRow; this.currentDirCol = dirCol;
    }

    // Appelées par PacmanController.setScene() quand une touche est pressée
    public void movePlayerUp()    { setPlayerDirection(-1,  0); }
    public void movePlayerDown()  { setPlayerDirection( 1,  0); }
    public void movePlayerLeft()  { setPlayerDirection( 0, -1); }
    public void movePlayerRight() { setPlayerDirection( 0,  1); }

    // Appelées par Ghost.moveRandomly() pour déléguer le déplacement à Partie
    public void moveUp(AbstractCharacter c)    { deplacer(c, -1,  0); }
    public void moveDown(AbstractCharacter c)  { deplacer(c,  1,  0); }
    public void moveLeft(AbstractCharacter c)  { deplacer(c,  0, -1); }
    public void moveRight(AbstractCharacter c) { deplacer(c,  0,  1); }

    private void deplacer(AbstractCharacter c, int dRow, int dCol) {
        // Calculer la case cible
        int newRow = c.getRow() + dRow;
        int newCol = c.getColumn() + dCol;

        // Tunnel horizontal : sortir à gauche → réapparaître à droite (et inversement)
        if      (newCol < 0)               newCol = map.getWidth() - 1;
        else if (newCol >= map.getWidth()) newCol = 0;

        // Bloquer si hors carte (vertical) ou si la case est un mur
        if (!map.isOnMap(newRow, newCol)) return;
        Tile cible = map.get(newRow, newCol);
        if (cible.getContent() == TileContent.WALL) return;

        // --- Logique de collecte (uniquement pour le joueur) ---
        if (c == player) {
            if (cible.getGum() == Gum.PACGUM) {
                cible.setGum(null);                          // fait disparaître la gomme (binding auto)
                score.set(score.get() + POINTS_PACGUM);     // met à jour le score (binding auto sur le label)
                soundManager.play("Pacman-chomp");
            } else if (cible.getGum() == Gum.MEGAGUM) {
                cible.setGum(null);
                score.set(score.get() + POINTS_MEGAGUM);
                ticksAfraidRestants = TICKS_AFRAID_MAX;      // démarre le compte à rebours de 5 s
                afraid.set(true);                            // les fantômes passent en image "hurt" (binding auto)
                soundManager.play("megagum");                // ⚠️ son non chargé dans SoundManager
            }
        }

        // Déplacer effectivement le personnage → notifie les listeners → bouge le sprite dans le GridPane
        c.setPosition(newRow, newCol);

        // Vérifier si un fantôme est sur la même case que Pacman
        verifierCollisions();
    }

    private void verifierCollisions() {
        if (player == null) return;
        for (Ghost g : ghosts) {
            // Collision si même ligne ET même colonne
            if (g.getRow() == player.getRow() && g.getColumn() == player.getColumn()) {
                if (afraid.get()) {
                    // Mode peur → Pacman mange le fantôme : respawn à sa case d'origine
                    int[] spawn = ghostSpawns.get(g);
                    g.setPosition(spawn[0], spawn[1]); // retour au spawn
                    g.resetDirection();                // repart comme au début
                    score.set(score.get() + POINTS_FANTOME);
                    soundManager.play("Voicy_Hitmarker");
                } else {
                    // Mode normal → Pacman perd une vie
                    soundManager.play("Voicy_Pacman-Death");
                    player.decHealth();
                    lives.set(player.getHealth()); // met à jour les icônes de vies (listener dans setPartie)
                    currentDirRow = 0; currentDirCol = 0; // stopper le mouvement
                    if (player.getHealth() > 0) relancer(); // vies restantes → repositionner
                    else                         gameOver(); // plus de vies → fin de partie
                    return; // arrêter la boucle : inutile de continuer après une mort
                }
            }
        }
    }

    private void ajouterFantome(String couleur, int ligne, int colonne) {
        Ghost g = new Ghost(couleur, this);
        g.setPosition(ligne, colonne);
        ghosts.add(g);
        ghostSpawns.put(g, new int[]{ligne, colonne}); // mémoriser la case de spawn pour le respawn
        controller.bindGhost(g);                        // ajouter le sprite à la grille
    }

    private void relancer() {
        // Replacer Pacman à sa position de départ
        player.setPosition(PACMAN_DEPART_LIGNE, PACMAN_DEPART_COLONNE);
        // Replacer chaque fantôme à son spawn et réinitialiser sa direction
        for (Ghost g : ghosts) {
            int[] spawn = ghostSpawns.get(g);
            g.setPosition(spawn[0], spawn[1]);
            g.resetDirection();
        }
        afraid.set(false);         // annuler le mode peur
        ticksAfraidRestants = 0;
    }

    // Arrête tout et déclenche l'affichage de l'écran Game Over (via le listener dans setPartie)
    private void gameOver() { stop(); gameOver.set(true); }

    // ⚠️ Définie mais jamais appelée : aucun test sur les gommes restantes → victoire impossible
    private void victory() { stop(); victory.set(true); }

    public void placeOnMap(AbstractCharacter character) {
        List<Tile> empties = map.getEmptyTiles(); // toutes les cases PATH
        if (empties.isEmpty()) return;
        // Choisir une case au hasard et y téléporter le personnage
        Tile choisie = empties.get(random.nextInt(empties.size()));
        character.setPosition(choisie.getRow(), choisie.getColumn());
    }
}
```

---

## GameMap

```java
public class GameMap {

    private int height, width;
    private Tile[][] tiles; // grille 2D, tiles[ligne][colonne]

    public GameMap(int height, int width) {
        this.height = height; this.width = width;
        this.tiles = new Tile[height][width];
        // Instancier une Tile pour chaque case, en lui donnant ses coordonnées
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                tiles[i][j] = new Tile(i, j);
    }

    // Accès direct à une tuile par ses coordonnées
    public Tile get(int row, int col) { return tiles[row][col]; }

    // Vérifie que (row, col) est bien à l'intérieur de la grille
    // → utilisé dans deplacer() et moveRandomly() avant tout accès à tiles[][]
    public boolean isOnMap(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    public int getHeight() { return height; }
    public int getWidth()  { return width; }

    // Parcourt toute la grille et retourne les tuiles sur lesquelles un personnage peut se déplacer
    // → utilisé par placeOnMap() pour trouver une case libre aléatoire
    public List<Tile> getEmptyTiles() {
        List<Tile> result = new ArrayList<>();
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                if (tiles[i][j].isEmpty()) result.add(tiles[i][j]); // isEmpty() = c'est un PATH
        return result;
    }
}
```

---

## Tile

```java
public class Tile {

    private final int row, column; // position fixe dans la grille (immuable)

    // ObjectProperty<T> = propriété observable générique
    // Quand setContent() ou setGum() est appelée, les bindings de la vue se recalculent automatiquement
    private final ObjectProperty<TileContent> content = new SimpleObjectProperty<>();
    private final ObjectProperty<Gum>         gum     = new SimpleObjectProperty<>();

    public Tile(int row, int column) { this.row = row; this.column = column; }

    public int getRow()    { return row; }
    public int getColumn() { return column; }

    // Retourne la valeur courante (WALL ou PATH)
    public TileContent getContent()              { return content.get(); }
    // Changer le contenu → notifie automatiquement le binding de l'ImageView de fond
    public void        setContent(TileContent c) { content.set(c); }
    // Retourne la propriété elle-même → utilisée dans Bindings.createObjectBinding()
    public ObjectProperty<TileContent> getContentProperty() { return content; }

    public Gum  getGum()       { return gum.get(); }
    // Mettre null → l'ImageView de la gomme affichera rien (gomme disparaît à l'écran)
    public void setGum(Gum g)  { gum.set(g); }
    public ObjectProperty<Gum> getGumProperty() { return gum; }

    // Une tuile est vide si son contenu est PATH (pas un mur)
    // → utilisé par getEmptyTiles() et comme garde dans deplacer()
    public boolean isEmpty() {
        TileContent c = content.get();
        return c != null && c.isEmpty(); // null-safe : une tuile sans contenu n'est pas traversable
    }
}
```

---

## TileContent (enum)

```java
public enum TileContent {

    // Chemin : traversable par tous les personnages
    PATH { @Override public boolean isEmpty() { return true;  } },

    // Mur : bloquant, personne ne peut y entrer
    WALL { @Override public boolean isEmpty() { return false; } };

    // Convertit le nom de la constante en minuscules → "path" ou "wall"
    // → utilisé par loadImage() pour trouver path.png ou wall.png
    public String getName() { return name().toLowerCase(); }

    public abstract boolean isEmpty();
}
```

---

## Gum (enum)

```java
public enum Gum {
    PACGUM,   // collectée → +10 pts, son "Pacman-chomp"
    MEGAGUM;  // collectée → +30 pts, active le mode peur 5 s

    // "pacgum" ou "megagum" → utilisé par loadImage() pour trouver pacgum.png / megagum.png
    public String getName() { return name().toLowerCase(); }
}
```

---

## SoundManager

```java
public class SoundManager {

    // Map nom → clip audio : accès O(1) par nom de son
    private final Map<String, AudioClip> clips = new HashMap<>();

    // Propriété observable → le bouton mute peut s'y abonner pour changer son texte
    private final BooleanProperty muted = new SimpleBooleanProperty(false);

    public SoundManager() {
        // Charger tous les sons au démarrage une seule fois
        charger("Voicy_Pacman-music");
        charger("Voicy_Hitmarker");
        charger("Voicy_Pacman-Death");
        charger("Pacman-chomp");
        // ⚠️ "megagum" oublié ici → soundManager.play("megagum") dans Partie ne fera rien
    }

    private void charger(String nom) {
        // Chercher d'abord en .wav, puis en .mp3 si introuvable
        URL url = getClass().getResource("assets/sounds/" + nom + ".wav");
        if (url == null) url = getClass().getResource("assets/sounds/" + nom + ".mp3");
        // Si le fichier existe, créer et stocker le clip
        if (url != null) clips.put(nom, new AudioClip(url.toExternalForm()));
        // Si le fichier n'existe pas → rien (le clip ne sera pas dans la map, play() ignorera l'appel)
    }

    public void play(String nom) {
        if (muted.get()) return; // ne rien faire si le son est coupé
        AudioClip clip = clips.get(nom);
        if (clip != null) clip.play(); // null-safe : si le son n'a pas été chargé, on ignore
    }

    public void stop(String nom)  { AudioClip c = clips.get(nom); if (c != null) c.stop(); }

    // Arrête tous les clips d'un coup → appelée par stop() et toggleMute()
    public void stopAll() { clips.values().forEach(AudioClip::stop); }

    public void toggleMute() {
        muted.set(!muted.get()); // inverse l'état
        if (muted.get()) stopAll(); // si on vient de muter → couper les sons en cours
        // si on vient de démuter → les sons reprendront au prochain appel de play()
    }

    public boolean isMuted()               { return muted.get(); }
    public BooleanProperty mutedProperty() { return muted; }
}
```

---

## PacmanInterface

```java
// Contrat entre Partie (modèle) et PacmanController (vue)
// → Partie ne connaît que cette interface, pas l'implémentation concrète
// → permet de changer toute la vue sans toucher au modèle
public interface PacmanInterface {
    void setPartie(Partie partie); // injecte la partie et pose les bindings
    void setStage(Stage stage);    // nécessaire pour changer de scène
    void setScene(Scene scene);    // pour enregistrer les listeners clavier
    void prepare(GameMap map);     // construit la grille graphique depuis la carte
    void bindPlayer(Player player);// ajoute le sprite Pacman et lie sa position
    void bindGhost(Ghost ghost);   // ajoute le sprite d'un fantôme et lie sa position + image
}
```

---

## PacmanController

```java
public class PacmanController implements PacmanInterface {

    private static final int TAILLE_CASE = 28; // chaque case de la grille = 28×28 pixels

    // Composants injectés depuis Pacman.fxml par JavaFX
    @FXML GridPane  gameBoard;   // la grille de jeu (28 colonnes × 31 lignes)
    @FXML Label     scoreLabel;  // affiche "SCORE : 1234"
    @FXML HBox      livesBox;    // contient les icônes de vies (pacman.png × nb vies)
    @FXML StackPane gameArea;
    @FXML Button    restartButton, menuButton, muteButton;

    private Partie    partie;
    private Scene     scene;
    private Stage     stage;
    private double    anglePacman = 0;  // rotation actuelle de l'image Pacman
    private ImageView pacmanView;       // référence au sprite Pacman pour appliquer la rotation

    @Override
    public void setPartie(Partie partie) {
        this.partie = partie;

        // Binding unidirectionnel : scoreLabel suit scoreProperty() en temps réel
        // Bindings.concat construit "SCORE : " + valeur_entière
        scoreLabel.textProperty().bind(
                Bindings.concat("SCORE : ", partie.scoreProperty().asString()));

        // Listener sur les vies : redessine les icônes à chaque changement
        partie.livesProperty().addListener((obs, ov, nv) ->
                mettreAJourVies(nv.intValue()));
        mettreAJourVies(partie.livesProperty().get()); // initialiser l'affichage tout de suite

        // Listener sur le bouton mute : met à jour le texte quand l'état change
        partie.getSoundManager().mutedProperty().addListener((obs, ov, nv) ->
                muteButton.setText(nv ? "SON : OFF" : "SON : ON"));
        muteButton.setText(partie.getSoundManager().isMuted() ? "SON : OFF" : "SON : ON");

        // Listener sur gameOver : quand la partie se termine, afficher l'écran Game Over
        partie.gameOverProperty().addListener((obs, ov, nv) -> {
            if (nv) afficherGameOver(); // nv = true → game over déclenché
        });
    }

    @Override public void setStage(Stage stage) { this.stage = stage; }

    @Override
    public void setScene(Scene scene) {
        this.scene = scene;
        // Enregistrer le listener clavier sur la scène (pas sur un nœud particulier)
        scene.setOnKeyPressed(event -> {
            if (partie == null) return;
            switch (event.getCode()) {
                // Chaque touche : 1) change la direction dans Partie, 2) met à jour l'angle de rotation
                case Z: partie.movePlayerUp();    anglePacman = -90; appliquerRotation(); break;
                case S: partie.movePlayerDown();  anglePacman =  90; appliquerRotation(); break;
                case Q: partie.movePlayerLeft();  anglePacman = 180; appliquerRotation(); break;
                case D: partie.movePlayerRight(); anglePacman =   0; appliquerRotation(); break;
                default: break;
            }
        });
    }

    @Override
    public void prepare(GameMap map) {
        // Nettoyer l'ancien contenu du GridPane (utile en cas de restart)
        gameBoard.getChildren().clear();
        gameBoard.getColumnConstraints().clear();
        gameBoard.getRowConstraints().clear();

        // Définir la largeur de chaque colonne
        for (int c = 0; c < map.getWidth(); c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(TAILLE_CASE);
            gameBoard.getColumnConstraints().add(cc);
        }
        // Définir la hauteur de chaque ligne
        for (int l = 0; l < map.getHeight(); l++) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(TAILLE_CASE);
            gameBoard.getRowConstraints().add(rc);
        }

        // --- Passe 1 : couche de fond (mur ou chemin) ---
        for (int ligne = 0; ligne < map.getHeight(); ligne++) {
            for (int col = 0; col < map.getWidth(); col++) {
                Tile tile = map.get(ligne, col);
                ImageView fond = new ImageView();
                fond.setFitWidth(TAILLE_CASE);
                fond.setFitHeight(TAILLE_CASE);
                // createObjectBinding recalcule l'image quand contentProperty change
                // → si une tuile passe de WALL à PATH, l'image change automatiquement
                fond.imageProperty().bind(Bindings.createObjectBinding(
                        () -> tile.getContent() == null ? null : loadImage(tile.getContent().getName()),
                        tile.getContentProperty())); // 2e arg = dépendances du binding
                gameBoard.add(fond, col, ligne); // add(noeud, colonne, ligne) ← attention à l'ordre
            }
        }

        // --- Passe 2 : couche des gommes (par-dessus le fond) ---
        for (int ligne = 0; ligne < map.getHeight(); ligne++) {
            for (int col = 0; col < map.getWidth(); col++) {
                Tile tile = map.get(ligne, col);
                ImageView gomme = new ImageView();
                gomme.setFitWidth(TAILLE_CASE);
                gomme.setFitHeight(TAILLE_CASE);
                // Quand setGum(null) est appelé dans deplacer(), gumProperty change
                // → le binding recalcule : null → image null → ImageView vide → gomme disparaît
                gomme.imageProperty().bind(Bindings.createObjectBinding(
                        () -> tile.getGum() == null ? null : loadImage(tile.getGum().getName()),
                        tile.getGumProperty()));
                gameBoard.add(gomme, col, ligne);
            }
        }
    }

    @Override
    public void bindPlayer(Player player) {
        // Créer l'ImageView avec l'image initiale de Pacman
        pacmanView = new ImageView(loadImage(player.getName())); // getName() = "pacman"
        pacmanView.setFitWidth(TAILLE_CASE);
        pacmanView.setFitHeight(TAILLE_CASE);
        // Lier les propriétés de position → le sprite se déplace automatiquement
        installerListenersPosition(pacmanView, player);
        // Ajouter à la grille à la position initiale
        gameBoard.add(pacmanView, player.getColumn(), player.getRow());
    }

    @Override
    public void bindGhost(Ghost ghost) {
        ImageView view = new ImageView();
        view.setFitWidth(TAILLE_CASE);
        view.setFitHeight(TAILLE_CASE);
        // Binding sur afraidProperty : quand un MEGAGUM est mangé, afraid passe à true
        // → tous les fantômes changent d'image vers "hurt.png" d'un coup
        view.imageProperty().bind(Bindings.createObjectBinding(
                () -> partie.afraidProperty().get()
                        ? loadImage("hurt")           // mode peur → image effrayée
                        : loadImage(ghost.getName()), // normal → couleur du fantôme
                partie.afraidProperty())); // re-évalué à chaque changement de afraid
        installerListenersPosition(view, ghost);
        gameBoard.add(view, ghost.getColumn(), ghost.getRow());
    }

    private Image loadImage(String name) {
        // Construit l'URL complète vers le fichier PNG dans les resources
        // getResource() cherche dans le classpath : /fr/univartois/.../assets/images/
        // toExternalForm() convertit l'URL en String lisible par JavaFX
        // Les deux derniers args : largeur, hauteur, preserveRatio, smooth
        return new Image(
                getClass().getResource(
                    "/fr/univartois/butinfo/ihm/assets/images/" + name + ".png"
                ).toExternalForm(),
                TAILLE_CASE, TAILLE_CASE, true, true);
        // ⚠️ Pas de cache : un nouvel objet Image est créé à chaque appel
        //    (potentiellement coûteux car appelé à chaque tick d'animation)
    }

    private void installerListenersPosition(ImageView view, AbstractCharacter character) {
        // Quand la ligne du personnage change (via setPosition()), mettre à jour l'index du GridPane
        character.getRowProperty().addListener((obs, ov, nv) ->
                GridPane.setRowIndex(view, nv.intValue()));
        // Idem pour la colonne
        character.getColumnProperty().addListener((obs, ov, nv) ->
                GridPane.setColumnIndex(view, nv.intValue()));
        // Résultat : chaque appel à setPosition() déplace le sprite visuellement sans code supplémentaire
    }

    private void appliquerRotation() {
        // setRotate() tourne l'image autour de son centre (en degrés)
        // 0° = droite, -90° = haut, 90° = bas, 180° = gauche
        if (pacmanView != null) pacmanView.setRotate(anglePacman);
    }

    private void mettreAJourVies(int vies) {
        livesBox.getChildren().clear(); // supprimer les anciennes icônes
        for (int i = 0; i < vies; i++) {
            ImageView iv = new ImageView(loadImage("pacman")); // une icône par vie
            iv.setFitWidth(24);  // plus petites que les cases du jeu (28px)
            iv.setFitHeight(24);
            livesBox.getChildren().add(iv); // ajouter dans le HBox de droite à gauche
        }
    }

    private void afficherGameOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GameOver.fxml"));
            Parent root = loader.load();
            GameOverController ctrl = loader.getController();
            ctrl.setStage(stage);
            ctrl.setScore(partie.scoreProperty().get()); // passer le score final
            Scene gameOverScene = new Scene(root, scene.getWidth(), scene.getHeight());
            // Platform.runLater : s'assurer que le changement de scène se fait sur le thread JavaFX
            // (ce listener peut être déclenché depuis la Timeline, qui tourne sur le thread JavaFX,
            //  mais Platform.runLater garantit que le rendu est terminé avant de changer)
            Platform.runLater(() -> stage.setScene(gameOverScene));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void onRestart() { partie.restart(); }

    @FXML private void onMenu() {
        try {
            partie.stop(); // arrêter les sons et les Timelines avant de quitter
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Menu.fxml"));
            Parent root = loader.load();
            MenuController menuCtrl = loader.getController();
            menuCtrl.setStage(stage);
            stage.setScene(new Scene(root, scene.getWidth(), scene.getHeight()));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void onMute() { partie.getSoundManager().toggleMute(); }
}
```

---

## MenuController

```java
public class MenuController {

    private Stage stage;
    public void setStage(Stage stage) { this.stage = stage; }

    @FXML
    private void onPlay() throws IOException {
        // 1. Charger le FXML de jeu et récupérer le contrôleur automatiquement instancié par JavaFX
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Pacman.fxml"));
        Parent root = loader.load();
        PacmanInterface controller = loader.getController(); // c'est un PacmanController

        // 2. Créer la partie et relier modèle ↔ vue
        Partie partie = new Partie();
        partie.setController(controller); // la partie connaît la vue via l'interface
        controller.setPartie(partie);     // la vue connaît la partie, pose les bindings
        controller.setStage(stage);

        // 3. Créer la scène, enregistrer les listeners clavier, démarrer
        Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
        controller.setScene(scene); // enregistre Z/Q/S/D
        partie.start();             // construit la carte, crée les personnages, lance les Timelines
        stage.setScene(scene);      // afficher la nouvelle scène
    }

    // Ferme complètement l'application JavaFX
    @FXML private void onQuit() { Platform.exit(); }
}
```

---

## GameOverController

```java
public class GameOverController {

    @FXML Label finalScoreLabel; // label qui affiche le score final
    private Stage stage;

    public void setStage(Stage stage) { this.stage = stage; }

    // Appelée par PacmanController.afficherGameOver() pour afficher le score
    public void setScore(int score) { finalScoreLabel.setText(String.valueOf(score)); }

    @FXML
    private void onRetry() throws IOException {
        // Exactement la même logique que MenuController.onPlay()
        // → recréer une Partie fraîche et repartir depuis zéro
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Pacman.fxml"));
        Parent root = loader.load();
        PacmanInterface controller = loader.getController();

        Partie partie = new Partie();
        partie.setController(controller);
        controller.setPartie(partie);
        controller.setStage(stage);

        Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
        controller.setScene(scene);
        partie.start();
        stage.setScene(scene);
    }

    @FXML
    private void onMenu() throws IOException {
        // Retour au menu principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Menu.fxml"));
        Parent root = loader.load();
        MenuController menu = loader.getController();
        menu.setStage(stage);
        stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
    }
}
```

---

## PacmanApplication

```java
public class PacmanApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Charger l'écran d'accueil
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Menu.fxml"));
        Parent root = loader.load();
        MenuController controller = loader.getController();
        controller.setStage(stage); // donner le stage au menu pour qu'il puisse changer de scène

        stage.setScene(new Scene(root, 800, 950)); // taille fixe de la fenêtre
        stage.setTitle("Pacman");
        stage.show();
    }

    // launch() appelle start() sur le thread JavaFX Application Thread
    public static void main(String[] args) { launch(); }
}
```
