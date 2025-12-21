import Controller.GameController;
import View.BoardPanel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class FlagDoesNotEndTurnTest {

    @Test
    void TC_JU_FLAG_001_rightClickDoesNotEndTurn() throws Exception {
        GameController controller = GameController.getInstance();
        controller.startNewGame("EASY");

        AtomicBoolean endedTurnValue = new AtomicBoolean(true); // start true so we detect change

        BoardPanel.MoveCallback cb = endedTurnValue::set;

        BoardPanel panel = new BoardPanel(
                controller,
                1,          // boardNumber
                false,      // initiallyWaiting
                cb
        );

        // pick a cell (0,0 is fine for flagging; no reveal happens)
        int r = 0, c = 0;

        // call private handleClick(r,c,true)
        Method m = BoardPanel.class.getDeclaredMethod("handleClick", int.class, int.class, boolean.class);
        m.setAccessible(true);
        m.invoke(panel, r, c, true);

        assertFalse(endedTurnValue.get(),
                "REQ-FLAG-TURN-01: right-click flag/unflag must call onMove(false) and not end turn");
    }
}
