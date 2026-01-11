package Model;

/**
 * Defines game difficulty levels and their configuration parameters.
 * Each level controls board size, mines, special cells, lives and scoring rules.
 */
public enum Difficulty {
    EASY(
            9, 9,      // rows, cols
            10,        // mines
            6,         // question cells
            2,         // surprise cells
            10,        // starting lives
            5,         // activation cost (score points)
            8,         // surprise value (points)
            1,         //  mineFlagReward (+1pt)
            -3         // nonMineFlagPenalty (-3pts)
    ),
    MEDIUM(
            13, 13,
            26,
            7,
            3,
            8,
            8,
            12,         // surprise value
            1,          // mineFlagReward (+1pt)
            -3          // nonMineFlagPenalty (-3pts)
    ),
    HARD(
            16, 16,
            44,
            11,
            4,
            6,
            12,
            16,         // surprise value
            1,          // mineFlagReward (+1pt)
            -3          // nonMineFlagPenalty (-3pts)
    );
    // Board dimensions and content
    private final int rows;
    private final int cols;
    private final int mines;
    private final int questionCells;
    private final int surpriseCells;
    private final int startingLives;
    private final int activationCost;
    private final int surpriseValue;
    private final int mineFlagReward;     //  +1 pt for correctly flagging a mine
    private final int nonMineFlagPenalty; //  -3 pts for incorrectly flagging

    Difficulty(int rows, int cols, int mines, int questionCells, int surpriseCells, int startingLives, int activationCost, int surpriseValue, int mineFlagReward, int nonMineFlagPenalty) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.questionCells = questionCells;
        this.surpriseCells = surpriseCells;
        this.startingLives = startingLives;
        this.activationCost = activationCost;
        this.surpriseValue = surpriseValue;
        this.mineFlagReward = mineFlagReward;
        this.nonMineFlagPenalty = nonMineFlagPenalty;
    }

    // --- Getters ---
    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMines() {
        return mines;
    }

    public int getQuestionCells() {
        return questionCells;
    }

    public int getSurpriseCells() {
        return surpriseCells;
    }

    public int getStartingLives() {
        return startingLives;
    }

    public int getActivationCost() {
        return activationCost;
    }

    public int getSurpriseValue() {
        return surpriseValue;
    }

    public int getMineFlagReward() {
        return mineFlagReward;
    }       // NEW

    public int getNonMineFlagPenalty() {
        return nonMineFlagPenalty;
    } // NEW
}