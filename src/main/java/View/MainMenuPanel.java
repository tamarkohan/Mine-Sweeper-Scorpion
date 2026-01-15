package View;

import Controller.GameController;
import util.LanguageManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    // The new notification label
    private final JLabel toastLabel;
    private final Timer toastTimer;

    public MainMenuPanel(MainMenuListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());

        bg = new BackgroundPanel("/ui/menu/bg.png");
        bg.setLayout(null);
        add(bg, BorderLayout.CENTER);

        createButtons();

        // -- LANGUAGE BUTTON --
        btnLanguage = new IconButton("/ui/menu/lang_btn.png", true);
        btnLanguage.setOnClick(() -> {
            toggleLanguageLogic();
            refreshButtonIcons();
            showLanguageToast(); // Show the text label
            if (listener != null) listener.onLanguageToggle();
        });
        bg.add(btnLanguage);

        // -- NOTIFICATION LABEL (Small text on black background) --
        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setOpaque(true);
        toastLabel.setBackground(new Color(0, 0, 0, 180)); // Semi-transparent black
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toastLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 1)); // Neon blue border
        toastLabel.setVisible(false);
        bg.add(toastLabel);

        // Timer to hide text after 2 seconds
        toastTimer = new Timer(2000, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                revalidate();
                repaint();
            }
        });
    }

    private void showLanguageToast() {
        boolean isHe = GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE;
        toastLabel.setText(isHe ? "עברית" : "English");
        toastLabel.setSize(toastLabel.getPreferredSize().width + 20, 30);

        // Force re-layout to position it correctly above the button
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
        boolean isHe = GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE;
        // Updated to match your file structure (.png for both english and hebrew)
        return switch (action) {
            case "START" -> isHe ? "/ui/menu/hebrewNewGame.png" : "/ui/menu/start_new_game_btn.png";
            case "HISTORY" -> isHe ? "/ui/menu/hebrewHistoryGames.png" : "/ui/menu/view_game_history_btn.png";
            case "HOWTO" -> isHe ? "/ui/menu/hebrewHowToPlay.png" : "/ui/menu/how_to_play_btn.png";
            case "ADMIN" -> isHe ? "/ui/menu/hebrewQuestionManager.png" : "/ui/menu/question_manager_btn.png";
            default -> "";
        };
    }

    private void toggleLanguageLogic() {
        GameController gc = GameController.getInstance();
        if (gc.getCurrentLanguage() == LanguageManager.Language.EN) {
            gc.setCurrentLanguage(LanguageManager.Language.HE);
        } else {
            gc.setCurrentLanguage(LanguageManager.Language.EN);
        }
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

        // Language Button: Bottom Right
        int langBtnSize = 50;
        int langMargin = 75;
        int langX = W - langMargin;
        int langY = H - langMargin;
        btnLanguage.setBounds(langX, langY, langBtnSize, langBtnSize);

        // Notification Label: Positioned just above the language button
        if (toastLabel.isVisible()) {
            int lblW = toastLabel.getWidth();
            int lblH = toastLabel.getHeight();
            // Centered horizontally relative to button, but 10px above it
            toastLabel.setBounds(langX + (langBtnSize - lblW) / 2, langY - lblH - 10, lblW, lblH);
        }
    }
}