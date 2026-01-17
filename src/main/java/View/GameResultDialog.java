package View;

import Controller.GameController;
import util.LanguageManager;
import util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameResultDialog extends JDialog {

    public enum ResultAction {
        RESTART, EXIT, CLOSE
    }

    private ResultAction action = ResultAction.CLOSE;

    // Colors to match the game theme
    private static final Color BG_COLOR = new Color(10, 18, 35);
    private static final Color WIN_COLOR = new Color(80, 255, 120);
    private static final Color LOSE_COLOR = new Color(255, 80, 80);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color STAT_LABEL_COLOR = new Color(180, 190, 210);

    // Fonts (Arial supports Hebrew characters properly)
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 32);
    private static final Font STAT_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font VALUE_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font BTN_FONT = new Font("Arial", Font.BOLD, 14);

    private GameResultDialog(Window owner, GameController.GameSummaryDTO summary, long durationSeconds, int surprisesOpened) {
        super(owner, "", ModalityType.APPLICATION_MODAL);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isHe = (lang == LanguageManager.Language.HE);
        boolean isWin = summary.isWin;
        SwingUtilities.invokeLater(() -> {
            if (isWin) SoundManager.winGame();
            else SoundManager.loseGame();
        });

        // 1. Determine Title Text
        String titleText;
        if (isWin) {
            titleText = isHe ? "ניצחון!" : "VICTORY!";
        } else {
            // UPDATED HERE:
            titleText = isHe ? "הפסד" : "GAME OVER";
        }
        setTitle(titleText);

        // 2. Setup Main Container
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isWin ? WIN_COLOR : LOSE_COLOR, 3),
                BorderFactory.createEmptyBorder(25, 40, 25, 40)
        ));

        // 3. Title Label
        JLabel lblTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(isWin ? WIN_COLOR : LOSE_COLOR);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(20));

        // 4. Stats Panel (Grid Layout)
        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 15, 8));
        statsPanel.setOpaque(false);

        // Define Labels
        String txtScore = isHe ? "ניקוד סופי:" : "Final Score:";
        String txtTime = isHe ? "זמן משחק:" : "Time:";
        String txtLives = isHe ? "חיים שנותרו:" : "Lives Left:";
        String txtSurprises = isHe ? "הפתעות שנפתחו:" : "Surprises:";
        String txtQuestions = isHe ? "תשובות נכונות:" : "Correct Answers:";

        // Add Stats Rows
        addStatRow(statsPanel, txtScore, String.valueOf(summary.sharedScore), isHe);
        addStatRow(statsPanel, txtLives, String.valueOf(summary.sharedLives), isHe);

        long mins = durationSeconds / 60;
        long secs = durationSeconds % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);
        addStatRow(statsPanel, txtTime, timeStr, isHe);

        addStatRow(statsPanel, txtSurprises, String.valueOf(surprisesOpened), isHe);
        addStatRow(statsPanel, txtQuestions, summary.correctAnswers + " / " + summary.totalQuestions, isHe);

        // Wrapper to center the grid
        JPanel statsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statsWrapper.setOpaque(false);
        statsWrapper.add(statsPanel);

        content.add(statsWrapper);
        content.add(Box.createVerticalStrut(25));

        // 5. Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        String txtRestart = isHe ? "משחק חוזר" : "Play Again";
        String txtExit = isHe ? "יציאה לתפריט" : "Main Menu";

        JButton btnRestart = createStyledButton(txtRestart, WIN_COLOR);
        JButton btnExit = createStyledButton(txtExit, new Color(200, 200, 200));

        btnRestart.addActionListener(e -> {
            action = ResultAction.RESTART;
            dispose();
        });

        btnExit.addActionListener(e -> {
            action = ResultAction.EXIT;
            dispose();
        });

        btnPanel.add(btnExit);
        btnPanel.add(btnRestart);

        content.add(btnPanel);

        // 6. Finish Dialog Setup
        setContentPane(content);
        setUndecorated(true);
        pack(); // Auto-size to fit content
        setLocationRelativeTo(owner);

        // Safety check for width
        if (getWidth() < 350) {
            setSize(350, getHeight());
            setLocationRelativeTo(owner);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (action == ResultAction.CLOSE) action = ResultAction.EXIT;
            }
        });
    }

    private void addStatRow(JPanel panel, String label, String value, boolean isHe) {
        JLabel l = new JLabel(label);
        l.setFont(STAT_FONT);
        l.setForeground(STAT_LABEL_COLOR);

        JLabel v = new JLabel(value);
        v.setFont(VALUE_FONT);
        v.setForeground(TEXT_COLOR);

        if (isHe) {
            l.setHorizontalAlignment(SwingConstants.RIGHT);
            v.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(v);
            panel.add(l);
        } else {
            l.setHorizontalAlignment(SwingConstants.LEFT);
            v.setHorizontalAlignment(SwingConstants.RIGHT);
            panel.add(l);
            panel.add(v);
        }
    }

    private JButton createStyledButton(String text, Color borderColor) {
        JButton btn = new JButton(text);
        btn.setFont(BTN_FONT);
        btn.setForeground(borderColor);
        btn.setBackground(new Color(30, 30, 40));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(50, 50, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(30, 30, 40));
            }
        });

        return btn;
    }

    public static ResultAction showResultDialog(Window owner, GameController.GameSummaryDTO summary, long durationSeconds, int surprisesOpened) {
        GameResultDialog dlg = new GameResultDialog(owner, summary, durationSeconds, surprisesOpened);
        dlg.setVisible(true);
        return dlg.action;
    }
}