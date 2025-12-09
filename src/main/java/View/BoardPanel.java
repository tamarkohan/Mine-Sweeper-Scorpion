package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * View component for a single player's board.
 * Displays a grid of buttons and interacts only with GameController (not with the Model directly).
 */
public class BoardPanel extends JPanel {

    private final GameController controller;
    private final int boardNumber;   // 1 or 2
    public interface MoveCallback {
        // endedTurn = true → a real reveal happened
        // endedTurn = false → only flag/unflag
        void onMove(boolean endedTurn);
    }

    private final MoveCallback moveCallback;
    private JButton[][] buttons;
    private JLabel waitLabel;
    private boolean waiting;         // true = this player is currently not allowed to play

    public BoardPanel(GameController controller,
                      int boardNumber,
                      boolean initiallyWaiting,
                      MoveCallback  moveCallback) {
        this.controller = controller;
        this.boardNumber = boardNumber;
        this.waiting = initiallyWaiting;
        this.moveCallback = moveCallback;

        initComponents();
    }
    /**
     * Builds the board UI: grid of buttons + overlay label for "WAIT FOR YOUR TURN".
     */
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
                btn.addActionListener(e -> handleClick(rr, cc, false));

                // 2. MouseListener for RIGHT-CLICK (Flagging)
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleClick(rr, cc, true);
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
     * Main click handler. Routes to the correct logic based on click type.
     */
    private void handleClick(int r, int c, boolean isFlagging) {
        if (!controller.isGameRunning()) return;
        if (controller.getCurrentPlayerTurn() != boardNumber) return;
        if (waiting) return;

        boolean endedTurn = false; // will become true only if we revealed a hidden cell

        if (isFlagging) {
            // Right-click: flag/unflag only if not revealed
            if (!controller.isCellRevealed(boardNumber, r, c)) {
                controller.toggleFlagUI(boardNumber, r, c);
            }
        } else {
            // Left-click: reveal
            boolean wasRevealed = controller.isCellRevealed(boardNumber, r, c);

            if (!wasRevealed) {
                controller.revealCellUI(boardNumber, r, c);
                endedTurn = true;  // this is a real move
                refresh();
            }

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

        refresh();

        if (moveCallback != null) {
            moveCallback.onMove(endedTurn);   // tell GamePanel if we should end turn
        }

        refresh();
    }




    /**
     * Updates the "waiting" state for this board (used when the turn changes).
     */
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
        if (waitLabel != null) {
            waitLabel.setVisible(waiting);
        }
    }
    /**
     * Refreshes the visual state of all buttons according to the controller's cell view data.
     */
    public void refresh() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        boolean gameIsRunning = controller.isGameRunning();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = buttons[r][c];
                GameController.CellViewData data =
                        controller.getCellViewData(boardNumber, r, c);

                if (!gameIsRunning) {
                    btn.setEnabled(false);
                } else {
                    btn.setEnabled(data.enabled);
                }

                btn.setText(data.text);
            }
        }
        revalidate();
        repaint();
    }
}