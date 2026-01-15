package View;

import Controller.GameController;
import util.LanguageManager;
import javax.swing.*;
import java.awt.*;

public class StartPanel extends JPanel {

    public interface StartGameListener {
        void onStartGame(String player1Name, String player2Name, String difficultyKey);
        void onBackToMenu();
    }

    private final StartGameListener listener;
    private BackgroundPanel bg;

    private NeonInputField p1Field;
    private NeonInputField p2Field;

    // Difficulty Buttons
    private IconToggleButton tEasy;
    private IconToggleButton tMed;
    private IconToggleButton tHard;

    private IconButton btnStart;
    private IconButton btnBack;
    private IconButton btnLanguage;

    // Labels that change text
    private NeonTextLabel lblPlayer1;
    private NeonTextLabel lblPlayer2;
    private JLabel lblLevel;

    // Level Info Box
    private JLabel lblLevelInfo;
    private String currentDifficulty = "EASY";

    // Language toast notification
    private JLabel toastLabel;
    private Timer toastTimer;

    // Level Colors
    private final Color colorEasy = new Color(120, 255, 170); // Green
    private final Color colorMed = new Color(80, 180, 255);  // Blue
    private final Color colorHard = new Color(255, 80, 80);   // Red

    public StartPanel(StartGameListener listener) {
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        bg = new BackgroundPanel("/ui/start/bG.png");
        bg.setLayout(null);
        add(bg, BorderLayout.CENTER);

        // -- PLAYER LABELS --
        lblPlayer1 = new NeonTextLabel("PLAYER 1", new Color(255, 80, 80));
        lblPlayer2 = new NeonTextLabel("PLAYER 2", new Color(80, 180, 255));
        lblPlayer1.setFont(new Font("Segoe UI", Font.BOLD, 32)); // Enlarged font
        lblPlayer2.setFont(new Font("Segoe UI", Font.BOLD, 32)); // Enlarged font
        bg.add(lblPlayer1);
        bg.add(lblPlayer2);

        // -- LEVEL LABEL --
        lblLevel = new JLabel("LEVEL:");
        lblLevel.setHorizontalAlignment(SwingConstants.CENTER);
        lblLevel.setForeground(Color.WHITE);
        lblLevel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 26));
        lblLevel.setOpaque(false);
        bg.add(lblLevel);

        // -- LEVEL INFO BOX --
        lblLevelInfo = new JLabel();
        lblLevelInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLevelInfo.setOpaque(true);
        lblLevelInfo.setBackground(Color.BLACK);
        bg.add(lblLevelInfo);

        // -- INPUT FIELDS --
        p1Field = new NeonInputField("PLAYER 1", new Color(255, 80, 80));
        p2Field = new NeonInputField("PLAYER 2", new Color(80, 180, 255));
        bg.add(p1Field);
        bg.add(p2Field);

        // -- TOGGLE BUTTONS --
        tEasy = new IconToggleButton("/ui/start/easy_btn.png", colorEasy);
        tMed = new IconToggleButton("/ui/start/medium_btn.png", colorMed);
        tHard = new IconToggleButton("/ui/start/hard_btn.png", colorHard);

        Font diffFont = new Font("Arial Black", Font.PLAIN, 28);
        tEasy.setFont(diffFont);
        tMed.setFont(diffFont);
        tHard.setFont(diffFont);

        ButtonGroup group = new ButtonGroup();
        group.add(tEasy);
        group.add(tMed);
        group.add(tHard);

        tEasy.setSelected(true);

        tEasy.addActionListener(e -> updateLevelInfo("EASY"));
        tMed.addActionListener(e -> updateLevelInfo("MEDIUM"));
        tHard.addActionListener(e -> updateLevelInfo("HARD"));

        bg.add(tEasy);
        bg.add(tMed);
        bg.add(tHard);

        // -- ACTION BUTTONS --
        btnStart = new IconButton("/ui/start/start_btn.png");
        btnStart.setOnClick(this::handleStart);
        bg.add(btnStart);

        btnBack = new IconButton("/ui/icons/back.png");
        btnBack.setOnClick(() -> listener.onBackToMenu());
        bg.add(btnBack);

        // -- LANGUAGE TOGGLE BUTTON --
        btnLanguage = new IconButton("/ui/menu/lang_btn.png", true);
        btnLanguage.setOnClick(this::toggleLanguage);
        bg.add(btnLanguage);

        // -- LANGUAGE TOAST NOTIFICATION --
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

        // -- INITIALIZATION --
        updateLevelInfo("EASY");
        updateUIText();

        // -- LAYOUT --
        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutByRatio();
            }
        });

        SwingUtilities.invokeLater(this::layoutByRatio);
    }

    private void updateUIText() {
        boolean isHe = GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE;

        if (isHe) {
            lblLevel.setText("רמת קושי:");
            lblPlayer1.setText("שחקן 1");
            lblPlayer2.setText("שחקן 2");
        } else {
            lblLevel.setText("LEVEL:");
            lblPlayer1.setText("PLAYER 1");
            lblPlayer2.setText("PLAYER 2");
        }
        updateButtonImages(isHe);
    }

    private void updateButtonImages(boolean isHe) {
        String startPath = isHe ? "/ui/start/hebrewStartGameBtn.png" : "/ui/start/start_btn.png";

        String easyPath = isHe ? "/ui/start/hebrewEasyBtn.png" : "/ui/start/easy_btn.png";
        String medPath  = isHe ? "/ui/start/hebrewMediumBtn.png" : "/ui/start/medium_btn.png";
        String hardPath = isHe ? "/ui/start/hebrewHardBtn.png" : "/ui/start/hard_btn.png";

        btnStart.setIconPath(startPath);
        tEasy.setIconPath(easyPath);
        tMed.setIconPath(medPath);
        tHard.setIconPath(hardPath);
        repaint();
    }

    private void updateLevelInfo(String level) {
        this.currentDifficulty = level;
        String sizeStr;
        int lives, mines, questions, surprises;
        Color activeColor;

        switch (level) {
            case "EASY" -> {
                sizeStr = "9x9"; lives = 10; mines = 10; questions = 6; surprises = 2;
                activeColor = colorEasy;
            }
            case "MEDIUM" -> {
                sizeStr = "13x13"; lives = 8; mines = 26; questions = 7; surprises = 3;
                activeColor = colorMed;
            }
            case "HARD" -> {
                sizeStr = "16x16"; lives = 6; mines = 44; questions = 11; surprises = 4;
                activeColor = colorHard;
            }
            default -> {
                sizeStr = "9x9"; lives = 10; mines = 10; questions = 6; surprises = 2;
                activeColor = colorEasy;
            }
        }

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isHe = (lang == LanguageManager.Language.HE);

        String infoHtml;
        String hexColor = String.format("#%02x%02x%02x", activeColor.getRed(), activeColor.getGreen(), activeColor.getBlue());

        if (isHe) {
            infoHtml = "<html><div style='text-align: center; direction: rtl; color: " + hexColor + "; font-family: Arial; font-size: 13px;'>" +
                    "<b>" + lives + " חיים משותפים" + "</b><br>" +
                    "<b>" + "לוח " + sizeStr + "</b><br>" +
                    "<b>" + mines + " מוקשים לשחקן" + "</b><br>" +
                    "<span style='font-size: 10px;'>" + surprises + " הפתעות | " + questions + " שאלות" + "</span>" +
                    "</div></html>";
        } else {
            String line1 = "Board " + sizeStr + " | " + lives + " Shared Lives";
            String line2 = mines + " Mines to play";
            String line3 = questions + " Questions | " + surprises + " Surprises";

            infoHtml = "<html><div style='text-align: center; color: " + hexColor + "; font-family: Arial; font-size: 13px;'>" +
                    "<b>" + line1 + "</b><br>" +
                    "<b>" + line2 + "</b><br>" +
                    "<span style='font-size: 10px;'>" + line3 + "</span>" +
                    "</div></html>";
        }

        lblLevelInfo.setText(infoHtml);
        lblLevelInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activeColor, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        if (bg != null && bg.getWidth() > 0) {
            layoutByRatio();
        }
    }

    /**
     * FINAL CORRECTED LAYOUT: Difficulty buttons much larger, Start button height doubled
     */
    private void layoutByRatio() {
        int W = bg.getWidth();
        int H = bg.getHeight();
        if (W <= 0 || H <= 0) return;

        // Player Labels
        setBoundsRatio(lblPlayer1, 0.19, 0.28, 0.30, 0.06, W, H);
        setBoundsRatio(lblPlayer2, 0.51, 0.28, 0.30, 0.06, W, H);

        // Player Inputs
        setBoundsRatio(p1Field, 0.20, 0.33, 0.28, 0.12, W, H);
        setBoundsRatio(p2Field, 0.52, 0.33, 0.28, 0.12, W, H);

        // LEVEL LABEL - moved up slightly
        setBoundsRatio(lblLevel, 0.41, 0.45, 0.18, 0.06, W, H);

        // DIFFICULTY BUTTONS - SIGNIFICANTLY LARGER
        // Each button now takes about 21% width and 10% height
        int btnW = (int) (W * 0.21);
        int btnH = (int) (H * 0.10);
        int gap = (int) (W * 0.02);

        int totalButtonsWidth = btnW * 3 + gap * 2;
        int startXButtons = (W - totalButtonsWidth) / 2;
        int yButtons = (int) (H * 0.52);

        tEasy.setBounds(startXButtons, yButtons, btnW, btnH);
        tMed.setBounds(startXButtons + btnW + gap, yButtons, btnW, btnH);
        tHard.setBounds(startXButtons + (btnW + gap) * 2, yButtons, btnW, btnH);

        // Level Info Box - positioned below selected button
        int targetX;
        if ("MEDIUM".equals(currentDifficulty)) targetX = tMed.getX();
        else if ("HARD".equals(currentDifficulty)) targetX = tHard.getX();
        else targetX = tEasy.getX();

        Dimension prefSize = lblLevelInfo.getPreferredSize();
        int infoW = prefSize.width + 10;
        int infoH = prefSize.height;
        int infoX = targetX + (btnW - infoW) / 2;
        int infoY = yButtons + btnH + 10;
        lblLevelInfo.setBounds(infoX, infoY, infoW, infoH);

        // START BUTTON - Same height as level buttons, much wider, positioned higher
        int startBtnW = (int) (W * 0.30); // Much wider - 50% of screen width
        int startBtnH = (int)(H*0.30); // Same height as difficulty buttons
        int startBtnX = (W - startBtnW) / 2;
        int startBtnY = (int) (H * 0.63); // Moved up from 0.73
        btnStart.setBounds(startBtnX, startBtnY, startBtnW, startBtnH);

        // Back button (bottom left)
        setBoundsRatio(btnBack, 0.03, 0.92, 0.04, 0.06, W, H);

        // Language button (bottom right, matching MainMenuPanel)
        int langBtnSize = 50;
        int langMargin = 75;
        int langX = W - langMargin;
        int langY = H - langMargin;
        btnLanguage.setBounds(langX, langY, langBtnSize, langBtnSize);

        // Toast notification label (positioned above language button)
        if (toastLabel.isVisible()) {
            int lblW = toastLabel.getWidth();
            int lblH = toastLabel.getHeight();
            toastLabel.setBounds(langX + (langBtnSize - lblW) / 2, langY - lblH - 10, lblW, lblH);
        }

        bg.revalidate();
        bg.repaint();
    }

    private void setBoundsRatio(JComponent c, double x, double y, double w, double h, int W, int H) {
        c.setBounds((int) (x * W), (int) (y * H), (int) (w * W), (int) (h * H));
    }

    private void toggleLanguage() {
        GameController gc = GameController.getInstance();
        if (gc.getCurrentLanguage() == LanguageManager.Language.EN) {
            gc.setCurrentLanguage(LanguageManager.Language.HE);
        } else {
            gc.setCurrentLanguage(LanguageManager.Language.EN);
        }
        updateUIText();
        updateLevelInfo(currentDifficulty); // Force update the level info box
        showLanguageToast();
        revalidate();
        repaint();
    }

    private void showLanguageToast() {
        boolean isHe = GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE;
        toastLabel.setText(isHe ? "עברית" : "English");

        // Calculate size based on text
        Dimension prefSize = toastLabel.getPreferredSize();
        int lblW = prefSize.width + 20;
        int lblH = 30;
        toastLabel.setSize(lblW, lblH);

        // Position above language button (bottom right)
        int W = bg.getWidth();
        int H = bg.getHeight();
        int langBtnSize = 50;
        int langMargin = 75;
        int langX = W - langMargin;
        int langY = H - langMargin;

        // Center horizontally relative to language button, positioned above it
        toastLabel.setBounds(langX + (langBtnSize - lblW) / 2, langY - lblH - 10, lblW, lblH);

        toastLabel.setVisible(true);
        toastTimer.restart();
    }

    private void handleStart() {
        String p1 = p1Field.getText().trim();
        String p2 = p2Field.getText().trim();
        if (p1.isEmpty() || p2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter names for both players.", "Missing Names", JOptionPane.WARNING_MESSAGE);
            return;
        }
        listener.onStartGame(p1, p2, currentDifficulty);
    }

    public void resetFields() {
        p1Field.setText("");
        p2Field.setText("");
        tEasy.setSelected(true);
        updateLevelInfo("EASY");
        updateUIText();
    }
}