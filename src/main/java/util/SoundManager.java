package util;

import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {

    private static Clip clip;

    public static void playLoop(String resourcePath) {
        try {
            stop(); // stop previous music if any

            URL url = SoundManager.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("Sound not found: " + resourcePath);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);

            // calm volume
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(-20.0f); // dB
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
