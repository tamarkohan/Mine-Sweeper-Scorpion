package View;

import Model.Game;
import Model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameResultDialog extends JDialog {

    public enum ResultAction { RESTART, EXIT, CLOSE }
    private ResultAction action = ResultAction.CLOSE;

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color WIN_ACCENT = new Color(65, 255, 240);
    private static final Color LOSE_ACCENT = new Color(255, 80, 80);

    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_MUTED = new Color(225, 230, 255);

    private GameResultDialog(Window owner, Game game) {
        super(owner, "Game Result", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        boolean isWin = game.getGameState() == GameState.WON;
        Color accent = isWin ? WIN_ACCENT : LOSE_ACCENT;

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout(14, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // ===== Title =====
        TitleCard title = new TitleCard(isWin, accent);
        root.add(title, BorderLayout.NORTH);

        // ===== Stats =====
        JPanel statsGrid = new JPanel(new GridLayout(4, 2, 10, 10));
        statsGrid.setOpaque(false);

        addStat(statsGrid, "Shared Score", String.valueOf(game.getSharedScore()));
        addStat(statsGrid, "Shared Lives", String.valueOf(game.getSharedLives()));
        addStat(statsGrid, "Questions Answered", String.valueOf(game.getTotalQuestionsAnswered()));
        addStat(statsGrid, "Correct Answers", String.valueOf(game.getTotalCorrectAnswers()));

        NeonCard statsCard = new NeonCard(accent);
        statsCard.setLayout(new BorderLayout());
        statsCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        statsCard.add(statsGrid, BorderLayout.CENTER);

        root.add(statsCard, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        NeonButton btnClose = new NeonButton("Close", false, accent);
        NeonButton btnRestart = new NeonButton("Restart", true, accent);
        NeonButton btnExit = new NeonButton("Exit", false, accent);

        btnRestart.addActionListener(e -> { action = ResultAction.RESTART; dispose(); });
        btnExit.addActionListener(e -> { action = ResultAction.EXIT; dispose(); });
        btnClose.addActionListener(e -> { action = ResultAction.CLOSE; dispose(); });

        btnPanel.add(btnClose);
        btnPanel.add(btnRestart);
        btnPanel.add(btnExit);

        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setResizable(false);
        setSize(520, 340);
        setLocationRelativeTo(owner);
    }

    private static void addStat(JPanel grid, String key, String value) {
        JLabel k = new JLabel(key + ":");
        k.setForeground(TEXT_MUTED);
        k.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel v = new JLabel(value);
        v.setForeground(TEXT);
        v.setFont(new Font("Arial", Font.BOLD, 14));
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        grid.add(k);
        grid.add(v);
    }

    public ResultAction getAction() { return action; }

    public static ResultAction showResultDialog(Window owner, Game game) {
        GameResultDialog dlg = new GameResultDialog(owner, game);
        dlg.setVisible(true);
        return dlg.getAction();
    }

    // =========================
    // Background
    // =========================
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOTTOM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    // =========================
    // Title Card (Glow + icon)
    // =========================
    private static class TitleCard extends JPanel {
        private final boolean isWin;
        private final Color accent;

        TitleCard(boolean isWin, Color accent) {
            this.isWin = isWin;
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(480, 90));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 36;

            // glow
            paintGlow(g2, 8, 8, w - 16, h - 16, arc, accent, 0.35f);

            // card
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(6, 6, w - 12, h - 12, arc, arc);

            // border
            g2.setStroke(new BasicStroke(2.8f));
            g2.setColor(accent);
            g2.drawRoundRect(6, 6, w - 12, h - 12, arc, arc);

            String title = isWin ? "YOU WON" : "GAME OVER";
            String icon = isWin ? "\uD83C\uDFC6" : "\uD83D\uDCA5"; // 🏆 / 💥

            g2.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();

            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            int iconW = fm.stringWidth(icon);
            int titleW = fm.stringWidth(title);

            int totalW = iconW + 10 + titleW;
            int x = (w - totalW) / 2;

            g2.setColor(TEXT);
            g2.drawString(icon, x, y);
            g2.drawString(title, x + iconW + 10, y);

            g2.dispose();
        }
    }

    // =========================
    // Neon Card
    // =========================
    private static class NeonCard extends JPanel {
        private final Color accent;

        NeonCard(Color accent) {
            this.accent = accent;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 28;

            paintGlow(g2, 6, 6, w - 12, h - 12, arc, accent, 0.25f);

            g2.setColor(new Color(0, 0, 0, 105));
            g2.fillRoundRect(4, 4, w - 8, h - 8, arc, arc);

            g2.setStroke(new BasicStroke(2.4f));
            g2.setColor(accent);
            g2.drawRoundRect(4, 4, w - 8, h - 8, arc, arc);

            g2.dispose();
        }
    }

    // =========================
    // Neon Button with hover
    // =========================
    private static class NeonButton extends JButton {
        private final boolean primary;
        private final Color accent;
        private boolean hover = false;

        NeonButton(String text, boolean primary, Color accent) {
            super(text);
            this.primary = primary;
            this.accent = accent;

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(TEXT);
            setFont(new Font("Arial", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 40));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 18;

            Color border = primary ? accent : new Color(210, 220, 255);
            Color fill = primary ? new Color(15, 40, 80) : new Color(20, 30, 60);
            if (hover) fill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 220);

            paintGlow(g2, 6, 6, w - 12, h - 12, arc, border, primary ? 0.22f : 0.12f);

            g2.setColor(fill);
            g2.fillRoundRect(4, 4, w - 8, h - 8, arc, arc);

            g2.setStroke(new BasicStroke(primary ? 2.6f : 2.0f));
            g2.setColor(border);
            g2.drawRoundRect(4, 4, w - 8, h - 8, arc, arc);

            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(TEXT);
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    // =========================
    // Glow Painter Helper
    // =========================
    private static void paintGlow(Graphics2D g2, int x, int y, int w, int h, int arc, Color c, float alpha) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));

        g2.setColor(c);
        for (int i = 10; i >= 1; i--) {
            g2.setStroke(new BasicStroke(i * 2f));
            g2.drawRoundRect(x, y, w, h, arc, arc);
        }

        g2.setComposite(old);
    }
}
