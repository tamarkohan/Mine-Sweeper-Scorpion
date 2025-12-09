
import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;
import Model.GameState;
import View.BoardPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import Controller.GameController;
import javax.swing.*;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Turn system tests – based on:
 * - TC-BB-TURNS-001
 * - TC-WB-TURNS-001
 * - TC-BB-TURN-001
 */
public class TurnSystemTest {

    private GameController controller;
    private Game game;

    @BeforeEach
    void setup() {
        controller = GameController.getInstance();
        controller.startNewGame("MEDIUM");
        game = controller.getCurrentGame();
        assertNotNull(game);
        assertEquals(Difficulty.MEDIUM, game.getDifficulty());
    }

    // =========================
    // Helper: call handleClick via reflection
    // =========================
    private void invokeHandleClick(BoardPanel panel, int row, int col, boolean isFlagging) {
        try {
            Method m = BoardPanel.class.getDeclaredMethod(
                    "handleClick", int.class, int.class, boolean.class);
            m.setAccessible(true);
            m.invoke(panel, row, col, isFlagging);
        } catch (Exception e) {
            fail("Failed to invoke BoardPanel.handleClick via reflection: " + e.getMessage());
        }
    }

    // =========================
    // TC-BB-TURNS-001
    // =========================
    @Test
    @DisplayName("TC-BB-TURNS-001: Click on opponent board is ignored")
    void clickOnOpponentBoard_isIgnored_noScoreNoLivesNoTurnChange() {
        // Precondition: Player 1 is active
        int initialTurn = controller.getCurrentPlayerTurn();
        assertEquals(1, initialTurn, "Expected Player 1 to start");

        int initialScore = game.getSharedScore();
        int initialLives = game.getSharedLives();

        Board opponentBoardModel = game.getBoard2();
        assertNotNull(opponentBoardModel);

        BoardPanel opponentPanel = new BoardPanel(
                controller,
                2,
                true,
                () -> {}
        );

        // בוחרים תא חוקי כלשהו – נגיד (0,0)
        int row = 0, col = 0;
        Cell opponentCell = opponentBoardModel.getCell(row, col);
        assertNotNull(opponentCell);
        assertFalse(opponentCell.isRevealed());

        // פעולה: Player 1 מנסה ללחוץ על לוח 2 (לוח היריב)
        invokeHandleClick(opponentPanel, row, col, false);

        // ✔ Clicked cell on opponent board remains HIDDEN.
        assertFalse(opponentCell.isRevealed(),
                "Opponent cell should remain HIDDEN after illegal click");

        // ✔ No change in shared score.
        assertEquals(initialScore, game.getSharedScore(),
                "Score must not change after illegal click on opponent board");

        // ✔ No change in shared lives.
        assertEquals(initialLives, game.getSharedLives(),
                "Lives must not change after illegal click on opponent board");

        // ✔ Turn does NOT change.
        assertEquals(initialTurn, controller.getCurrentPlayerTurn(),
                "Turn must not change after illegal click on opponent board");

        // ✔ Game state remains RUNNING.
        assertEquals(GameState.RUNNING, game.getGameState());
    }

    // =========================
    // TC-WB-TURNS-001 (valid move on own board)
    // =========================
    @Test
    @DisplayName("TC-WB-TURNS-001: Valid click on own board reveals cell and switches turn")
    void validClickOnOwnBoard_revealsCell_andSwitchesTurn() {
        // Precondition: Player 1 active
        assertEquals(1, controller.getCurrentPlayerTurn());

        Board board1Model = game.getBoard1();
        assertNotNull(board1Model);

        // בוחרים תא בטוח (לא מוקש) שעדיין HIDDEN
        int row = -1, col = -1;
        outer:
        for (int r = 0; r < board1Model.getRows(); r++) {
            for (int c = 0; c < board1Model.getCols(); c++) {
                Cell cell = board1Model.getCell(r, c);
                if (!cell.isMine() && !cell.isRevealed()) {
                    row = r;
                    col = c;
                    break outer;
                }
            }
        }
        assertTrue(row >= 0 && col >= 0, "Could not find a safe hidden cell on Board 1");

        Cell targetCell = board1Model.getCell(row, col);
        assertFalse(targetCell.isRevealed());

        int initialTurn = controller.getCurrentPlayerTurn();
        int initialLives = game.getSharedLives();

        // בונים BoardPanel עם moveCallback שמבצע switchTurn – כמו ב-GamePanel
        BoardPanel board1Panel = new BoardPanel(
                controller,
                1,
                false,                      // Player 1 is not waiting
                controller::processTurnEnd  // moveCallback מחליף תור
        );

        // פעולה: שחקן 1 לוחץ על תא חוקי בלוח שלו
        invokeHandleClick(board1Panel, row, col, false);

        // ✔ Clicked cell on current player board changes from HIDDEN to REVEALED.
        assertTrue(targetCell.isRevealed(),
                "Cell on current player's board must be revealed after valid click");

        // ✔ currentPlayer changes from 1 to 2.
        assertEquals(2, controller.getCurrentPlayerTurn(),
                "Turn must switch from Player 1 to Player 2 after valid move");

        // ✔ No error popup – נבדוק שאין חריגה ושמצב המשחק עדיין RUNNING
        assertEquals(GameState.RUNNING, game.getGameState());

        // ✔ Lives לא אמורות להשתנות על תא בטוח
        assertEquals(initialLives, game.getSharedLives());
    }

    // =========================
    // TC-BB-TURN-001 – Integration of valid + invalid + next player
    // =========================
    @Test
    @DisplayName("TC-BB-TURN-001: Only active player can play; after valid move turn switches; inactive player is blocked")
    void turnFlow_validThenInvalidThenOtherPlayer() {
        // Game Setup: Medium, Player 1 active
        assertEquals(1, controller.getCurrentPlayerTurn());
        int initialLives = game.getSharedLives();

        Board board1Model = game.getBoard1();
        Board board2Model = game.getBoard2();
        assertNotNull(board1Model);
        assertNotNull(board2Model);

        // ===== שלב 1: Player 1 מהלך חוקי בלוח A (Board 1) =====
        int p1Row = -1, p1Col = -1;
        outer1:
        for (int r = 0; r < board1Model.getRows(); r++) {
            for (int c = 0; c < board1Model.getCols(); c++) {
                Cell cell = board1Model.getCell(r, c);
                if (!cell.isMine() && !cell.isRevealed()) {
                    p1Row = r;
                    p1Col = c;
                    break outer1;
                }
            }
        }
        assertTrue(p1Row >= 0);

        Cell p1Cell = board1Model.getCell(p1Row, p1Col);

        BoardPanel board1Panel = new BoardPanel(
                controller,
                1,
                false,
                controller::processTurnEnd   // בדיוק כמו ב-GamePanel.handleMoveMade
        );
        BoardPanel board2Panel = new BoardPanel(
                controller,
                2,
                true,                        
                controller::processTurnEnd
        );

        int scoreBefore = game.getSharedScore();

        // Player 1 – מהלך ראשון חוקי
        invokeHandleClick(board1Panel, p1Row, p1Col, false);

        assertTrue(p1Cell.isRevealed(), "First click by Player 1 must reveal the cell");
        assertEquals(2, controller.getCurrentPlayerTurn(),
                "Turn must switch automatically to Player 2 after valid move");
        assertEquals(initialLives, game.getSharedLives(),
                "Lives should not change on safe cell");
        assertTrue(game.getSharedScore() >= scoreBefore + 1,
                "Score should go up at least by +1 for safe reveal");

        // ===== שלב 2: Player 1 מנסה שוב לשחק (Invalid Turn) =====
        int turnBeforeInvalid = controller.getCurrentPlayerTurn();
        int scoreBeforeInvalid = game.getSharedScore();
        int livesBeforeInvalid = game.getSharedLives();

        // סימולציה: Player 1 שוב מנסה לחיצה על Board 1,
        // אבל כרגע currentPlayer = 2 → זה מהלך לא חוקי
        invokeHandleClick(board1Panel, p1Row, p1Col, false);

        // ✔ Second click by Player 1 is blocked and ignored.
        assertEquals(turnBeforeInvalid, controller.getCurrentPlayerTurn(),
                "Turn must NOT change after invalid click by inactive player");
        assertEquals(scoreBeforeInvalid, game.getSharedScore(),
                "Score must NOT change on invalid turn");
        assertEquals(livesBeforeInvalid, game.getSharedLives(),
                "Lives must NOT change on invalid turn");

        // ===== שלב 3: Player 2 מבצע מהלך חוקי בלוח B =====
        // מעדכנים את ה-waiting (Player 2 עכשיו אקטיבי)
        board2Panel.setWaiting(false);

        int p2Row = -1, p2Col = -1;
        outer2:
        for (int r = 0; r < board2Model.getRows(); r++) {
            for (int c = 0; c < board2Model.getCols(); c++) {
                Cell cell = board2Model.getCell(r, c);
                if (!cell.isMine() && !cell.isRevealed()) {
                    p2Row = r;
                    p2Col = c;
                    break outer2;
                }
            }
        }
        assertTrue(p2Row >= 0);

        Cell p2Cell = board2Model.getCell(p2Row, p2Col);
        int scoreBeforeP2 = game.getSharedScore();

        invokeHandleClick(board2Panel, p2Row, p2Col, false);

        // ✔ Player 2’s click on Board B is accepted.
        assertTrue(p2Cell.isRevealed(), "Player 2's valid click on Board 2 must reveal the cell");
        assertEquals(1, controller.getCurrentPlayerTurn(),
                "After Player 2 move, turn should switch back to Player 1");
        assertTrue(game.getSharedScore() >= scoreBeforeP2 + 1);

        // ✔ No crash, no freeze, no error popup → נבדוק שהמשחק עדיין RUNNING
        assertEquals(GameState.RUNNING, game.getGameState());
    }
}
