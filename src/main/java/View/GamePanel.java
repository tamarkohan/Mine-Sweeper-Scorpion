package View;

import Controller.GameController;
import util.LanguageManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private IconButton btnLanguage;
    private long startTimeMillis;
    private JPanel wrap1;
    private JPanel wrap2;
    private JPanel centerPanel;
    private Timer resizeStabilizer;
    private Timer gameTimer;
    private final Runnable onBackToMenu;

    private JLabel langToastLabel;
    private Timer langToastTimer;

    // Toast colors - Green for correct/good, Red for wrong/bad
    private static final Color COLOR_GREEN = new Color(80, 200, 120);
    private static final Color COLOR_RED = new Color(220, 60, 60);

    // --- PERMANENT DIMENSIONS ---
    private static final int FIXED_BOX_WIDTH = 250;
    private static final int FIXED_BOX_HEIGHT = 65;
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

        lblMinesLeft1 = new JLabel(getMinesLeftText(1), SwingConstants.CENTER);
        lblMinesLeft1.setForeground(Color.WHITE);
        lblMinesLeft1.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft1.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftTopGroup.add(Box.createVerticalStrut(155));
        leftTopGroup.add(playerBox1);
        leftTopGroup.add(Box.createVerticalStrut(12));
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

        lblMinesLeft2 = new JLabel(getMinesLeftText(2), SwingConstants.CENTER);
        lblMinesLeft2.setForeground(Color.WHITE);
        lblMinesLeft2.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft2.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightTopGroup.add(Box.createVerticalStrut(155));
        rightTopGroup.add(playerBox2);
        rightTopGroup.add(Box.createVerticalStrut(12));
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

        // --- FOOTER ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(1, 145));

        JPanel scoreLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        scoreLivesPanel.setOpaque(false);

        LanguageManager.Language lang = controller.getCurrentLanguage();
        lblScore = new JLabel(LanguageManager.get("score", lang) + ": 0");
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));

        lblLives = new JLabel(LanguageManager.get("lives", lang) + ": " +
                controller.getSharedLives() + "/" + controller.getMaxLives());
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(new Font("Arial", Font.BOLD, 18));

        lblTime = new JLabel(LanguageManager.get("time", lang) + ": 00:00");
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
        btnLanguage = new IconButton("/ui/icons/language.png", true);

        btnRestart.setPreferredSize(new Dimension(40, 30));
        btnExit.setPreferredSize(new Dimension(40, 30));
        btnLanguage.setPreferredSize(new Dimension(40, 30));

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);
        rightControls.add(btnLanguage);
        rightControls.add(btnRestart);

        controlsBar.add(btnExit, BorderLayout.WEST);
        controlsBar.add(rightControls, BorderLayout.EAST);

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

        btnLanguage.setOnClick(this::handleLanguageToggle);

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
            LanguageManager.Language currentLang = controller.getCurrentLanguage();
            lblTime.setText(LanguageManager.get("time", currentLang) +
                    String.format(": %02d:%02d", minutes, seconds));
        });
        gameTimer.start();
    }

    private void handleLanguageToggle() {
        GameController gc = GameController.getInstance();

        // Toggle language
        if (gc.getCurrentLanguage() == LanguageManager.Language.EN) {
            gc.setCurrentLanguage(LanguageManager.Language.HE);
        } else {
            gc.setCurrentLanguage(LanguageManager.Language.EN);
        }

        // Switch questions from pre-loaded cache (instant)
        gc.getQuestionManager().switchLanguageFromCache();

        // Update all UI text
        updateAllLanguageTexts();

        // Refresh boards
        boardPanel1.refresh();
        boardPanel2.refresh();

        // Show language toast
        showLanguageToast();
    }

    private void updateAllLanguageTexts() {
        LanguageManager.Language lang = controller.getCurrentLanguage();

        lblMinesLeft1.setText(getMinesLeftText(1));
        lblMinesLeft2.setText(getMinesLeftText(2));
        lblScore.setText(LanguageManager.get("score", lang) + ": " + controller.getSharedScore());
        lblLives.setText(LanguageManager.get("lives", lang) + ": " +
                controller.getSharedLives() + "/" + controller.getMaxLives());

        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / 1000) / 60;
        lblTime.setText(LanguageManager.get("time", lang) +
                String.format(": %02d:%02d", minutes, seconds));

        revalidate();
        repaint();
    }

    private String getMinesLeftText(int boardNumber) {
        LanguageManager.Language lang = controller.getCurrentLanguage();
        return LanguageManager.get("mines_left", lang) + ": " + controller.getMinesLeft(boardNumber);
    }

    private void showLanguageToast() {
        JLayeredPane lp = getRootPane().getLayeredPane();

        if (langToastLabel != null && langToastLabel.getParent() != null) {
            lp.remove(langToastLabel);
        }

        LanguageManager.Language lang = controller.getCurrentLanguage();
        String text = (lang == LanguageManager.Language.HE) ?
                LanguageManager.get("lang_hebrew", lang) :
                LanguageManager.get("lang_english", lang);

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(0, 0, 0, 200));
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 255, 255), 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        Dimension pref = label.getPreferredSize();
        int minWidth = 100;
        int width = Math.max(pref.width, minWidth);
        int height = pref.height;

        Point btnLangLocation = SwingUtilities.convertPoint(
                btnLanguage.getParent(),
                btnLanguage.getLocation(),
                lp
        );

        int toastX = btnLangLocation.x + (btnLanguage.getWidth() / 2) - (width / 2);
        int toastY = btnLangLocation.y - height - 10;

        toastX = Math.max(10, Math.min(toastX, lp.getWidth() - width - 10));
        toastY = Math.max(10, toastY);

        label.setBounds(toastX, toastY, width, height);

        lp.add(label, JLayeredPane.POPUP_LAYER);
        lp.repaint();
        langToastLabel = label;

        if (langToastTimer != null && langToastTimer.isRunning()) {
            langToastTimer.stop();
        }

        langToastTimer = new Timer(1400, e -> {
            lp.remove(label);
            lp.repaint();
        });
        langToastTimer.setRepeats(false);
        langToastTimer.start();
    }

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
            // Show MODAL dialog - blocks until user closes it
            showResultDialog(outcomeMessage);
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
     * Shows a MODAL result dialog that blocks until the user clicks X or OK.
     * This prevents overlapping dialogs.
     */
    private void showResultDialog(String message) {
        if (message == null || message.isBlank()) return;

        LanguageManager.Language lang = controller.getCurrentLanguage();
        boolean isHebrew = (lang == LanguageManager.Language.HE);

        // Determine if this is a positive or negative outcome
        boolean isPositive = isPositiveOutcome(message);
        Color accentColor = isPositive ? COLOR_GREEN : COLOR_RED;

        // Translate the message
        String translatedMessage = translateToastMessage(message, lang);

        // Determine title
        String title = determineToastTitle(message, lang);

        // Create and show modal dialog
        ResultMessageDialog.show(
                SwingUtilities.getWindowAncestor(this),
                title,
                translatedMessage,
                accentColor,
                isHebrew
        );
    }

    /**
     * Determines if the outcome is positive (correct/good) or negative (wrong/bad)
     */
    private boolean isPositiveOutcome(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();

        if (lower.contains("correct")) return true;
        if (lower.contains("good surprise") || lower.contains("good")) return true;
        if (lower.contains("result: good")) return true;

        if (lower.contains("wrong")) return false;
        if (lower.contains("bad surprise") || lower.contains("bad")) return false;
        if (lower.contains("result: bad")) return false;
        if (lower.contains("penalty")) return false;

        return true;
    }

    /**
     * Determines the appropriate title for the dialog based on message content
     */
    private String determineToastTitle(String message, LanguageManager.Language lang) {
        String lower = message.toLowerCase();

        if (lower.contains("correct")) {
            return lang == LanguageManager.Language.HE ? "נכון!" : "Correct!";
        } else if (lower.contains("wrong")) {
            return lang == LanguageManager.Language.HE ? "שגוי!" : "Wrong!";
        } else if (lower.contains("good surprise") || (lower.contains("surprise") && lower.contains("good"))) {
            return lang == LanguageManager.Language.HE ? "הפתעה טובה!" : "Good Surprise!";
        } else if (lower.contains("bad surprise") || (lower.contains("surprise") && lower.contains("bad"))) {
            return lang == LanguageManager.Language.HE ? "הפתעה רעה!" : "Bad Surprise!";
        } else if (lower.contains("surprise")) {
            return lang == LanguageManager.Language.HE ? "הפתעה!" : "Surprise!";
        } else if (lower.contains("flag")) {
            return lang == LanguageManager.Language.HE ? "דגל" : "Flag";
        }

        return lang == LanguageManager.Language.HE ? "תוצאה" : "Result";
    }

    /**
     * Translates toast messages to the current language
     */
    private String translateToastMessage(String message, LanguageManager.Language lang) {
        if (message == null || lang == LanguageManager.Language.EN) {
            return message;
        }

        String result = message;

        // Translate difficulty levels
        result = result.replace("EASY", "קל");
        result = result.replace("MEDIUM", "בינוני");
        result = result.replace("HARD", "קשה");
        result = result.replace("EXPERT", "מומחה");

        // Translate outcome words
        result = result.replace("Wrong!", "שגוי!");
        result = result.replace("Wrong", "שגוי");
        result = result.replace("Correct!", "נכון!");
        result = result.replace("Correct", "נכון");

        // Translate surprise-specific phrases
        result = result.replace("Surprise activated!", "ההפתעה הופעלה!");
        result = result.replace("Surprise result: GOOD", "תוצאת ההפתעה: טוב");
        result = result.replace("Surprise result: BAD", "תוצאת ההפתעה: רע");
        result = result.replace("Reward:", "פרס:");
        result = result.replace("Penalty:", "עונש:");

        // Translate common phrases
        result = result.replace("reveal random 3x3", "חשיפת 3x3 אקראי");
        result = result.replace("revealed random 3x3 area", "נחשף אזור 3x3 אקראי");
        result = result.replace("reveal 1 mine", "חשיפת מוקש אחד");
        result = result.replace("revealed 1 mine", "נחשף מוקש אחד");
        result = result.replace("OR nothing", "או כלום");
        result = result.replace("Chosen: nothing", "נבחר: כלום");
        result = result.replace("reward", "פרס");
        result = result.replace("Reward", "פרס");

        // Translate stat labels
        result = result.replace("Activation cost:", "עלות הפעלה:");
        result = result.replace("Special effect:", "אפקט מיוחד:");
        result = result.replace("Score:", "ניקוד:");
        result = result.replace("Lives:", "חיים:");

        // Translate units
        result = result.replace("+1 life", "+1 חיים");
        result = result.replace("-1 life", "-1 חיים");
        result = result.replace(" pts,", " נק',");
        result = result.replace(" pts.", " נק'.");
        result = result.replace(" pts", " נק'");
        result = result.replace(" life.", " חיים.");
        result = result.replace(" life,", " חיים,");
        result = result.replace(" life", " חיים");
        result = result.replace(" lives", " חיים");

        // Translate game terms
        result = result.replace("game.", "משחק.");
        result = result.replace("game)", "משחק)");
        result = result.replace("(Easy", "(קל");
        result = result.replace("(Medium", "(בינוני");
        result = result.replace("(Hard", "(קשה");
        result = result.replace("(Expert", "(מומחה");
        result = result.replace("area", "אזור");
        result = result.replace("mine", "מוקש");

        // Translate surprise outcomes
        result = result.replace("Good surprise!", "הפתעה טובה!");
        result = result.replace("Bad surprise!", "הפתעה רעה!");
        result = result.replace("GOOD", "טוב");
        result = result.replace("BAD", "רע");
        result = result.replace("Good", "טוב");
        result = result.replace("Bad", "רע");

        return result;
    }

    public void updateStatus() {
        lblMinesLeft1.setText(getMinesLeftText(1));
        lblMinesLeft2.setText(getMinesLeftText(2));

        LanguageManager.Language lang = controller.getCurrentLanguage();
        lblScore.setText(LanguageManager.get("score", lang) + ": " + controller.getSharedScore());
        lblLives.setText(LanguageManager.get("lives", lang) + ": " +
                controller.getSharedLives() + "/" + controller.getMaxLives());
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
        GameResultDialog.ResultAction action = GameResultDialog.showResultDialog(
                SwingUtilities.getWindowAncestor(this), summary, durationSeconds,
                controller.getTotalSurprisesOpened()
        );
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
        if (resizeStabilizer != null && resizeStabilizer.isRunning()) {
            resizeStabilizer.restart();
            return;
        }
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