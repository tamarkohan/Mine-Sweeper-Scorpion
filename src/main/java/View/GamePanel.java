package View;

import Controller.GameController;
import util.LanguageManager;
import util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Panel that always reports LEFT_TO_RIGHT so layout never flips in RTL locales.
 */
@SuppressWarnings("serial")
final class FixedLTRPanel extends JPanel {
    FixedLTRPanel(LayoutManager layout) { super(layout); }
    @Override
    public ComponentOrientation getComponentOrientation() { return ComponentOrientation.LEFT_TO_RIGHT; }
}

/**
 * Main in-game panel: displays two boards, player info, score, lives and controls.
 * Handles the "Thinking" animation during language switching.
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
    private JPanel topPanel;
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
    private static final int TOP_HEADER_HEIGHT = 240;

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

        String title = getExitTitle(lang);
        String message = getExitMessage(lang);
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

    private String getExitTitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "יציאה מהמשחק";
            case AR -> "الخروج من اللعبة";
            case RU -> "Выход из игры";
            case ES -> "Salir del juego";
            default -> "Exit Game";
        };
    }

    private String getExitMessage(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "האם אתה בטוח שברצונך לצאת?\nההתקדמות במשחק תאבד.";
            case AR -> "هل أنت متأكد أنك تريد الخروج؟\nسيتم فقدان تقدم اللعبة.";
            case RU -> "Вы уверены, что хотите выйти?\nПрогресс игры будет потерян.";
            case ES -> "¿Estás seguro de que quieres salir?\nEl progreso del juego se perderá.";
            default -> "Are you sure you want to exit?\nGame progress will be lost.";
        };
    }

    private void showRestartConfirmation() {
        LanguageManager.Language lang = controller.getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String title = getRestartTitle(lang);
        String message = getRestartMessage(lang);
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

    private String getRestartTitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "התחלה מחדש";
            case AR -> "إعادة تشغيل اللعبة";
            case RU -> "Перезапуск игры";
            case ES -> "Reiniciar juego";
            default -> "Restart Game";
        };
    }

    private String getRestartMessage(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "האם אתה בטוח שברצונך להתחיל מחדש?\nההתקדמות הנוכחית תאבד.";
            case AR -> "هل أنت متأكد أنك تريد إعادة التشغيل؟\nسيتم فقدان التقدم الحالي.";
            case RU -> "Вы уверены, что хотите перезапустить?\nТекущий прогресс будет потерян.";
            case ES -> "¿Estás seguro de que quieres reiniciar?\nEl progreso actual se perderá.";
            default -> "Are you sure you want to restart?\nCurrent progress will be lost.";
        };
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        String levelName = controller.getDifficultyName();
        String bgPath = switch (levelName) {
            case "MEDIUM" -> "/ui/game/bg_medium.png";
            case "HARD" -> "/ui/game/bg_hard.png";
            default -> "/ui/game/bg_easy.png";
        };

        BackgroundPanel bg = new BackgroundPanel(bgPath);
        bg.setLayout(new BorderLayout());
        bg.setOpaque(false);
        bg.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        add(bg, BorderLayout.CENTER);

        centerPanel = new FixedLTRPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        bg.add(centerPanel, BorderLayout.CENTER);

        // *** INCREASED: Dynamic header height based on difficulty ***
        int headerHeight = switch (levelName) {
            case "EASY" -> 240;      // Was 200
            case "MEDIUM" -> 260;    // Was 220
            case "HARD" -> 270;      // Was 230
            default -> 240;
        };

        Dimension boxDim = new Dimension(FIXED_BOX_WIDTH, FIXED_BOX_HEIGHT);
        Dimension headerDim = new Dimension(100, headerHeight);

        // --- Player 1 Side (displayed on RIGHT) ---
        JPanel leftSide = new JPanel(new BorderLayout());
        leftSide.setOpaque(false);
        leftSide.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JPanel leftTopGroup = new JPanel();
        leftTopGroup.setLayout(new BoxLayout(leftTopGroup, BoxLayout.Y_AXIS));
        leftTopGroup.setOpaque(false);
        leftTopGroup.setPreferredSize(headerDim);
        leftTopGroup.setMinimumSize(headerDim);
        leftTopGroup.setMaximumSize(headerDim);

        playerBox1 = new NeonInputField(new Color(255, 80, 80));
        playerBox1.setText(LanguageManager.get("player1", controller.getCurrentLanguage())); // ימין: اللاعب 1
        playerBox1.setDisplayMode(true);
        playerBox1.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox1.setFieldWidth(FIXED_BOX_WIDTH);
        playerBox1.setPreferredSize(boxDim);
        playerBox1.setMaximumSize(boxDim);

        lblMinesLeft1 = new JLabel(getMinesLeftText(1), SwingConstants.CENTER);
        lblMinesLeft1.setForeground(Color.WHITE);
        lblMinesLeft1.setFont(new Font("Dialog", Font.BOLD, 14));
        lblMinesLeft1.setAlignmentX(Component.CENTER_ALIGNMENT);

        // *** INCREASED: Dynamic top spacing ***
        int topSpacing = switch (levelName) {
            case "EASY" -> 160;      // Was 120
            case "MEDIUM" -> 180;    // Was 140
            case "HARD" -> 195;      // Was 155
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

        // --- Player 2 Side (displayed on LEFT) ---
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JPanel rightTopGroup = new JPanel();
        rightTopGroup.setLayout(new BoxLayout(rightTopGroup, BoxLayout.Y_AXIS));
        rightTopGroup.setOpaque(false);
        rightTopGroup.setPreferredSize(headerDim);
        rightTopGroup.setMinimumSize(headerDim);
        rightTopGroup.setMaximumSize(headerDim);

        playerBox2 = new NeonInputField(new Color(80, 180, 255));
        playerBox2.setText(LanguageManager.get("player2", controller.getCurrentLanguage())); // שמאל: اللاعب 2
        playerBox2.setDisplayMode(true);
        playerBox2.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox2.setFieldWidth(FIXED_BOX_WIDTH);
        playerBox2.setPreferredSize(boxDim);
        playerBox2.setMaximumSize(boxDim);

        lblMinesLeft2 = new JLabel(getMinesLeftText(2), SwingConstants.CENTER);
        lblMinesLeft2.setForeground(Color.WHITE);
        lblMinesLeft2.setFont(new Font("Dialog", Font.BOLD, 14));
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

        // Force physical positions: gridx=0 = left (Player 2), gridx=1 = right (Player 1)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 20);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy = 0;
        gbc.gridx = 0;
        centerPanel.add(rightSide, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 20, 0, 0);
        centerPanel.add(leftSide, gbc);
        topPanel = centerPanel;
        applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        topPanel.revalidate();
        topPanel.repaint();

        // --- Footer ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(1, 145));

        JPanel scoreLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        scoreLivesPanel.setOpaque(false);

        LanguageManager.Language lang = controller.getCurrentLanguage();
        lblScore = new JLabel(LanguageManager.get("score", lang) + ": 0");
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(new Font("Dialog", Font.BOLD, 18));

        lblLives = new JLabel(LanguageManager.get("lives", lang) + ": " + controller.getSharedLives() + "/" + controller.getMaxLives());
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(new Font("Dialog", Font.BOLD, 18));

        lblTime = new JLabel(LanguageManager.get("time", lang) + ": 00:00");
        lblTime.setForeground(Color.WHITE);
        lblTime.setFont(new Font("Dialog", Font.BOLD, 18));

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

    private void showLanguagePopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(new Color(30, 30, 40));

        for (LanguageManager.Language lang : LanguageManager.Language.values()) {
            JMenuItem item = new JMenuItem(LanguageManager.getDisplayName(lang));
            item.setBackground(new Color(30, 30, 40));
            item.setForeground(Color.WHITE);
            item.setFont(new Font("Dialog", Font.PLAIN, 14));

            if (lang == controller.getCurrentLanguage()) {
                item.setForeground(new Color(0, 255, 255));
                item.setFont(new Font("Dialog", Font.BOLD, 14));
            }

            item.addActionListener(e -> handleLanguageSelection(lang));
            popup.add(item);
        }

        popup.show(btnLanguage, 0, -popup.getPreferredSize().height);
    }

    private void handleLanguageSelection(LanguageManager.Language newLang) {
        if (newLang == controller.getCurrentLanguage()) return;

        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null);

        new Thread(() -> {
            try {
                GameController gc = GameController.getInstance();
                gc.setCurrentLanguage(newLang);
                gc.getQuestionManager().switchLanguageFromCache();
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    updateAllLanguageTexts();
                    boardPanel1.refresh();
                    boardPanel2.refresh();
                    showLanguageToast();
                    btnLanguage.setIconPath("/ui/icons/language.png");
                    btnLanguage.setOnClick(this::showLanguagePopup);
                });
            }
        }).start();
    }

    private void updateAllLanguageTexts() {
        LanguageManager.Language lang = controller.getCurrentLanguage();
        playerBox1.setText(LanguageManager.get("player1", lang)); // ימין: اللاعب 1
        playerBox2.setText(LanguageManager.get("player2", lang)); // שמאל: اللاعب 2
        lblMinesLeft1.setText(getMinesLeftText(1));
        lblMinesLeft2.setText(getMinesLeftText(2));
        lblScore.setText(LanguageManager.get("score", lang) + ": " + controller.getSharedScore());
        lblLives.setText(LanguageManager.get("lives", lang) + ": " + controller.getSharedLives() + "/" + controller.getMaxLives());

        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / 1000) / 60;
        lblTime.setText(LanguageManager.get("time", lang) + String.format(": %02d:%02d", minutes, seconds));

        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        if (topPanel != null) { topPanel.revalidate(); topPanel.repaint(); }
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
        label.setFont(new Font("Dialog", Font.BOLD, 14));
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

        // Queue visual effect for reward reveals BEFORE refresh (so BoardPanel knows not to play mine sound)
        if (outcomeMessage != null) {
            if (outcomeMessage.contains("revealed 1 mine")) {
                boardPanel1.queueEffect(BoardPanel.EffectType.REVEAL_1_MINE);
                boardPanel2.queueEffect(BoardPanel.EffectType.REVEAL_1_MINE);
            } else if (outcomeMessage.contains("revealed random 3x3")) {
                boardPanel1.queueEffect(BoardPanel.EffectType.REVEAL_3X3);
                boardPanel2.queueEffect(BoardPanel.EffectType.REVEAL_3X3);
            }
            showResultDialog(outcomeMessage);
        }

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

        String lower = message.toLowerCase();

        if (lower.contains("good surprise") || (lower.contains("surprise") && lower.contains("good")) || lower.contains("result: good")) {
            SoundManager.correctAnswer();
        } else if (lower.contains("bad surprise") || (lower.contains("surprise") && lower.contains("bad")) || lower.contains("result: bad")) {
            SoundManager.wrongAnswer();
        } else if (lower.contains("correct")) {
            SoundManager.correctAnswer();
        } else if (lower.contains("wrong")) {
            SoundManager.wrongAnswer();
        }

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
        if (lower.contains("correct")) {
            return switch (lang) {
                case HE -> "נכון!";
                case AR -> "صحيح!";
                case RU -> "Правильно!";
                case ES -> "¡Correcto!";
                default -> "Correct!";
            };
        } else if (lower.contains("wrong")) {
            return switch (lang) {
                case HE -> "שגוי!";
                case AR -> "خطأ!";
                case RU -> "Неправильно!";
                case ES -> "¡Incorrecto!";
                default -> "Wrong!";
            };
        } else if (lower.contains("good surprise") || (lower.contains("surprise") && lower.contains("good"))) {
            return switch (lang) {
                case HE -> "הפתעה טובה!";
                case AR -> "مفاجأة جيدة!";
                case RU -> "Хороший сюрприз!";
                case ES -> "¡Buena sorpresa!";
                default -> "Good Surprise!";
            };
        } else if (lower.contains("bad surprise") || (lower.contains("surprise") && lower.contains("bad"))) {
            return switch (lang) {
                case HE -> "הפתעה רעה!";
                case AR -> "مفاجأة سيئة!";
                case RU -> "Плохой сюрприз!";
                case ES -> "¡Mala sorpresa!";
                default -> "Bad Surprise!";
            };
        } else if (lower.contains("surprise")) {
            return switch (lang) {
                case HE -> "הפתעה!";
                case AR -> "مفاجأة!";
                case RU -> "Сюрприз!";
                case ES -> "¡Sorpresa!";
                default -> "Surprise!";
            };
        } else if (lower.contains("flag")) {
            return switch (lang) {
                case HE -> "דגל";
                case AR -> "علم";
                case RU -> "Флаг";
                case ES -> "Bandera";
                default -> "Flag";
            };
        }
        return switch (lang) {
            case HE -> "תוצאה";
            case AR -> "النتيجة";
            case RU -> "Результат";
            case ES -> "Resultado";
            default -> "Result";
        };
    }

    private String translateToastMessage(String message, LanguageManager.Language lang) {
        if (message == null || lang == LanguageManager.Language.EN) return message;
        String result = message;

        // Language-specific translations
        if (lang == LanguageManager.Language.HE) {
            result = result.replace("EASY", "קל").replace("MEDIUM", "בינוני").replace("HARD", "קשה").replace("EXPERT", "מומחה");
            result = result.replace("Wrong!", "שגוי!").replace("Wrong ", "שגוי ").replace("Wrong:", "שגוי:");
            result = result.replace("Correct!", "נכון!").replace("Correct ", "נכון ").replace("Correct:", "נכון:");
            result = result.replace("Surprise activated!", "ההפתעה הופעלה!");
            result = result.replace("Surprise result:", "תוצאת ההפתעה:");
            result = result.replace("Reward:", "פרס:").replace("Penalty:", "עונש:");
            // Special effect translations
            result = result.replace("Special effect:", "אפקט מיוחד:");
            result = result.replace("reveal random 3x3", "חשיפת 3x3 אקראי");
            result = result.replace("revealed random 3x3 area", "נחשף אזור 3x3 אקראי");
            result = result.replace("reveal 1 mine", "חשיפת מוקש אחד");
            result = result.replace("revealed 1 mine", "נחשף מוקש אחד");
            result = result.replace("no unrevealed mines to show", "אין מוקשים לחשיפה");
            result = result.replace("no unrevealed cells for 3x3 area", "אין תאים לחשיפת 3x3");
            result = result.replace("(Easy game)", "(משחק קל)");
            result = result.replace("(reward)", "(פרס)");
            // New luck mechanic translations
            result = result.replace("(50% luck)", "(50% מזל)");
            result = result.replace("(50% luck between -1 or -2 lives)", "(50% מזל בין -1 ל-2 חיים)");
            result = result.replace("(50% luck between +1 or +2 lives)", "(50% מזל בין +1 ל+2 חיים)");
            result = result.replace("Unlucky - penalty applied.", "לא בר מזל - העונש הופעל.");
            result = result.replace("Lucky - no penalty!", "בר מזל - ללא עונש!");
            result = result.replace("Result:", "תוצאה:");
            result = result.replace("reward", "פרס").replace("Reward", "פרס");
            result = result.replace("Activation cost:", "עלות הפעלה:").replace("Score:", "ניקוד:").replace("Lives:", "חיים:");
            result = result.replace("+1 life", "+1 חיים").replace("-1 life", "-1 חיים").replace("+2 lives", "+2 חיים").replace("-2 lives", "-2 חיים").replace("+3 lives", "+3 חיים").replace("-3 lives", "-3 חיים");
            result = result.replace(" pts,", " נק',").replace(" pts.", " נק'.").replace(" pts", " נק'");
            result = result.replace(" life.", " חיים.").replace(" life,", " חיים,").replace(" life", " חיים").replace(" lives", " חיים");
            result = result.replace("game.", "משחק.").replace("game)", "משחק)");
            result = result.replace("(Easy", "(קל").replace("(Medium", "(בינוני").replace("(Hard", "(קשה").replace("(Expert", "(מומחה");
            result = result.replace("area", "אזור").replace("mine", "מוקש");
            result = result.replace("Good surprise!", "הפתעה טובה!").replace("Bad surprise!", "הפתעה רעה!");
            result = result.replace("GOOD", "טוב").replace("BAD", "רע").replace("Good", "טוב").replace("Bad", "רע");
            result = result.replace("You didn't answer the question.", "לא ענית על השאלה.");
            result = result.replace("Activation cost was deducted.", "עלות ההפעלה נוכתה.");
        } else if (lang == LanguageManager.Language.AR) {
            result = result.replace("EASY", "سهل").replace("MEDIUM", "متوسط").replace("HARD", "صعب").replace("EXPERT", "خبير");
            result = result.replace("Wrong!", "خطأ!").replace("Wrong ", "خطأ ").replace("Wrong:", "خطأ:");
            result = result.replace("Correct!", "صحيح!").replace("Correct ", "صحيح ").replace("Correct:", "صحيح:");
            result = result.replace("Surprise activated!", "تم تفعيل المفاجأة!");
            result = result.replace("Surprise result:", "نتيجة المفاجأة:");
            result = result.replace("Reward:", "مكافأة:").replace("Penalty:", "عقوبة:");
            // Special effect translations
            result = result.replace("Special effect:", "تأثير خاص:");
            result = result.replace("reveal random 3x3", "كشف 3x3 عشوائي");
            result = result.replace("revealed random 3x3 area", "تم كشف منطقة 3x3 عشوائية");
            result = result.replace("reveal 1 mine", "كشف لغم واحد");
            result = result.replace("revealed 1 mine", "تم كشف لغم واحد");
            result = result.replace("no unrevealed mines to show", "لا توجد ألغام للكشف");
            result = result.replace("no unrevealed cells for 3x3 area", "لا توجد خلايا للكشف 3x3");
            result = result.replace("(Easy game)", "(لعبة سهلة)");
            result = result.replace("(reward)", "(مكافأة)");
            // New luck mechanic translations
            result = result.replace("(50% luck)", "(50% حظ)");
            result = result.replace("(50% luck between -1 or -2 lives)", "(50% حظ بين -1 او -2 ارواح)");
            result = result.replace("(50% luck between +1 or +2 lives)", "(50% حظ بين +1 او +2 ارواح)");
            result = result.replace("Unlucky - penalty applied.", "سيء الحظ - تم تطبيق العقوبة.");
            result = result.replace("Lucky - no penalty!", "محظوظ - بدون عقوبة!");
            result = result.replace("Result:", "النتيجة:");
            result = result.replace("Activation cost:", "تكلفة التفعيل:").replace("Score:", "النقاط:").replace("Lives:", "الأرواح:");
            result = result.replace("+1 life", "+1 حياة").replace("-1 life", "-1 حياة").replace("+2 lives", "+2 أرواح").replace("-2 lives", "-2 أرواح").replace("+3 lives", "+3 أرواح").replace("-3 lives", "-3 أرواح");
            result = result.replace(" pts,", " نقطة،").replace(" pts.", " نقطة.").replace(" pts", " نقطة");
            result = result.replace(" life,", " حياة،").replace(" life.", " حياة.").replace(" life", " حياة").replace(" lives", " أرواح");
            result = result.replace("Good surprise!", "مفاجأة جيدة!").replace("Bad surprise!", "مفاجأة سيئة!");
            result = result.replace("GOOD", "جيد").replace("BAD", "سيء").replace("Good", "جيد").replace("Bad", "سيء");
            result = result.replace("You didn't answer the question.", "لم تجب على السؤال.");
            result = result.replace("Activation cost was deducted.", "تم خصم تكلفة التفعيل.");
            // Arabic RTL: عرض التغيير بصيغة قبل/بعد بدل الأسهم (مثلاً: النقاط: قبل 85، بعد 96)
            result = result.replaceAll("(\\d+)\\s*->\\s*(\\d+)", "قبل $1، بعد $2");
        } else if (lang == LanguageManager.Language.RU) {
            result = result.replace("EASY", "Легкий").replace("MEDIUM", "Средний").replace("HARD", "Сложный").replace("EXPERT", "Эксперт");
            result = result.replace("Wrong!", "Неправильно!").replace("Wrong ", "Неправильно ").replace("Wrong:", "Неправильно:");
            result = result.replace("Correct!", "Правильно!").replace("Correct ", "Правильно ").replace("Correct:", "Правильно:");
            result = result.replace("Surprise activated!", "Сюрприз активирован!");
            result = result.replace("Surprise result:", "Результат сюрприза:");
            result = result.replace("Reward:", "Награда:").replace("Penalty:", "Штраф:");
            // Special effect translations
            result = result.replace("Special effect:", "Специальный эффект:");
            result = result.replace("reveal random 3x3", "раскрыть случайный 3x3");
            result = result.replace("revealed random 3x3 area", "раскрыта случайная область 3x3");
            result = result.replace("reveal 1 mine", "раскрыть 1 мину");
            result = result.replace("revealed 1 mine", "раскрыта 1 мина");
            result = result.replace("no unrevealed mines to show", "нет мин для раскрытия");
            result = result.replace("no unrevealed cells for 3x3 area", "нет клеток для раскрытия 3x3");
            result = result.replace("(Easy game)", "(Легкая игра)");
            result = result.replace("(reward)", "(награда)");
            // New luck mechanic translations
            result = result.replace("(50% luck)", "(50% удача)");
            result = result.replace("(50% luck between -1 or -2 lives)", "(50% удача между -1 или -2 жизни)");
            result = result.replace("(50% luck between +1 or +2 lives)", "(50% удача между +1 или +2 жизни)");
            result = result.replace("Unlucky - penalty applied.", "Не повезло - штраф применен.");
            result = result.replace("Lucky - no penalty!", "Повезло - без штрафа!");
            result = result.replace("Result:", "Результат:");
            result = result.replace("Activation cost:", "Стоимость активации:").replace("Score:", "Счёт:").replace("Lives:", "Жизни:");
            result = result.replace("+1 life", "+1 жизнь").replace("-1 life", "-1 жизнь").replace("+2 lives", "+2 жизни").replace("-2 lives", "-2 жизни").replace("+3 lives", "+3 жизни").replace("-3 lives", "-3 жизни");
            result = result.replace(" pts,", " очк.,").replace(" pts.", " очк.").replace(" pts", " очк.");
            result = result.replace(" life,", " жизнь,").replace(" life.", " жизнь.").replace(" life", " жизнь").replace(" lives", " жизни");
            result = result.replace("Good surprise!", "Хороший сюрприз!").replace("Bad surprise!", "Плохой сюрприз!");
            result = result.replace("GOOD", "Хорошо").replace("BAD", "Плохо").replace("Good", "Хорошо").replace("Bad", "Плохо");
            result = result.replace("You didn't answer the question.", "Вы не ответили на вопрос.");
            result = result.replace("Activation cost was deducted.", "Стоимость активации вычтена.");
        } else if (lang == LanguageManager.Language.ES) {
            result = result.replace("EASY", "Facil").replace("MEDIUM", "Medio").replace("HARD", "Dificil").replace("EXPERT", "Experto");
            result = result.replace("Wrong!", "Incorrecto!").replace("Wrong ", "Incorrecto ").replace("Wrong:", "Incorrecto:");
            result = result.replace("Correct!", "Correcto!").replace("Correct ", "Correcto ").replace("Correct:", "Correcto:");
            result = result.replace("Surprise activated!", "Sorpresa activada!");
            result = result.replace("Surprise result:", "Resultado de sorpresa:");
            result = result.replace("Reward:", "Recompensa:").replace("Penalty:", "Penalizacion:");
            // Special effect translations
            result = result.replace("Special effect:", "Efecto especial:");
            result = result.replace("reveal 1 mine", "revelar 1 mina");
            result = result.replace("revealed 1 mine", "se revelo 1 mina");
            result = result.replace("reveal random 3x3", "revelar 3x3 aleatorio");
            result = result.replace("revealed random 3x3 area", "se revelo area 3x3 aleatoria");
            result = result.replace("no unrevealed mines to show", "no hay minas para revelar");
            result = result.replace("no unrevealed cells for 3x3 area", "no hay celdas para revelar 3x3");
            result = result.replace("(Easy game)", "(juego Facil)");
            result = result.replace("(reward)", "(recompensa)");
            // New luck mechanic translations
            result = result.replace("(50% luck)", "(50% suerte)");
            result = result.replace("(50% luck between -1 or -2 lives)", "(50% suerte entre -1 o -2 vidas)");
            result = result.replace("(50% luck between +1 or +2 lives)", "(50% suerte entre +1 o +2 vidas)");
            result = result.replace("Unlucky - penalty applied.", "Sin suerte - penalizacion aplicada.");
            result = result.replace("Lucky - no penalty!", "Con suerte - sin penalizacion!");
            result = result.replace("Result:", "Resultado:");
            result = result.replace("Activation cost:", "Costo de activacion:").replace("Score:", "Puntos:").replace("Lives:", "Vidas:");
            result = result.replace("+1 life", "+1 vida").replace("-1 life", "-1 vida").replace("+2 lives", "+2 vidas").replace("-2 lives", "-2 vidas").replace("+3 lives", "+3 vidas").replace("-3 lives", "-3 vidas");
            result = result.replace(" pts,", " pts,").replace(" pts.", " pts.").replace(" pts", " pts");
            result = result.replace(" life,", " vida,").replace(" life.", " vida.").replace(" life", " vida").replace(" lives", " vidas");
            result = result.replace("Good surprise!", "Buena sorpresa!").replace("Bad surprise!", "Mala sorpresa!");
            result = result.replace("GOOD", "Bueno").replace("BAD", "Malo").replace("Good", "Bueno").replace("Bad", "Malo");
            result = result.replace("You didn't answer the question.", "No respondiste la pregunta.");
            result = result.replace("Activation cost was deducted.", "Se dedujo el costo de activacion.");
        }

        // Fix arrow for RTL: Arabic uses قبل/بعد (already applied above); Hebrew uses ← (96 ← 85)
        if (LanguageManager.isRTL(lang) && lang != LanguageManager.Language.AR) {
            result = result.replaceAll("(\\d+)\\s*->\\s*(\\d+)", "$2 \u2190 $1"); // Y ← X
        } else if (lang != LanguageManager.Language.AR) {
            result = result.replace("->", "\u2192"); // → for LTR
        }
        // RTL: تثبيت الإشارة قبل الرقم (+8 نقطة وليس 8+ نقطة) باستخدام LTR embedding
        if (LanguageManager.isRTL(lang)) {
            result = result.replaceAll("([+-]\\d+)", "\u202A$1\u202C");
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

        if (availW <= 40 || availH <= 40) return;

        String diff = controller.getDifficultyName();

        int rows = switch (diff) {
            case "MEDIUM" -> 13;
            case "HARD" -> 16;
            default -> 9;
        };
        int cols = rows;

        int effectivePadding = 48;
        int usableW = availW - effectivePadding;
        int usableH = availH - effectivePadding;

        int cellFromWidth = usableW / cols;
        int cellFromHeight = usableH / rows;
        int cell = Math.min(cellFromWidth, cellFromHeight);

        int minCell = 22;
        int maxCell = switch (diff) {
            case "EASY" -> 55;
            case "MEDIUM" -> 42;
            case "HARD" -> 36;
            default -> 55;
        };

        cell = Math.max(minCell, Math.min(maxCell, cell));

        boardPanel1.setCellSize(cell);
        boardPanel2.setCellSize(cell);
    }

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