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
    // Player area labels
    private NeonInputField playerBox1;
    private NeonInputField playerBox2;
    private JLabel lblMinesLeft1;
    private JLabel lblMinesLeft2;

    // Bottom status
    private JLabel lblScore;
    private JLabel lblLives;
    private JPanel heartsPanel;
    private java.util.List<NeonHeart> heartLabels;

    // Control Buttons
    private IconButton btnRestart;
    private IconButton btnExit;
    private final long startTimeMillis;
    private JPanel wrap1;
    private JPanel wrap2;
    private JPanel centerPanel;
    private Timer resizeStabilizer;
    private final Runnable onBackToMenu;

    private JLabel toastLabel;


    public GamePanel(GameController controller,
                     String player1Name, String player2Name,Runnable onBackToMenu) {
        this.controller = controller;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.onBackToMenu = onBackToMenu;
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
        setOpaque(false);

        // ===== Pick background by difficulty =====
        String levelName = controller.getDifficultyName();
        String bgPath;
        switch (levelName) {
            case "EASY" -> bgPath = "/ui/game/bg_easy.png";
            case "MEDIUM" -> bgPath = "/ui/game/bg_medium.png";
            case "HARD" -> bgPath = "/ui/game/bg_hard.png";
            default -> bgPath = "/ui/game/bg_easy.png";
        }

        BackgroundPanel bg = new BackgroundPanel(bgPath);
        bg.setLayout(new BorderLayout());
        bg.setOpaque(false);
        add(bg, BorderLayout.CENTER);


        // =========================
        // CENTER: two player panels
        // =========================
        centerPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        centerPanel.setOpaque(false);
        bg.add(centerPanel, BorderLayout.CENTER);

        // ----- Player 1 side -----
        JPanel leftSide = new JPanel(new BorderLayout());
        leftSide.setOpaque(false);

// top area (name + mines)
        JPanel leftTop = new JPanel();
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));
        leftTop.setOpaque(false);
        leftTop.setBorder(BorderFactory.createEmptyBorder(42, 0, 0, 0));

        playerBox1 = new NeonInputField(new Color(255, 80, 80));
        playerBox1.setText(player1Name);
        playerBox1.setDisplayMode(true);
        playerBox1.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox1.setFieldWidth(210); // half of 420
        leftTop.add(playerBox1);
        leftTop.add(Box.createVerticalStrut(20));

        lblMinesLeft1 = new JLabel("MINES LEFT: " + controller.getTotalMines(1), SwingConstants.CENTER);
        lblMinesLeft1.setForeground(Color.WHITE);
        lblMinesLeft1.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft1.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftTop.add(lblMinesLeft1);

        JPanel leftTopWrapper = new JPanel(new BorderLayout());
        leftTopWrapper.setOpaque(false);

        leftTopWrapper.setBorder(
                BorderFactory.createEmptyBorder(90, 0, 0, 0)
        );

        leftTopWrapper.add(leftTop, BorderLayout.CENTER);
        leftSide.add(leftTopWrapper, BorderLayout.NORTH);

// board area (expands!)
        boardPanel1 = new BoardPanel(controller, 1, false, this::handleMoveMade);
        boardPanel1.setOpaque(false);

        NeonFramePanel leftGlow = new NeonFramePanel(new Color(255, 80, 80), 14, 24);
        leftGlow.add(boardPanel1, new GridBagConstraints());

        wrap1 = new JPanel(new GridBagLayout());
        wrap1.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;     // ✅ DO NOT stretch
        gbc.weightx = 0;
        gbc.weighty = 0;

        wrap1.add(leftGlow, gbc);
        leftSide.add(wrap1, BorderLayout.CENTER);


        // ----- Player 2 side -----
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);

// top area (name + mines)
        JPanel rightTop = new JPanel();
        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.Y_AXIS));
        rightTop.setOpaque(false);
        rightTop.setBorder(BorderFactory.createEmptyBorder(42, 0, 0, 0));

        playerBox2 = new NeonInputField(new Color(80, 180, 255));
        playerBox2.setText(player2Name);
        playerBox2.setDisplayMode(true);
        playerBox2.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox2.setFieldWidth(210); // half of 420
        rightTop.add(playerBox2);
        rightTop.add(Box.createVerticalStrut(20));

        lblMinesLeft2 = new JLabel("MINES LEFT: " + controller.getTotalMines(2), SwingConstants.CENTER);
        lblMinesLeft2.setForeground(Color.WHITE);
        lblMinesLeft2.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft2.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightTop.add(lblMinesLeft2);

        JPanel rightTopWrapper = new JPanel(new BorderLayout());
        rightTopWrapper.setOpaque(false);

        rightTopWrapper.setBorder(
                BorderFactory.createEmptyBorder(90, 0, 0, 0)
        );

        rightTopWrapper.add(rightTop, BorderLayout.CENTER);
        rightSide.add(rightTopWrapper, BorderLayout.NORTH);

// board area (expands!)
        boardPanel2 = new BoardPanel(controller, 2, true, this::handleMoveMade);
        boardPanel2.setOpaque(false);

        NeonFramePanel rightGlow = new NeonFramePanel(new Color(80, 180, 255), 14, 24);
        rightGlow.add(boardPanel2, new GridBagConstraints());

        wrap2 = new JPanel(new GridBagLayout());
        wrap2.setOpaque(false);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.anchor = GridBagConstraints.CENTER;
        gbc2.fill = GridBagConstraints.NONE;    // ✅ DO NOT stretch
        gbc2.weightx = 0;
        gbc2.weighty = 0;

        wrap2.add(rightGlow, gbc2);
        rightSide.add(wrap2, BorderLayout.CENTER);


        centerPanel.removeAll();
        centerPanel.add(leftSide);
        centerPanel.add(rightSide);
        centerPanel.revalidate();
        centerPanel.repaint();

// =========================
// BOTTOM FOOTER
// =========================
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

// a bit taller so we have space to push the group down
        footer.setPreferredSize(new Dimension(1, 145)); // 120 -> 130 (tweak)

// ---- SCORE + LIVES ----
        JPanel scoreLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        scoreLivesPanel.setOpaque(false);

        lblScore = new JLabel("SCORE: 0");
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));

        lblLives = new JLabel("LIVES: " + controller.getSharedLives() + "/" + controller.getMaxLives());
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(new Font("Arial", Font.BOLD, 18));

        scoreLivesPanel.add(lblScore);
        scoreLivesPanel.add(lblLives);

// ---- HEARTS ----
        heartsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        heartsPanel.setOpaque(false);
        buildHearts();

// ---- STATUS GROUP (Score/Lives ABOVE Hearts) ----
        JPanel statusGroup = new JPanel();
        statusGroup.setOpaque(false);
        statusGroup.setLayout(new BoxLayout(statusGroup, BoxLayout.Y_AXIS));

// push the whole group down a bit
        statusGroup.add(Box.createVerticalGlue());
        statusGroup.add(Box.createVerticalStrut(14)); // keep this (overall position)

        statusGroup.add(Box.createVerticalStrut(6));  //  moves ONLY score/lives down
        statusGroup.add(scoreLivesPanel);

        statusGroup.add(Box.createVerticalStrut(10));
        statusGroup.add(heartsPanel);


// ---- CONTROLS BAR (buttons at very bottom) ----
        JPanel controlsBar = new JPanel(new BorderLayout());
        controlsBar.setOpaque(false);
        controlsBar.setBorder(BorderFactory.createEmptyBorder(0, 30, 18, 30));

        btnRestart = new IconButton("/ui/icons/restart.png", true);
        btnExit    = new IconButton("/ui/icons/back.png", true);

        btnRestart.setPreferredSize(new Dimension(40, 30));
        btnExit.setPreferredSize(new Dimension(40, 30));

        controlsBar.add(btnRestart, BorderLayout.EAST);
        controlsBar.add(btnExit, BorderLayout.WEST);
        btnRestart.setOnClick(() -> {
            controller.restartGame();

            // refresh view
            updateStatus();
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();

            // re-fit board sizes (after restart board might reset)
            requestResizeBoards();
        });

        btnExit.setOnClick(() -> {
            controller.endGame(); // clears currentGame
            if (onBackToMenu != null) onBackToMenu.run(); // go to menu
        });


// attach to footer
        footer.add(statusGroup, BorderLayout.CENTER);
        footer.add(controlsBar, BorderLayout.SOUTH);

        bg.add(footer, BorderLayout.SOUTH);




        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                requestResizeBoards();
            }
        });



        SwingUtilities.invokeLater(() -> {
            requestResizeBoards();
            requestResizeBoards(); // second pass after layout settles
        });


    }



    /**
     * Builds the row of heart icons according to the starting number of lives.
     */
    private void buildHearts() {
        heartLabels = new ArrayList<>();
        int maxLives = controller.getMaxLives();

        heartsPanel.removeAll();

        Color heartFill = new Color(255, 70, 70);      // softer red
        Color glow      = new Color(255, 40, 40);      // subtle halo


        for (int i = 0; i < maxLives; i++) {
            NeonHeart heart = new NeonHeart(heartFill, glow);
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

            int currentBoard = controller.getCurrentPlayerTurn(); // the one who activated
            BoardPanel target = (currentBoard == 1) ? boardPanel1 : boardPanel2;
            Color neon = (currentBoard == 1) ? new Color(255, 60, 60) : new Color(80, 180, 255);

            String msgLower = outcomeMessage.toLowerCase();

            // IMPORTANT: match your exact wording from the Model messages
            if (msgLower.contains("3x3")) {
                // queue 3x3 effect
                target.queueEffect(/* BoardPanel.EffectType */ BoardPanel.EffectType.REVEAL_3X3); // see note below
                showToast("3×3 REVEAL!", neon);
            } else if (msgLower.contains("reveal 1 mine") || msgLower.contains("reveal one mine")) {
                target.queueEffect(/* BoardPanel.EffectType */ BoardPanel.EffectType.REVEAL_1_MINE);
                showToast("MINE REVEALED!", neon);
            } else {

                // If it's a QUESTION outcome message -> show big OutcomeDialog (green/red)
                boolean looksLikeQuestionOutcome =
                        msgLower.contains("activation cost:") &&
                                (msgLower.contains("correct") || msgLower.contains("wrong")
                                        || msgLower.contains("didn't answer") || msgLower.contains("did not answer")
                                        || msgLower.contains("skipped"));

                if (looksLikeQuestionOutcome) {
                    OutcomeDialog.showQuestionOutcomeFromMessage(
                            SwingUtilities.getWindowAncestor(this),
                            outcomeMessage
                    );
                } else {
                    boolean looksLikeSurpriseOutcome =
                            msgLower.contains("surprise activated") ||
                                    msgLower.contains("🎲") ||
                                    msgLower.contains("surprise result");

                    if (looksLikeQuestionOutcome) {
                        OutcomeDialog.showQuestionOutcomeFromMessage(
                                SwingUtilities.getWindowAncestor(this),
                                outcomeMessage
                        );
                    }
                    else if (looksLikeSurpriseOutcome) {
                        //  big dialog for surprise too (cyan border)
                        OutcomeDialog.showSurpriseOutcomeFromMessage(
                                SwingUtilities.getWindowAncestor(this),
                                outcomeMessage
                        );

                    }
                    else {
                        // keep toast only for small/other messages
                        showToast(outcomeMessage, neon);
                    }
                }

            }

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

        // remove previous toast (if any)
        JLayeredPane lp = getRootPane().getLayeredPane();
        if (toastLabel != null && toastLabel.getParent() != null) {
            lp.remove(toastLabel);
        }

        JLabel label = new JLabel("<html><div style='text-align:center;'>" +
                message.replace("\n", "<br>") +
                "</div></html>", SwingConstants.CENTER);

        label.setOpaque(true);
        label.setBackground(new Color(0, 0, 0, 190));
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        Dimension pref = label.getPreferredSize();
        int x = (getWidth() - pref.width) / 2;
        int y = getHeight() - pref.height - 30;
        label.setBounds(x, y, pref.width, pref.height);

        lp.add(label, JLayeredPane.POPUP_LAYER);
        lp.repaint();

        toastLabel = label;

        Timer t = new Timer(1400, e -> {
            lp.remove(label);
            lp.repaint();
        });
        t.setRepeats(false);
        t.start();
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

    /**
     * Displays the result of the Surprise tile.
     */
    private void displayOutcomePopup(String message) {
        Window w = SwingUtilities.getWindowAncestor(this);

        if (message != null && message.toLowerCase().contains("correct")) {
            OutcomeDialog.showCorrect(w, message);
        } else if (message != null && message.toLowerCase().contains("wrong")) {
            OutcomeDialog.showWrong(w, message);
        } else {
            OutcomeDialog.show(w, "Message", message);
        }
    }




    /**
     * Show "WAIT FOR YOUR TURN" on the board that is not active.
     */
    private void updateTurnUI() {
        int current = controller.getCurrentPlayerTurn();
        boardPanel1.setWaiting(current != 1);
        boardPanel2.setWaiting(current != 2);

        playerBox1.setActive(current == 1);
        playerBox2.setActive(current == 2);
    }

    /**
     * Colors heart icons according to current lives.
     */
    private void updateHearts() {
        int lives = controller.getSharedLives();
        int max = controller.getMaxLives();

        for (int i = 0; i < max && i < heartLabels.size(); i++) {
            heartLabels.get(i).setActive(i < lives);
        }
    }


    /**
     * Handles the visual changes and dialog when the game ends.
     */
    private void handleGameOverUI() {
        boardPanel1.setWaiting(true);
        boardPanel2.setWaiting(true);
        boardPanel1.refresh();
        boardPanel2.refresh();

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
    }

    private void resizeBoardsToFit() {
        if (wrap1 == null || wrap2 == null || boardPanel1 == null || boardPanel2 == null) return;

        // available space where the board is placed (both sides)
        int availW = Math.min(wrap1.getWidth(), wrap2.getWidth());
        int availH = Math.min(wrap1.getHeight(), wrap2.getHeight());
        if (availW <= 0 || availH <= 0) return;

        String diff = controller.getDifficultyName();

        // grid size by difficulty (adjust if yours are different!)
        int rows, cols;
        switch (diff) {
            case "EASY" -> { rows = 10; cols = 10; }
            case "MEDIUM" -> { rows = 14; cols = 14; }
            case "HARD" -> { rows = 16; cols = 16; }
            default -> { rows = 10; cols = 10; }
        }

        // leave a little margin inside the wrapper so it doesn't touch
        int margin = 20;
        int usableW = Math.max(1, availW - margin);
        int usableH = Math.max(1, availH - margin);

        // compute max cell size that fits both width and height
        int cellByW = usableW / cols;
        int cellByH = usableH / rows;
        int cell = Math.min(cellByW, cellByH);

        // clamp to keep it pretty (optional)
        int minCell = 18;
        int maxCell = diff.equals("EASY") ? 48 : (diff.equals("MEDIUM") ? 35 : 28);
        cell = Math.max(minCell, Math.min(maxCell, cell));

        boardPanel1.setCellSize(cell);
        boardPanel2.setCellSize(cell);
    }









    private void updateCenterPadding() {
        // Minimal padding to maximize board space
        int top = 15;
        int side = 15;
        int bottom = 0;

        // In large screens - almost no padding
        if (getHeight() > 800) {
            top = 5;
            side = 10;
            bottom = 0;
        }

        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(top, side, bottom, side)
        );
    }


    private void requestResizeBoards() {
        if (resizeStabilizer != null && resizeStabilizer.isRunning()) {
            resizeStabilizer.restart();
            return;
        }

        resizeStabilizer = new Timer(40, e -> {
            if (wrap1 == null || wrap2 == null || boardPanel1 == null || boardPanel2 == null) return;

            // מחכים שה-layout באמת יתייצב
            if (wrap1.getWidth() <= 0 || wrap1.getHeight() <= 0 ||
                    wrap2.getWidth() <= 0 || wrap2.getHeight() <= 0) {
                return;
            }

            resizeBoardsToFit();
            boardPanel1.repaint();
            boardPanel2.repaint();


            ((Timer) e.getSource()).stop();
        });

        resizeStabilizer.setRepeats(true);
        resizeStabilizer.start();
    }}




