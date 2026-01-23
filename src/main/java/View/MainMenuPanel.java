package View;

import Controller.GameController;
import util.LanguageManager;
import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    public interface MainMenuListener {
        void onStartGameClicked();
        void onHistoryClicked();
        void onHowToPlayClicked();
        void onManageQuestionsClicked();
        void onLanguageToggle();
    }

    private final MainMenuListener listener;
    private final BackgroundPanel bg;

    private IconButton btnStart;
    private IconButton btnHistory;
    private IconButton btnHowTo;
    private IconButton btnAdmin;
    private final IconButton btnLanguage;

    private final JLabel toastLabel;
    private final Timer toastTimer;

    // --- Thinking Icon Path ---
    private static final String THINKING_ICON = "/ui/menu/thinking.png";

    public MainMenuPanel(MainMenuListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());

        bg = new BackgroundPanel("/ui/menu/bg.png");
        bg.setLayout(null);
        add(bg, BorderLayout.CENTER);

        createButtons();

        // -- LANGUAGE BUTTON --
        btnLanguage = new IconButton("/ui/menu/lang_btn.png", true);
        btnLanguage.setOnClick(this::handleLanguageSwitch);
        bg.add(btnLanguage);

        // -- NOTIFICATION LABEL --
        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setOpaque(true);
        toastLabel.setBackground(new Color(0, 0, 0, 180));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toastLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 1));
        toastLabel.setVisible(false);
        bg.add(toastLabel);

        toastTimer = new Timer(2000, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                revalidate();
                repaint();
            }
        });

        // Pre-load images
        new Thread(() -> {
            String[] preloadPaths = {
                    "/ui/menu/hebrewNewGame.png", "/ui/menu/hebrewHistoryGames.png",
                    "/ui/menu/hebrewHowToPlay.png", "/ui/menu/hebrewQuestionManager.png",
                    // Arabic icons (you'll need to create these)
                    "/ui/menu/arabicNewGame.png", "/ui/menu/arabicHistoryGames.png",
                    "/ui/menu/arabicHowToPlay.png", "/ui/menu/arabicQuestionManager.png",
                    // Russian icons (you'll need to create these)
                    "/ui/menu/russianNewGame.png", "/ui/menu/russianHistoryGames.png",
                    "/ui/menu/russianHowToPlay.png", "/ui/menu/russianQuestionManager.png",
                    // Spanish icons (you'll need to create these)
                    "/ui/menu/spanishNewGame.png", "/ui/menu/spanishHistoryGames.png",
                    "/ui/menu/spanishHowToPlay.png", "/ui/menu/spanishQuestionManager.png",
                    THINKING_ICON
            };
            for (String p : preloadPaths) {
                try { new IconButton(p, true); } catch (Exception ignored) {}
            }
        }).start();
    }

    /**
     * Handles the threading logic for language switching
     * Now cycles through all languages: EN -> HE -> AR -> RU -> ES -> EN ...
     */
    private void handleLanguageSwitch() {
        // 1. Show thinking icon immediately
        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null); // Disable clicks

        // 2. Background Thread
        new Thread(() -> {
            try {
                // A. Cycle to next language
                cycleLanguage();

                // B. Heavy Load
                GameController.getInstance().getQuestionManager().switchLanguageFromCache();

                // C. Visual Pause (optional, for effect)
                Thread.sleep(300);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 3. Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    refreshButtonIcons();
                    btnLanguage.setIconPath("/ui/menu/lang_btn.png");
                    showLanguageToast();
                    btnLanguage.setOnClick(this::handleLanguageSwitch); // Re-enable

                    if (listener != null) listener.onLanguageToggle();
                });
            }
        }).start();
    }

    private void showLanguageToast() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        toastLabel.setText(LanguageManager.getDisplayName(lang));
        toastLabel.setSize(toastLabel.getPreferredSize().width + 20, 30);
        doLayout();
        toastLabel.setVisible(true);
        toastTimer.restart();
    }

    private void createButtons() {
        btnStart = new IconButton(getIconPath("START"), true);
        btnHistory = new IconButton(getIconPath("HISTORY"), true);
        btnHowTo = new IconButton(getIconPath("HOWTO"), true);
        btnAdmin = new IconButton(getIconPath("ADMIN"), true);

        btnStart.setOnClick(() -> { if (listener != null) listener.onStartGameClicked(); });
        btnHistory.setOnClick(() -> { if (listener != null) listener.onHistoryClicked(); });
        btnHowTo.setOnClick(() -> { if (listener != null) listener.onHowToPlayClicked(); });
        btnAdmin.setOnClick(() -> { if (listener != null) listener.onManageQuestionsClicked(); });

        bg.add(btnStart);
        bg.add(btnHistory);
        bg.add(btnHowTo);
        bg.add(btnAdmin);
    }

    private void refreshButtonIcons() {
        btnStart.setIconPath(getIconPath("START"));
        btnHistory.setIconPath(getIconPath("HISTORY"));
        btnHowTo.setIconPath(getIconPath("HOWTO"));
        btnAdmin.setIconPath(getIconPath("ADMIN"));
        revalidate();
        repaint();
    }

    private String getIconPath(String action) {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        // Get language prefix for icon paths
        String langPrefix = switch (lang) {
            case EN -> "";  // English uses default names
            case HE -> "hebrew";
            case AR -> "arabic";
            case RU -> "russian";
            case ES -> "spanish";
        };

        // If English, use original path names
        if (lang == LanguageManager.Language.EN) {
            return switch (action) {
                case "START" -> "/ui/menu/start_new_game_btn.png";
                case "HISTORY" -> "/ui/menu/view_game_history_btn.png";
                case "HOWTO" -> "/ui/menu/how_to_play_btn.png";
                case "ADMIN" -> "/ui/menu/question_manager_btn.png";
                default -> "";
            };
        }

        // For other languages, use the naming pattern
        return switch (action) {
            case "START" -> "/ui/menu/" + langPrefix + "NewGame.png";
            case "HISTORY" -> "/ui/menu/" + langPrefix + "HistoryGames.png";
            case "HOWTO" -> "/ui/menu/" + langPrefix + "HowToPlay.png";
            case "ADMIN" -> "/ui/menu/" + langPrefix + "QuestionManager.png";
            default -> "";
        };
    }

    /**
     * Cycles to the next language in the rotation
     */
    private void cycleLanguage() {
        GameController gc = GameController.getInstance();
        LanguageManager.Language current = gc.getCurrentLanguage();
        LanguageManager.Language next = LanguageManager.getNextLanguage(current);
        gc.setCurrentLanguage(next);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        bg.setBounds(0, 0, getWidth(), getHeight());
        int W = bg.getWidth();
        int H = bg.getHeight();

        int leftMargin = (int) (W * 0.18);
        int topStart   = (int) (H * 0.31);
        int btnW       = (int) (W * 0.62);
        int btnH       = (int) (H * 0.090);
        int gap        = (int) (H * 0.030);

        btnStart.setBounds(leftMargin, topStart, btnW, btnH);
        btnHistory.setBounds(leftMargin, topStart + (btnH + gap), btnW, btnH);
        btnHowTo.setBounds(leftMargin, topStart + (btnH + gap) * 2, btnW, btnH);
        btnAdmin.setBounds(leftMargin, topStart + (btnH + gap) * 3, btnW, btnH);

        int langBtnSize = 50;
        int langMargin = 75;
        int langX = W - langMargin;
        int langY = H - langMargin;
        btnLanguage.setBounds(langX, langY, langBtnSize, langBtnSize);

        if (toastLabel.isVisible()) {
            int lblW = toastLabel.getWidth();
            int lblH = toastLabel.getHeight();
            toastLabel.setBounds(langX + (langBtnSize - lblW) / 2, langY - lblH - 10, lblW, lblH);
        }
    }

    /**
     * Refreshes the UI to match the current language setting.
     * Called when returning from other frames that may have changed the language.
     */
    public void refreshLanguage() {
        refreshButtonIcons();
        revalidate();
        repaint();
    }
}