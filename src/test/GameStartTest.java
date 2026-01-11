import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;

public class GameStartTest {
    /**
     * Simple console-based test class for game startup and core rules.
     * Verifies:
     * - Game initialization by difficulty
     * - Restart logic (reset of lives, score and boards)
     * - Basic rule behavior: flagging mines/safe cells and revealing mines.
     */
    public static void main(String[] args) {
        GameController controller = GameController.getInstance();

        // =============================================================
        // PART 1: Initialization & Restart Tests
        // =============================================================

        controller.startNewGame(Difficulty.EASY);
        Game game = controller.getCurrentGame();

        System.out.println("=== TEST SET 1: Initialization & Restart ===");
        check("Game should not be null", game != null);
        assert game != null;
        check("Difficulty should be EASY", game.getDifficulty() == Difficulty.EASY);

        Board b1 = game.getBoard1();
        Board b2 = game.getBoard2();

        check("Board1 should not be null", b1 != null);
        check("Board2 should not be null", b2 != null);

        assert b1 != null;
        check("Board1 rows == EASY rows", b1.getRows() == Difficulty.EASY.getRows());
        check("Board1 cols == EASY cols", b1.getCols() == Difficulty.EASY.getCols());

        check("Board1 mines == EASY mines", b1.getTotalMines() == Difficulty.EASY.getMines());
        check("Board1 question cells == EASY question cells",
                b1.getTotalQuestionCells() == Difficulty.EASY.getQuestionCells());

        check("Shared lives == EASY startingLives",
                game.getSharedLives() == Difficulty.EASY.getStartingLives());
        check("Shared score starts at 0", game.getSharedScore() == 0);
        // Mutate game state to verify restart logic
        game.setSharedLives(game.getSharedLives() - 3);
        game.setSharedScore(25);
        Board oldBoard1 = game.getBoard1();

        controller.restartGame();
        Game restarted = controller.getCurrentGame();

        System.out.println("\n--- Restart Verification ---");
        check("Shared lives reset to EASY startingLives",
                restarted.getSharedLives() == Difficulty.EASY.getStartingLives());
        check("Shared score reset to 0",
                restarted.getSharedScore() == 0);
        check("Board1 should be a new instance after restart",
                restarted.getBoard1() != oldBoard1);

        // =============================================================
        // PART 2: Board Logic Tests (Revealing, Flagging, Scoring)
        // =============================================================
        System.out.println("\n=== TEST SET 2: Game Rules & Logic ===");

        Game activeGame = controller.getCurrentGame();
        Board testBoard = activeGame.getBoard1();

        // --- TEST A: Flagging a Mine (Should Increase Score) ---
        int scoreBeforeFlagMine = activeGame.getSharedScore();
        Cell mineCell = findCellWithContent(testBoard, Cell.CellContent.MINE);

        if (mineCell != null) {
            testBoard.toggleFlag(mineCell.getRow(), mineCell.getCol());
            check("Flagging a Mine should INCREASE score",
                    activeGame.getSharedScore() > scoreBeforeFlagMine);
        } else {
            System.out.println("[SKIP] Could not find a Mine to test flagging.");
        }

        // --- TEST B: Flagging a Safe Cell (Should Decrease Score) ---
        int scoreAfterGoodFlag = activeGame.getSharedScore();
        Cell safeCell = findCellWithContent(testBoard, Cell.CellContent.EMPTY);

        if (safeCell != null) {
            testBoard.toggleFlag(safeCell.getRow(), safeCell.getCol());
            check("Flagging a Safe Cell should DECREASE score",
                    activeGame.getSharedScore() < scoreAfterGoodFlag);
        } else {
            System.out.println("[SKIP] Could not find a Safe cell to test flagging.");
        }

        // --- TEST C: Revealing a Mine (Should Lose Life) ---
        controller.restartGame();
        activeGame = controller.getCurrentGame();
        testBoard = activeGame.getBoard1();

        int livesBeforeExplosion = activeGame.getSharedLives();
        Cell explodeCell = findCellWithContent(testBoard, Cell.CellContent.MINE);

        if (explodeCell != null) {
            testBoard.revealCell(explodeCell.getRow(), explodeCell.getCol());
            check("Revealing a Mine should SUBTRACT 1 life",
                    activeGame.getSharedLives() == livesBeforeExplosion - 1);
        } else {
            System.out.println("[SKIP] Could not find a Mine to test reveal.");
        }
    }

    // -------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------

    /**
     * Prints a PASS/FAIL line for a single condition.
     */
    private static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
        } else {
            System.out.println("[FAIL] " + description);
        }
    }

    /**
     * Finds the first cell on the board that matches the given content type.
     * Returns null if no such cell exists.
     */
    private static Cell findCellWithContent(Board board, Cell.CellContent type) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).getContent() == type) {
                    return board.getCell(r, c);
                }
            }
        }
        return null;
    }
}
