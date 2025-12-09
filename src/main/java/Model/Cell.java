package Model;
/**
 * Represents a single cell on the Minesweeper board.
 * Stores position, content type, current state, and metadata for special cell logic.
 */
public class Cell {
    // Cell content types
    public enum CellContent { EMPTY, MINE, QUESTION, SURPRISE, NUMBER }
    // Cell visibility states
    public enum CellState { HIDDEN, REVEALED, FLAGGED }

    private final int row;
    private final int col;
    private CellContent content;
    private CellState state;
    private int adjacentMines;
    private boolean used;
    private Integer questionId;
    /**
     * Creates a new EMPTY, HIDDEN cell at the specified board coordinates.
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.content = CellContent.EMPTY;
        this.state = CellState.HIDDEN;
        this.adjacentMines = 0;
        this.used = false;
        this.questionId = null;
    }

    // --- Basic getters/setters used by Board and Game ---

    public CellContent getContent() {
        return content;
    }

    public void setContent(CellContent content) {
        this.content = content;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    // --- HELPER METHODS FOR LOGIC  ---

    public boolean isRevealed() {
        return state == CellState.REVEALED;
    }

    public boolean isFlagged() {
        return state == CellState.FLAGGED;
    }

    public boolean isQuestionOrSurprise() {
        return content == CellContent.QUESTION || content == CellContent.SURPRISE;
    }

    // --- Game actions ---

    /**
     * Marks the cell as REVEALED.
     */
    public void reveal() {
        this.state = CellState.REVEALED;
    }

    // -------- Game Logic Helpers --------

    /**
     * Returns true if the cell contains a mine.
     */
    public boolean isMine() {
        return this.content == CellContent.MINE;
    }

    /**
     * Toggles the visible state between HIDDEN and FLAGGED.
     * A cell must be HIDDEN to be flagged or unflagged.
     * @return true if the state was successfully changed, false otherwise.
     */
    public boolean toggleFlag() {
        // Cannot flag or unflag a revealed cell
        if (this.state == CellState.REVEALED) {
            return false;
        }

        if (this.state == CellState.HIDDEN) {
            // Change from HIDDEN to FLAGGED
            this.state = CellState.FLAGGED;
            return true;
        } else if (this.state == CellState.FLAGGED) {
            // Change from FLAGGED back to HIDDEN
            this.state = CellState.HIDDEN;
            return true;
        }
        // Should not happen, but return false defensively
        return false;
    }

    // --- getters and setters for controller logic ---

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }
    public int getAdjacentMines() {
        return adjacentMines;
    }


}