package util;

import javax.sound.sampled.*;
import java.net.URL;

public final class SoundManager {

    private static Clip bgClip;
    private static Clip clickClip;

    private static boolean muted = false;
    private static String currentTrack;

    private static final float BG_VOLUME_DB = -18.0f;
    private static final float CLICK_VOLUME_DB = -8.0f;

    private SoundManager() {
    }

    /**
     * Call once when app starts
     */
    public static void init() {
        clickClip = loadClip("/audio/ui_click.wav");
        setVolume(clickClip, CLICK_VOLUME_DB);
    }

    /**
     * Play UI click
     */
    public static void click() {
        if (muted) return;
        play(clickClip);
    }

    /**
     * Background music loop
     */
    public static void playLoop(String resourcePath) {
        try {
            if (bgClip != null && bgClip.isRunning() && resourcePath.equals(currentTrack)) return;

            stopBackground();
            currentTrack = resourcePath;

            bgClip = loadClip(resourcePath);
            if (bgClip == null) return;

            setVolume(bgClip, BG_VOLUME_DB);

            if (!muted) {
                bgClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgClip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopBackground() {
        if (bgClip != null) {
            bgClip.stop();
            bgClip.close();
            bgClip = null;
        }
    }

    public static void toggleMute() {
        muted = !muted;

        if (bgClip != null) {
            if (muted) bgClip.stop();
            else {
                bgClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgClip.start();
            }
        }
    }

    public static boolean isMuted() {
        return muted;
    }

    // ---- helpers ----

    private static Clip loadClip(String resourcePath) {
        try {
            URL url = SoundManager.class.getResource(resourcePath);
            System.out.println("Loading sound: " + resourcePath + " -> " + url); // debug
            if (url == null) {
                System.err.println("Sound not found: " + resourcePath);
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip c = AudioSystem.getClip();
            c.open(ais);
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void play(Clip c) {
        if (c == null) return;
        if (c.isRunning()) c.stop();
        c.setFramePosition(0);
        c.start();
    }

    private static void setVolume(Clip c, float db) {
        if (c != null && c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            ((FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN)).setValue(db);
        }
    }

    public static void stop() {
        stopBackground();
    }
}
