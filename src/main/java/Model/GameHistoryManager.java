package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Manager – stores all past games.
 * Pure MODEL (no Swing) for clean MVC.
 */
public class GameHistoryManager {

    private static GameHistoryManager instance;

    // list of Model.GameHistoryEntry
    private final List<GameHistoryEntry> entries = new ArrayList<>();

    private GameHistoryManager() { }

    public static GameHistoryManager getInstance() {
        if (instance == null) {
            instance = new GameHistoryManager();
        }
        return instance;
    }

    /** Adds a completed game to history. */
    public void addEntry(GameHistoryEntry entry) {
        if (entry == null) return;
        // newest first (optional)
        entries.add(0, entry);
    }

    /** Returns read-only list (Controller filters it). */
    public List<GameHistoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Clears history (optional admin/debug). */
    public void clear() {
        entries.clear();
    }
}
