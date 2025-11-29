package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter; // NEW
import java.awt.event.MouseEvent;   // NEW

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

                // 1. ActionListener for standard LEFT-CLICK (Reveal)
                btn.addActionListener(e -> handleClick(rr, cc, false)); // false = isFlagging

                // 2. MouseListener for RIGHT-CLICK (Flagging)
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Check for right-click (usually BUTTON3)
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleClick(rr, cc, true); // true = isFlagging
                        }
                    }
                });

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

    /**
     * Handles both revealing (isFlagging=false) and flagging (isFlagging=true).
     */
    private void handleClick(int r, int c, boolean isFlagging) {
        if (!controller.isGameRunning()) return;
        if (controller.getCurrentPlayerTurn() != boardNumber) return;
        if (waiting) return;

        if (isFlagging) {
            // Right-click → דגלים רק על תאים סגורים
            if (!controller.isCellRevealed(boardNumber, r, c)) {
                controller.toggleFlagUI(boardNumber, r, c);
            }
        } else {
            // LEFT CLICK

            boolean wasRevealed = controller.isCellRevealed(boardNumber, r, c);

            // 1) אם התא לא היה נחשף → קודם נחשוף אותו (כולל קסקדה אם צריך)
            if (!wasRevealed) {
                controller.revealCellUI(boardNumber, r, c);
                // נרענן כדי שהשחקן יראה את ה-Q/S לפני החלון
                refresh();
            }

            // 2) אם התא הוא QUESTION/SURPRISE → אפשר לשאול על הפעלה
            if (controller.isQuestionOrSurprise(boardNumber, r, c)) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Activate this special cell?\n(cost according to difficulty)",
                        "Special Cell",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    controller.activateSpecialCellUI(boardNumber, r, c);
                }
            }
        }

        // רענון סופי אחרי הפעלה/דגל/חשיפה
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