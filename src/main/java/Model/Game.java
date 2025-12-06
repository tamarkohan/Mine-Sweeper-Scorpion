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

    // 🔥 NEW ADDITION 1: Field to store the message for the View
    private String lastActionMessage;

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
        this.lastActionMessage = null; // Initialize the message field

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
            this.sharedLives = 0;
            gameState = GameState.LOST;
            endGameProcessing();
            return;
        }

        // 2. Win Condition Check (All mines found on EITHER board)
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

            // 2. Auto-reveal all cells (Requires Board.revealAll() implemented in Board class)
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

    public void addLife(int pointsValue) {
        if (sharedLives < MAX_LIVES) {
            sharedLives++;
        } else {
            sharedScore += pointsValue;
            System.out.println("Life cap reached! Converted life gain to " + pointsValue + " points.");
        }
        checkGameStatus();
    }

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

    public void activateSpecialCell(Cell.CellContent cellContent, Integer questionId) {
        if (cellContent != Cell.CellContent.QUESTION && cellContent != Cell.CellContent.SURPRISE) {
            return;
        }

        int cost = difficulty.getActivationCost();

        if (sharedScore < cost) {
            this.lastActionMessage = "You need at least " + cost + " points to activate this " + cellContent.name().toLowerCase() + " cell.";
            return;
        }

        sharedScore -= cost;

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

    public void processQuestionAnswer(QuestionLevel qLevel, boolean isCorrect) {
        if (gameState != GameState.RUNNING) return;

        Random rand = new Random();

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
        } else if (difficulty == Difficulty.MEDIUM) {
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
        } else if (difficulty == Difficulty.HARD) {
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
        checkGameStatus();
    }

    private void addRewards(int points, int lives) {
        this.sharedScore += points;
        for (int i = 0; i < lives; i++) {
            addLife(points);
        }
        System.out.println("Correct! +" + points + " pts, +" + lives + " lives.");
    }

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

    // --- Getters ---

    public GameState getGameState() { return gameState; }
    public Board getBoard1() { return board1; }
    public Board getBoard2() { return board2; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getSharedLives() { return sharedLives; }
    public int getSharedScore() { return sharedScore; }
    public int getMaxLives() { return MAX_LIVES; }
}