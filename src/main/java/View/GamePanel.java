package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;



/**
 * Main in-game panel: displays two boards, player info, score, lives and controls.
 * Communicates only with GameController (no direct access to the Model layer).
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

    // Control Buttons
    private JButton btnPause;
    private JButton btnRestart;
    private JButton btnExit;
    private final long startTimeMillis;


    public GamePanel(GameController controller,
                     String player1Name, String player2Name) {
        this.controller = controller;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.startTimeMillis = System.currentTimeMillis();


        // Register the question presenter so QUESTION cells will show a popup.
        controller.registerQuestionPresenter(question ->
                QuestionDialog.showQuestionDialog(SwingUtilities.getWindowAncestor(this), question));

        initComponents();
        updateStatus();
        updateTurnUI();
    }
    /**
     * Builds the UI layout: title, two player areas, and bottom status/control area.
     */
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
                controller.getMaxLives());
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

        btnPause = new JButton("Pause");
        btnRestart = new JButton("Restart");
        btnExit = new JButton("Exit");

        styleControlButton(btnPause);
        styleControlButton(btnRestart);
        styleControlButton(btnExit);

        btnExit.addActionListener(e -> {
            // stop or reset game state if needed
            controller.endGame();

            // go back to menu
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof MainFrame frame) {
                frame.showMainMenu();
            }
        });

        btnRestart.addActionListener(e -> {
            controller.restartGame();
            buildHearts();
            updateStatus();
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
            btnPause.setEnabled(true); // Re-enable pause on restart
        });

        controlsPanel.add(btnPause);
        controlsPanel.add(btnRestart);
        controlsPanel.add(btnExit);

        bottomOuter.add(Box.createVerticalStrut(5));
        bottomOuter.add(controlsPanel);
        bottomOuter.add(Box.createVerticalStrut(10));

        add(bottomOuter, BorderLayout.SOUTH);
    }
    /**
     * Creates a styled label for a player's name header box.
     */
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
    /**
     * Applies consistent styling to control buttons.
     */
    private void styleControlButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(3, 12, 3, 12));
    }
    /**
     * Builds the row of heart icons according to the starting number of lives.
     */
    private void buildHearts() {
        heartLabels = new ArrayList<>();
        int maxLives = controller.getMaxLives();

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

    /**
     * Called after each move from a BoardPanel.
     * Updates status, handles game over, and switches turns when appropriate.
     */
    // endedTurn = true → revealed a cell, switch player (after small delay)
// endedTurn = false → only flag, same player continues
    private void handleMoveMade(boolean endedTurn) {
        updateStatus();

        String outcomeMessage = controller.getAndClearLastActionMessage();
        if (outcomeMessage != null) {
            displayOutcomePopup(outcomeMessage);
        }

        if (controller.isGameOver()) {
            handleGameOverUI();
            return;
        }

        if (endedTurn && controller.isGameRunning()) {
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();

            Timer delayTimer = new Timer(500, e -> {
                controller.processTurnEnd();
                updateTurnUI();
                boardPanel1.refresh();
                boardPanel2.refresh();
            });
            delayTimer.setRepeats(false);
            delayTimer.start();

        } else {
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
        }
    }



    /**
     * Displays a dialog at the end of the game (victory or game over) with final score.
     */
    private void showGameOverDialog() {
        String title;
        String message;

        // אם אין יותר לבבות – הפסד
        if (controller.getSharedLives() <= 0) {
            title = "Game Over";
            message = "All lives are gone.\nFinal score: " + controller.getSharedScore();
        } else {
            // אחרת – הנחנו שכל הלוחות נפתרו -> ניצחון
            title = "Victory!";
            message = "All safe cells are revealed!\nFinal score: " + controller.getSharedScore();
        }

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Updates score, lives, mines-left labels and heart colors.
     */
    public void updateStatus() {
        lblMinesLeft1.setText("MINES LEFT: " + controller.getMinesLeft(1));
        lblMinesLeft2.setText("MINES LEFT: " + controller.getMinesLeft(2));
        lblScore.setText("SCORE: " + controller.getSharedScore());
        lblLives.setText("LIVES: " + controller.getSharedLives() + "/" +
                controller.getMaxLives());
        updateHearts();
        revalidate();
        repaint();
    }

    /** Displays the result of the Surprise tile. */
    private void displayOutcomePopup(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Message",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Show "WAIT FOR YOUR TURN" on the board that is not active. */
    private void updateTurnUI() {
        int current = controller.getCurrentPlayerTurn();
        boardPanel1.setWaiting(current != 1);
        boardPanel2.setWaiting(current != 2);

        if (current == 1) {
            lblPlayer1Box.setBackground(new Color(90, 90, 110));
            lblPlayer2Box.setBackground(new Color(60, 60, 80));
        } else if (current == 2) {
            lblPlayer1Box.setBackground(new Color(60, 60, 80));
            lblPlayer2Box.setBackground(new Color(90, 90, 110));
        } else {
            lblPlayer1Box.setBackground(new Color(60, 60, 80));
            lblPlayer2Box.setBackground(new Color(60, 60, 80));
        }
    }
    /**
     * Colors heart icons according to current lives.
     */
    private void updateHearts() {
        int lives = controller.getSharedLives();
        int max = controller.getMaxLives();

        for (int i = 0; i < max && i < heartLabels.size(); i++) {
            JLabel heart = heartLabels.get(i);
            if (i < lives) {
                heart.setForeground(Color.RED);
            } else {
                heart.setForeground(Color.DARK_GRAY);
            }
        }
    }

    /** Handles the visual changes and dialog when the game ends. */
    private void handleGameOverUI() {
        boardPanel1.setWaiting(true);
        boardPanel2.setWaiting(true);
        boardPanel1.refresh();
        boardPanel2.refresh();
        btnPause.setEnabled(false);

        boolean isWin = controller.getCurrentGame().getGameState() == Model.GameState.WON;
        String title = isWin ? "Congratulations, You Won!" : "Game Over";
        String message;

        if (isWin) {
            message = String.format(
                "VICTORY!\n\nFinal Score: %d\n\nPlease use the buttons below to Restart or Exit.",
                controller.getSharedScore()
            );
        } else {
            message = String.format(
                "GAME OVER!\n\nFinal Score: %d\n\nPlease use the buttons below to Restart or Exit.",
                controller.getSharedScore()
            );
        }

        JOptionPane.showMessageDialog(
            this,
            message,
            title,
            isWin ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );

        long durationSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000L;
        controller.recordFinishedGame(player1Name, player2Name, durationSeconds);
}}
