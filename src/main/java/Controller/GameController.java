package Controller;

import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;
import Model.GameState;

/**
 * Controller class between the UI (View) and the Game model.
 * All communication between View and Model should pass through here.
 */
public class GameController {

    private Game currentGame;

    /**
     * Creates a new Game instance with the selected difficulty.
     * This is the main entry point for starting a cooperative game.
     */
    public void startNewGame(Difficulty difficulty) {
        currentGame = new Game(difficulty);
    }

    /**
     * Overload: creates a new Game instance from a difficulty key ("EASY", "MEDIUM", "HARD").
     * This lets the View pass only a String (no direct reference to the Model's enum).
     */
    public void startNewGame(String difficultyKey) {
        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(difficultyKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            difficulty = Difficulty.EASY;
        }
        startNewGame(difficulty);
    }

    /**
     * Restarts the current game using the same difficulty.
     * If no game exists yet, nothing happens.
     */
    public void restartGame() {
        if (currentGame != null) {
            currentGame.restartGame();
        }
    }

    /**
     * Returns the current Game instance.
     * NOTE: Views should avoid using this directly – it's mostly for tests or debugging.
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    // ======================================================
    //  GAME STATE / TURN INFO FOR THE VIEW
    // ======================================================

    public boolean isGameRunning() {
        return currentGame != null && currentGame.getGameState() == GameState.RUNNING;
    }

    public boolean isGameOver() {
        if (currentGame == null) return false;
        return currentGame.getGameState() != GameState.RUNNING;
    }

    public int getCurrentPlayerTurn() {
        return (currentGame != null) ? currentGame.getCurrentPlayerTurn() : 0;
    }

    public String getDifficultyName() {
        if (currentGame == null || currentGame.getDifficulty() == null) return "";
        return currentGame.getDifficulty().name();
    }

    public int getSharedLives() {
        return (currentGame != null) ? currentGame.getSharedLives() : 0;
    }

    public int getSharedScore() {
        return (currentGame != null) ? currentGame.getSharedScore() : 0;
    }

    public int getStartingLives() {
        if (currentGame == null || currentGame.getDifficulty() == null) return 0;
        return currentGame.getDifficulty().getStartingLives();
    }

    public int getMaxLives() {
        return (currentGame != null) ? currentGame.getMaxLives() : 0;
    }

    public String getAndClearLastActionMessage() {
        if (currentGame != null) {
            return currentGame.getAndClearLastActionMessage();
        }
        return null;
    }
    
    public void processTurnEnd() {
        if (currentGame == null || currentGame.getGameState() != GameState.RUNNING) return;
        currentGame.switchTurn();
    }

    // ======================================================
    //  BOARD-LEVEL INFO FOR THE VIEW
    // ======================================================

    private Board getBoard(int boardNumber) {
        if (currentGame == null) return null;
        return (boardNumber == 1) ? currentGame.getBoard1() : currentGame.getBoard2();
    }

    public int getBoardRows(int boardNumber) {
        Board b = getBoard(boardNumber);
        return (b != null) ? b.getRows() : 0;
    }

    public int getBoardCols(int boardNumber) {
        Board b = getBoard(boardNumber);
        return (b != null) ? b.getCols() : 0;
    }

    public int getTotalMines(int boardNumber) {
        Board b = getBoard(boardNumber);
        return (b != null) ? b.getTotalMines() : 0;
    }

    public int getMinesLeft(int boardNumber) {
        Board b = getBoard(boardNumber);
        if (b == null) return 0;

        int total = b.getTotalMines();
        int foundMines = 0;

        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                Cell cell = b.getCell(r, c);
                if (cell == null) continue;

                if (cell.isMine() && (cell.isRevealed() || cell.isFlagged())) {
                    foundMines++;
                }
            }
        }

        int remaining = total - foundMines;
        return Math.max(remaining, 0);
    }

    // ======================================================
    //  CELL-LEVEL OPERATIONS FOR THE VIEW
    // ======================================================

    public void revealCellUI(int boardNumber, int row, int col) {
        if (currentGame == null || !isGameRunning()) return;
        Board board = getBoard(boardNumber);
        if (board == null) return;
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) return;
        board.revealCell(row, col);
    }

    public void toggleFlagUI(int boardNumber, int row, int col) {
        if (currentGame == null || !isGameRunning()) return;
        Board board = getBoard(boardNumber);
        if (board == null) return;
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) return;
        board.toggleFlag(row, col);
    }

    public CellViewData getCellViewData(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        if (board == null) return new CellViewData(true, "");
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) return new CellViewData(true, "");
        Cell cell = board.getCell(row, col);
        if (cell == null) return new CellViewData(true, "");

        switch (cell.getState()) {
            case HIDDEN:
                return new CellViewData(true, "");
            case FLAGGED:
                return new CellViewData(true, "F");
            case REVEALED:
                switch (cell.getContent()) {
                    case MINE:
                        return new CellViewData(false, "M");
                    case NUMBER:
                        return new CellViewData(false, String.valueOf(cell.getAdjacentMines()));
                    case QUESTION:
                        return new CellViewData(false, "Q");
                    case SURPRISE:
                        return new CellViewData(false, "S");
                    case EMPTY:
                    default:
                        return new CellViewData(false, "");
                }
            default:
                return new CellViewData(true, "");
        }
    }

    public static class CellViewData {
        public final boolean enabled;
        public final String text;

        public CellViewData(boolean enabled, String text) {
            this.enabled = enabled;
            this.text = text;
        }
    }

    public boolean isCellFlagged(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        return board != null && board.isFlagged(row, col);
    }
}