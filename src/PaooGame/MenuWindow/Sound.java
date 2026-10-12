package PaooGame.MenuWindow;

import javax.sound.sampled.*;
import java.net.URL;

public class Sound {

    private static Sound instance;

    private Clip clip;
    private FloatControl volumeControl;

    private Sound() {
        // constructor privat ca să nu poată fi apelat new Sound()
    }

    public static Sound getInstance() {
        if (instance == null) {
            instance = new Sound();
        }
        return instance;
    }

    public void playMusic(String path) {

        try {
            // opțional: oprește muzica veche dacă există deja
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.close();
            }

            URL soundURL = getClass().getResource(path);

            AudioInputStream audio = AudioSystem.getAudioInputStream(soundURL);

            clip = AudioSystem.getClip();
            clip.open(audio);

            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVolume(int value) {

        if (volumeControl == null) return;

        float min = volumeControl.getMinimum();
        float max = volumeControl.getMaximum();

        float volume = min + (value / 100f) * (max - min);

        volumeControl.setValue(volume);
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}