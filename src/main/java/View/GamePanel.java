package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main in-game panel: shows two boards, players, score, lives, controls.
 * מדבר רק עם GameController – בלי Model ישיר.
 */
public class GamePanel extends JPanel {

    private final GameController controller;

    private final String player1Name;
    private final String player2Name;

    private BoardPanel boardPanel1;
    private BoardPanel boardPanel2;

    // Top labels
    private JLabel lblTitle;
    private JLabel lblLevel;

    // Player area labels
    private JLabel lblPlayer1Box;
    private JLabel lblPlayer2Box;
    private JLabel lblMinesLeft1;
    private JLabel lblMinesLeft2;

    // Bottom status
    private JLabel lblScore;
    private JLabel lblLives;
    private JPanel heartsPanel;
    private List<JLabel> heartLabels;

    public GamePanel(GameController controller,
                     String player1Name, String player2Name) {
        this.controller = controller;
        this.player1Name = player1Name;
        this.player2Name = player2Name;

        initComponents();
        updateStatus();
        updateTurnUI();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        String levelName = controller.getDifficultyName();

        // ===== TOP: title + level =====
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.BLACK);

        lblTitle = new JLabel("SCORPION MINESWEEPER", SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblLevel = new JLabel("LEVEL:  " + levelName, SwingConstants.CENTER);
        lblLevel.setForeground(Color.WHITE);
        lblLevel.setFont(new Font("Arial", Font.BOLD, 20));
        lblLevel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(lblTitle);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(lblLevel);
        topPanel.add(Box.createVerticalStrut(10));

        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: two player panels =====
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        centerPanel.setBackground(Color.BLACK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // ----- Player 1 side -----
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
        leftSide.setBackground(Color.BLACK);

        lblPlayer1Box = createPlayerBoxLabel(player1Name);
        leftSide.add(lblPlayer1Box);

        leftSide.add(Box.createVerticalStrut(5));

        lblMinesLeft1 = new JLabel("MINES LEFT: " + controller.getTotalMines(1),
                SwingConstants.CENTER);
        lblMinesLeft1.setForeground(Color.WHITE);
        lblMinesLeft1.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft1.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftSide.add(lblMinesLeft1);

        leftSide.add(Box.createVerticalStrut(5));

        // Player 1 starts → not waiting
        boardPanel1 = new BoardPanel(controller, 1, false, this::handleMoveMade);
        leftSide.add(boardPanel1);

        // ----- Player 2 side -----
        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBackground(Color.BLACK);

        lblPlayer2Box = createPlayerBoxLabel(player2Name);
        rightSide.add(lblPlayer2Box);

        rightSide.add(Box.createVerticalStrut(5));

        lblMinesLeft2 = new JLabel("MINES LEFT: " + controller.getTotalMines(2),
                SwingConstants.CENTER);
        lblMinesLeft2.setForeground(Color.WHITE);
        lblMinesLeft2.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft2.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightSide.add(lblMinesLeft2);

        rightSide.add(Box.createVerticalStrut(5));

        // Player 2 waits at start
        boardPanel2 = new BoardPanel(controller, 2, true, this::handleMoveMade);
        rightSide.add(boardPanel2);

        centerPanel.add(leftSide);
        centerPanel.add(rightSide);

        add(centerPanel, BorderLayout.CENTER);

        // ===== BOTTOM: score + lives + hearts + controls =====
        JPanel bottomOuter = new JPanel();
        bottomOuter.setLayout(new BoxLayout(bottomOuter, BoxLayout.Y_AXIS));
        bottomOuter.setBackground(Color.BLACK);

        // Score & lives line
        JPanel scoreLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        scoreLivesPanel.setBackground(Color.BLACK);

        lblScore = new JLabel("SCORE: 0");
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));

        lblLives = new JLabel("LIVES: " + controller.getSharedLives() + "/" +
                controller.getStartingLives());
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(new Font("Arial", Font.BOLD, 18));

        scoreLivesPanel.add(lblScore);
        scoreLivesPanel.add(lblLives);

        bottomOuter.add(scoreLivesPanel);

        // Hearts row
        heartsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        heartsPanel.setBackground(Color.BLACK);
        buildHearts();

        bottomOuter.add(heartsPanel);

        // Control buttons (pause, restart, exit)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
        controlsPanel.setBackground(Color.BLACK);

        JButton btnPause = new JButton("Pause");
        JButton btnRestart = new JButton("Restart");
        JButton btnExit = new JButton("Exit");

        styleControlButton(btnPause);
        styleControlButton(btnRestart);
        styleControlButton(btnExit);

        btnExit.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        btnRestart.addActionListener(e -> {
            controller.restartGame();
            // Rebuild hearts and status after restart
            buildHearts();
            updateStatus();
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
        });

        controlsPanel.add(btnPause);
        controlsPanel.add(btnRestart);
        controlsPanel.add(btnExit);

        bottomOuter.add(Box.createVerticalStrut(5));
        bottomOuter.add(controlsPanel);
        bottomOuter.add(Box.createVerticalStrut(10));

        add(bottomOuter, BorderLayout.SOUTH);
    }

    private JLabel createPlayerBoxLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(60, 60, 80));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        return lbl;
    }

    private void styleControlButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(3, 12, 3, 12));
    }

    private void buildHearts() {
        heartLabels = new ArrayList<>();
        int maxLives = controller.getStartingLives();

        heartsPanel.removeAll();
        for (int i = 0; i < maxLives; i++) {
            JLabel heart = new JLabel("❤");
            heart.setFont(new Font("Dialog", Font.PLAIN, 22));
            heart.setForeground(Color.RED);
            heartLabels.add(heart);
            heartsPanel.add(heart);
        }
        heartsPanel.revalidate();
        heartsPanel.repaint();
    }

    /** Called after each move from a BoardPanel. */
    private void handleMoveMade() {
        if (controller.isGameRunning()) {
            controller.switchTurn();
        }
        updateStatus();
        updateTurnUI();
    }

    /** Refresh SCORE, LIVES, MINES LEFT, HEARTS. */
    public void updateStatus() {
        lblMinesLeft1.setText("MINES LEFT: " + controller.getMinesLeft(1));
        lblMinesLeft2.setText("MINES LEFT: " + controller.getMinesLeft(2));

        lblScore.setText("SCORE: " + controller.getSharedScore());
        lblLives.setText("LIVES: " + controller.getSharedLives() + "/" +
                controller.getStartingLives());

        updateHearts();
        revalidate();
        repaint();
    }

    /** Show “WAIT FOR YOUR TURN” on the board that is not active. */
    private void updateTurnUI() {
        int current = controller.getCurrentPlayerTurn();  // 1 or 2
        boardPanel1.setWaiting(current != 1);
        boardPanel2.setWaiting(current != 2);
    }

    private void updateHearts() {
        int lives = controller.getSharedLives();
        int max = controller.getStartingLives();

        for (int i = 0; i < max && i < heartLabels.size(); i++) {
            JLabel heart = heartLabels.get(i);
            if (i < lives) {
                heart.setForeground(Color.RED);
            } else {
                heart.setForeground(Color.DARK_GRAY);
            }
        }
    }
}