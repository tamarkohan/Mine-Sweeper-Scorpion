package Controller;

import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;
import Model.GameState;
import Model.QuestionManager;

/**
 * Controller class between the UI (View) and the Game model.
 * All communication between View and Model pass through here.
 */
public class GameController {

    // Singleton pattern – ensures only one controller exists for the whole app
    private static GameController instance;

    private Game currentGame;
    private QuestionManager questionManager;
    // Private constructor – prevents external instantiation

    private GameController() {
    }
    // Returns the single shared Controller instance
    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }
    /**
     * Creates a new Game instance with the selected difficulty.
     * This is the main entry point for starting a cooperative game.
     */
    public void startNewGame(Difficulty difficulty) {
        ensureQuestionManager();
        currentGame = new Game(difficulty);
        currentGame.setQuestionManager(questionManager);
        // Presenter is set by the View layer via registerQuestionPresenter
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
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * Provides access to the shared QuestionManager, creating/loading if needed.
     */
    public QuestionManager getQuestionManager() {
        ensureQuestionManager();
        return questionManager;
    }

    private void ensureQuestionManager() {
        if (questionManager == null) {
            questionManager = new QuestionManager();
            questionManager.loadQuestions();
        }
    }

    /**
     * Registers the UI question presenter (popup) to be invoked on QUESTION cells.
     */
    public void registerQuestionPresenter(Game.QuestionPresenter presenter) {
        if (currentGame != null) {
            currentGame.setQuestionPresenter(presenter);
        }
    }

    // ======================================================
    //  GAME STATE / TURN INFO FOR THE VIEW
    // ======================================================

    // Returns true if a game exists and is currently in RUNNING state.

    public boolean isGameRunning() {
        return currentGame != null && currentGame.getGameState() == GameState.RUNNING;
    }
    // Returns true if the game has ended with WIN or LOSS.
    public boolean isGameOver() {
        if (currentGame == null) return false;
        return currentGame.getGameState() != GameState.RUNNING;
    }
    // Returns the current player's turn
    public int getCurrentPlayerTurn() {
        return (currentGame != null) ? currentGame.getCurrentPlayerTurn() : 0;
    }
    // Switches turn between players.
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

    // Returns board instance based on board number
    private Board getBoard(int boardNumber) {
        if (currentGame == null) return null;
        return (boardNumber == 1) ? currentGame.getBoard1() : currentGame.getBoard2();
    }
    // Returns the number of rows for the specified board.
    public int getBoardRows(int boardNumber) {
        Board b = getBoard(boardNumber);
        return (b != null) ? b.getRows() : 0;
    }
    // Returns the number of columns for the specified board.
    public int getBoardCols(int boardNumber) {
        Board b = getBoard(boardNumber);
        return (b != null) ? b.getCols() : 0;
    }
    // Returns the total number of mines placed on the specified board.
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

    /**
     * Used by the UI to reveal a cell following MVC (View -> Controller -> Model).
     * This delegates to Board.revealCell, which contains the game logic.
     */
    public boolean revealCellUI(int boardNumber, int row, int col) {
        if (currentGame == null || !isGameRunning()) return false;

        Board board = getBoard(boardNumber);
        if (board == null) return;
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) return;
        board.revealCell(row, col);
    }

    /**
     * Used by the UI (right-click) to toggle the flag state of a cell.
     * This delegates to Board.toggleFlag, which contains the game logic and scoring.
     */
    public void toggleFlagUI(int boardNumber, int row, int col) {
        if (currentGame == null || !isGameRunning()) return;
        Board board = getBoard(boardNumber);
        if (board == null) return;
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) return;
        board.toggleFlag(row, col);
    }


    /**
     * Provides UI-only cell data (text + enabled state) without exposing Model internals.
     */
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

            case REVEALED: {
                boolean enabled;
                String text;

                switch (cell.getContent()) {
                    case MINE:
                        text = "M";
                        enabled = false; // מוקש נחשף – אי אפשר ללחוץ
                        break;

                    case NUMBER:
                        text = String.valueOf(cell.getAdjacentMines());
                        enabled = false; // מספר – כמו מיניסוויפר רגיל
                        break;

                    case QUESTION:
                        text = "Q";
                        // אפשר ללחוץ ולהפעיל *רק אם עדיין לא used*
                        enabled = !cell.isUsed();
                        break;

                    case SURPRISE:
                        text = "S";
                        enabled = !cell.isUsed();
                        break;

                    case EMPTY:
                    default:
                        text = "";
                        enabled = false;
                        break;
                }

                return new CellViewData(enabled, text);
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

    /**
     * Reveals a cell on the specified board.
     * For question and surprise cells, this method:
     * - Checks if the cell was already used
     * - If already used, skips the special effect and does nothing
     * - If not used, marks it as used and triggers the special effect
     * @param boardNumber 1 for board1, 2 for board2
     * @param row the row index of the cell
     * @param col the column index of the cell
     * @return true if the cell was successfully revealed/activated, false otherwise
     */

    public boolean activateSpecialCellUI(int boardNumber, int row, int col) {
        if (currentGame == null || !isGameRunning()) return false;
        Board board = getBoard(boardNumber);
        if (board == null) return false;
        return board.activateSpecialCell(row, col);
    }
    public boolean isQuestionOrSurprise(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        if (board == null) return false;
        Cell cell = board.getCell(row, col);
        if (cell == null) return false;

        Cell.CellContent content = cell.getContent();
        return content == Cell.CellContent.QUESTION ||
                content == Cell.CellContent.SURPRISE;
    }
    public boolean isCellRevealed(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        if (board == null) return false;
        Cell cell = board.getCell(row, col);
        if (cell == null) return false;
        return cell.isRevealed();
    }


}


