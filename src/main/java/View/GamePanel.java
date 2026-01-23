package View;

import Controller.GameController;
import util.LanguageManager;
import util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Main in-game panel: displays two boards, player info, score, lives and controls.
 * Handles the "Thinking" animation during language switching.
 * Updated: Language popup menu with all 5 languages support.
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
    private final Runnable onBackToStart;

    private JLabel langToastLabel;
    private Timer langToastTimer;

    // Toast colors
    private static final Color COLOR_GREEN = new Color(80, 200, 120);
    private static final Color COLOR_RED = new Color(220, 60, 60);
    private static final Color COLOR_YELLOW = new Color(255, 200, 80);
    private static final Color COLOR_CYAN = new Color(80, 200, 255);

    // Dimensions
    private static final int FIXED_BOX_WIDTH = 250;
    private static final int FIXED_BOX_HEIGHT = 65;

    // Thinking Icon Path
    private static final String THINKING_ICON = "/ui/icons/thinking.png";

    public GamePanel(GameController controller,
                     String player1Name, String player2Name, Runnable onBackToStart) {
        this.controller = controller;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.onBackToStart = onBackToStart;
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
        setupEscapeKey();
        updateStatus();
        updateTurnUI();
    }

    private void setupEscapeKey() {
        // Register ESC key to trigger exit confirmation
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitGame");
        actionMap.put("exitGame", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showExitConfirmation();
            }
        });
    }

    private void showExitConfirmation() {
        LanguageManager.Language lang = controller.getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String title = LanguageManager.get("exit_game", lang);
        String message = LanguageManager.get("exit_confirm", lang);
        SoundManager.exitDialog();

        boolean confirmed = ConfirmDialog.show(
                SwingUtilities.getWindowAncestor(this),
                title,
                message,
                COLOR_YELLOW,
                isRTL
        );

        if (confirmed) {
            if (gameTimer != null) gameTimer.stop();
            controller.endGame();
            if (onBackToStart != null) onBackToStart.run();
        }
    }

    private void showRestartConfirmation() {
        LanguageManager.Language lang = controller.getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String title = LanguageManager.get("restart_game", lang);
        String message = LanguageManager.get("restart_confirm", lang);
        SoundManager.exitDialog();
        boolean confirmed = ConfirmDialog.show(
                SwingUtilities.getWindowAncestor(this),
                title,
                message,
                COLOR_CYAN,
                isRTL
        );

        if (confirmed) {
            controller.restartGame();
            startTimeMillis = System.currentTimeMillis();
            if (gameTimer != null) gameTimer.restart();
            updateStatus();
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
            requestResizeBoards();
        }
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

        // *** FIX: Increased header height to move names down ***
        int headerHeight = switch (levelName) {
            case "EASY" -> 240;
            case "MEDIUM" -> 260;
            case "HARD" -> 270;
            default -> 240;
        };

        Dimension boxDim = new Dimension(FIXED_BOX_WIDTH, FIXED_BOX_HEIGHT);
        Dimension headerDim = new Dimension(100, headerHeight);

        // --- Player 1 Side ---
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

        // *** FIX: Increased top spacing to push names down ***
        int topSpacing = switch (levelName) {
            case "EASY" -> 160;
            case "MEDIUM" -> 180;
            case "HARD" -> 195;
            default -> 160;
        };

        leftTopGroup.add(Box.createVerticalStrut(topSpacing));
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

        // --- Player 2 Side ---
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

        rightTopGroup.add(Box.createVerticalStrut(topSpacing));
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

        // --- Footer ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(1, 145));

        JPanel scoreLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        scoreLivesPanel.setOpaque(false);

        LanguageManager.Language lang = controller.getCurrentLanguage();
        lblScore = new JLabel(LanguageManager.get("score", lang) + ": 0");
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));

        lblLives = new JLabel(LanguageManager.get("lives", lang) + ": " + controller.getSharedLives() + "/" + controller.getMaxLives());
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(new Font("Arial", Font.BOLD, 18));

        lblTime = new JLabel(LanguageManager.get("time", lang) + ": 00:00");
        lblTime.setForeground(Color.WHITE);
        lblTime.setFont(new Font("Arial", Font.BOLD, 18));

        scoreLivesPanel.add(lblScore); scoreLivesPanel.add(lblLives); scoreLivesPanel.add(lblTime);

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

        btnRestart.setOnClick(this::showRestartConfirmation);
        btnExit.setOnClick(this::showExitConfirmation);
        // Changed: Use popup menu instead of toggle
        btnLanguage.setOnClick(this::showLanguagePopup);

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
            lblTime.setText(LanguageManager.get("time", currentLang) + String.format(": %02d:%02d", minutes, seconds));
        });
        gameTimer.start();

        new Thread(() -> new IconButton(THINKING_ICON, true)).start();
    }

    /**
     * Shows language selection popup menu with all 5 languages
     */
    private void showLanguagePopup() {
        JPopupMenu langMenu = new JPopupMenu();
        langMenu.setBackground(new Color(11, 15, 26));
        langMenu.setBorder(BorderFactory.createLineBorder(new Color(0, 245, 255)));

        for (LanguageManager.Language lang : LanguageManager.Language.values()) {
            JMenuItem item = new JMenuItem(LanguageManager.getDisplayName(lang));
            item.setForeground(Color.WHITE);
            item.setBackground(new Color(11, 15, 26));
            item.setFont(new Font("Arial", Font.BOLD, 14));

            // Highlight current language
            if (lang == GameController.getInstance().getCurrentLanguage()) {
                item.setForeground(new Color(0, 245, 255));
            }

            item.addActionListener(e -> handleLanguageSelection(lang));
            langMenu.add(item);
        }

        // Show popup above the button
        Dimension size = langMenu.getPreferredSize();
        langMenu.show(btnLanguage, 0, -size.height);
    }

    /**
     * Handles language selection from popup menu
     */
    private void handleLanguageSelection(LanguageManager.Language lang) {
        // Skip if same language selected
        if (lang == GameController.getInstance().getCurrentLanguage()) {
            return;
        }

        // 1. Show thinking icon immediately
        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null); // Prevent spamming

        // 2. Background Thread
        new Thread(() -> {
            try {
                GameController gc = GameController.getInstance();
                gc.setCurrentLanguage(lang);

                // Heavy Load
                gc.getQuestionManager().switchLanguageFromCache();

                // Optional delay for visual smoothness
                Thread.sleep(300);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 3. Update UI on Main Thread
                SwingUtilities.invokeLater(() -> {
                    updateAllLanguageTexts();
                    boardPanel1.refresh();
                    boardPanel2.refresh();
                    showLanguageToast();

                    // Restore button
                    btnLanguage.setIconPath("/ui/icons/language.png");
                    btnLanguage.setOnClick(this::showLanguagePopup);
                });
            }
        }).start();
    }

    private void updateAllLanguageTexts() {
        LanguageManager.Language lang = controller.getCurrentLanguage();
        lblMinesLeft1.setText(getMinesLeftText(1));
        lblMinesLeft2.setText(getMinesLeftText(2));
        lblScore.setText(LanguageManager.get("score", lang) + ": " + controller.getSharedScore());
        lblLives.setText(LanguageManager.get("lives", lang) + ": " + controller.getSharedLives() + "/" + controller.getMaxLives());

        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / 1000) / 60;
        lblTime.setText(LanguageManager.get("time", lang) + String.format(": %02d:%02d", minutes, seconds));

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
        String text = LanguageManager.getDisplayName(lang);

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
        int minWidth = 100; int width = Math.max(pref.width, minWidth); int height = pref.height;

        Point btnLangLocation = SwingUtilities.convertPoint(btnLanguage.getParent(), btnLanguage.getLocation(), lp);
        int toastX = btnLangLocation.x + (btnLanguage.getWidth() / 2) - (width / 2);
        int toastY = btnLangLocation.y - height - 10;

        toastX = Math.max(10, Math.min(toastX, lp.getWidth() - width - 10));
        toastY = Math.max(10, toastY);

        label.setBounds(toastX, toastY, width, height);
        lp.add(label, JLayeredPane.POPUP_LAYER);
        lp.repaint();
        langToastLabel = label;

        if (langToastTimer != null && langToastTimer.isRunning()) { langToastTimer.stop(); }
        langToastTimer = new Timer(1400, e -> { lp.remove(label); lp.repaint(); });
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
        heartsPanel.revalidate(); heartsPanel.repaint();
    }

    private void handleMoveMade(boolean endedTurn) {
        updateStatus();
        String outcomeMessage = controller.getAndClearLastActionMessage();
        if (outcomeMessage != null) { showResultDialog(outcomeMessage); }

        if (controller.isGameOver()) { handleGameOverUI(); return; }

        if (endedTurn && controller.isGameRunning()) {
            updateTurnUI(); boardPanel1.refresh(); boardPanel2.refresh();
            Timer delayTimer = new Timer(500, e -> { controller.processTurnEnd(); updateTurnUI(); boardPanel1.refresh(); boardPanel2.refresh(); });
            delayTimer.setRepeats(false); delayTimer.start();
        } else {
            updateTurnUI(); boardPanel1.refresh(); boardPanel2.refresh();
        }
    }

    private void showResultDialog(String message) {
        if (message == null || message.isBlank()) return;

        // --- PLAY SOUND FIRST (surprise uses same sounds as answers) ---
        String lower = message.toLowerCase();

        // Surprise result
        if (lower.contains("good surprise") || (lower.contains("surprise") && lower.contains("good")) || lower.contains("result: good")) {
            SoundManager.correctAnswer();   // GOOD surprise = correct sound
        } else if (lower.contains("bad surprise") || (lower.contains("surprise") && lower.contains("bad")) || lower.contains("result: bad")) {
            SoundManager.wrongAnswer();     // BAD surprise = wrong sound
        }
        // (Optional) If you also want correct/wrong question messages to play here too:
        else if (lower.contains("correct")) {
            SoundManager.correctAnswer();
        } else if (lower.contains("wrong")) {
            SoundManager.wrongAnswer();
        }

        // --- EXISTING CODE ---
        LanguageManager.Language lang = controller.getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);
        boolean isPositive = isPositiveOutcome(message);
        Color accentColor = isPositive ? COLOR_GREEN : COLOR_RED;
        String translatedMessage = translateToastMessage(message, lang);
        String title = determineToastTitle(message, lang);

        ResultMessageDialog.show(
                SwingUtilities.getWindowAncestor(this),
                title,
                translatedMessage,
                accentColor,
                isRTL
        );
    }


    private boolean isPositiveOutcome(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        if (lower.contains("correct") || lower.contains("good surprise") || lower.contains("good") || lower.contains("result: good")) return true;
        if (lower.contains("wrong") || lower.contains("bad surprise") || lower.contains("bad") || lower.contains("result: bad") || lower.contains("penalty")) return false;
        return true;
    }

    private String determineToastTitle(String message, LanguageManager.Language lang) {
        String lower = message.toLowerCase();
        if (lower.contains("correct")) return LanguageManager.get("outcome_correct", lang);
        else if (lower.contains("wrong")) return LanguageManager.get("outcome_wrong", lang);
        else if (lower.contains("good surprise") || (lower.contains("surprise") && lower.contains("good"))) return LanguageManager.get("surprise_good", lang);
        else if (lower.contains("bad surprise") || (lower.contains("surprise") && lower.contains("bad"))) return LanguageManager.get("surprise_bad", lang);
        else if (lower.contains("surprise")) return LanguageManager.get("surprise", lang);
        else if (lower.contains("flag")) return LanguageManager.get("flag", lang);
        return LanguageManager.get("result", lang);
    }

    /**
     * Translates toast messages for all 5 languages
     */
    private String translateToastMessage(String message, LanguageManager.Language lang) {
        if (message == null || lang == LanguageManager.Language.EN) return message;

        String result = message;

        // Common replacements for all non-English languages
        switch (lang) {
            case HE -> {
                result = result.replace("EASY", "קל").replace("MEDIUM", "בינוני").replace("HARD", "קשה").replace("EXPERT", "מומחה");
                result = result.replace("Wrong!", "שגוי!").replace("Wrong", "שגוי").replace("Correct!", "נכון!").replace("Correct", "נכון");
                result = result.replace("Surprise activated!", "ההפתעה הופעלה!").replace("Surprise result: GOOD", "תוצאת ההפתעה: טוב").replace("Surprise result: BAD", "תוצאת ההפתעה: רע");
                result = result.replace("Reward:", "פרס:").replace("Penalty:", "עונש:");
                result = result.replace("reveal random 3x3", "חשיפת 3x3 אקראי").replace("revealed random 3x3 area", "נחשף אזור 3x3 אקראי");
                result = result.replace("reveal 1 mine", "חשיפת מוקש אחד").replace("revealed 1 mine", "נחשף מוקש אחד");
                result = result.replace("OR nothing", "או כלום").replace("Chosen: nothing", "נבחר: כלום");
                // ** Added CHOSEN **
                result = result.replace("Chosen", LanguageManager.get("chosen", lang));
                result = result.replace("reward", "פרס").replace("Reward", "פרס");
                result = result.replace("Activation cost:", "עלות הפעלה:").replace("Special effect:", "אפקט מיוחד:").replace("Score:", "ניקוד:").replace("Lives:", "חיים:");
                result = result.replace("+1 life", "+1 חיים").replace("-1 life", "-1 חיים");
                result = result.replace(" pts,", " נק',").replace(" pts.", " נק'.").replace(" pts", " נק'");
                result = result.replace(" life.", " חיים.").replace(" life,", " חיים,").replace(" life", " חיים").replace(" lives", " חיים");
                result = result.replace("game.", "משחק.").replace("game)", "משחק)");
                result = result.replace("(Easy", "(קל").replace("(Medium", "(בינוני").replace("(Hard", "(קשה").replace("(Expert", "(מומחה");
                result = result.replace("area", "אזור").replace("mine", "מוקש");
                result = result.replace("Good surprise!", "הפתעה טובה!").replace("Bad surprise!", "הפתעה רעה!");
                result = result.replace("GOOD", "טוב").replace("BAD", "רע").replace("Good", "טוב").replace("Bad", "רע");
            }
            case AR -> {
                result = result.replace("EASY", "سهل").replace("MEDIUM", "متوسط").replace("HARD", "صعب").replace("EXPERT", "خبير");
                result = result.replace("Wrong!", "خطأ!").replace("Wrong", "خطأ").replace("Correct!", "صحيح!").replace("Correct", "صحيح");
                result = result.replace("Surprise activated!", "تم تفعيل المفاجأة!").replace("Surprise result: GOOD", "نتيجة المفاجأة: جيد").replace("Surprise result: BAD", "نتيجة المفاجأة: سيء");
                result = result.replace("Reward:", "مكافأة:").replace("Penalty:", "عقوبة:");
                result = result.replace("reveal random 3x3", "كشف 3x3 عشوائي").replace("revealed random 3x3 area", "تم كشف منطقة 3x3 عشوائية");
                result = result.replace("reveal 1 mine", "كشف لغم واحد").replace("revealed 1 mine", "تم كشف لغم واحد");
                result = result.replace("OR nothing", "أو لا شيء").replace("Chosen: nothing", "المختار: لا شيء");
                // ** Added CHOSEN **
                result = result.replace("Chosen", LanguageManager.get("chosen", lang));
                result = result.replace("reward", "مكافأة").replace("Reward", "مكافأة");
                result = result.replace("Activation cost:", "تكلفة التفعيل:").replace("Special effect:", "تأثير خاص:").replace("Score:", "النقاط:").replace("Lives:", "الأرواح:");
                result = result.replace("+1 life", "+1 حياة").replace("-1 life", "-1 حياة");
                result = result.replace(" pts,", " نقطة,").replace(" pts.", " نقطة.").replace(" pts", " نقطة");
                result = result.replace(" life.", " حياة.").replace(" life,", " حياة,").replace(" life", " حياة").replace(" lives", " أرواح");
                result = result.replace("game.", "لعبة.").replace("game)", "لعبة)");
                result = result.replace("(Easy", "(سهل").replace("(Medium", "(متوسط").replace("(Hard", "(صعب").replace("(Expert", "(خبير");
                result = result.replace("area", "منطقة").replace("mine", "لغم");
                result = result.replace("Good surprise!", "مفاجأة جيدة!").replace("Bad surprise!", "مفاجأة سيئة!");
                result = result.replace("GOOD", "جيد").replace("BAD", "سيء").replace("Good", "جيد").replace("Bad", "سيء");
            }
            case RU -> {
                result = result.replace("EASY", "ЛЕГКО").replace("MEDIUM", "СРЕДНЕ").replace("HARD", "СЛОЖНО").replace("EXPERT", "ЭКСПЕРТ");
                result = result.replace("Wrong!", "Неправильно!").replace("Wrong", "Неправильно").replace("Correct!", "Правильно!").replace("Correct", "Правильно");
                result = result.replace("Surprise activated!", "Сюрприз активирован!").replace("Surprise result: GOOD", "Результат сюрприза: Хорошо").replace("Surprise result: BAD", "Результат сюрприза: Плохо");
                result = result.replace("Reward:", "Награда:").replace("Penalty:", "Штраф:");
                result = result.replace("reveal random 3x3", "открыть случайную 3x3 область").replace("revealed random 3x3 area", "открыта случайная область 3x3");
                result = result.replace("reveal 1 mine", "открыть 1 мину").replace("revealed 1 mine", "открыта 1 мина");
                result = result.replace("OR nothing", "ИЛИ ничего").replace("Chosen: nothing", "Выбрано: ничего");
                // ** Added CHOSEN **
                result = result.replace("Chosen", LanguageManager.get("chosen", lang));
                result = result.replace("reward", "награда").replace("Reward", "Награда");
                result = result.replace("Activation cost:", "Стоимость активации:").replace("Special effect:", "Спецэффект:").replace("Score:", "Счёт:").replace("Lives:", "Жизни:");
                result = result.replace("+1 life", "+1 жизнь").replace("-1 life", "-1 жизнь");
                result = result.replace(" pts,", " очк.,").replace(" pts.", " очк.").replace(" pts", " очк.");
                result = result.replace(" life.", " жизнь.").replace(" life,", " жизнь,").replace(" life", " жизнь").replace(" lives", " жизней");
                result = result.replace("game.", "игра.").replace("game)", "игра)");
                result = result.replace("(Easy", "(Легко").replace("(Medium", "(Средне").replace("(Hard", "(Сложно").replace("(Expert", "(Эксперт");
                result = result.replace("area", "область").replace("mine", "мина");
                result = result.replace("Good surprise!", "Хороший сюрприз!").replace("Bad surprise!", "Плохой сюрприз!");
                result = result.replace("GOOD", "Хорошо").replace("BAD", "Плохо").replace("Good", "Хорошо").replace("Bad", "Плохо");
            }
            case ES -> {
                result = result.replace("EASY", "FÁCIL").replace("MEDIUM", "MEDIO").replace("HARD", "DIFÍCIL").replace("EXPERT", "EXPERTO");
                result = result.replace("Wrong!", "¡Incorrecto!").replace("Wrong", "Incorrecto").replace("Correct!", "¡Correcto!").replace("Correct", "Correcto");
                result = result.replace("Surprise activated!", "¡La sorpresa fue activada!").replace("Surprise result: GOOD", "Resultado de la sorpresa: Bueno").replace("Surprise result: BAD", "Resultado de la sorpresa: Malo");
                result = result.replace("Reward:", "Recompensa:").replace("Penalty:", "Penalización:");
                result = result.replace("reveal random 3x3", "revelar 3x3 aleatorio").replace("revealed random 3x3 area", "se reveló área 3x3 aleatoria");
                result = result.replace("reveal 1 mine", "revelar 1 mina").replace("revealed 1 mine", "se reveló 1 mina");
                result = result.replace("OR nothing", "O nada").replace("Chosen: nothing", "Elegido: nada");
                // ** Added CHOSEN **
                result = result.replace("Chosen", LanguageManager.get("chosen", lang));
                result = result.replace("reward", "recompensa").replace("Reward", "Recompensa");
                result = result.replace("Activation cost:", "Costo de activación:").replace("Special effect:", "Efecto especial:").replace("Score:", "Puntos:").replace("Lives:", "Vidas:");
                result = result.replace("+1 life", "+1 vida").replace("-1 life", "-1 vida");
                result = result.replace(" pts,", " pts,").replace(" pts.", " pts.").replace(" pts", " pts");
                result = result.replace(" life.", " vida.").replace(" life,", " vida,").replace(" life", " vida").replace(" lives", " vidas");
                result = result.replace("game.", "juego.").replace("game)", "juego)");
                result = result.replace("(Easy", "(Fácil").replace("(Medium", "(Medio").replace("(Hard", "(Difícil").replace("(Expert", "(Experto");
                result = result.replace("area", "área").replace("mine", "mina");
                result = result.replace("Good surprise!", "¡Buena sorpresa!").replace("Bad surprise!", "¡Mala sorpresa!");
                result = result.replace("GOOD", "Bueno").replace("BAD", "Malo").replace("Good", "Bueno").replace("Bad", "Malo");
            }
            default -> { }
        }

        return result;
    }

    public void updateStatus() {
        lblMinesLeft1.setText(getMinesLeftText(1));
        lblMinesLeft2.setText(getMinesLeftText(2));
        LanguageManager.Language lang = controller.getCurrentLanguage();
        lblScore.setText(LanguageManager.get("score", lang) + ": " + controller.getSharedScore());
        lblLives.setText(LanguageManager.get("lives", lang) + ": " + controller.getSharedLives() + "/" + controller.getMaxLives());
        updateHearts();
        revalidate(); repaint();
    }

    private void updateTurnUI() {
        int current = controller.getCurrentPlayerTurn();
        boardPanel1.setWaiting(current != 1); boardPanel2.setWaiting(current != 2);
        playerBox1.setActive(current == 1); playerBox2.setActive(current == 2);
    }

    private void updateHearts() {
        int lives = controller.getSharedLives(); int max = controller.getMaxLives();
        for (int i = 0; i < max && i < heartLabels.size(); i++) { heartLabels.get(i).setActive(i < lives); }
    }

    private void handleGameOverUI() {
        if (gameTimer != null) gameTimer.stop();
        boardPanel1.setWaiting(true); boardPanel2.setWaiting(true);
        boardPanel1.refresh(); boardPanel2.refresh();
        long durationSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000L;
        controller.recordFinishedGame(player1Name, player2Name, durationSeconds);
        GameController.GameSummaryDTO summary = controller.getGameSummaryDTO();
        GameResultDialog.ResultAction action = GameResultDialog.showResultDialog(SwingUtilities.getWindowAncestor(this), summary, durationSeconds, controller.getTotalSurprisesOpened());
        if (action == GameResultDialog.ResultAction.RESTART) {
            controller.restartGame(); startTimeMillis = System.currentTimeMillis(); if (gameTimer != null) gameTimer.restart();
            updateStatus(); updateTurnUI(); boardPanel1.refresh(); boardPanel2.refresh(); requestResizeBoards();
        } else if (action == GameResultDialog.ResultAction.EXIT) {
            controller.endGame(); if (onBackToStart != null) onBackToStart.run();
        }
    }

    private void resizeBoardsToFit() {
        if (wrap1 == null || wrap2 == null || boardPanel1 == null || boardPanel2 == null) return;

        int availW = Math.min(wrap1.getWidth(), wrap2.getWidth());
        int availH = Math.min(wrap1.getHeight(), wrap2.getHeight());

        if (availW <= 40 || availH <= 40) return;  // Need minimum space

        String diff = controller.getDifficultyName();

        // Get grid dimensions
        int rows = switch (diff) {
            case "MEDIUM" -> 13;
            case "HARD" -> 16;
            default -> 9;
        };
        int cols = rows;

        // *** KEY FIX: Unified cell size calculation with better proportions ***
        // Account for glow effect padding (approximately 48px total)
        int effectivePadding = 48;
        int usableW = availW - effectivePadding;
        int usableH = availH - effectivePadding;

        // Calculate maximum cell size that fits
        int cellFromWidth = usableW / cols;
        int cellFromHeight = usableH / rows;
        int cell = Math.min(cellFromWidth, cellFromHeight);

        // *** UNIFIED LIMITS: Same range for all difficulties ***
        int minCell = 22;  // Absolute minimum (readable)
        int maxCell = switch (diff) {
            case "EASY" -> 55;     // Generous max for Easy
            case "MEDIUM" -> 42;   // Balanced for Medium
            case "HARD" -> 36;     // Compact for Hard
            default -> 55;
        };

        // Clamp to range
        cell = Math.max(minCell, Math.min(maxCell, cell));

        // Apply to both boards
        boardPanel1.setCellSize(cell);
        boardPanel2.setCellSize(cell);}

    private void requestResizeBoards() {
        if (resizeStabilizer != null && resizeStabilizer.isRunning()) { resizeStabilizer.restart(); return; }
        resizeStabilizer = new Timer(40, e -> {
            if (wrap1 == null || wrap2 == null) return;
            resizeBoardsToFit(); boardPanel1.repaint(); boardPanel2.repaint();
            ((Timer) e.getSource()).stop();
        });
        resizeStabilizer.start();
    }
}