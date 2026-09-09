package model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.AudioClip;

public class SoundManager {

    private final Map<String, AudioClip> clips = new HashMap<>();
    private final BooleanProperty muted = new SimpleBooleanProperty(false);

    public SoundManager() {
        charger("Voicy_Pacman-music");
        charger("Voicy_Hitmarker");
        charger("Voicy_Pacman-Death");
        charger("Pacman-chomp");
    }

    private static final String DOSSIER_SONS = "/fr/univartois/butinfo/ihm/assets/sounds/";

    private void charger(String nom) {
        URL url = getClass().getResource(DOSSIER_SONS + nom + ".wav");
        if (url == null) {
            url = getClass().getResource(DOSSIER_SONS + nom + ".mp3");
        }
        if (url != null) {
            clips.put(nom, new AudioClip(url.toExternalForm()));
        }
    }

    public void play(String nom) {
        if (muted.get()) return;
        AudioClip clip = clips.get(nom);
        if (clip != null) {
            clip.play();
        }
    }

    public void stop(String nom) {
        AudioClip clip = clips.get(nom);
        if (clip != null) {
            clip.stop();
        }
    }

    public void stopAll() {
        for (AudioClip clip : clips.values()) {
            clip.stop();
        }
    }

    public BooleanProperty mutedProperty() {
        return muted;
    }

    public boolean isMuted() {
        return muted.get();
    }

    public void toggleMute() {
        muted.set(!muted.get());
        if (muted.get()) {
            stopAll();
        }
    }
}
