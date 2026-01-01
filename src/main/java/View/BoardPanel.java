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

    private boolean waiting;

    public BoardPanel(GameController controller,
                      int boardNumber,
                      boolean initiallyWaiting,
                      MoveCallback moveCallback) {
        this.controller = controller;
        this.boardNumber = boardNumber;
        this.waiting = initiallyWaiting;
        this.moveCallback = moveCallback;

        initComponents();
    }

    /**
     * Builds the board UI: grid of buttons.
     */
    private void initComponents() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        setLayout(new GridLayout(rows, cols));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        buttons = new JButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int rr = r;
                final int cc = c;

                JButton btn = new JButton();
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setFocusable(false);
                btn.setPreferredSize(new Dimension(25, 25));

                // LEFT CLICK → reveal
                btn.addActionListener(e -> handleClick(rr, cc, false));

                // RIGHT CLICK → flag / unflag
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleClick(rr, cc, true);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (waiting) {
                            // כשמעבירים עכבר – נצייר שוב את שכבת ה-WAIT
                            BoardPanel.this.repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (waiting) {
                            BoardPanel.this.repaint();
                        }
                    }
                });

                buttons[r][c] = btn;
                add(btn);
            }
        }

        refresh();
    }


    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        if (!waiting || !controller.isGameRunning()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = 5;
        int w = getWidth() - 2 * margin;
        int h = getHeight() - 2 * margin;


        g2.setColor(new Color(230, 230, 230, 210));
        g2.fillRoundRect(margin, margin, w, h, 20, 20);


        String text = "WAIT FOR YOUR TURN";
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = (getWidth() - textWidth) / 2;
        int textY = getHeight() / 2;

        g2.setColor(Color.BLACK);
        g2.drawString(text, textX, textY);


        int lineY = textY + 15;
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(getWidth() / 6, lineY, getWidth() * 5 / 6, lineY);

        g2.dispose();
    }

    /**
     * Main click handler. Routes to the correct logic based on click type.
     */
    private void handleClick(int r, int c, boolean isFlagging) {
        if (!controller.isGameRunning()) return;
        if (controller.getCurrentPlayerTurn() != boardNumber) return;
        if (waiting) return;

        boolean endedTurn = false;

        if (isFlagging) {
            if (!controller.isCellRevealed(boardNumber, r, c)) {
                controller.toggleFlagUI(boardNumber, r, c);
            }
            // flagging does NOT end turn
        } else {

            boolean wasRevealedBeforeClick = controller.isCellRevealed(boardNumber, r, c);
            boolean revealedNow = false;

            // reveal only if it wasn't revealed yet
            if (!wasRevealedBeforeClick) {
                controller.revealCellUI(boardNumber, r, c);
                revealedNow = true;   // <-- THIS is the key
                refresh();
            }

            // special? always show popup
            if (controller.isQuestionOrSurprise(boardNumber, r, c)) {

                String cellType = controller.isQuestionCell(boardNumber, r, c) ? "Question" : "Surprise";

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "This is a " + cellType + " cell.\nDo you want to activate it?\n",
                        cellType + " Cell",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    boolean activated = controller.activateSpecialCellUI(boardNumber, r, c);
                    endedTurn = activated; // activation success ends turn
                } else {
                    // ✅ NO ends turn ONLY if the cell was revealed NOW (this click)
                    endedTurn = revealedNow;
                }

            } else {
                // not special -> if we revealed now, turn ends
                endedTurn = revealedNow;
            }
        }

        refresh();

        if (moveCallback != null) {
            moveCallback.onMove(endedTurn);
        }

        refresh();
    }




    /**
     * Updates the "waiting" state for this board (used when the turn changes).
     */
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;


        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);
        if (buttons != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    buttons[r][c].setEnabled(!waiting && controller.isGameRunning());
                }
            }
        }

        repaint();
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

                if (!gameIsRunning || waiting) {
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
