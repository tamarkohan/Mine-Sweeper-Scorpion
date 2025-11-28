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
        GameState state = currentGame.getGameState();
        return state == GameState.WON || state == GameState.LOST;
    }

    public int getCurrentPlayerTurn() {
        return (currentGame != null) ? currentGame.getCurrentPlayerTurn() : 0;
    }

    public void switchTurn() {
        if (currentGame != null) {
            currentGame.switchTurn();
        }
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

    /**
     * Computes remaining mines on a board, based on revealed/flagged correct mines.
     */
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

    /**
     * Used by the UI to reveal a cell following MVC (View -> Controller -> Model).
     * This delegates to Board.revealCell, which contains the game logic.
     */
    public boolean revealCellUI(int boardNumber, int row, int col) {
        if (currentGame == null || !isGameRunning()) return false;

        Board board = getBoard(boardNumber);
        if (board == null) return false;

        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) {
            return false;
        }

        board.revealCell(row, col);
        return true;
    }

    /**
     * 🔥 NEW: Used by the UI (right-click) to toggle the flag state of a cell.
     * This delegates to Board.toggleFlag, which contains the game logic and scoring.
     */
    public void toggleFlagUI(int boardNumber, int row, int col) {
        if (currentGame == null || !isGameRunning()) return;

        Board board = getBoard(boardNumber);
        if (board == null) return;

        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) {
            return;
        }

        // Call the Model's logic method to toggle the flag, update score, etc.
        board.toggleFlag(row, col);
    }


    /**
     * Returns display data for a single cell.
     * The View uses only this (text + enabled) and לא נוגעת ב-Cell / Board.
     */
    public CellViewData getCellViewData(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        if (board == null) {
            return new CellViewData(true, "");
        }

        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) {
            return new CellViewData(true, "");
        }

        Cell cell = board.getCell(row, col);
        if (cell == null) {
            return new CellViewData(true, "");
        }

        switch (cell.getState()) {
            case HIDDEN:
                return new CellViewData(true, "");

            case FLAGGED:
                return new CellViewData(true, "F");

            case REVEALED:
                // Revealed cells are disabled
                switch (cell.getContent()) {
                    case MINE:
                        return new CellViewData(false, "M");
                    case NUMBER:
                        return new CellViewData(false,
                                String.valueOf(cell.getAdjacentMines()));
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

    /**
     * Small DTO for what the View needs for each cell.
     * No direct Model enums/types נחשפים ל-View.
     */
    public static class CellViewData {
        public final boolean enabled;
        public final String text;

        public CellViewData(boolean enabled, String text) {
            this.enabled = enabled;
            this.text = text;
        }
    }

    // ======================================================
    //  ORIGINAL REVEAL LOGIC (כמו שהיה אצלך) – לא נוגעת בו
    // ======================================================

    /**
     * Reveals a cell on the specified board.
     * For question and surprise cells, this method:
     * - Checks if the cell was already used
     * - If already used, skips the special effect and does nothing
     * - If not used, marks it as used and triggers the special effect
     *
     * NOTE: This method נשאר כמו שהוא, כדי לא לשנות לוגיקה קיימת.
     * כרגע ה-View משתמש ב-revealCellUI, אבל אפשר להשתמש גם בזה אם תצטרכי.
     *
     * @param boardNumber 1 for board1, 2 for board2
     * @param row the row index of the cell
     * @param col the column index of the cell
     * @return true if the cell was successfully revealed/activated, false otherwise
     */
    public boolean revealCell(int boardNumber, int row, int col) {
        if (currentGame == null) {
            return false;
        }

        Board board = (boardNumber == 1) ? currentGame.getBoard1() : currentGame.getBoard2();
        if (board == null || row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) {
            return false;
        }

        Cell cell = board.getCell(row, col);
        if (cell == null || cell.getState() == Cell.CellState.REVEALED) {
            return false;
        }

        // Handle question and surprise cells
        if (cell.getContent() == Cell.CellContent.QUESTION ||
                cell.getContent() == Cell.CellContent.SURPRISE) {

            // Check if cell is already used
            if (cell.isUsed()) {
                // Cell was already used, skip the special effect
                // Just reveal it (change state to REVEALED) without triggering effect
                cell.setState(Cell.CellState.REVEALED);
                return true;
            }

            // Cell is not used yet - activate it for the first time
            cell.setUsed(true);
            cell.setState(Cell.CellState.REVEALED);

            // Trigger the special effect via Game class
            currentGame.activateSpecialCell(cell.getContent(), cell.getQuestionId());
        }

    return false;
}}
