import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;
import Model.GameState;

/**
 * USER STORY 2 - USED QUESTION/SURPRISE CELLS:
 * 
 * Unit tests to verify that question and surprise cells can only be used once.
 * Tests both the Board.revealCell() logic (core implementation) and the
 * GameController integration.
 */
public class UsedCellTest {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("USER STORY 2: USED CELL TEST");
        System.out.println("==========================================\n");

        boolean allTestsPassed = true;

        // Test Board.revealCell() directly (core logic)
        allTestsPassed &= testBoardRevealCellLogic();
        
        // Test GameController integration
        allTestsPassed &= testGameControllerIntegration();

        System.out.println("\n==========================================");
        if (allTestsPassed) {
            System.out.println("ALL USED CELL TESTS PASSED ✓");
        } else {
            System.out.println("SOME USED CELL TESTS FAILED ✗");
        }
        System.out.println("==========================================");
    }

    /**
     * Tests the core Board.revealCell() logic directly.
     * This is the main implementation for User Story 2.
     */
    private static boolean testBoardRevealCellLogic() {
        System.out.println("=== Test: Board.revealCell() Logic ===\n");
        
        Game game = new Game(Difficulty.EASY);
        Board board = game.getBoard1();
        
        // Set initial score to allow activation
        game.setSharedScore(20);
        int initialScore = game.getSharedScore();
        int activationCost = game.getDifficulty().getActivationCost();
        
        boolean allPassed = true;

        // ----- Test 1: First activation of question cell -----
        System.out.println("Test 1: First activation of question cell via Board.revealCell()");
        Cell questionCell = board.getCell(0, 0);
        questionCell.setContent(Cell.CellContent.QUESTION);
        questionCell.setQuestionId(1);
        questionCell.setState(Cell.CellState.HIDDEN);
        questionCell.setUsed(false);
        
        allPassed &= check("Question cell should not be used initially", !questionCell.isUsed());
        allPassed &= check("Cell should be HIDDEN initially", 
                questionCell.getState() == Cell.CellState.HIDDEN);
        
        // Reveal via Board.revealCell() - this should mark as used and trigger effect
        board.revealCell(0, 0);

        allPassed &= check("Question cell should NOT be marked as used after reveal only",
                !questionCell.isUsed());
        allPassed &= check("Cell should be REVEALED after revealCell()",
                questionCell.getState() == Cell.CellState.REVEALED);
        allPassed &= check("Score should change by +1 for safe reveal (no activation yet)",
                game.getSharedScore() == initialScore + 1);


        // ----- Test 2: Second activation attempt (should skip effect) -----
        System.out.println("\nTest 2: Second activation attempt (should skip effect)");
        int scoreBeforeSecondClick = game.getSharedScore();
        
        // Manually reset state to HIDDEN to simulate another click attempt
        // (In real game, cell would already be revealed, but we test the logic)
        questionCell.setState(Cell.CellState.HIDDEN);
        
        // Try to reveal again - should skip effect since cell is already used
        // Note: Even though effect is skipped, revealing a safe cell still gives +1 point
        board.revealCell(0, 0);

        allPassed &= check("Question cell should still NOT be marked as used",
                !questionCell.isUsed());
        allPassed &= check("Score should increase by +1 for second reveal",
                game.getSharedScore() == scoreBeforeSecondClick + 1);

        allPassed &= check("Cell should be REVEALED after second click", 
                questionCell.getState() == Cell.CellState.REVEALED);

        // ----- Test 3: Surprise cell -----
        System.out.println("\nTest 3: Surprise cell usage via Board.revealCell()");

// Find a guaranteed SAFE cell (not a mine)
        Cell surpriseCell = null;
        int sr = -1, sc = -1;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell candidate = board.getCell(r, c);
                if (!candidate.isMine()) {
                    surpriseCell = candidate;
                    sr = r;
                    sc = c;
                    break;
                }
            }
            if (surpriseCell != null) break;
        }

// Safety check (should never fail in normal game)
        if (surpriseCell == null) {
            System.out.println("[FAIL] Could not find a safe cell for surprise test");
            return false;
        }

// Turn that safe cell into a SURPRISE
        surpriseCell.setContent(Cell.CellContent.SURPRISE);
        surpriseCell.setState(Cell.CellState.HIDDEN);
        surpriseCell.setUsed(false);

        int scoreBeforeSurprise = game.getSharedScore();

// First reveal – should give +1
        board.revealCell(sr, sc);
        allPassed &= check("SURPRISE cell should NOT be marked as used on reveal only",
                !surpriseCell.isUsed());
        allPassed &= check("Score should increase by +1 for safe reveal",
                game.getSharedScore() == scoreBeforeSurprise + 1);



        // Try to activate again
        surpriseCell.setState(Cell.CellState.HIDDEN);
        int scoreBeforeSecondSurprise = game.getSharedScore();
        board.revealCell(1, 1);
        allPassed &= check("Second surprise reveal should NOT mark as used (no activation here)",
                !surpriseCell.isUsed());

        // When cell is already used, special effect is skipped, but revealing still gives +1 point
        allPassed &= check("Score should increase by +1 only (effect skipped, no activation cost)", 
                game.getSharedScore() == scoreBeforeSecondSurprise + 1);

        // ----- Test 4: Normal cells (not question/surprise) should work normally -----
        System.out.println("\nTest 4: Normal cells should work normally");
        Cell normalCell = board.getCell(2, 2);
        normalCell.setContent(Cell.CellContent.EMPTY);
        normalCell.setState(Cell.CellState.HIDDEN);
        normalCell.setUsed(false);
        
        board.revealCell(2, 2);
        allPassed &= check("Normal cell should be REVEALED", 
                normalCell.getState() == Cell.CellState.REVEALED);
        allPassed &= check("Normal cell should not be marked as used", !normalCell.isUsed());

        System.out.println();
        return allPassed;
    }

    /**
     * Tests the GameController integration to ensure the used cell logic
     * works through the controller layer.
     */
    private static boolean testGameControllerIntegration() {
        System.out.println("=== Test: GameController Integration ===\n");
        
        GameController controller = GameController.getInstance();

        controller.startNewGame(Difficulty.EASY);
        Game game = controller.getCurrentGame();
        Board board = game.getBoard1();
        
        // Get a cell and manually set it as a question cell for testing
        Cell questionCell = board.getCell(0, 0);
        questionCell.setContent(Cell.CellContent.QUESTION);
        questionCell.setQuestionId(1);
        questionCell.setState(Cell.CellState.HIDDEN);
        questionCell.setUsed(false);
        
        // Set initial score to allow activation
        game.setSharedScore(20);
        int initialScore = game.getSharedScore();
        int activationCost = game.getDifficulty().getActivationCost();
        
        boolean allPassed = true;

        // First activation via controller
        System.out.println("Test: First activation via GameController");
        allPassed &= check("Question cell should not be used initially", !questionCell.isUsed());

        controller.revealCellUI(1, 0, 0);
        allPassed &= check("Question cell should NOT be used after reveal only",
                !questionCell.isUsed());
        allPassed &= check("Score should be +1 after reveal",
                game.getSharedScore() == initialScore + 1);

// Now activate
        int scoreBeforeActivation = game.getSharedScore();
        controller.activateSpecialCellUI(1, 0, 0);

        allPassed &= check("Question cell should be marked as used after activation",
                questionCell.isUsed());
        allPassed &= check("Score should decrease by activationCost on activation",
                game.getSharedScore() == scoreBeforeActivation - activationCost);


        // Second activation attempt (cell is already revealed, so should be blocked)
        System.out.println("\nTest: Second activation attempt via GameController");
        int scoreBeforeSecondClick = game.getSharedScore();
        controller.revealCellUI(1, 0, 0); // Should be blocked since already revealed
        allPassed &= check("Question cell should still be marked as used", questionCell.isUsed());
        allPassed &= check("Score should NOT change (cell already revealed)", 
                game.getSharedScore() == scoreBeforeSecondClick);

        System.out.println();
        return allPassed;
    }

    private static boolean check(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
        } else {
            System.out.println("[FAIL] " + description);
        }
        return condition;
    }
}
