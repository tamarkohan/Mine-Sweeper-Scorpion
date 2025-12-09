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
    /**
     * Initializes a board according to the given difficulty and parent Game.
     * Places mines, question cells, surprise cells, and computes number cells.
     */
    public Board(Difficulty difficulty, Game game) {
        this.game = game;
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.totalMines = difficulty.getMines();
        this.totalQuestionCells = difficulty.getQuestionCells();
        this.totalSurpriseCells = difficulty.getSurpriseCells();
        this.cells = new Cell[rows][cols];

        // Total safe cells = all cells minus mines
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
    /**
     * Places mines, question cells and surprise cells, then calculates number cells.
     */
    private void placeMinesAndSpecialCells() {
        placeContent(totalMines, Cell.CellContent.MINE);
        placeContent(totalQuestionCells, Cell.CellContent.QUESTION);
        placeContent(totalSurpriseCells, Cell.CellContent.SURPRISE);
        calculateNumbers();
    }
    /**
     * Randomly assigns a given content type to EMPTY cells until count is reached.
     */
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
    /**
     * Converts suitable EMPTY cells to NUMBER cells based on adjacent mines.
     */
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
    /**
     * Counts how many neighboring cells (8-directional) contain mines.
     */
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
     * Reveals a cell, applies scoring and lives logic, and checks game status.
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
     * Activates a QUESTION or SURPRISE cell once, if it was revealed and not used.
     *
     * @return true if activation was successful, false otherwise
     */
    public boolean activateSpecialCell(int r, int c) {
        if (!isValid(r, c)) return false;

        Cell cell = cells[r][c];

        // Must be already revealed
        if (!cell.isRevealed()) {
            return false;
        }

        // Only for QUESTION or SURPRISE cells
        if (cell.getContent() != Cell.CellContent.QUESTION &&
                cell.getContent() != Cell.CellContent.SURPRISE) {
            return false;
        }

        // Can be activated only once
        if (cell.isUsed()) {
            return false;
        }

        cell.setUsed(true);
        game.activateSpecialCell(cell.getContent(), cell.getQuestionId());
        return true;
    }




    /**
     * Recursively reveals neighbors around an EMPTY cell (flood-fill behavior).
     */
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
     * This method must be VOID or return only if the action was successful for the Controller's logic to work.
     */
    public void toggleFlag(int r, int c) { // 🔥 Changed to VOID
        if (!isValid(r, c) || cells[r][c].isRevealed() || game.getGameState() != GameState.RUNNING) return;

        Cell cell = cells[r][c];

        // Determine the state *before* toggling
        boolean wasFlagged = cell.isFlagged();

        // Toggle the flag (returns true if the state changed successfully)
        boolean stateChanged = cell.toggleFlag();

        if (stateChanged) {
            if (!wasFlagged) {
                // Case 1: Flag was SET (HIDDEN -> FLAGGED) - This ends the turn.
                if (cell.isMine()) {
                    game.setSharedScore(game.getSharedScore() + game.getDifficulty().getMineFlagReward());
                } else {
                    game.setSharedScore(game.getSharedScore() + game.getDifficulty().getNonMineFlagPenalty());
                }
                // No return true/false here, as the Controller handles the turn switch logic using isFlagged() checks.

            } else {
                // Case 2: Flag was UNSET (FLAGGED -> HIDDEN) - Correction move.
                // No score reversal is implemented here, just the state change.
            }
        }
        // After every move, check if we Won or Lost
        game.checkGameStatus();
    }


    /**
     * Reveals a single random mine cell without affecting score (reward type).
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
     * Reveals a random 3x3 area using the standard reveal logic.
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
    /**
     * Returns true if (r,c) is inside the board boundaries.
     */
    private boolean isValid ( int r, int c){
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
    /**
     * Returns true when all non-mine cells have been revealed.
     */
    public boolean isSolved() {
        // A board is solved when all non-mine cells have been revealed.
        // This state is tracked by the safeCellsRemaining counter.
        return safeCellsRemaining == 0;
    }
    /**
     * Reveals all cells without changing score or lives (used at game end).
     */
    public void revealAll() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                // Only set to REVEALED if it hasn't been revealed yet.
                if (cell.getState() != Cell.CellState.REVEALED) {
                    cell.setState(Cell.CellState.REVEALED);
                    // Intentionally avoids calling revealCell() to skip side effects.
                }
            }
        }
    }

    /**
     * 🔥 NEW: Helper method for the Controller to check flag status (Fixes error #2 in Controller).
     */
    public boolean isFlagged(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return false;
        // Assuming 'cells' is the array of Cell objects
        return cells[r][c].isFlagged();
    }

    /**
     * Checks if all mines on the board have been found (revealed or flagged).
     * @return true if all mines are found, false otherwise.
     */
    public boolean areAllMinesFound() {
        int foundMines = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (cell.isMine() && (cell.isRevealed() || cell.isFlagged())) {
                    foundMines++;
                }
            }
        }
        return foundMines == totalMines;
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