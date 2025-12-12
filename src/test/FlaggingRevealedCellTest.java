import Model.Board;
import Model.Cell;
import Model.Game;
import Controller.GameController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlaggingRevealedCellTest {

    private GameController controller;
    private Game game;
    private Board board1;

    @BeforeEach
    void setup() {
        controller = GameController.getInstance();
        controller.startNewGame("EASY");   // deterministic size, simpler
        game = controller.getCurrentGame();
        assertNotNull(game);

        board1 = game.getBoard1();
        assertNotNull(board1);
    }

    @Test
    @DisplayName("UT-01: Flagging a revealed cell is blocked (no flag is placed)")
    void flaggingRevealedCell_isBlocked() {
        int row = 0, col = 0;

        // Step 1: reveal the cell first
        board1.revealCell(row, col);

        Cell cell = board1.getCell(row, col);
        assertNotNull(cell);
        assertTrue(cell.isRevealed(), "Cell must be revealed before attempting to flag");

        // Step 2: try to flag a revealed cell
        board1.toggleFlag(row, col);

        // Expected: still not flagged
        assertFalse(cell.isFlagged(), "Revealed cell must NOT become flagged");
    }
}
