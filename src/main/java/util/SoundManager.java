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
    private static Clip correctClip;
    private static Clip wrongClip;

    private static final float CORRECT_VOLUME_DB = -8.0f;
    private static final float WRONG_VOLUME_DB = -8.0f;
    private static Clip winClip;
    private static Clip loseClip;

    private static final float WIN_VOLUME_DB = -5.0f;
    private static final float LOSE_VOLUME_DB = -5.0f;
    private static Clip specialCellDialogClip;
    private static final float SPECIAL_DIALOG_VOLUME_DB = -7.0f;
    private static Clip exitDialogClip;
    private static final float EXIT_DIALOG_VOLUME_DB = -7.0f;
    private static Clip cellClickClip;
    private static final float CELL_CLICK_VOLUME_DB = -18.0f; // quiet
    private static Clip typeClip;
    private static final float TYPE_VOLUME_DB = -20.0f; // quiet typing

    private SoundManager() {
    }

    /**
     * Call once when app starts
     */
    public static void init() {
        clickClip = loadClip("/audio/ui_click.wav");
        setVolume(clickClip, CLICK_VOLUME_DB);

        cellClickClip = loadClip("/audio/ui_click.wav");   // <-- NEW
        setVolume(cellClickClip, CELL_CLICK_VOLUME_DB);    // <-- NEW

        correctClip = loadClip("/audio/correct_answer.wav");
        wrongClip = loadClip("/audio/wrong_answer.wav");
        setVolume(correctClip, CORRECT_VOLUME_DB);
        setVolume(wrongClip, WRONG_VOLUME_DB);

        winClip  = loadClip("/audio/win_game.wav");
        loseClip = loadClip("/audio/lose_game.wav");
        setVolume(winClip, WIN_VOLUME_DB);
        setVolume(loseClip, LOSE_VOLUME_DB);

        specialCellDialogClip = loadClip("/audio/special_cell_dialog.wav");
        setVolume(specialCellDialogClip, SPECIAL_DIALOG_VOLUME_DB);

        exitDialogClip = loadClip("/audio/exit_dialog.wav");
        setVolume(exitDialogClip, EXIT_DIALOG_VOLUME_DB);
        typeClip = loadClip("/audio/key_type.wav");
        setVolume(typeClip, TYPE_VOLUME_DB);


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

    public static void correctAnswer() {
        if (muted) return;
        play(correctClip);
    }

    public static void wrongAnswer() {
        if (muted) return;
        play(wrongClip);
    }
    public static void winGame() {
        if (muted) return;
        play(winClip);
    }

    public static void loseGame() {
        if (muted) return;
        play(loseClip);
    }
    public static void specialCellDialog() {
        if (muted) return;
        play(specialCellDialogClip);
    }
    public static void exitDialog() {
        if (muted) return;
        play(exitDialogClip);
    }

    public static void cellClick() {
        if (muted) return;
        play(cellClickClip);
    }
    public static void typeKey() {
        if (muted) return;
        play(typeClip);
    }



}
