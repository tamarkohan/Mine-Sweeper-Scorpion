package Model;

import java.util.Random;

/**
 * Represents a single board for one player.
 * Handles revealing, flagging, and tracking safe cells for victory.
 */
public class Board {

    private final int rows;
    private final int cols;
    private final int totalMines;
    private final int totalQuestionCells;
    private final int totalSurpriseCells;
    private final Cell[][] cells;
    private final Game game;

    // Counter to track how many safe cells are left to reveal
    private int safeCellsRemaining;

    public Board(Difficulty difficulty, Game game) {
        this.game = game;
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.totalMines = difficulty.getMines();
        this.totalQuestionCells = difficulty.getQuestionCells();
        this.totalSurpriseCells = difficulty.getSurpriseCells();
        this.cells = new Cell[rows][cols];

        // Calculate total cells that must be revealed to win:
        // Total Cells - Mines = Safe Cells
        this.safeCellsRemaining = (rows * cols) - totalMines;

        // Initialize cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }

        // Place logic
        placeMinesAndSpecialCells();
    }

    private void placeMinesAndSpecialCells() {
        placeContent(totalMines, Cell.CellContent.MINE);
        placeContent(totalQuestionCells, Cell.CellContent.QUESTION);
        placeContent(totalSurpriseCells, Cell.CellContent.SURPRISE);
        calculateNumbers();
    }

    private void placeContent(int count, Cell.CellContent type) {
        Random random = new Random();
        int placed = 0;
        while (placed < count) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (cells[r][c].getContent() == Cell.CellContent.EMPTY) {
                cells[r][c].setContent(type);
                placed++;
            }
        }
    }

    private void calculateNumbers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine()) continue;
                int mines = countNeighborMines(r, c);
                if (mines > 0 && cells[r][c].getContent() == Cell.CellContent.EMPTY) {
                    cells[r][c].setContent(Cell.CellContent.NUMBER);
                    cells[r][c].setAdjacentMines(mines);
                }
            }
        }
    }

    private int countNeighborMines(int r, int c) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nr = r + i;
                int nc = c + j;
                if (isValid(nr, nc) && cells[nr][nc].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Reveals a cell and checks for Game Over / Victory conditions.
     */
    public void revealCell(int r, int c) {
        if (!isValid(r, c)) return;
        Cell cell = cells[r][c];

        // Block action if cell is already processed OR game is not running
        if (cell.isRevealed() || cell.isFlagged() || game.getGameState() != GameState.RUNNING) return;

        cell.reveal();

        // 1. Scoring and Safe Cell Tracking
        if (!cell.isMine()) {
            safeCellsRemaining--;
            game.setSharedScore(game.getSharedScore() + 1);
        }

        // 2. Content Handling
        switch (cell.getContent()) {
            case MINE:
                game.setSharedLives(game.getSharedLives() - 1);
                break;

            case EMPTY:
                autoRevealEmptyCells(r, c);
                break;

            case QUESTION:
                break;

            case SURPRISE:
                break;

            case NUMBER:
                break;
        }

        // After every move, check if we Won or Lost
        game.checkGameStatus();
    }

    /**
     * מפעיל משבצת QUESTION / SURPRISE לפי בחירת השחקן.
     * מפעיל רק פעם אחת – אם USED כבר true לא עושה כלום.
     */
    public boolean activateSpecialCell(int r, int c) {
        if (!isValid(r, c)) return false;

        Cell cell = cells[r][c];

        // חייבת להיות כבר משבצת שנחשפה
        if (!cell.isRevealed()) {
            return false;
        }

        // רק על שאלות והפתעות
        if (cell.getContent() != Cell.CellContent.QUESTION &&
                cell.getContent() != Cell.CellContent.SURPRISE) {
            return false;
        }

        // אם כבר הופעל בעבר – לא מפעילים שוב
        if (cell.isUsed()) {
            return false;
        }

        // מסמנים כ-used ומפעילים לוגיקה במשחק
        cell.setUsed(true);
        game.activateSpecialCell(cell.getContent(), cell.getQuestionId());
        return true;
    }





    private void autoRevealEmptyCells ( int r, int c){
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nr = r + i;
                int nc = c + j;
                if (isValid(nr, nc)) {
                    Cell neighbor = cells[nr][nc];
                    if (!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.isMine()) {
                        // Recursion flows back through revealCell to update state, score, and safeCellsRemaining
                        revealCell(nr, nc);
                    }
                }
            }
        }
    }

    /**
     * Toggles flag state and applies points based on rule.
     * SRS 3.1.1: Mine +1pt, Non-Mine -3pts.
     */
    public void toggleFlag(int r, int c) {
        if (!isValid(r, c) || game.getGameState() != GameState.RUNNING) return;

        Cell cell = cells[r][c];
        cell.toggleFlag();  // רק משנה סטייט

        // ניקוד רק אם עכשיו התא ב־FLAGGED
        if (cell.getState() == Cell.CellState.FLAGGED) {
            if (cell.isMine()) {
                game.setSharedScore(game.getSharedScore() + game.getDifficulty().getMineFlagReward());
            } else {
                game.setSharedScore(game.getSharedScore() + game.getDifficulty().getNonMineFlagPenalty());
            }
        }
    }


    /**
     * Implements "Reveal Mine" reward (Easy/Medium, Correct).
     * Reveals a single, random, unrevealed, unflagged mine.
     */
    public void revealRandomMine() {
        Random rand = new Random();
        int attempts = rows * cols * 2; // Limit attempts to prevent infinite loop

        while (attempts > 0) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            Cell cell = cells[r][c];

            if (cell.isMine() && !cell.isRevealed() && !cell.isFlagged()) {
                cell.reveal();
                System.out.println("Reward: A mine at (" + r + "," + c + ") was safely revealed.");
                // Note: The rule states "חשיפת משבצת מוקש" (Mine cell reveal) as the reward.
                // The footnote states no score for automatic mine reveal.
                return;
            }
            attempts--;
        }
        System.out.println("Reward: Could not find an unrevealed mine to show.");
    }

    /**
     * Implements "Show 3x3 random cells" reward (Easy/Hard, Correct).
     */
    public void revealRandom3x3Area() {
        Random rand = new Random();
        int r = rand.nextInt(rows - 2); // Ensure 3x3 area fits
        int c = rand.nextInt(cols - 2);

        System.out.println("Reward: Revealing 3x3 area starting at (" + r + "," + c + ")");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // Use revealCell to correctly update state, score, and safeCellsRemaining,
                // and handle any mines found in the area.
                revealCell(r + i, c + j);
            }
        }
    }

    private boolean isValid ( int r, int c){
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
    public boolean isSolved() {
        // A board is solved when all non-mine cells have been revealed.
        // This state is tracked by the safeCellsRemaining counter.
        return safeCellsRemaining == 0;
    }
    public void revealAll() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                // Only set to REVEALED if it hasn't been revealed yet.
                if (cell.getState() != Cell.CellState.REVEALED) {
                    cell.setState(Cell.CellState.REVEALED);
                    // Note: We don't call revealCell() here to avoid triggering score/life changes
                    // or recursive reveals during the final state update.
                }
            }
        }
    }
    // --- Getters ---

    public int getSafeCellsRemaining () {
        return safeCellsRemaining;
    }

    public int getRows () {
        return rows;
    }
    public int getCols () {
        return cols;
    }
    public int getTotalMines () {
        return totalMines;
    }
    public int getTotalQuestionCells () {
        return totalQuestionCells;
    }
    public int getTotalSurpriseCells () {
        return totalSurpriseCells;
    }
    public Cell[][] getCells () {
        return cells;
    }
    public Cell getCell ( int row, int col){
        if (isValid(row, col)) return cells[row][col];
        return null;
    }
}