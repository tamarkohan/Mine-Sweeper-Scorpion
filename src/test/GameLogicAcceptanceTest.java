import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;
import Model.GameState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Acceptance test for Minesweeper game logic.
 * Tests are based on SRS requirements:
 * - Revealing safe cells (EMPTY/NUMBER/QUESTION/SURPRISE) adds +1 point
 * - Revealing mines decreases lives by 1, no score change
 * - Flagging mines adds +1 point
 * - Flagging safe cells subtracts 3 points
 * - Question/Surprise cells behave as empty on reveal (+1), activation is separate
 */
public class GameLogicAcceptanceTest {

    private static final int TEST_SIZE = 5;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("GAME LOGIC ACCEPTANCE TEST");
        System.out.println("==========================================\n");

        // A) Start Phase
        testA_StartPhase();

        // B) Reveal Phase (recursive reveal)
        testB_RevealPhase();

        // C) Scoring & Flags
        testC_ScoringAndFlags();

        // D) Lose Condition
        testD_LoseCondition();

        // E) Win Condition
        testE_WinCondition();

        // F) Used Cells
        testF_UsedCells();

        System.out.println("\n==========================================");
        System.out.println("ALL ACCEPTANCE TESTS COMPLETED");
        System.out.println("==========================================");
    }

    // ============================================================
    // A) START PHASE
    // ============================================================

    /**
     * A) Start Phase
     * SRS Coverage: Game initialization, difficulty settings, board creation
     */
    private static void testA_StartPhase() {
        System.out.println("=== START TEST ===");

        GameController controller = new GameController();
        controller.startNewGame(Difficulty.EASY);
        Game game = controller.getCurrentGame();

        boolean allPassed = true;

        allPassed &= check("Game should not be null", game != null);
        assert game != null;

        allPassed &= check("Difficulty should be EASY",
                game.getDifficulty() == Difficulty.EASY);

        Board b1 = game.getBoard1();
        Board b2 = game.getBoard2();

        allPassed &= check("Board1 should not be null", b1 != null);
        allPassed &= check("Board2 should not be null", b2 != null);

        assert b1 != null;
        assert b2 != null;

        allPassed &= check("Board1 rows == EASY rows",
                b1.getRows() == Difficulty.EASY.getRows());
        allPassed &= check("Board1 cols == EASY cols",
                b1.getCols() == Difficulty.EASY.getCols());

        allPassed &= check("Shared lives == EASY startingLives",
                game.getSharedLives() == Difficulty.EASY.getStartingLives());
        allPassed &= check("Shared score starts at 0",
                game.getSharedScore() == 0);

        allPassed &= check("Game state should be RUNNING",
                game.getGameState() == GameState.RUNNING);

        printTestResult("START TEST", allPassed);
        System.out.println();
    }

    // ============================================================
    // B) REVEAL PHASE (RECURSIVE REVEAL)
    // ============================================================

    /**
     * B) Reveal Phase (recursive reveal)
     * SRS Coverage:
     * - Revealing EMPTY cells adds +1 point and triggers cascade
     * - Revealing NUMBER cells adds +1 point
     * - Revealing safe cells does not change lives
     * - Mines are not revealed during cascade
     */
    private static void testB_RevealPhase() {
        System.out.println("=== REVEAL LOGIC ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        // Clear entire board first (not just 5x5)
        clearBoard(board);

        // Configure deterministic 5x5 board
        // Layout:
        // E E E 1 M
        // E E E 1 1
        // E E E E E
        // 1 1 E E E
        // M 1 E E E
        configureRevealTestBoard(board);

        System.out.println("\nInitial Board State (5x5 test area):");
        printBoardState(board);

        int scoreBefore = game.getSharedScore();
        int livesBefore = game.getSharedLives();
        int safeCellsBefore = board.getSafeCellsRemaining();

        // Reveal cell (0,0) - should trigger recursive reveal
        System.out.println("\nRevealing cell (0,0) - EMPTY with 0 adjacent mines...");
        board.revealCell(0, 0);

        System.out.println("\nBoard State After Reveal (5x5 test area):");
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

        // Boundary numbered cells should be revealed
        allPassed &= check("Boundary cell (0,3) NUMBER should be revealed",
                board.getCell(0, 3).isRevealed());
        allPassed &= check("Boundary cell (1,3) NUMBER should be revealed",
                board.getCell(1, 3).isRevealed());
        allPassed &= check("Boundary cell (1,4) NUMBER should be revealed",
                board.getCell(1, 4).isRevealed());

        // Mines should NOT be revealed
        allPassed &= check("Mine (0,4) should NOT be revealed",
                !board.getCell(0, 4).isRevealed());
        allPassed &= check("Mine (4,0) should NOT be revealed",
                !board.getCell(4, 0).isRevealed());

        // Safe cells remaining should decrease
        allPassed &= check("Safe cells remaining should decrease",
                board.getSafeCellsRemaining() < safeCellsBefore);

        // SRS: Revealing safe cells adds +1 point each
        // Count actual revealed safe cells in test area
        int actualRevealedInTestArea = 0;
        for (int r = 0; r < TEST_SIZE; r++) {
            for (int c = 0; c < TEST_SIZE; c++) {
                Cell cell = board.getCell(r, c);
                if (cell != null && cell.isRevealed() && !cell.isMine()) {
                    actualRevealedInTestArea++;
                }
            }
        }


        int actualScoreIncrease = game.getSharedScore() - scoreBefore;

        allPassed &= check(
                "Score should increase by AT LEAST the number of revealed safe cells in the 5x5 test area " +
                        "(expected >= " + actualRevealedInTestArea + ", actual: " + actualScoreIncrease + ")",
                actualScoreIncrease >= actualRevealedInTestArea
        );

        // Lives should NOT change
        allPassed &= check("Lives should not change when revealing safe cells",
                game.getSharedLives() == livesBefore);

        printTestResult("REVEAL LOGIC", allPassed);
        System.out.println();
    }

    // ============================================================
    // C) SCORING & FLAGS
    // ============================================================

    /**
     * C) Scoring & Flags
     * SRS Coverage:
     * - Flagging mine: +1 point, lives unchanged
     * - Flagging safe cell: -3 points, lives unchanged
     * - Revealing QUESTION cell: +1 point (like empty) + הפעלת שאלה שגוזלת נקודות
     */
    private static void testC_ScoringAndFlags() {
        System.out.println("=== SCORING TEST ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureScoringTestBoard(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        boolean allPassed = true;

        // Test 1: Flagging a mine
        int scoreBeforeMineFlag = game.getSharedScore();
        int livesBeforeMineFlag = game.getSharedLives();

        System.out.println("\n1. Flagging mine at (0,0)...");
        board.toggleFlag(0, 0);

        // SRS: Flagging mine adds +1 point
        allPassed &= check("Score should increase by exactly +1 after flagging a mine (expected: " +
                        (scoreBeforeMineFlag + 1) + ", actual: " + game.getSharedScore() + ")",
                game.getSharedScore() == scoreBeforeMineFlag + 1);
        allPassed &= check("Lives should not change when flagging",
                game.getSharedLives() == livesBeforeMineFlag);

        // Test 2: Flagging a safe cell (EMPTY)
        int scoreBeforeSafeFlag = game.getSharedScore();
        int livesBeforeSafeFlag = game.getSharedLives();

        System.out.println("\n2. Flagging safe cell at (2,2)...");
        board.toggleFlag(2, 2);

        // SRS: Flagging safe cell subtracts -3 points
        allPassed &= check("Score should decrease by exactly -3 after flagging a safe cell (expected: " +
                        (scoreBeforeSafeFlag - 3) + ", actual: " + game.getSharedScore() + ")",
                game.getSharedScore() == scoreBeforeSafeFlag - 3);
        allPassed &= check("Lives should not change when flagging safe cell",
                game.getSharedLives() == livesBeforeSafeFlag);

        // Test 3: Revealing a QUESTION cell (כולל עלות הפעלה)
        board.toggleFlag(2, 2); // Unflag safe cell

        game.setSharedScore(20); // Set score high enough
        int scoreBeforeQuestion = game.getSharedScore();
        int livesBeforeQuestion = game.getSharedLives();
        int activationCost = game.getDifficulty().getActivationCost();

        System.out.println("\n3. Revealing question cell at (1,1)...");
        board.revealCell(1, 1);

        int expectedQuestionScore = scoreBeforeQuestion + 1 - activationCost; // +1 safe, -activationCost
        allPassed &= check(
                "Score after QUESTION = previous +1 (safe) - activationCost(" + activationCost + ")" +
                        " (expected: " + expectedQuestionScore + ", actual: " + game.getSharedScore() + ")",
                game.getSharedScore() == expectedQuestionScore
        );
        allPassed &= check("Lives should not change when revealing question cell",
                game.getSharedLives() == livesBeforeQuestion);

        Cell questionCell = board.getCell(1, 1);
        allPassed &= check("Question cell should be revealed",
                questionCell.isRevealed());

        printTestResult("SCORING TEST", allPassed);
        System.out.println();
    }

    // ============================================================
    // D) LOSE CONDITION
    // ============================================================

    /**
     * D) Lose Condition
     * SRS Coverage:
     * - Revealing mine: decreases lives by 1, no score change
     * - When lives reach 0: GameState.LOST
     */
    private static void testD_LoseCondition() {
        System.out.println("=== LOSE CONDITION ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureLoseTestBoard(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        boolean allPassed = true;

        // Set lives to 1 so we can test losing
        game.setSharedLives(1);
        int scoreBefore = game.getSharedScore();
        int livesBefore = game.getSharedLives();

        System.out.println("\nRevealing mine at (2,2) - should lose game...");
        board.revealCell(2, 2);

        // SRS: Revealing mine decreases lives by 1, no score change
        allPassed &= check("Lives should decrease by 1",
                game.getSharedLives() == livesBefore - 1);
        allPassed &= check("Score should NOT change when revealing a mine (expected: " +
                        scoreBefore + ", actual: " + game.getSharedScore() + ")",
                game.getSharedScore() == scoreBefore);

        // Game state should be LOST
        allPassed &= check("Game state should be LOST",
                game.getGameState() == GameState.LOST);

        System.out.println("\nBoard State After Game Over:");
        printBoardState(board);

        // Check if mine is revealed
        allPassed &= check("Mine cell should be revealed after game over",
                board.getCell(2, 2).isRevealed());

        printTestResult("LOSE CONDITION", allPassed);
        System.out.println();
    }

    // ============================================================
    // E) WIN CONDITION
    // ============================================================

    /**
     * E) Win Condition
     * SRS Coverage: Win requires all safe cells revealed on both boards.
     * כאן אנחנו בודקים רק לוח אחד + לוגיקת ניקוד.
     */
    private static void testE_WinCondition() {
        System.out.println("=== WIN CONDITION ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureWinTestBoard(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        int initialSafeCells = board.getSafeCellsRemaining();
        int initialScore = game.getSharedScore();
        System.out.println("Initial safe cells remaining: " + initialSafeCells);
        System.out.println("Initial score: " + initialScore);

        boolean allPassed = true;

        // Reveal all safe cells on the entire board
        System.out.println("\nRevealing all safe cells on the entire board...");
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell != null && !cell.isMine() && !cell.isRevealed()) {
                    board.revealCell(r, c);
                }
            }
        }

        System.out.println("\nBoard State After Revealing All Safe Cells:");
        printBoardState(board);

        int finalSafeCells = board.getSafeCellsRemaining();
        int finalScore = game.getSharedScore();
        System.out.println("Final safe cells remaining: " + finalSafeCells);
        System.out.println("Final score: " + finalScore);

        // בודקים שמספר התאים הבטוחים ירד
        allPassed &= check("Safe cells remaining should decrease after reveals",
                finalSafeCells < initialSafeCells);

        // סופרים כמה תאים בטוחים באמת נחשפו
        int revealedSafeCells = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell != null && cell.isRevealed() && !cell.isMine()) {
                    revealedSafeCells++;
                }
            }
        }

        int expectedScoreIncrease = revealedSafeCells;
        int actualScoreIncrease = finalScore - initialScore;

        allPassed &= check(
                "Score should increase by +1 for each revealed safe cell " +
                        "(expected increase: " + expectedScoreIncrease +
                        ", actual increase: " + actualScoreIncrease + ")",
                actualScoreIncrease == expectedScoreIncrease
        );

        System.out.println("Note: Full win requires both boards to be cleared. Testing reveal + scoring only.");

        printTestResult("WIN CONDITION", allPassed);
        System.out.println();
    }

    // ============================================================
    // F) USED CELLS
    // ============================================================

    /**
     * F) Used Cells
     * SRS Coverage:
     * - Question/Surprise cells must be marked as USED after activation
     * - Reveal and activation are separate steps
     */
    private static void testF_UsedCells() {
        System.out.println("=== USED CELLS TEST ===");

        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();

        clearBoard(board);
        configureUsedCellsTestBoard(board);

        System.out.println("\nInitial Board State:");
        printBoardState(board);

        // Set score high enough for activation
        game.setSharedScore(20);

        boolean allPassed = true;

        // Step 1: Reveal a question cell
        System.out.println("\nStep 1: Revealing question cell at (1,1)...");
        board.revealCell(1, 1);

        Cell questionCell = board.getCell(1, 1);
        allPassed &= check("Question cell should be revealed",
                questionCell.isRevealed());

        // SRS: After reveal, cell should NOT be used yet (activation is separate)
        allPassed &= check("Question cell should NOT be used immediately after reveal",
                !questionCell.isUsed());
        System.out.println("Question cell isUsed after reveal: " + questionCell.isUsed());

        // Step 2: Activate the question cell (כרגע אין מתודה, רושמים רק TODO)
        System.out.println("\nStep 2: Activating question cell at (1,1)...");
        System.out.println("Note: Activation is separate from reveal - need to call activation method");
        System.out.println("TODO: Add activation method call here when implemented");

        // Step 3: Reveal other cells
        System.out.println("\nStep 3: Revealing other cells...");
        board.revealCell(0, 0); // Should trigger recursive reveal
        board.revealCell(3, 3); // Reveal a numbered cell

        System.out.println("\nBoard State After Reveals:");
        printBoardState(board);

        // Collect all revealed cells
        List<Cell> revealedCells = new ArrayList<>();
        Set<String> cellKeys = new HashSet<>();

        for (int r = 0; r < TEST_SIZE; r++) {
            for (int c = 0; c < TEST_SIZE; c++) {
                Cell cell = board.getCell(r, c);
                if (cell != null && cell.isRevealed()) {
                    revealedCells.add(cell);
                    String key = r + "," + c;
                    cellKeys.add(key);
                }
            }
        }

        // Check no duplicates
        allPassed &= check("No duplicate revealed cells",
                revealedCells.size() == cellKeys.size());

        // Check that all revealed cells are tracked
        allPassed &= check("All revealed cells are tracked",
                !revealedCells.isEmpty());

        System.out.println("Total revealed cells: " + revealedCells.size());
        System.out.println("Unique cell positions: " + cellKeys.size());

        // Print used status for question/surprise cells
        for (Cell cell : revealedCells) {
            if (cell.isQuestionOrSurprise()) {
                System.out.println("Cell (" + cell.getRow() + "," + cell.getCol() + ") isUsed: " + cell.isUsed());
            }
        }

        printTestResult("USED CELLS TEST", allPassed);
        System.out.println();
    }

    // ============================================================
    // Helper Methods for Board Configuration
    // ============================================================

    /**
     * Clears the entire board (not just 5x5) to ensure deterministic tests
     */
    private static void clearBoard(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell != null) {
                    cell.setContent(Cell.CellContent.EMPTY);
                    cell.setAdjacentMines(0);
                    cell.setState(Cell.CellState.HIDDEN);
                    cell.setUsed(false);
                }
            }
        }
    }

    private static void configureRevealTestBoard(Board board) {
        board.getCell(0, 4).setContent(Cell.CellContent.MINE);
        board.getCell(4, 0).setContent(Cell.CellContent.MINE);
        calculateAdjacentMines(board, 0, 3);
        calculateAdjacentMines(board, 1, 3);
        calculateAdjacentMines(board, 1, 4);
        calculateAdjacentMines(board, 3, 0);
        calculateAdjacentMines(board, 3, 1);
        calculateAdjacentMines(board, 4, 1);
    }

    private static void configureScoringTestBoard(Board board) {
        board.getCell(0, 0).setContent(Cell.CellContent.MINE);
        board.getCell(1, 1).setContent(Cell.CellContent.QUESTION);
        board.getCell(1, 1).setQuestionId(1);
    }

    private static void configureLoseTestBoard(Board board) {
        board.getCell(2, 2).setContent(Cell.CellContent.MINE);
    }

    private static void configureWinTestBoard(Board board) {
        board.getCell(0, 0).setContent(Cell.CellContent.MINE);
        board.getCell(4, 4).setContent(Cell.CellContent.MINE);
    }

    private static void configureUsedCellsTestBoard(Board board) {
        board.getCell(0, 4).setContent(Cell.CellContent.MINE);
        board.getCell(1, 1).setContent(Cell.CellContent.QUESTION);
        board.getCell(1, 1).setQuestionId(1);
        board.getCell(3, 3).setContent(Cell.CellContent.NUMBER);
        board.getCell(3, 3).setAdjacentMines(1);
    }

    /**
     * Calculates adjacent mines for a cell and sets it to NUMBER if count > 0
     * Uses actual board size, not TEST_SIZE constant
     */
    private static void calculateAdjacentMines(Board board, int r, int c) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nr = r + i;
                int nc = c + j;
                if (nr >= 0 && nr < board.getRows() && nc >= 0 && nc < board.getCols()) {
                    Cell neighbor = board.getCell(nr, nc);
                    if (neighbor != null && neighbor.isMine()) {
                        count++;
                    }
                }
            }
        }
        if (count > 0) {
            Cell cell = board.getCell(r, c);
            if (cell != null) {
                cell.setContent(Cell.CellContent.NUMBER);
                cell.setAdjacentMines(count);
            }
        }
    }

    // ============================================================
    // Helper Methods for Display and Testing
    // ============================================================

    private static void printBoardState(Board board) {
        System.out.println("\n   ");
        for (int c = 0; c < TEST_SIZE; c++) {
            System.out.print("  " + c);
        }
        System.out.println();

        for (int r = 0; r < TEST_SIZE; r++) {
            System.out.print(r + "  ");
            for (int c = 0; c < TEST_SIZE; c++) {
                Cell cell = board.getCell(r, c);
                if (cell == null) {
                    System.out.print("?? ");
                    continue;
                }

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

    private static boolean check(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
        } else {
            System.out.println("[FAIL] " + description);
        }
        return condition;
    }

    private static void printTestResult(String testName, boolean allPassed) {
        System.out.println("\n" + testName + " RESULT: " +
                (allPassed ? "ALL CHECKS PASSED" : "SOME CHECKS FAILED"));
    }
}
