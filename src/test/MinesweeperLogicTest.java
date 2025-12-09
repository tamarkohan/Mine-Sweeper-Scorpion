import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;

/**
 * Manual console test suite for the Board.revealCell recursion logic.
 * Covers:
 * - Cascading reveal of EMPTY cells and boundary NUMBER cells
 * - Behavior when revealing NUMBER, MINE and FLAGGED cells
 * - Recursion boundary conditions with flagged and pre-revealed cells.
 */
public class MinesweeperLogicTest {
    // Size of the printed test area (top-left 5x5 part of the board)
    private static final int TEST_SIZE = 5;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("MINESWEEPER LOGIC MANUAL TESTS");
        System.out.println("==========================================\n");

        // Test 1: Reveal EMPTY cell with 0 adjacent mines (recursive reveal)
        test1_RecursiveRevealEmptyChain();

        // Test 2: Reveal numbered cell (no recursion)
        test2_RevealNumberedCell();

        // Test 3: Reveal mine (only that cell)
        test3_RevealMine();

        // Test 4: Reveal flagged cell (should not reveal)
        test4_RevealFlaggedCell();

        // Test 5: Verify recursion behavior (boundary conditions)
        test5_RecursionBoundaryConditions();

        System.out.println("\n==========================================");
        System.out.println("ALL TESTS COMPLETED");
        System.out.println("==========================================");
    }

    /**
     * TEST 1: revealCell on an EMPTY cell with 0 adjacent mines
     * Must reveal full recursive empty-chain and boundary numbers.
     */
    private static void test1_RecursiveRevealEmptyChain() {
        System.out.println("=== TEST 1: Recursive Reveal Empty Chain ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureTestBoard1(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        System.out.println("\nRevealing cell (0,0) - EMPTY with 0 adjacent mines...");
        board.revealCell(0, 0);

        System.out.println("\nBoard State After Reveal:");
        printBoardState(board);

        boolean allPassed = true;

        // All EMPTY cells in the chain should be revealed
        allPassed &= check("Cell (0,0) should be revealed",
                board.getCell(0, 0).isRevealed());
        allPassed &= check("Cell (0,1) should be revealed",
                board.getCell(0, 1).isRevealed());
        allPassed &= check("Cell (0,2) should be revealed",
                board.getCell(0, 2).isRevealed());
        allPassed &= check("Cell (1,0) should be revealed",
                board.getCell(1, 0).isRevealed());
        allPassed &= check("Cell (1,1) should be revealed",
                board.getCell(1, 1).isRevealed());
        allPassed &= check("Cell (1,2) should be revealed",
                board.getCell(1, 2).isRevealed());
        allPassed &= check("Cell (2,0) should be revealed",
                board.getCell(2, 0).isRevealed());
        allPassed &= check("Cell (2,1) should be revealed",
                board.getCell(2, 1).isRevealed());
        allPassed &= check("Cell (2,2) should be revealed",
                board.getCell(2, 2).isRevealed());
        allPassed &= check("Cell (2,3) should be revealed",
                board.getCell(2, 3).isRevealed());
        allPassed &= check("Cell (2,4) should be revealed",
                board.getCell(2, 4).isRevealed());
        allPassed &= check("Cell (3,2) should be revealed",
                board.getCell(3, 2).isRevealed());
        allPassed &= check("Cell (3,3) should be revealed",
                board.getCell(3, 3).isRevealed());
        allPassed &= check("Cell (3,4) should be revealed",
                board.getCell(3, 4).isRevealed());
        allPassed &= check("Cell (4,2) should be revealed",
                board.getCell(4, 2).isRevealed());
        allPassed &= check("Cell (4,3) should be revealed",
                board.getCell(4, 3).isRevealed());
        allPassed &= check("Cell (4,4) should be revealed",
                board.getCell(4, 4).isRevealed());

        // Boundary numbered cells should be revealed
        allPassed &= check("Boundary cell (0,3) NUMBER should be revealed",
                board.getCell(0, 3).isRevealed());
        allPassed &= check("Boundary cell (1,3) NUMBER should be revealed",
                board.getCell(1, 3).isRevealed());
        allPassed &= check("Boundary cell (1,4) NUMBER should be revealed",
                board.getCell(1, 4).isRevealed());
        allPassed &= check("Boundary cell (3,0) NUMBER should be revealed",
                board.getCell(3, 0).isRevealed());
        allPassed &= check("Boundary cell (3,1) NUMBER should be revealed",
                board.getCell(3, 1).isRevealed());
        allPassed &= check("Boundary cell (4,1) NUMBER should be revealed",
                board.getCell(4, 1).isRevealed());

        // Mines should NOT be revealed
        allPassed &= check("Mine (0,4) should NOT be revealed",
                !board.getCell(0, 4).isRevealed());
        allPassed &= check("Mine (4,0) should NOT be revealed",
                !board.getCell(4, 0).isRevealed());

        printTestResult("TEST 1", allPassed);
        System.out.println();
    }

    /**
     * TEST 2: revealCell on a numbered cell (adjacent mines > 0)
     * Only that cell should be revealed (no recursion)
     */
    private static void test2_RevealNumberedCell() {
        System.out.println("=== TEST 2: Reveal Numbered Cell (No Recursion) ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureTestBoard2(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        System.out.println("\nRevealing cell (2,2) - NUMBER...");
        board.revealCell(2, 2);

        System.out.println("\nBoard State After Reveal:");
        printBoardState(board);

        boolean allPassed = true;

        allPassed &= check("Numbered cell (2,2) should be revealed",
                board.getCell(2, 2).isRevealed());

        allPassed &= check("Adjacent EMPTY cell (1,2) should NOT be revealed",
                !board.getCell(1, 2).isRevealed());
        allPassed &= check("Adjacent EMPTY cell (2,1) should NOT be revealed",
                !board.getCell(2, 1).isRevealed());
        allPassed &= check("Adjacent EMPTY cell (2,3) should NOT be revealed",
                !board.getCell(2, 3).isRevealed());
        allPassed &= check("Adjacent EMPTY cell (3,2) should NOT be revealed",
                !board.getCell(3, 2).isRevealed());

        printTestResult("TEST 2", allPassed);
        System.out.println();
    }

    /**
     * TEST 3: revealCell on a Mine
     * Only that cell is revealed, no neighbors are affected.
     */
    private static void test3_RevealMine() {
        System.out.println("=== TEST 3: Reveal Mine (No Neighbor Effect) ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureTestBoard3(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        int livesBefore = game.getSharedLives();

        System.out.println("\nRevealing cell (2,2) - MINE...");
        board.revealCell(2, 2);

        System.out.println("\nBoard State After Reveal:");
        printBoardState(board);

        boolean allPassed = true;

        allPassed &= check("Mine cell (2,2) should be revealed",
                board.getCell(2, 2).isRevealed());

        allPassed &= check("Lives should decrease by 1",
                game.getSharedLives() == livesBefore - 1);

        allPassed &= check("Adjacent EMPTY cell (1,2) should NOT be revealed",
                !board.getCell(1, 2).isRevealed());
        allPassed &= check("Adjacent EMPTY cell (2,1) should NOT be revealed",
                !board.getCell(2, 1).isRevealed());
        allPassed &= check("Adjacent EMPTY cell (2,3) should NOT be revealed",
                !board.getCell(2, 3).isRevealed());
        allPassed &= check("Adjacent EMPTY cell (3,2) should NOT be revealed",
                !board.getCell(3, 2).isRevealed());

        printTestResult("TEST 3", allPassed);
        System.out.println();
    }

    /**
     * TEST 4: revealCell on a flagged cell
     * Reveal must NOT happen.
     */
    private static void test4_RevealFlaggedCell() {
        System.out.println("=== TEST 4: Reveal Flagged Cell (Should Block) ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureTestBoard4(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        board.toggleFlag(2, 2);
        System.out.println("\nFlagged cell (2,2)...");

        System.out.println("\nBoard State After Flagging:");
        printBoardState(board);

        System.out.println("\nAttempting to reveal flagged cell (2,2)...");
        board.revealCell(2, 2);

        System.out.println("\nBoard State After Reveal Attempt:");
        printBoardState(board);

        boolean allPassed = true;

        allPassed &= check("Flagged cell (2,2) should still be flagged",
                board.getCell(2, 2).isFlagged());
        allPassed &= check("Flagged cell (2,2) should NOT be revealed",
                !board.getCell(2, 2).isRevealed());

        printTestResult("TEST 4", allPassed);
        System.out.println();
    }

    /**
     * TEST 5: recursion boundary conditions with flagged & pre-revealed cells.
     */
    private static void test5_RecursionBoundaryConditions() {
        System.out.println("=== TEST 5: Recursion Boundary Conditions ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureTestBoard5(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        // Pre-flag one cell and pre-reveal another
        board.toggleFlag(1, 1);
        board.getCell(3, 3).reveal();

        System.out.println("\nBoard State After Pre-flagging (1,1) and Pre-revealing (3,3):");
        printBoardState(board);

        System.out.println("\nRevealing cell (0,0) - EMPTY with 0 adjacent mines...");
        board.revealCell(0, 0);

        System.out.println("\nBoard State After Reveal:");
        printBoardState(board);

        boolean allPassed = true;

        allPassed &= check("Empty cell (0,0) should be revealed",
                board.getCell(0, 0).isRevealed());
        allPassed &= check("Empty cell (0,1) should be revealed",
                board.getCell(0, 1).isRevealed());
        allPassed &= check("Empty cell (1,0) should be revealed",
                board.getCell(1, 0).isRevealed());
        allPassed &= check("Empty cell (2,0) should be revealed",
                board.getCell(2, 0).isRevealed());
        allPassed &= check("Empty cell (2,1) should be revealed",
                board.getCell(2, 1).isRevealed());

        allPassed &= check("Flagged cell (1,1) should remain flagged",
                board.getCell(1, 1).isFlagged());
        allPassed &= check("Flagged cell (1,1) should NOT be revealed",
                !board.getCell(1, 1).isRevealed());

        allPassed &= check("Previously revealed cell (3,3) should remain revealed",
                board.getCell(3, 3).isRevealed());

        allPassed &= check("Numbered boundary cell (0,2) should be revealed",
                board.getCell(0, 2).isRevealed());
        allPassed &= check("Numbered boundary cell (1,2) should be revealed",
                board.getCell(1, 2).isRevealed());

        allPassed &= check("Cell beyond numbered boundary (2,2) should NOT be revealed",
                !board.getCell(2, 2).isRevealed());

        printTestResult("TEST 5", allPassed);
        System.out.println();
    }

    // ============================================================
    // Helper Methods for Board Configuration (size-safe)
    // ============================================================

    private static void clearBoard(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell != null) {
                    cell.setContent(Cell.CellContent.EMPTY);
                    cell.setAdjacentMines(0);
                    cell.setState(Cell.CellState.HIDDEN);
                }
            }
        }
    }

    private static void configureTestBoard1(Board board) {
        // E E E 1 M
        // E E E 1 1
        // E E E E E
        // 1 1 E E E
        // M 1 E E E
        if (board.getRows() < 5 || board.getCols() < 5) {
            throw new IllegalStateException("Board must be at least 5x5 for this test");
        }

        board.getCell(0, 4).setContent(Cell.CellContent.MINE);
        board.getCell(4, 0).setContent(Cell.CellContent.MINE);

        calculateAdjacentMines(board, 0, 3);
        calculateAdjacentMines(board, 1, 3);
        calculateAdjacentMines(board, 1, 4);
        calculateAdjacentMines(board, 3, 0);
        calculateAdjacentMines(board, 3, 1);
        calculateAdjacentMines(board, 4, 1);
    }

    private static void configureTestBoard2(Board board) {
        if (board.getRows() < 5 || board.getCols() < 5) {
            throw new IllegalStateException("Board must be at least 5x5 for this test");
        }

        board.getCell(1, 1).setContent(Cell.CellContent.MINE);
        board.getCell(1, 2).setContent(Cell.CellContent.MINE);
        board.getCell(1, 3).setContent(Cell.CellContent.MINE);
        board.getCell(2, 1).setContent(Cell.CellContent.MINE);
        board.getCell(2, 3).setContent(Cell.CellContent.MINE);
        board.getCell(3, 1).setContent(Cell.CellContent.MINE);
        board.getCell(3, 2).setContent(Cell.CellContent.MINE);
        board.getCell(3, 3).setContent(Cell.CellContent.MINE);

        calculateAdjacentMines(board, 2, 2);
    }

    private static void configureTestBoard3(Board board) {
        if (board.getRows() < 5 || board.getCols() < 5) {
            throw new IllegalStateException("Board must be at least 5x5 for this test");
        }
        board.getCell(2, 2).setContent(Cell.CellContent.MINE);
    }

    private static void configureTestBoard4(Board board) {
        // All cells remain EMPTY
    }

    private static void configureTestBoard5(Board board) {
        if (board.getRows() < 5 || board.getCols() < 5) {
            throw new IllegalStateException("Board must be at least 5x5 for this test");
        }

        board.getCell(0, 3).setContent(Cell.CellContent.MINE);
        board.getCell(1, 3).setContent(Cell.CellContent.MINE);
        board.getCell(2, 3).setContent(Cell.CellContent.MINE);

        calculateAdjacentMines(board, 0, 2);
        calculateAdjacentMines(board, 1, 2);
        calculateAdjacentMines(board, 2, 2);
    }
    /**
     * Calculates NUMBER value for a specific cell based on surrounding mines.
     */
    private static void calculateAdjacentMines(Board board, int r, int c) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nr = r + i;
                int nc = c + j;
                if (nr >= 0 && nr < board.getRows() && nc >= 0 && nc < board.getCols()) {
                    if (board.getCell(nr, nc).isMine()) {
                        count++;
                    }
                }
            }
        }
        if (count > 0) {
            board.getCell(r, c).setContent(Cell.CellContent.NUMBER);
            board.getCell(r, c).setAdjacentMines(count);
        }
    }

    // ============================================================
    // Helper Methods for Display and Testing
    // ============================================================
    /**
     * Prints the top-left TEST_SIZE x TEST_SIZE area of the board (state + content).
     */
    private static void printBoardState(Board board) {
        int rows = Math.min(TEST_SIZE, board.getRows());
        int cols = Math.min(TEST_SIZE, board.getCols());

        System.out.println("\n   ");
        for (int c = 0; c < cols; c++) {
            System.out.print("  " + c);
        }
        System.out.println();

        for (int r = 0; r < rows; r++) {
            System.out.print(r + "  ");
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                String state;
                if (cell.isRevealed()) {
                    state = "R";
                } else if (cell.isFlagged()) {
                    state = "F";
                } else {
                    state = "H";
                }

                String content;
                switch (cell.getContent()) {
                    case MINE:
                        content = "M";
                        break;
                    case NUMBER:
                        content = String.valueOf(cell.getAdjacentMines());
                        break;
                    case EMPTY:
                        content = "E";
                        break;
                    case QUESTION:
                        content = "Q";
                        break;
                    case SURPRISE:
                        content = "S";
                        break;
                    default:
                        content = "?";
                }

                System.out.print(state + content + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    /**
     * Prints [PASS]/[FAIL] and returns the condition (for accumulating results).
     */
    private static boolean check(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
        } else {
            System.out.println("[FAIL] " + description);
        }
        return condition;
    }
    /**
     * Prints a short summary line for each test block.
     */
    private static void printTestResult(String testName, boolean allPassed) {
        System.out.println("\n" + testName + " RESULT: "
                + (allPassed ? "ALL CHECKS PASSED" : "SOME CHECKS FAILED"));
    }
}
