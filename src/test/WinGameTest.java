/*
 * Test Class for TC-BB-WONGAME-001
 * Verifies win detection, score calculation, and board state after victory.
 */
import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Game;
import Model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WinGameTest {

    private GameController controller;
    private Game game;

    @BeforeEach
    void setup() {
        controller = GameController.getInstance();
        // Start an EASY game to minimize the number of cells to process
        controller.startNewGame("EASY");
        game = controller.getCurrentGame();
        assertNotNull(game, "Game should be initialized");
    }

    @Test
    @DisplayName("TC-BB-WONGAME-001: Verify system detects win, updates score, reveals boards, and freezes game")
    void verifyWinConditionAndPostGameStates() {
        // 1. Verify initial state
        assertTrue(controller.isGameRunning(), "Game should be running initially");
        assertFalse(controller.isGameOver(), "Game should not be over initially");

        // 2. Reveal all safe cells on both boards to trigger a Win
        // (Assuming cooperative logic requires clearing safe cells)
        revealAllSafeCells(1);
        
        // If the game requires both boards to be cleared, continue to board 2
        if (controller.isGameRunning()) {
            revealAllSafeCells(2);
        }

        // 3. Verify Game Over and Win State
        assertTrue(controller.isGameOver(), "Game should be over after revealing all safe cells");
        assertEquals(GameState.WON, game.getGameState(), "GameState should be WON");

        // 4. Verify Final Score
        // Score should be positive (points for revealing cells + potential bonus)
        assertTrue(controller.getSharedScore() > 0, "Final score should be positive");
        
        // 5. Verify Boards are Fully Revealed
        // Requirement: "Boards are fully Revealed"
        // We check that even mines (which we didn't click) are now revealed.
        verifyBoardIsFullyRevealed(1);
        verifyBoardIsFullyRevealed(2);

        // 6. Verify Game is Frozen (Pause disabled / Interactions ignored)
        // Requirement: "Pause button disabled" (Implied by Game Over state preventing actions)
        
        // Attempt to reveal a cell (should fail/return false)
        boolean result = controller.revealCellUI(1, 0, 0);
        assertFalse(result, "Should not be able to perform reveal action after game is over");

        // Attempt to toggle a flag (should not change state)
        Cell testCell = game.getBoard1().getCell(0, 0);
        boolean originalFlagState = testCell.isFlagged();
        controller.toggleFlagUI(1, 0, 0);
        assertEquals(originalFlagState, testCell.isFlagged(), "Should not be able to toggle flags after game is over");
    }

    @Test
    @DisplayName("TC-BB-WONGAME-001: Verify win by flagging all mines (Successful Mine Identification)")
    void verifyWinByFlaggingMines() {
        Board board1 = game.getBoard1();
        
        // Identify all mines on Board 1 to simulate player knowledge
        List<int[]> mineCoordinates = new ArrayList<>();
        for (int r = 0; r < board1.getRows(); r++) {
            for (int c = 0; c < board1.getCols(); c++) {
                if (board1.getCell(r, c).isMine()) {
                    mineCoordinates.add(new int[]{r, c});
                }
            }
        }

        assertFalse(mineCoordinates.isEmpty(), "Board should have mines generated");
        int totalMines = mineCoordinates.size();

        // 2. Pre-condition: 9 out of 10 mines have been flagged (or all except one)
        for (int i = 0; i < totalMines - 1; i++) {
            int[] coords = mineCoordinates.get(i);
            controller.toggleFlagUI(1, coords[0], coords[1]);
        }

        // Verify game is still running before the final flag
        assertTrue(controller.isGameRunning(), "Game should be running before identifying the last mine");
        assertFalse(controller.isGameOver(), "Game Over state should be false initially");

        // 3. Critical Input: Player performs a Flag action on the last remaining mine
        int[] lastMine = mineCoordinates.get(totalMines - 1);
        controller.toggleFlagUI(1, lastMine[0], lastMine[1]);

        // 4. Verify Win State ("The team Wins!..." popup logic triggered by state change)
        assertFalse(controller.isGameRunning(), "Game should stop running after winning");
        assertTrue(controller.isGameOver(), "Game should be in Game Over state");
        assertEquals(GameState.WON, game.getGameState(), "GameState should be WON");

        // 5. Verify Scoring
        // "Final score = Player 1 Base Score + (remaining lives*N)"
        // We verify the score is calculated (positive value)
        assertTrue(controller.getSharedScore() > 0, "Final score should be positive and calculated");

        // 6. Verify Boards State: Fully Revealed and Frozen
        
        // Check Frozen: Attempt to reveal a safe cell (should fail)
        // Find a safe cell coordinates
        int safeRow = -1, safeCol = -1;
        for (int r = 0; r < board1.getRows(); r++) {
            for (int c = 0; c < board1.getCols(); c++) {
                if (!board1.getCell(r, c).isMine()) {
                    safeRow = r; safeCol = c; break;
                }
            }
            if (safeRow != -1) break;
        }
        
        boolean revealResult = controller.revealCellUI(1, safeRow, safeCol);
        assertFalse(revealResult, "Board should be frozen; reveal action should return false");

        // Check Revealed: The safe cell should be revealed automatically upon win
        Cell safeCell = board1.getCell(safeRow, safeCol);
        assertTrue(safeCell.isRevealed(), "Safe cells should be automatically revealed after win");
    }

    /**
     * Helper to reveal all non-mine cells on a specific board.
     */
    private void revealAllSafeCells(int boardNumber) {
        Board board = (boardNumber == 1) ? game.getBoard1() : game.getBoard2();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                // Stop if game ended (e.g. won early)
                if (controller.isGameOver()) return;

                Cell cell = board.getCell(r, c);
                // Peek at model to avoid mines
                if (!cell.isMine() && !cell.isRevealed()) {
                    controller.revealCellUI(boardNumber, r, c);
                }
            }
        }
    }

    /**
     * Helper to verify that every cell on the board is revealed.
     */
    private void verifyBoardIsFullyRevealed(int boardNumber) {
        Board board = (boardNumber == 1) ? game.getBoard1() : game.getBoard2();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                assertTrue(cell.isRevealed(), 
                    String.format("Cell at (%d, %d) on Board %d should be revealed after win", r, c, boardNumber));
            }
        }
    }
}