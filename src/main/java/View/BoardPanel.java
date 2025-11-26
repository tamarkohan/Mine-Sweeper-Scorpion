package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Shows a single board (for one player).
 * לא מכיר בכלל את Board / Cell / Game – רק GameController.
 */
public class BoardPanel extends JPanel {

    private final GameController controller;
    private final int boardNumber;   // 1 or 2
    private final Runnable moveCallback;

    private JButton[][] buttons;
    private JLabel waitLabel;
    private boolean waiting;         // true = "WAIT FOR YOUR TURN"

    public BoardPanel(GameController controller,
                      int boardNumber,
                      boolean initiallyWaiting,
                      Runnable moveCallback) {
        this.controller = controller;
        this.boardNumber = boardNumber;
        this.waiting = initiallyWaiting;
        this.moveCallback = moveCallback;

        initComponents();
    }

    private void initComponents() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        setLayout(new OverlayLayout(this));
        setBackground(Color.BLACK);

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
        gridPanel.setBackground(Color.BLACK);

        buttons = new JButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int rr = r;
                final int cc = c;

                JButton btn = new JButton();
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setFocusable(false);
                btn.setPreferredSize(new Dimension(25, 25));

                btn.addActionListener(e -> handleClick(rr, cc));

                buttons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        add(gridPanel);

        // Overlay label for "WAIT FOR YOUR TURN"
        waitLabel = new JLabel("WAIT FOR YOUR TURN", SwingConstants.CENTER);
        waitLabel.setFont(new Font("Arial", Font.BOLD, 14));
        waitLabel.setForeground(Color.BLACK);
        waitLabel.setOpaque(true);
        waitLabel.setBackground(new Color(255, 255, 255, 170));
        waitLabel.setAlignmentX(0.5f);
        waitLabel.setAlignmentY(0.5f);
        waitLabel.setVisible(waiting);

        add(waitLabel);

        refresh();
    }

    private void handleClick(int r, int c) {
        if (!controller.isGameRunning()) return;

        // Not this board's turn?
        if (controller.getCurrentPlayerTurn() != boardNumber) return;

        // Also ignore if this panel is marked as waiting
        if (waiting) return;

        controller.revealCellUI(boardNumber, r, c);

        refresh();

        if (moveCallback != null) {
            moveCallback.run();
        }
    }

    /**
     * Called by GamePanel when the turn changes.
     */
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
        if (waitLabel != null) {
            waitLabel.setVisible(waiting);
        }
    }

    /**
     * Repaint buttons according to cell state/content via controller.
     */
    public void refresh() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = buttons[r][c];
                GameController.CellViewData data =
                        controller.getCellViewData(boardNumber, r, c);

                btn.setEnabled(data.enabled);
                btn.setText(data.text);
            }
        }
    }
}
