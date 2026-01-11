package Model;

import Model.specialcell.SpecialCellActivator;
import Model.specialcell.factory.ActivatorFactoryRegistry;
import Model.specialcell.factory.QuestionActivatorFactory;
import Model.specialcell.factory.SurpriseActivatorFactory;

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
    private int totalSurprisesOpened;

    //  Field to store the message for the View
    private String lastActionMessage;
    private int totalQuestionsAnswered;
    private int totalCorrectAnswers;

    //  Factory Method registry (DP1)
    private final ActivatorFactoryRegistry activatorRegistry =
            new ActivatorFactoryRegistry(
                    new QuestionActivatorFactory(),
                    new SurpriseActivatorFactory()
            );

    /**
     * Question difficulty levels used for scoring and life rewards/penalties.
     */
    public enum QuestionLevel {EASY, MEDIUM, HARD, EXPERT}

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
        this.totalSurprisesOpened = 0;

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
     * <p>
     * DP1: Factory Method - Game no longer decides which activator to instantiate.
     */
    public boolean activateSpecialCell(Board board, Cell.CellContent cellContent) {
        SpecialCellActivator activator = activatorRegistry.create(cellContent, this, board);
        if (activator == null) {
            return false;
        }
        return activator.activate(); // template method (final) runs the flow
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
        QuestionResult presentQuestion(Question question);
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

    public GameState getGameState() {
        return gameState;
    }

    public Board getBoard1() {
        return board1;
    }

    public Board getBoard2() {
        return board2;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getSharedLives() {
        return sharedLives;
    }

    public int getSharedScore() {
        return sharedScore;
    }

    public int getMaxLives() {
        return MAX_LIVES;
    }

    public int getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public boolean hasQuestionPresenter() {
        return questionPresenter != null;
    }

    public QuestionResult presentQuestion(Question q) {
        return questionPresenter.presentQuestion(q);
    }

    public int getTotalSurprisesOpened() {
        return totalSurprisesOpened;
    }

    public void incrementSurprisesOpened() {
        totalSurprisesOpened++;
    }

}
