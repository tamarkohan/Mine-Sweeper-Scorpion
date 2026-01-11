package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameResultDialog extends JDialog {

    public enum ResultAction {RESTART, EXIT}

    private ResultAction action = ResultAction.EXIT; // default if user closes window


    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color WIN_ACCENT = new Color(65, 255, 240);
    private static final Color LOSE_ACCENT = new Color(255, 80, 80);

    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_MUTED = new Color(225, 230, 255);

    private GameResultDialog(Window owner,
                             GameController.GameSummaryDTO summary,
                             long durationSeconds,
                             int surprisesOpened) {
        super(owner, "Game Result", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                action = ResultAction.EXIT;
                dispose();
            }
        });

        boolean isWin = summary.isWin;
        Color accent = isWin ? WIN_ACCENT : LOSE_ACCENT;

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout(14, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // ===== Title ===== (NO neon rectangle, just clean title)
        TitleCard title = new TitleCard(isWin, accent);
        root.add(title, BorderLayout.NORTH);

        // ===== Stats =====
        JPanel statsGrid = new JPanel(new GridBagLayout());
        statsGrid.setOpaque(false);

        int row = 0;
        row = addStatRow(statsGrid, row, "Shared Score:", String.valueOf(summary.sharedScore));
        row = addStatRow(statsGrid, row, "Shared Lives:", String.valueOf(summary.sharedLives));
        row = addStatRow(statsGrid, row, "Questions Answered:", String.valueOf(summary.totalQuestions));
        row = addStatRow(statsGrid, row, "Correct Answers:", String.valueOf(summary.correctAnswers));
        row = addStatRow(statsGrid, row, "Surprises Opened:", String.valueOf(surprisesOpened));
        row = addStatRow(statsGrid, row, "Time:", formatDuration(durationSeconds));
        row = addStatRow(statsGrid, row, "Accuracy:", formatAccuracy(summary.correctAnswers, summary.totalQuestions));


        NeonCard statsCard = new NeonCard(accent);
        statsCard.setLayout(new BorderLayout());
        statsCard.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        statsCard.add(statsGrid, BorderLayout.CENTER);

        root.add(statsCard, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        btnPanel.setOpaque(false);

        NeonButton btnRestart = new NeonButton("Restart", true, accent);
        NeonButton btnExit = new NeonButton("Exit", false, accent);

        btnRestart.addActionListener(e -> {
            action = ResultAction.RESTART;
            dispose();
        });
        btnExit.addActionListener(e -> {
            action = ResultAction.EXIT;
            dispose();
        });

        btnPanel.add(btnRestart);
        btnPanel.add(btnExit);

        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setResizable(false);
        setSize(640, 360);
        setLocationRelativeTo(owner);
    }

    private static int addStatRow(JPanel panel, int row, String key, String value) {
        // Left label (title)
        JLabel k = new JLabel(key);
        k.setForeground(TEXT_MUTED);
        k.setFont(new Font("Arial", Font.BOLD, 14));
        k.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0)); // fixes top/bottom clipping

        // Right label (value) — keep it close, not at the far edge
        JLabel v = new JLabel(value);
        v.setForeground(TEXT);
        v.setFont(new Font("Arial", Font.BOLD, 14));
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0)); // fixes top/bottom clipping

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Column 0: title gets most width
        gbc.gridx = 0;
        gbc.weightx = 0.75;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(k, gbc);

        // Column 1: value gets less width, stays near titles
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(v, gbc);

        return row + 1;
    }

    private static String formatDuration(long seconds) {
        if (seconds <= 0) return "00:00";
        long m = seconds / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private static String formatAccuracy(int correct, int total) {
        if (total <= 0) return "0%";
        double p = (correct * 100.0) / total;
        return String.format("%.0f%%", p);
    }

    public ResultAction getAction() {
        return action;
    }

    // NEW signature (adds duration + surprisesOpened)
    public static ResultAction showResultDialog(Window owner,
                                                GameController.GameSummaryDTO summary,
                                                long durationSeconds,
                                                int surprisesOpened) {
        GameResultDialog dlg = new GameResultDialog(owner, summary, durationSeconds, surprisesOpened);
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
    // Title Card (NO neon rectangle)
    // =========================
    private static class TitleCard extends JPanel {
        private final boolean isWin;
        private final Color accent;

        TitleCard(boolean isWin, Color accent) {
            this.isWin = isWin;
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(640, 72));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 26;

            // subtle dark card (no glow, no neon border)
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // simple thin border (neutral)
            g2.setStroke(new BasicStroke(1.6f));
            g2.setColor(new Color(210, 220, 255, 120));
            g2.drawRoundRect(1, 1, w - 2, h - 2, arc, arc);

            String title = isWin ? "YOU WON" : "GAME OVER";

            // title text in accent (red when lose, turquoise when win)
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g2.getFontMetrics();

            int tx = (w - fm.stringWidth(title)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(accent);
            g2.drawString(title, tx, ty);

            g2.dispose();
        }
    }

    // =========================
    // Neon Card (stats only)
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

            paintGlow(g2, 6, 6, w - 12, h - 12, arc, accent, 0.18f);

            g2.setColor(new Color(0, 0, 0, 105));
            g2.fillRoundRect(4, 4, w - 8, h - 8, arc, arc);

            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(accent);
            g2.drawRoundRect(4, 4, w - 8, h - 8, arc, arc);

            g2.dispose();
        }
    }

    // =========================
    // Buttons
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
            setPreferredSize(new Dimension(120, 42));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
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

            paintGlow(g2, 6, 6, w - 12, h - 12, arc, border, primary ? 0.18f : 0.10f);

            g2.setColor(fill);
            g2.fillRoundRect(4, 4, w - 8, h - 8, arc, arc);

            g2.setStroke(new BasicStroke(primary ? 2.4f : 2.0f));
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
    // Glow helper
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
