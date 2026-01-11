package Model;

/**
 * Data class to hold game state information (score and level).
 * Used by the Observer pattern to notify observers of state changes.
 */
public class GameStateData {
    private final int score;
    private final String level;

    /**
     * Creates a new GameStateData instance.
     * @param score the current game score
     * @param level the current difficulty level (e.g., "EASY", "MEDIUM", "HARD")
     */
    public GameStateData(int score, String level) {
        this.score = score;
        this.level = level;
    }

    /**
     * Gets the current score.
     * @return the score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the current difficulty level.
     * @return the level name
     */
    public String getLevel() {
        return level;
    }
}

