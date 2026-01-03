package Controller;

import Model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        questionManager.resetForNewGame();
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
        if (board == null) return false;
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) return true;
        board.revealCell(row, col);
        return true;
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
                return new CellViewData(true, "🚩");

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

    public boolean isQuestionCell(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        if (board == null) return false;
        Cell cell = board.getCell(row, col);
        if (cell == null) return false;
        return cell.getContent() == Cell.CellContent.QUESTION;
    }

    public boolean isSurpriseCell(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        if (board == null) return false;
        Cell cell = board.getCell(row, col);
        if (cell == null) return false;
        return cell.getContent() == Cell.CellContent.SURPRISE;
    }

    public boolean isCellRevealed(int boardNumber, int row, int col) {
        Board board = getBoard(boardNumber);
        if (board == null) return false;
        Cell cell = board.getCell(row, col);
        if (cell == null) return false;
        return cell.isRevealed();
    }

    // =======================
    //  HISTORY VIEW DATA (DTOs)
    // =======================

    public static class GameHistoryRow {
        public final String players;
        public final String dateTime;
        public final String difficulty;
        public final int finalScore;
        public final int remainingLives;
        public final String correctAnswers;
        public final String accuracy;
        public final String duration;

        public GameHistoryRow(String players, String dateTime, String difficulty,
                              int finalScore, int remainingLives,
                              String correctAnswers, String accuracy, String duration) {
            this.players = players;
            this.dateTime = dateTime;
            this.difficulty = difficulty;
            this.finalScore = finalScore;
            this.remainingLives = remainingLives;
            this.correctAnswers = correctAnswers;
            this.accuracy = accuracy;
            this.duration = duration;
        }
    }

    public static class PlayerHistoryRow {
        public final String player;
        public final int totalGames;
        public final int bestScore;
        public final String averageAccuracy;
        public final String preferredDifficulty;

        public PlayerHistoryRow(String player, int totalGames, int bestScore,
                                String averageAccuracy, String preferredDifficulty) {
            this.player = player;
            this.totalGames = totalGames;
            this.bestScore = bestScore;
            this.averageAccuracy = averageAccuracy;
            this.preferredDifficulty = preferredDifficulty;
        }
    }

    /**
     * Called by the View when a game ends.
     * Stores a GameHistoryEntry in the Model layer.
     */
    public void recordFinishedGame(String player1Name, String player2Name, long durationSeconds) {
        if (currentGame == null) return;

        boolean isWin = currentGame.getGameState() == GameState.WON;

        int totalQ = currentGame.getTotalQuestionsAnswered();
        int correctQ = currentGame.getTotalCorrectAnswers();

        GameHistoryEntry entry = new GameHistoryEntry(
                LocalDateTime.now(),
                player1Name,
                player2Name,
                getDifficultyName(),
                isWin ? "WON" : "LOST",
                getSharedScore(),
                getSharedLives(),
                durationSeconds,
                totalQ,
                correctQ
        );

        GameHistoryManager.getInstance().addEntry(entry);
    }

    public List<GameHistoryRow> getGameHistory(String difficultyFilter,
                                               String resultFilter,
                                               String searchTerm) {
        String diff = (difficultyFilter == null) ? "All" : difficultyFilter;
        String res  = (resultFilter == null) ? "All" : resultFilter;
        String search = (searchTerm == null) ? "" : searchTerm.trim().toLowerCase();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

        List<GameHistoryRow> rows = new ArrayList<>();

        for (GameHistoryEntry e : GameHistoryManager.getInstance().getEntries()) {

            if (!"All".equalsIgnoreCase(diff) &&
                    !e.getDifficulty().equalsIgnoreCase(diff)) {
                continue;
            }

            if (!"All".equalsIgnoreCase(res) &&
                    !e.getResult().equalsIgnoreCase(res)) {
                continue;
            }

            String p1 = e.getPlayer1Name() == null ? "" : e.getPlayer1Name();
            String p2 = e.getPlayer2Name() == null ? "" : e.getPlayer2Name();
            String playersCombined = p1 + " + " + p2;

            if (!search.isEmpty()) {
                if (!p1.toLowerCase().contains(search) &&
                        !p2.toLowerCase().contains(search)) {
                    continue;
                }
            }

            rows.add(new GameHistoryRow(
                    playersCombined,
                    e.getTimestamp().format(fmt),
                    e.getDifficulty(),
                    e.getFinalScore(),
                    e.getLivesLeft(),
                    e.getCorrectAnswers() + "/" + e.getTotalQuestions(),
                    e.getFormattedAccuracy(),
                    e.getFormattedDuration()
            ));
        }

        return rows;
    }
    public List<PlayerHistoryRow> getPlayersHistory(String difficultyFilter,
                                                    String resultFilter,
                                                    String searchTerm) {
        // Reuse the same filtered games list
        List<GameHistoryRow> games = getGameHistory(difficultyFilter, resultFilter, searchTerm);

        // We need to re-read entries to get numeric accuracy etc.

        String diff = (difficultyFilter == null) ? "All" : difficultyFilter;
        String res  = (resultFilter == null) ? "All" : resultFilter;
        String search = (searchTerm == null) ? "" : searchTerm.trim().toLowerCase();

        Map<String, PlayerStats> map = new HashMap<>();

        for (GameHistoryEntry e : GameHistoryManager.getInstance().getEntries()) {

            if (!"All".equalsIgnoreCase(diff) &&
                    !e.getDifficulty().equalsIgnoreCase(diff)) {
                continue;
            }

            if (!"All".equalsIgnoreCase(res) &&
                    !e.getResult().equalsIgnoreCase(res)) {
                continue;
            }

            String p1 = e.getPlayer1Name() == null ? "" : e.getPlayer1Name();
            String p2 = e.getPlayer2Name() == null ? "" : e.getPlayer2Name();

            if (!search.isEmpty()) {
                if (!p1.toLowerCase().contains(search) &&
                        !p2.toLowerCase().contains(search)) {
                    continue;
                }
            }

            double gameAcc = e.getAccuracy();

            updatePlayerStats(map, p1, e, gameAcc);
            updatePlayerStats(map, p2, e, gameAcc);
        }

        List<PlayerHistoryRow> rows = new ArrayList<>();
        for (PlayerStats ps : map.values()) {
            String avgAccStr = ps.totalGames > 0
                    ? String.format("%.0f%%", ps.totalAccuracy / ps.totalGames)
                    : "-";

            rows.add(new PlayerHistoryRow(
                    ps.name,
                    ps.totalGames,
                    ps.bestScore,
                    avgAccStr,
                    ps.getPreferredDifficulty()
            ));
        }

        return rows;
    }

    // ==== helper used only inside controller ====

    private void updatePlayerStats(Map<String, PlayerStats> map,
                                   String playerName,
                                   GameHistoryEntry e,
                                   double gameAccuracy) {
        if (playerName == null || playerName.isEmpty()) return;

        PlayerStats ps = map.computeIfAbsent(playerName, PlayerStats::new);
        ps.totalGames++;
        ps.bestScore = Math.max(ps.bestScore, e.getFinalScore());
        ps.totalAccuracy += gameAccuracy;
        ps.incrementDifficulty(e.getDifficulty());
    }

    private static class PlayerStats {
        final String name;
        int totalGames = 0;
        int bestScore = 0;
        double totalAccuracy = 0.0;
        final Map<String, Integer> difficultyCounts = new HashMap<>();

        PlayerStats(String name) {
            this.name = name;
        }

        void incrementDifficulty(String diff) {
            if (diff == null) return;
            difficultyCounts.merge(diff.toUpperCase(), 1, Integer::sum);
        }

        String getPreferredDifficulty() {
            String best = "-";
            int max = 0;
            for (Map.Entry<String, Integer> e : difficultyCounts.entrySet()) {
                if (e.getValue() > max) {
                    max = e.getValue();
                    best = e.getKey();
                }
            }
            return best;
        }


    }

    /**
     * Ends the current game session and clears the model state.
     * Called when player exits to Main Menu.
     */
    public void endGame() {
        currentGame = null;
    }





}


