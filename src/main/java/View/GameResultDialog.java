package View;

import Controller.GameController;
import util.LanguageManager;
import util.LanguageManager.Language;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameResultDialog extends JDialog {

    public enum ResultAction {RESTART, EXIT}

    private ResultAction action = ResultAction.EXIT;

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

        Language lang = GameController.getInstance().getCurrentLanguage(); //

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

        // ===== Title =====
        TitleCard title = new TitleCard(isWin, accent, lang);
        root.add(title, BorderLayout.NORTH);

        // ===== Stats =====
        JPanel statsGrid = new JPanel(new GridBagLayout());
        statsGrid.setOpaque(false);

        int row = 0;
        row = addStatRow(statsGrid, row, LanguageManager.get("stat_score", lang), String.valueOf(summary.sharedScore));
        row = addStatRow(statsGrid, row, LanguageManager.get("stat_lives", lang), String.valueOf(summary.sharedLives));
        row = addStatRow(statsGrid, row, LanguageManager.get("stat_questions", lang), String.valueOf(summary.totalQuestions));
        row = addStatRow(statsGrid, row, LanguageManager.get("stat_correct", lang), String.valueOf(summary.correctAnswers));
        row = addStatRow(statsGrid, row, LanguageManager.get("stat_surprises", lang), String.valueOf(surprisesOpened));
        row = addStatRow(statsGrid, row, LanguageManager.get("time", lang), formatDuration(durationSeconds));
        row = addStatRow(statsGrid, row, LanguageManager.get("stat_accuracy", lang), formatAccuracy(summary.correctAnswers, summary.totalQuestions));

        NeonCard statsCard = new NeonCard(accent);
        statsCard.setLayout(new BorderLayout());
        statsCard.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        statsCard.add(statsGrid, BorderLayout.CENTER);

        root.add(statsCard, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        btnPanel.setOpaque(false);

        NeonButton btnRestart = new NeonButton(LanguageManager.get("restart", lang), true, accent);
        NeonButton btnExit = new NeonButton(LanguageManager.get("exit", lang), false, accent);

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
        setSize(640, 380); // Adjusted height for potential text wrapping
        setLocationRelativeTo(owner);
    }

    private static int addStatRow(JPanel panel, int row, String key, String value) {
        Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isHe = (lang == Language.HE); //

        JLabel k = new JLabel(key);
        k.setForeground(TEXT_MUTED);
        k.setFont(new Font("Arial", Font.BOLD, 14));
        k.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        // Align label text based on language
        if (isHe) k.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel v = new JLabel(value);
        v.setForeground(TEXT);
        v.setFont(new Font("Arial", Font.BOLD, 14));
        v.setHorizontalAlignment(isHe ? SwingConstants.LEFT : SwingConstants.RIGHT);
        v.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Column 0: Titles
        gbc.gridx = isHe ? 1 : 0;
        gbc.weightx = 0.75;
        gbc.anchor = isHe ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        panel.add(k, gbc);

        // Column 1: Values
        gbc.gridx = isHe ? 0 : 1;
        gbc.weightx = 0.25;
        gbc.anchor = isHe ? GridBagConstraints.WEST : GridBagConstraints.EAST;
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

    public static ResultAction showResultDialog(Window owner,
                                                GameController.GameSummaryDTO summary,
                                                long durationSeconds,
                                                int surprisesOpened) {
        GameResultDialog dlg = new GameResultDialog(owner, summary, durationSeconds, surprisesOpened);
        dlg.setVisible(true);
        return dlg.getAction();
    }

    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class TitleCard extends JPanel {
        private final boolean isWin;
        private final Color accent;
        private final Language lang;

        TitleCard(boolean isWin, Color accent, Language lang) {
            this.isWin = isWin;
            this.accent = accent;
            this.lang = lang;
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

            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setStroke(new BasicStroke(1.6f));
            g2.setColor(new Color(210, 220, 255, 120));
            g2.drawRoundRect(1, 1, w - 2, h - 2, arc, arc);

            // Fetch translated title
            String title = isWin ? LanguageManager.get("you_won", lang) : LanguageManager.get("game_over", lang);

            g2.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(title)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(accent);
            g2.drawString(title, tx, ty);
            g2.dispose();
        }
    }

    private static class NeonCard extends JPanel {
        private final Color accent;
        NeonCard(Color accent) { this.accent = accent; setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 28;
            paintGlow(g2, 6, 6, w - 12, h - 12, arc, accent, 0.18f);
            g2.setColor(new Color(0, 0, 0, 105));
            g2.fillRoundRect(4, 4, w - 8, h - 8, arc, arc);
            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(accent);
            g2.drawRoundRect(4, 4, w - 8, h - 8, arc, arc);
            g2.dispose();
        }
    }

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
            setPreferredSize(new Dimension(130, 42)); // Slightly wider for Hebrew text

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 18;
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