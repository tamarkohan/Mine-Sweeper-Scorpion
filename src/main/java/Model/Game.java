package Model;

import java.util.Random;

public class Game {
    private final int MAX_LIVES = 10;
    private Board board1;
    private Board board2;
    private Difficulty difficulty;
    private int sharedLives;
    private int sharedScore;
    private GameState gameState;
    private int currentPlayerTurn;

    public enum QuestionLevel { EASY, MEDIUM, HARD, EXPERT }

    public Game(Difficulty difficulty) {
        startNewGame(difficulty);
    }

    public void startNewGame(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.sharedLives = difficulty.getStartingLives();
        this.sharedScore = 0;
        this.gameState = GameState.RUNNING;
        this.currentPlayerTurn = 1;

        this.board1 = new Board(difficulty, this);
        this.board2 = new Board(difficulty, this);
    }

    public void restartGame() {
        if (this.difficulty != null) {
            startNewGame(this.difficulty);
        }
    }

    // --- Game Status & End Game Logic ---

    public void checkGameStatus() {
        if (gameState != GameState.RUNNING) return;

        // 1. Loss Condition Check (Lives <= 0)
        if (sharedLives <= 0) {
            this.sharedLives = 0; // Ensure display doesn't show negative lives
            gameState = GameState.LOST;
            endGameProcessing();
            return;
        }

        // 2. Win Condition Check (All safe cells cleared)
        if (board1.getSafeCellsRemaining() == 0 && board2.getSafeCellsRemaining() == 0) {
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

    // --- Life Management (Restored and Corrected) ---

    /**
     * 🔥 RESTORED & CORRECTED: Setter for sharedLives.
     * Used by external classes (like Board) that deduct or add lives.
     */
    public void setSharedLives(int newLives) {
        // 1. Apply Life Cap and Score Conversion
        if (newLives > MAX_LIVES) {
            int excess = newLives - MAX_LIVES;
            this.sharedLives = MAX_LIVES;
            // Convert excess lives to score using the activation cost as a multiplier
            this.sharedScore += excess * difficulty.getActivationCost();
            System.out.println("Life cap reached! Converted " + excess + " excess lives to " + (excess * difficulty.getActivationCost()) + " points.");
        } else {
            this.sharedLives = newLives;
        }

        // 2. Check Game Status (for Loss or Win)
        checkGameStatus();
    }

    /**
     * NEW: A method to add lives, enforcing the MAX_LIVES limit and conversion to score.
     * Used internally by rewards/surprises.
     * @param pointsValue The score to award if the life is capped.
     */
    public void addLife(int pointsValue) {
        if (sharedLives < MAX_LIVES) {
            sharedLives++;
            // Note: score for the action (e.g. correct answer) must be added separately.
        } else {
            // Convert surplus life to the value associated with the action
            sharedScore += pointsValue;
            System.out.println("Life cap reached! Converted life gain to " + pointsValue + " points.");
        }
        checkGameStatus();
    }

    /**
     * NEW: A clean method to deduct lives, triggering the loss check.
     * Used internally by penalties/surprises.
     */
    public void deductLife(int lives) {
        this.sharedLives -= lives;
        checkGameStatus();
    }

    // --- Getters and Setters ---

    public void setSharedScore(int sharedScore) {
        this.sharedScore = sharedScore;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    // --- LOGIC FROM IMAGES ---

    public void activateSpecialCell(Cell.CellContent cellContent, Integer questionId) {
        if (cellContent != Cell.CellContent.QUESTION && cellContent != Cell.CellContent.SURPRISE) {
            return;
        }

        int cost = difficulty.getActivationCost();

        // 1. Check Cost
        if (sharedScore < cost) {
            System.out.println("Not enough points to activate special cell! (Cost: " + cost + ")");
            return;
        }

        // Deduct cost
        sharedScore -= cost;

        // 2. Route Logic
        if (cellContent == Cell.CellContent.SURPRISE) {
            handleSurprise();
        } else if (cellContent == Cell.CellContent.QUESTION) {
            System.out.println("Question Activated! Waiting for answer...");
        }
    }

    private void handleSurprise() {
        Random rand = new Random();
        boolean isGoodSurprise = rand.nextBoolean();
        int pointsValue = difficulty.getSurpriseValue();

        if (isGoodSurprise) {
            // Good Surprise: Gained points AND attempted 1 life.
            sharedScore += pointsValue;
            addLife(pointsValue); // Add 1 life (handles cap using pointsValue for conversion)
            System.out.println("Surprise! Gained " + pointsValue + " points and attempted 1 Life.");
        } else {
            // Bad Surprise: Lost points AND lost 1 life.
            sharedScore -= pointsValue;
            deductLife(1); // Lost 1 life (triggers loss check)
            System.out.println("Surprise! Lost " + pointsValue + " points and 1 Life.");
        }
    }

    public void processQuestionAnswer(QuestionLevel qLevel, boolean isCorrect) {
        if (gameState != GameState.RUNNING) return;

        Random rand = new Random();

        // --- EASY GAME DIFFICULTY ---
        if (difficulty == Difficulty.EASY) {
            if (isCorrect) {
                switch (qLevel) {
                    case EASY:   addRewards(3, 1); break;
                    case MEDIUM: addRewards(6, 0); break;
                    case HARD:   addRewards(10, 0); break;
                    case EXPERT: addRewards(15, 2); break;
                }
            } else {
                switch (qLevel) {
                    case EASY:   if (rand.nextBoolean()) applyPenalties(3, 0); break;
                    case MEDIUM: if (rand.nextBoolean()) applyPenalties(6, 0); break;
                    case HARD:   applyPenalties(10, 0); break;
                    case EXPERT: applyPenalties(15, 1); break;
                }
            }
        }

        // --- MEDIUM GAME DIFFICULTY ---
        else if (difficulty == Difficulty.MEDIUM) {
            if (isCorrect) {
                switch (qLevel) {
                    case EASY:   addRewards(8, 1); break;
                    case MEDIUM: addRewards(10, 1); break;
                    case HARD:   addRewards(15, 1); break;
                    case EXPERT: addRewards(20, 2); break;
                }
            } else {
                switch (qLevel) {
                    case EASY:   applyPenalties(8, 0); break;
                    case MEDIUM: if (rand.nextBoolean()) applyPenalties(10, 1); break;
                    case HARD:   applyPenalties(15, 1); break;
                    case EXPERT:
                        if (rand.nextBoolean()) applyPenalties(20, 1);
                        else applyPenalties(20, 2);
                        break;
                }
            }
        }

        // --- HARD GAME DIFFICULTY ---
        else if (difficulty == Difficulty.HARD) {
            if (isCorrect) {
                switch (qLevel) {
                    case EASY:   addRewards(10, 1); break;
                    case MEDIUM:
                        if (rand.nextBoolean()) addRewards(15, 1);
                        else addRewards(15, 2);
                        break;
                    case HARD:   addRewards(20, 2); break;
                    case EXPERT: addRewards(40, 3); break;
                }
            } else {
                switch (qLevel) {
                    case EASY:   applyPenalties(10, 1); break;
                    case MEDIUM: if (rand.nextBoolean()) applyPenalties(15, 1); break;
                    case HARD:   applyPenalties(20, 2); break;
                    case EXPERT: applyPenalties(40, 3); break;
                }
            }
        }

        // Final check just in case, though it's run by life changes.
        checkGameStatus();
    }

    private void addRewards(int points, int lives) {
        this.sharedScore += points;
        // Use the dedicated life management method, passing the point reward value for cap conversion
        for (int i = 0; i < lives; i++) {
            addLife(points);
        }
        System.out.println("Correct! +" + points + " pts, +" + lives + " lives.");
    }

    private void applyPenalties(int points, int lives) {
        this.sharedScore -= points;
        // Use the dedicated life deduction method (triggers loss check)
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

    // --- Getters ---

    public GameState getGameState() { return gameState; }
    public Board getBoard1() { return board1; }
    public Board getBoard2() { return board2; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getSharedLives() { return sharedLives; }
    public int getSharedScore() { return sharedScore; }
}