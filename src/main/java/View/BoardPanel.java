package View;

import Controller.GameController;
import util.LanguageManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel {

    private final GameController controller;
    private final int boardNumber;

    public interface MoveCallback {
        void onMove(boolean endedTurn);
    }

    private static final int MIN_CELL = 18;
    private int cellSize = 40;
    private Font cachedCellFont;

    private final MoveCallback moveCallback;
    private JButton[][] buttons;
    private boolean waiting;

    public enum EffectType {REVEAL_3X3, REVEAL_1_MINE}

    public EffectType pendingEffect = null;
    private boolean[][] prevRevealed;
    private final java.util.Map<Point, Long> animStart = new java.util.HashMap<>();
    private javax.swing.Timer animTimer;

    public BoardPanel(GameController controller, int boardNumber, boolean initiallyWaiting, MoveCallback moveCallback) {
        this.controller = controller;
        this.boardNumber = boardNumber;
        this.waiting = initiallyWaiting;
        this.moveCallback = moveCallback;
        initComponents();
    }

    public void setCellSize(int newSize) {
        int maxCell = 120;
        newSize = Math.max(MIN_CELL, Math.min(newSize, maxCell));
        this.cellSize = newSize;
        this.cachedCellFont = new Font("Segoe UI Black", Font.BOLD, (int) (cellSize * 0.6));

        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);
        Dimension pref = new Dimension(cols * cellSize, rows * cellSize);
        setPreferredSize(pref);
        setMinimumSize(new Dimension(cols * MIN_CELL, rows * MIN_CELL));
        setMaximumSize(pref);

        if (buttons != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    buttons[r][c].setPreferredSize(new Dimension(cellSize, cellSize));
                    buttons[r][c].setFont(this.cachedCellFont);
                }
            }
        }
        revalidate();
        repaint();
    }

    private void initComponents() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);
        int maxDim = Math.max(rows, cols);
        this.cellSize = (maxDim <= 9) ? 48 : (maxDim <= 13 ? 42 : 30);
        this.cachedCellFont = new Font("Segoe UI Black", Font.BOLD, (int) (cellSize * 0.6));

        setLayout(new GridLayout(rows, cols, 0, 0));
        setOpaque(false);
        setDoubleBuffered(true);

        Dimension pref = new Dimension(cols * this.cellSize, rows * this.cellSize);
        setPreferredSize(pref);
        setMinimumSize(new Dimension(cols * MIN_CELL, rows * MIN_CELL));

        buttons = new JButton[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int rr = r;
                final int cc = c;
                JButton btn = new CellButton();
                btn.setMargin(new Insets(0, 0, 0, 0));
                styleCellButton(btn);
                btn.setPreferredSize(new Dimension(this.cellSize, this.cellSize));
                btn.setFocusPainted(false);
                btn.setFont(this.cachedCellFont);

                btn.addActionListener(e -> handleClick(rr, cc, false));
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) handleClick(rr, cc, true);
                    }
                });
                buttons[r][c] = btn;
                add(btn);
            }
        }
        prevRevealed = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                prevRevealed[r][c] = controller.isCellRevealed(boardNumber, r, c);
            }
        }
        refresh();
    }

    public void queueEffect(EffectType type) {
        this.pendingEffect = type;
    }

    private void handleClick(int r, int c, boolean isFlagging) {
        if (!controller.isGameRunning()) return;
        if (controller.getCurrentPlayerTurn() != boardNumber) return;
        if (waiting) return;

        boolean endedTurn = false;
        boolean stateChanged = false;

        if (isFlagging) {
            if (!controller.isCellRevealed(boardNumber, r, c)) {
                boolean ok = controller.toggleFlagUI(boardNumber, r, c);
                stateChanged = true;
                if (!ok) {
                    String msg = controller.getAndClearLastActionMessage();
                    if (msg != null && !msg.isBlank()) {
                        OutcomeDialog.show(SwingUtilities.getWindowAncestor(this), "FLAGS", msg);
                    }
                }
            }
        } else {
            boolean wasRevealed = controller.isCellRevealed(boardNumber, r, c);
            if (wasRevealed && !controller.isQuestionOrSurprise(boardNumber, r, c)) return;

            boolean revealedNow = false;
            if (!wasRevealed) {
                controller.revealCellUI(boardNumber, r, c);
                revealedNow = true;
                stateChanged = true;
            }

            if (controller.isQuestionOrSurprise(boardNumber, r, c)) {
                GameController.CellViewData d = controller.getCellViewData(boardNumber, r, c);
                if (d.enabled) {
                    refresh();
                    String type = controller.isQuestionCell(boardNumber, r, c) ? "Question" : "Surprise";
                    if (ActivationConfirmDialog.show(SwingUtilities.getWindowAncestor(this), type)) {
                        endedTurn = controller.activateSpecialCellUI(boardNumber, r, c);
                        stateChanged = true;
                    } else {
                        endedTurn = revealedNow;
                    }
                }
            } else {
                endedTurn = revealedNow;
            }
        }

        if (stateChanged || endedTurn) {
            if (moveCallback != null) {
                moveCallback.onMove(endedTurn);
            } else {
                refresh();
            }
        }
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
        repaint();
    }

    public void refresh() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);
        boolean gameIsRunning = controller.isGameRunning();
        java.util.Set<Point> newlyRevealed = null;

        if (pendingEffect != null && prevRevealed != null) {
            newlyRevealed = new java.util.HashSet<>();
            for (int rr = 0; rr < rows; rr++) {
                for (int cc = 0; cc < cols; cc++) {
                    boolean nowRev = controller.isCellRevealed(boardNumber, rr, cc);
                    if (nowRev && !prevRevealed[rr][cc]) {
                        if (pendingEffect == EffectType.REVEAL_1_MINE) {
                            GameController.CellViewData d = controller.getCellViewData(boardNumber, rr, cc);
                            if ("M".equals(d.text)) newlyRevealed.add(new Point(rr, cc));
                        } else {
                            newlyRevealed.add(new Point(rr, cc));
                        }
                    }
                }
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = buttons[r][c];
                GameController.CellViewData data = controller.getCellViewData(boardNumber, r, c);
                boolean revealed = controller.isCellRevealed(boardNumber, r, c);

                if (boardNumber == 1) {
                    btn.setBackground(revealed ? new Color(255, 215, 215) : new Color(255, 165, 165));
                    btn.setBorder(BorderFactory.createLineBorder(revealed ? new Color(200, 150, 150, 120) : new Color(184, 82, 82, 140), 1));
                } else {
                    btn.setBackground(revealed ? new Color(235, 235, 235) : new Color(210, 230, 255));
                    btn.setBorder(BorderFactory.createLineBorder(revealed ? new Color(120, 120, 120, 120) : new Color(40, 90, 160, 180), 1));
                }

                btn.setForeground(new Color(40, 40, 40));
                btn.setText("");
                btn.setIcon(null);
                btn.setDisabledIcon(null);

                String t = data.text;
                if ("🚩".equals(t)) {
                    btn.setIcon(IconCache.icon("/ui/cells/flag.png", (int) (cellSize * 0.80)));
                    btn.setDisabledIcon(btn.getIcon());
                } else if ("M".equals(t)) {
                    btn.setIcon(IconCache.icon("/ui/cells/mine.png", (int) (cellSize * 0.85)));
                    btn.setDisabledIcon(btn.getIcon());
                } else if ("Q".equals(t)) {
                    Icon icon = IconCache.icon("/ui/cells/question.png", (int) (cellSize * 0.82));
                    btn.setIcon(icon);
                    btn.setDisabledIcon(icon);
                } else if ("S".equals(t)) {
                    Icon icon = IconCache.icon("/ui/cells/surprise_btn.png", (int) (cellSize * 0.82));
                    btn.setIcon(icon);
                    btn.setDisabledIcon(icon);
                } else {
                    btn.setText(t);
                    if (!t.isEmpty() && Character.isDigit(t.charAt(0))) {
                        int val = Integer.parseInt(t);
                        btn.setForeground(getNumberColor(val));
                        btn.setFont(this.cachedCellFont);
                    }
                }

                btn.setEnabled(gameIsRunning && !waiting);

                boolean usedSpecial = (revealed && ("Q".equals(t) || "S".equals(t)) && !data.enabled);
                if (usedSpecial) markUsedSpecial(btn, "Q".equals(t));
                else clearUsedSpecial(btn);

                Point key = new Point(r, c);
                if (animStart.containsKey(key)) {
                    long start = animStart.get(key);
                    float tt = animPhase(start);
                    float pulse = (tt < 0.5f) ? (tt / 0.5f) : ((1f - tt) / 0.5f);
                    Color neon = (boardNumber == 1) ? new Color(255, 60, 60) : new Color(80, 180, 255);
                    btn.setBorder(BorderFactory.createLineBorder(new Color(neon.getRed(), neon.getGreen(), neon.getBlue(), 130 + Math.round(120 * pulse)), 2 + Math.round(4 * pulse)));
                }
            }
        }

        for (int rr = 0; rr < rows; rr++) {
            for (int cc = 0; cc < cols; cc++) {
                prevRevealed[rr][cc] = controller.isCellRevealed(boardNumber, rr, cc);
            }
        }
        if (pendingEffect != null && newlyRevealed != null && !newlyRevealed.isEmpty())
            startPulseAnimation(newlyRevealed);
        pendingEffect = null;
        revalidate();
        repaint();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        if (waiting && controller.isGameRunning()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margin = 5;
            int w = getWidth() - 2 * margin;
            int h = getHeight() - 2 * margin;

            g2.setColor(new Color(230, 230, 230, 210));
            g2.fillRoundRect(margin, margin, w, h, 20, 20);

            LanguageManager.Language lang = controller.getCurrentLanguage();
            String text = LanguageManager.get("wait_turn", lang);

            g2.setFont(new Font("Arial", Font.BOLD, 18));
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
    }

    private void startPulseAnimation(java.util.Set<Point> cells) {
        long now = System.currentTimeMillis();
        for (Point p : cells) animStart.put(p, now);
        if (animTimer == null) {
            animTimer = new javax.swing.Timer(30, e -> {
                for (Point p : animStart.keySet()) buttons[p.x][p.y].repaint();
                long t = System.currentTimeMillis();
                animStart.entrySet().removeIf(en -> (t - en.getValue()) > 900);
                if (animStart.isEmpty()) ((javax.swing.Timer) e.getSource()).stop();
            });
        }
        if (!animTimer.isRunning()) animTimer.start();
    }

    private float animPhase(long startMs) {
        float dt = (System.currentTimeMillis() - startMs) / 900f;
        return Math.max(0f, Math.min(1f, dt));
    }

    private Color getNumberColor(int value) {
        return switch (value) {
            case 1 -> new Color(0, 0, 255);
            case 2 -> new Color(0, 128, 0);
            case 3 -> new Color(255, 0, 0);
            case 4 -> new Color(128, 0, 128);
            case 5 -> new Color(128, 0, 0);
            case 6 -> new Color(0, 128, 128);
            case 7 -> new Color(0, 0, 0);
            case 8 -> new Color(128, 128, 128);
            default -> Color.BLACK;
        };
    }

    private void styleCellButton(JButton btn) {
        btn.setFocusable(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(new Color(20, 20, 20));
        if (boardNumber == 1) {
            btn.setBorder(BorderFactory.createLineBorder(new Color(190, 120, 120, 140), 1));
            btn.setBackground(new Color(255, 165, 165));
        } else {
            btn.setBorder(BorderFactory.createLineBorder(new Color(40, 90, 160, 180), 1));
            btn.setBackground(new Color(210, 230, 255));
        }
    }

    private void markUsedSpecial(JButton btn, boolean isQuestion) {
        btn.setCursor(Cursor.getDefaultCursor());
        Color base = btn.getBackground();
        btn.setBackground(new Color(Math.max(0, base.getRed() - 25), Math.max(0, base.getGreen() - 25), Math.max(0, base.getBlue() - 25)));
        btn.setBorder(BorderFactory.createDashedBorder(new Color(180, 180, 180, 170), 3f, 5f));
        btn.putClientProperty("USED_SPECIAL", Boolean.TRUE);
    }

    private void clearUsedSpecial(JButton btn) {
        btn.putClientProperty("USED_SPECIAL", null);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private class CellButton extends JButton {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (Boolean.TRUE.equals(getClientProperty("USED_SPECIAL"))) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(new Color(255, 255, 255, 180));
                int pad = 10;
                g2.drawLine(pad, pad, getWidth() - pad, getHeight() - pad);
                g2.drawLine(getWidth() - pad, pad, pad, getHeight() - pad);
                g2.dispose();
            }
        }
    }
}