package Model;

/**
 * Observer interface for the Observer design pattern.
 * Observers are notified when the game state (score, level) changes.
 */
public interface GameObserver {
    /**
     * Called by the subject when the game state changes.
     * @param state the current game state containing score and level information
     */
    void update(GameStateData state);
}

