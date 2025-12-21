package Model;

import java.util.Random;
/**
 * Represents a cooperative Minesweeper game with two boards.
 * Manages shared lives, shared score, difficulty settings, questions and turns.
 */
public class Game {
    // Maximum number of lives allowed (extra lives are converted to score)
    private final int MAX_LIVES = 10;
    private Board board1;
    private Board board2;
    private Difficulty difficulty;
    private int sharedLives;
    private int sharedScore;
    private GameState gameState;
    private int currentPlayerTurn;
    private QuestionManager questionManager;
    private QuestionPresenter questionPresenter;

    //  Field to store the message for the View
    private String lastActionMessage;
    private int totalQuestionsAnswered;
    private int totalCorrectAnswers;

    /**
     * Question difficulty levels used for scoring and life rewards/penalties.
     */
    public enum QuestionLevel { EASY, MEDIUM, HARD, EXPERT }
    /**
     * Creates a new game with the given difficulty.
     */
    public Game(Difficulty difficulty) {
        startNewGame(difficulty);
    }
    /**
     * Initializes or resets all game data for the given difficulty.
     * Creates two boards, sets initial lives, score and game state.
     */
    public void startNewGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.sharedLives = difficulty.getStartingLives();
        this.sharedScore = 0;
        this.gameState = GameState.RUNNING;
        this.currentPlayerTurn = 1;
        this.lastActionMessage = null; // Initialize the message field
        this.totalQuestionsAnswered = 0;
        this.totalCorrectAnswers = 0;

        this.board1 = new Board(difficulty, this);
        this.board2 = new Board(difficulty, this);
    }
    /**
     * Restarts the game using the last selected difficulty (if available).
     */
    public void restartGame() {
        if (this.difficulty != null) {
            startNewGame(this.difficulty);
        }
    }

    // --- Game Status & End Game Logic ---
    /**
     * Checks if the game has been won or lost, based on lives and safe cells.
     * Updates the game state and triggers end-of-game processing if needed.
     */
    public void checkGameStatus() {
        if (gameState != GameState.RUNNING) return;

        // Loss
        if (sharedLives <= 0) {
            sharedLives = 0;
            gameState = GameState.LOST;
            endGameProcessing();
            return;
        }

        // Win: one board found ALL mines
        if (board1.areAllMinesFound() || board2.areAllMinesFound()) {
            gameState = GameState.WON;
            endGameProcessing();
        }
    }


    /**
     * Performs all necessary steps when the game ends (Win or Loss).
     * This includes auto-revealing all cells and converting remaining lives to points.
     */
    private void endGameProcessing() {
        if (gameState == GameState.WON || gameState == GameState.LOST) {
            System.out.println("=== GAME ENDED: " + gameState + " ===");

            // 1. Convert remaining lives to points
            int lifeValue = difficulty.getActivationCost();
            int lifeBonus = sharedLives * lifeValue;
            sharedScore += lifeBonus;

            System.out.println("Final Life Bonus: " + sharedLives + " lives * " + lifeValue + " pts = +" + lifeBonus + " points.");

            // 2. Auto-reveal all cells
            if (board1 != null) board1.revealAll();
            if (board2 != null) board2.revealAll();

            printGameStatus();
        }
    }
    /**
     * Prints game state to the console (for debugging).
     */
    public void printGameStatus() {
        System.out.println("=== GAME STATUS UPDATE ===");
        System.out.println("State: " + gameState);
        System.out.println("Lives: " + sharedLives);
        System.out.println("Score: " + sharedScore);
        System.out.println("Board 1 Safe Cells Left: " + board1.getSafeCellsRemaining());
        System.out.println("Board 2 Safe Cells Left: " + board2.getSafeCellsRemaining());

        if (gameState == GameState.WON) {
            System.out.println("RESULT: VICTORY! The team cleared all mines.");
        } else if (gameState == GameState.LOST) {
            System.out.println("RESULT: GAME OVER. The team lost.");
        }
        System.out.println("==========================");
    }

    // --- Life Management ---

    /**
     * Sets the shared lives value, enforcing the MAX_LIVES cap.
     * Extra lives above the cap are converted to score.
     */
    public void setSharedLives(int newLives) {
        if (newLives > MAX_LIVES) {
            int excess = newLives - MAX_LIVES;
            this.sharedLives = MAX_LIVES;
            this.sharedScore += excess * difficulty.getActivationCost();
            System.out.println("Life cap reached! Converted " + excess + " excess lives to " + (excess * difficulty.getActivationCost()) + " points.");
        } else {
            this.sharedLives = newLives;
        }
        checkGameStatus();
    }

    /**
     * Adds one life if below MAX_LIVES; otherwise converts it to score.
     * Used by positive rewards (e.g. correct questions, surprises).
     *
     * @param pointsValue score value to add if life is converted due to cap.
     */
    public void addLife(int pointsValue) {
        if (sharedLives < MAX_LIVES) {
            sharedLives++;
        } else {
            sharedScore += pointsValue;
            System.out.println("Life cap reached! Converted life gain to " + pointsValue + " points.");
        }
        checkGameStatus();
    }

    /**
     * Deducts lives and triggers a status check for possible loss.
     */
    public void deductLife(int lives) {
        this.sharedLives -= lives;
        checkGameStatus();
    }

    // --- Getters and Setters ---

    public void setSharedScore(int sharedScore) {
        this.sharedScore = sharedScore;
        checkGameStatus();
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    // --- LOGIC FROM IMAGES ---
    /**
     * Activates the behavior of a QUESTION or SURPRISE cell after it was revealed and chosen.
     * Deducts activation cost from score, then routes to surprise logic or question handling.
     */
    public boolean activateSpecialCell(Board board, Cell.CellContent cellContent) {
        if (cellContent != Cell.CellContent.QUESTION &&
                cellContent != Cell.CellContent.SURPRISE) {
            return false;
        }

        int cost = difficulty.getActivationCost();

        // Not enough points -> NO activation
        if (sharedScore < cost) {
            this.lastActionMessage = "You need at least " + cost +
                    " points to activate this " +
                    cellContent.name().toLowerCase() + " cell.";
            return false;
        }

        // ==========================
        // SURPRISE
        // ==========================
        if (cellContent == Cell.CellContent.SURPRISE) {
            int beforeScore = sharedScore;
            int beforeLives = sharedLives;

            sharedScore -= cost;     // pay activation cost
            handleSurprise();        // applies reward/penalty internally

            int afterScore = sharedScore;
            int afterLives = sharedLives;

            this.lastActionMessage =
                    "🎲 Surprise activated!\n" +
                            "Activation cost: -" + cost + " pts\n" +
                            "Score: " + beforeScore + " → " + afterScore + "\n" +
                            "Lives: " + beforeLives + " → " + afterLives;

            return true;
        }

        // ==========================
        // QUESTION
        // ==========================
        if (questionManager == null || questionPresenter == null) {
            this.lastActionMessage = "Question system is not available.";
            return false;
        }

        Question q = questionManager.getRandomUnusedQuestionAnyLevel();
        if (q == null) {
            this.lastActionMessage = "No questions available.";
            return false;
        }



        int beforeScore = sharedScore;
        int beforeLives = sharedLives;

        // pay cost ONLY now
        sharedScore -= cost;

        boolean isCorrect = questionPresenter.presentQuestion(q);
        QuestionLevel level = q.getQuestionLevel();

        // apply table rules + get explanation
        ScoreRules.Result r = processQuestionAnswer(level, isCorrect);

        // special effects (EASY GAME only, correct only)
        String effect = "";
        if (difficulty == Difficulty.EASY && isCorrect) {
            if (level == QuestionLevel.MEDIUM) {
                board.revealRandomMine(); // reward reveal, no extra cost
                effect = "\nSpecial effect: revealed 1 mine (reward).";
            } else if (level == QuestionLevel.HARD) {
                board.revealRandom3x3AreaReward(); // reward reveal, no extra cost
                effect = "\nSpecial effect: revealed random 3x3 area (reward).";
            }
        }

        int afterScore = sharedScore;
        int afterLives = sharedLives;

        this.lastActionMessage =
                (isCorrect ? "✅ Correct!\n" : "❌ Wrong!\n") +
                        "Activation cost: -" + cost + " pts\n" +
                        r.details +
                        effect +
                        "\nScore: " + beforeScore + " → " + afterScore +
                        "\nLives: " + beforeLives + " → " + afterLives;

        return true;
    }





    /**
     * Handles SURPRISE cell effects: randomly applies good or bad outcome.
     * Good: points + life; Bad: lose points + lose life.
     */
    private void handleSurprise() {
        Random rand = new Random();
        boolean isGoodSurprise = rand.nextBoolean();
        int pointsValue = difficulty.getSurpriseValue();

        String intro = "You've activated a Surprise cell! There's a 50/50 chance of a reward or a penalty. Let's see what you got...\n\n";

        if (isGoodSurprise) {
            sharedScore += pointsValue;
            addLife(pointsValue);
            this.lastActionMessage = intro + "A stroke of luck! You've been awarded " + pointsValue + " points and an extra life!";
        } else {
            sharedScore -= pointsValue;
            deductLife(1);
            this.lastActionMessage = intro + "An unfortunate turn of events! You've lost " + pointsValue + " points and a life.";
        }
    }
    /**
     * Processes the result of a question answer and applies points/lives
     * according to game difficulty and question level.
     */
    public ScoreRules.Result processQuestionAnswer(QuestionLevel qLevel, boolean isCorrect) {
        if (gameState != GameState.RUNNING) {
            return new ScoreRules.Result(0, 0, "Game not running.");
        }

        totalQuestionsAnswered++;
        if (isCorrect) totalCorrectAnswers++;

        ScoreRules.Result r = ScoreRules.compute(difficulty, qLevel, isCorrect);

        // apply score delta
        sharedScore += r.deltaScore;

        // apply lives delta (with cap handling through addLife)
        if (r.deltaLives > 0) {
            for (int i = 0; i < r.deltaLives; i++) {
                addLife(difficulty.getActivationCost());
            }
        } else if (r.deltaLives < 0) {
            deductLife(-r.deltaLives);
        }

        checkGameStatus();
        return r;
    }




    /**
     * Hook for UI to present a question and return true/false for correctness.
     */
    public interface QuestionPresenter {
        boolean presentQuestion(Question question);
    }

    public void setQuestionPresenter(QuestionPresenter presenter) {
        this.questionPresenter = presenter;
    }

    public void setQuestionManager(QuestionManager manager) {
        this.questionManager = manager;
    }

    public QuestionManager getQuestionManager() {
        return questionManager;
    }

    /**
     * Applies positive rewards after a correct answer: adds points and lives.
     */
    private void addRewards(int points, int lives) {
        this.sharedScore += points;
        for (int i = 0; i < lives; i++) {
            addLife(points);
        }
        System.out.println("Correct! +" + points + " pts, +" + lives + " lives.");
    }
    /**
     * Applies penalties after an incorrect answer: removes points and lives.
     */
    private void applyPenalties(int points, int lives) {
        this.sharedScore -= points;
        deductLife(lives);
        System.out.println("Incorrect! -" + points + " pts, -" + lives + " lives.");
    }

    // --- Turn Handling ---

    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(int currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public void switchTurn() {
        if (gameState != GameState.RUNNING) return;
        currentPlayerTurn = (currentPlayerTurn == 1) ? 2 : 1;
    }

    public String getAndClearLastActionMessage() {
        String message = this.lastActionMessage;
        this.lastActionMessage = null;
        return message;
    }

    public void setLastActionMessage(String msg) {
        this.lastActionMessage = msg;
    }





    // --- Getters ---

    public GameState getGameState() { return gameState; }
    public Board getBoard1() { return board1; }
    public Board getBoard2() { return board2; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getSharedLives() { return sharedLives; }
    public int getSharedScore() { return sharedScore; }
    public int getMaxLives() { return MAX_LIVES; }
    public int getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

}