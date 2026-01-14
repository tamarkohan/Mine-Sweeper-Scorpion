package View;

import Controller.GameController;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Main in-game panel: displays two boards, player info, score, lives and controls.
 * Communicates only with GameController.
 */
public class GamePanel extends JPanel {

    private final GameController controller;
    private final String player1Name;
    private final String player2Name;

    private BoardPanel boardPanel1;
    private BoardPanel boardPanel2;

    private NeonInputField playerBox1;
    private NeonInputField playerBox2;
    private JLabel lblMinesLeft1;
    private JLabel lblMinesLeft2;

    private JLabel lblScore;
    private JLabel lblLives;
    private JLabel lblTime;
    private JPanel heartsPanel;
    private java.util.List<NeonHeart> heartLabels;

    private IconButton btnRestart;
    private IconButton btnExit;
    private long startTimeMillis;
    private JPanel wrap1;
    private JPanel wrap2;
    private JPanel centerPanel;
    private Timer resizeStabilizer;
    private Timer gameTimer;
    private final Runnable onBackToMenu;

    private JLabel toastLabel;

    // --- PERMANENT DIMENSIONS ---
    private static final int FIXED_BOX_WIDTH = 250;
    private static final int FIXED_BOX_HEIGHT = 65;

    // INCREASED: Locked height to 240 to give more room for the lowered elements
    private static final int TOP_HEADER_HEIGHT = 240;

    public GamePanel(GameController controller,
                     String player1Name, String player2Name, Runnable onBackToMenu) {
        this.controller = controller;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.onBackToMenu = onBackToMenu;
        this.startTimeMillis = System.currentTimeMillis();

        controller.registerQuestionPresenter(q -> {
            GameController.QuestionDTO dto = controller.buildQuestionDTO(q);
            GameController.QuestionAnswerResult ans =
                    QuestionDialog.showQuestionDialog(SwingUtilities.getWindowAncestor(this), dto);

            return switch (ans) {
                case CORRECT -> Model.QuestionResult.CORRECT;
                case WRONG -> Model.QuestionResult.WRONG;
                default -> Model.QuestionResult.SKIPPED;
            };
        });

        initComponents();
        updateStatus();
        updateTurnUI();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        String levelName = controller.getDifficultyName();
        String bgPath = switch (levelName) {
            case "MEDIUM" -> "/ui/game/bg_medium.png";
            case "HARD" -> "/ui/game/bg_hard.png";
            default -> "/ui/game/bg_easy.png";
        };

        BackgroundPanel bg = new BackgroundPanel(bgPath);
        bg.setLayout(new BorderLayout());
        bg.setOpaque(false);
        add(bg, BorderLayout.CENTER);

        centerPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        centerPanel.setOpaque(false);
        bg.add(centerPanel, BorderLayout.CENTER);

        Dimension boxDim = new Dimension(FIXED_BOX_WIDTH, FIXED_BOX_HEIGHT);
        Dimension headerDim = new Dimension(100, TOP_HEADER_HEIGHT);

        // ----- Player 1 Side -----
        JPanel leftSide = new JPanel(new BorderLayout());
        leftSide.setOpaque(false);

        JPanel leftTopGroup = new JPanel();
        leftTopGroup.setLayout(new BoxLayout(leftTopGroup, BoxLayout.Y_AXIS));
        leftTopGroup.setOpaque(false);

        leftTopGroup.setPreferredSize(headerDim);
        leftTopGroup.setMinimumSize(headerDim);
        leftTopGroup.setMaximumSize(headerDim);

        playerBox1 = new NeonInputField(new Color(255, 80, 80));
        playerBox1.setText(player1Name);
        playerBox1.setDisplayMode(true);
        playerBox1.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox1.setFieldWidth(FIXED_BOX_WIDTH);
        playerBox1.setPreferredSize(boxDim);
        playerBox1.setMaximumSize(boxDim);

        lblMinesLeft1 = new JLabel("MINES LEFT: " + controller.getTotalMines(1), SwingConstants.CENTER);
        lblMinesLeft1.setForeground(Color.WHITE);
        lblMinesLeft1.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft1.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- LOWERING LOGIC FOR PLAYER 1 ---
        leftTopGroup.add(Box.createVerticalStrut(155)); // Increased from 130 to lower the name box
        leftTopGroup.add(playerBox1);
        leftTopGroup.add(Box.createVerticalStrut(12));  // Increased from 8 to lower mines text more
        leftTopGroup.add(lblMinesLeft1);
        leftSide.add(leftTopGroup, BorderLayout.NORTH);

        boardPanel1 = new BoardPanel(controller, 1, false, this::handleMoveMade);
        boardPanel1.setOpaque(false);
        NeonFramePanel leftGlow = new NeonFramePanel(new Color(255, 80, 80), 14, 24);
        leftGlow.add(boardPanel1, new GridBagConstraints());
        wrap1 = new JPanel(new GridBagLayout());
        wrap1.setOpaque(false);
        wrap1.add(leftGlow, new GridBagConstraints());
        leftSide.add(wrap1, BorderLayout.CENTER);

        // ----- Player 2 Side -----
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);

        JPanel rightTopGroup = new JPanel();
        rightTopGroup.setLayout(new BoxLayout(rightTopGroup, BoxLayout.Y_AXIS));
        rightTopGroup.setOpaque(false);

        rightTopGroup.setPreferredSize(headerDim);
        rightTopGroup.setMinimumSize(headerDim);
        rightTopGroup.setMaximumSize(headerDim);

        playerBox2 = new NeonInputField(new Color(80, 180, 255));
        playerBox2.setText(player2Name);
        playerBox2.setDisplayMode(true);
        playerBox2.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox2.setFieldWidth(FIXED_BOX_WIDTH);
        playerBox2.setPreferredSize(boxDim);
        playerBox2.setMaximumSize(boxDim);

        lblMinesLeft2 = new JLabel("MINES LEFT: " + controller.getTotalMines(2), SwingConstants.CENTER);
        lblMinesLeft2.setForeground(Color.WHITE);
        lblMinesLeft2.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- LOWERING LOGIC FOR PLAYER 2 ---
        rightTopGroup.add(Box.createVerticalStrut(155)); // Matches Player 1
        rightTopGroup.add(playerBox2);
        rightTopGroup.add(Box.createVerticalStrut(12));  // Matches Player 1
        rightTopGroup.add(lblMinesLeft2);
        rightSide.add(rightTopGroup, BorderLayout.NORTH);

        boardPanel2 = new BoardPanel(controller, 2, true, this::handleMoveMade);
        boardPanel2.setOpaque(false);
        NeonFramePanel rightGlow = new NeonFramePanel(new Color(80, 180, 255), 14, 24);
        rightGlow.add(boardPanel2, new GridBagConstraints());
        wrap2 = new JPanel(new GridBagLayout());
        wrap2.setOpaque(false);
        wrap2.add(rightGlow, new GridBagConstraints());
        rightSide.add(wrap2, BorderLayout.CENTER);

        centerPanel.add(leftSide);
        centerPanel.add(rightSide);

        // ... [Remaining code for Footer, buildHearts, handleMoveMade, etc. stays the same] ...

        // --- FOOTER ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(1, 145));

        JPanel scoreLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        scoreLivesPanel.setOpaque(false);
        lblScore = new JLabel("SCORE: 0");
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));
        lblLives = new JLabel("LIVES: " + controller.getSharedLives() + "/" + controller.getMaxLives());
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(new Font("Arial", Font.BOLD, 18));
        lblTime = new JLabel("TIME: 00:00");
        lblTime.setForeground(Color.WHITE);
        lblTime.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLivesPanel.add(lblScore);
        scoreLivesPanel.add(lblLives);
        scoreLivesPanel.add(lblTime);

        heartsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        heartsPanel.setOpaque(false);
        buildHearts();

        JPanel statusGroup = new JPanel();
        statusGroup.setOpaque(false);
        statusGroup.setLayout(new BoxLayout(statusGroup, BoxLayout.Y_AXIS));
        statusGroup.add(scoreLivesPanel);
        statusGroup.add(Box.createVerticalStrut(10));
        statusGroup.add(heartsPanel);

        JPanel controlsBar = new JPanel(new BorderLayout());
        controlsBar.setOpaque(false);
        controlsBar.setBorder(BorderFactory.createEmptyBorder(0, 30, 18, 30));
        btnRestart = new IconButton("/ui/icons/restart.png", true);
        btnExit = new IconButton("/ui/icons/back.png", true);
        btnRestart.setPreferredSize(new Dimension(40, 30));
        btnExit.setPreferredSize(new Dimension(40, 30));
        controlsBar.add(btnRestart, BorderLayout.EAST);
        controlsBar.add(btnExit, BorderLayout.WEST);

        btnRestart.setOnClick(() -> {
            controller.restartGame();
            startTimeMillis = System.currentTimeMillis();
            if (gameTimer != null) gameTimer.restart();
            updateStatus();
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
            requestResizeBoards();
        });

        btnExit.setOnClick(() -> {
            if (gameTimer != null) gameTimer.stop();
            controller.endGame();
            if (onBackToMenu != null) onBackToMenu.run();
        });

        footer.add(statusGroup, BorderLayout.CENTER);
        footer.add(controlsBar, BorderLayout.SOUTH);
        bg.add(footer, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                requestResizeBoards();
            }
        });

        SwingUtilities.invokeLater(() -> requestResizeBoards());

        gameTimer = new Timer(1000, e -> {
            long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
            long seconds = (elapsedMillis / 1000) % 60;
            long minutes = (elapsedMillis / 1000) / 60;
            lblTime.setText(String.format("TIME: %02d:%02d", minutes, seconds));
        });
        gameTimer.start();
    }

    // [Rest of methods: updateStatus, updateTurnUI, etc. are omitted for brevity but remain unchanged]
    private void buildHearts() {
        heartLabels = new ArrayList<>();
        int maxLives = controller.getMaxLives();
        heartsPanel.removeAll();
        for (int i = 0; i < maxLives; i++) {
            NeonHeart heart = new NeonHeart(new Color(255, 70, 70), new Color(255, 40, 40));
            heartLabels.add(heart);
            heartsPanel.add(heart);
        }
        heartsPanel.revalidate();
        heartsPanel.repaint();
    }

    private void handleMoveMade(boolean endedTurn) {
        updateStatus();
        String outcomeMessage = controller.getAndClearLastActionMessage();
        if (outcomeMessage != null) {
            int currentBoard = controller.getCurrentPlayerTurn();
            Color neon = (currentBoard == 1) ? new Color(255, 60, 60) : new Color(80, 180, 255);
            showToast(outcomeMessage, neon);
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

    private void showToast(String message, Color accent) {
        if (message == null || message.isBlank()) return;
        JLayeredPane lp = getRootPane().getLayeredPane();
        if (toastLabel != null && toastLabel.getParent() != null) lp.remove(toastLabel);
        JLabel label = new JLabel("<html><div style='text-align:center;'>" + message.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(0, 0, 0, 190));
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent, 2, true), BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        Dimension pref = label.getPreferredSize();
        label.setBounds((getWidth() - pref.width) / 2, getHeight() - pref.height - 30, pref.width, pref.height);
        lp.add(label, JLayeredPane.POPUP_LAYER);
        lp.repaint();
        toastLabel = label;
        Timer t = new Timer(1400, e -> { lp.remove(label); lp.repaint(); });
        t.setRepeats(false);
        t.start();
    }

    public void updateStatus() {
        lblMinesLeft1.setText("MINES LEFT: " + controller.getMinesLeft(1));
        lblMinesLeft2.setText("MINES LEFT: " + controller.getMinesLeft(2));
        lblScore.setText("SCORE: " + controller.getSharedScore());
        lblLives.setText("LIVES: " + controller.getSharedLives() + "/" + controller.getMaxLives());
        updateHearts();
        revalidate();
        repaint();
    }

    private void updateTurnUI() {
        int current = controller.getCurrentPlayerTurn();
        boardPanel1.setWaiting(current != 1);
        boardPanel2.setWaiting(current != 2);
        playerBox1.setActive(current == 1);
        playerBox2.setActive(current == 2);
    }

    private void updateHearts() {
        int lives = controller.getSharedLives();
        int max = controller.getMaxLives();
        for (int i = 0; i < max && i < heartLabels.size(); i++) {
            heartLabels.get(i).setActive(i < lives);
        }
    }

    private void handleGameOverUI() {
        if (gameTimer != null) gameTimer.stop();
        boardPanel1.setWaiting(true);
        boardPanel2.setWaiting(true);
        boardPanel1.refresh();
        boardPanel2.refresh();
        long durationSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000L;
        controller.recordFinishedGame(player1Name, player2Name, durationSeconds);
        GameController.GameSummaryDTO summary = controller.getGameSummaryDTO();
        GameResultDialog.ResultAction action = GameResultDialog.showResultDialog(SwingUtilities.getWindowAncestor(this), summary, durationSeconds, controller.getTotalSurprisesOpened());
        if (action == GameResultDialog.ResultAction.RESTART) {
            controller.restartGame();
            startTimeMillis = System.currentTimeMillis();
            if (gameTimer != null) gameTimer.restart();
            updateStatus();
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
            requestResizeBoards();
        } else if (action == GameResultDialog.ResultAction.EXIT) {
            controller.endGame();
            if (onBackToMenu != null) onBackToMenu.run();
        }
    }

    private void resizeBoardsToFit() {
        if (wrap1 == null || wrap2 == null || boardPanel1 == null || boardPanel2 == null) return;

        int availW = Math.min(wrap1.getWidth(), wrap2.getWidth());
        int availH = Math.min(wrap1.getHeight(), wrap2.getHeight());
        if (availW <= 0 || availH <= 0) return;

        String diff = controller.getDifficultyName();
        int rows = switch (diff) { case "MEDIUM" -> 13; case "HARD" -> 16; default -> 9; };
        int cols = rows;

        int cell = Math.min((availW - 20) / cols, (availH - 20) / rows);

        int maxCell = diff.equals("EASY") ? 48 : (diff.equals("MEDIUM") ? 35 : 28);
        cell = Math.max(18, Math.min(maxCell, cell));

        boardPanel1.setCellSize(cell);
        boardPanel2.setCellSize(cell);
    }

    private void requestResizeBoards() {
        if (resizeStabilizer != null && resizeStabilizer.isRunning()) { resizeStabilizer.restart(); return; }
        resizeStabilizer = new Timer(40, e -> {
            if (wrap1 == null || wrap2 == null) return;
            resizeBoardsToFit();
            boardPanel1.repaint();
            boardPanel2.repaint();
            ((Timer) e.getSource()).stop();
        });
        resizeStabilizer.start();
    }
}