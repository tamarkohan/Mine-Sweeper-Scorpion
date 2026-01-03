package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import View.IconCache;

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
    private static final int MIN_CELL = 18;
    private static final int MAX_CELL = 80;   // <-- bigger than 60 so Medium/Hard can grow more
    private int cellSize = 40;

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

    public void setCellSize(int newSize) {
        int maxCell = 120; // אפשר להשאיר ככה, הגבלת גודל תאים
        newSize = Math.max(MIN_CELL, Math.min(newSize, maxCell));

        this.cellSize = newSize;

        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        Dimension pref = new Dimension(cols * cellSize, rows * cellSize);

        setPreferredSize(pref);
        setMinimumSize(new Dimension(cols * MIN_CELL, rows * MIN_CELL));
        setMaximumSize(pref); // חשוב ל-GridBag שלא “ישחק” לך עם זה

        if (buttons != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    buttons[r][c].setPreferredSize(new Dimension(cellSize, cellSize));
                }
            }
        }

        revalidate();
        repaint();
    }




    /**
     * Builds the board UI: grid of buttons.
     */
    private void initComponents() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        // Auto cell size based on grid size - START WITH LARGER VALUES
        int maxDim = Math.max(rows, cols);

        if (maxDim <= 9) {
            this.cellSize = 48;  // Easy: 9x9
        } else if (maxDim <= 13) {  // ← Changed from 12 to 13
            this.cellSize = 42;  // Medium: 13x13 - START BIGGER
        } else {
            this.cellSize = 30;  // Hard: 16x16
        }

// try 38-45 until it fits your background nicely

        // Let the board have a REAL preferred size (so it won't stretch huge)
        setLayout(new GridLayout(rows, cols, 0, 0));
        setOpaque(false);                // important: don't paint a solid bg
        setDoubleBuffered(true);

        // This forces Swing to keep the board at this size (when wrapped properly)

        Dimension pref = new Dimension(cols * this.cellSize, rows * this.cellSize);
        setPreferredSize(pref);

// minimum should be based on MIN_CELL, not current cellSize
        Dimension min = new Dimension(cols * MIN_CELL, rows * MIN_CELL);
        setMinimumSize(min);

        buttons = new JButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                final int rr = r;
                final int cc = c;

                JButton btn = new JButton();
                btn.setMargin(new Insets(0, 0, 0, 0));

                //  style AFTER creating button
                styleCellButton(btn);

                // size
                btn.setPreferredSize(new Dimension(this.cellSize, this.cellSize));

                // clicks...
                btn.addActionListener(e -> handleClick(rr, cc, false));

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleClick(rr, cc, true);
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
                    //  NO ends turn ONLY if the cell was revealed NOW (this click)
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

// reset every time
                btn.setText("");
                btn.setIcon(null);
                btn.setDisabledIcon(null);

                String t = data.text;

// --- FLAGS ---
                if ("🚩".equals(t)) {
                    btn.setIcon(IconCache.icon("/ui/cells/flag.png", (int)(cellSize * 0.80)));
                    btn.setDisabledIcon(btn.getIcon());
                }
// --- MINES ---
                else if ("M".equals(t)) {
                    btn.setIcon(IconCache.icon("/ui/cells/mine.png", (int)(cellSize * 0.85)));
                    btn.setDisabledIcon(btn.getIcon());
                }
// --- QUESTION ---
                else if ("Q".equals(t)) {
                    btn.setIcon(IconCache.icon("/ui/cells/question.png", (int)(cellSize * 0.82)));
                    btn.setDisabledIcon(btn.getIcon());
                }
// --- SURPRISE ---
                else if ("S".equals(t)) {
                    btn.setIcon(IconCache.icon("/ui/cells/surprise.png", (int)(cellSize * 0.82)));
                    btn.setDisabledIcon(btn.getIcon());
                }
// --- NUMBERS / EMPTY ---
                else {
                    // numbers or empty
                    btn.setText(t);
                }

                boolean revealed = controller.isCellRevealed(boardNumber, r, c);

                if (boardNumber == 1) {
                    if (!revealed) {
                        btn.setBackground(new Color(255, 165, 165));
                        btn.setForeground(Color.BLACK);
                        btn.setBorder(BorderFactory.createLineBorder(
                                new Color(184, 82, 82, 140), 1)); // ← lighter grid
                    } else {
                        btn.setBackground(new Color(255, 215, 215));
                        btn.setForeground(new Color(40, 40, 40));
                        btn.setBorder(BorderFactory.createLineBorder(
                                new Color(200, 150, 150, 120), 1)); // even softer
                    }
                }
                else {
                    if (!revealed) {
                        btn.setBackground(new Color(210, 230, 255));
                        btn.setBorder(BorderFactory.createLineBorder(new Color(40, 90, 160, 180), 1));
                    } else {
                        btn.setBackground(new Color(235, 235, 235));
                        btn.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120, 120), 1));
                    }
                }


            }
        }
        revalidate();
        repaint();
    }

    private void styleCellButton(JButton btn) {
        btn.setFocusable(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.CENTER);

        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(new Color(20, 20, 20));

        if (boardNumber == 1) {
            // red board grid color (a bit darker than cell)
            btn.setBorder(BorderFactory.createLineBorder(new Color(190, 120, 120, 140), 1));
            btn.setBackground(new Color(255, 165, 165));
        } else {
            // blue board grid color
            btn.setBorder(BorderFactory.createLineBorder(new Color(40, 90, 160, 180), 1));
            btn.setBackground(new Color(210, 230, 255));
        }

    }

}


