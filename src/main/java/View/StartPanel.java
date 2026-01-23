package View;

import Controller.GameController;
import util.LanguageManager;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import util.SoundManager;

public class StartPanel extends JPanel {

    public interface StartGameListener {
        void onStartGame(String player1Name, String player2Name, String difficultyKey);
        void onBackToMenu();
    }

    private final StartGameListener listener;
    private BackgroundPanel bg;

    private NeonInputField p1Field;
    private NeonInputField p2Field;

    private IconToggleButton tEasy, tMed, tHard;
    private IconButton btnStart, btnBack, btnLanguage;

    private NeonTextLabel lblPlayer1, lblPlayer2;
    private JLabel lblLevel, lblLevelInfo;
    private String currentDifficulty = "EASY";

    private JLabel toastLabel;
    private Timer toastTimer;

    private final Color colorEasy = new Color(120, 255, 170);
    private final Color colorMed = new Color(80, 180, 255);
    private final Color colorHard = new Color(255, 80, 80);

    private static final String THINKING_ICON = "/ui/start/thinking.png";

    public StartPanel(StartGameListener listener) {
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        bg = new BackgroundPanel("/ui/start/bG.png");
        bg.setLayout(null);
        add(bg, BorderLayout.CENTER);

        lblPlayer1 = new NeonTextLabel("PLAYER 1", new Color(255, 80, 80));
        lblPlayer2 = new NeonTextLabel("PLAYER 2", new Color(80, 180, 255));
        lblPlayer1.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblPlayer2.setFont(new Font("Segoe UI", Font.BOLD, 32));
        bg.add(lblPlayer1);
        bg.add(lblPlayer2);

        lblLevel = new JLabel("LEVEL:");
        lblLevel.setHorizontalAlignment(SwingConstants.CENTER);
        lblLevel.setForeground(Color.WHITE);
        lblLevel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 26));
        lblLevel.setOpaque(false);
        bg.add(lblLevel);

        lblLevelInfo = new JLabel();
        lblLevelInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLevelInfo.setOpaque(true);
        lblLevelInfo.setBackground(Color.BLACK);
        bg.add(lblLevelInfo);

        p1Field = new NeonInputField("PLAYER 1", new Color(255, 80, 80));
        p2Field = new NeonInputField("PLAYER 2", new Color(80, 180, 255));

        applyCharacterLimit(p1Field.textField, 15);
        applyCharacterLimit(p2Field.textField, 15);

        addTypingSound(p1Field.textField);
        addTypingSound(p2Field.textField);

        bg.add(p1Field);
        bg.add(p2Field);

        tEasy = new IconToggleButton("/ui/start/easy_btn.png", colorEasy);
        tMed = new IconToggleButton("/ui/start/medium_btn.png", colorMed);
        tHard = new IconToggleButton("/ui/start/hard_btn.png", colorHard);

        Font diffFont = new Font("Arial Black", Font.PLAIN, 28);
        tEasy.setFont(diffFont); tMed.setFont(diffFont); tHard.setFont(diffFont);

        ButtonGroup group = new ButtonGroup();
        group.add(tEasy); group.add(tMed); group.add(tHard);
        tEasy.setSelected(true);

        tEasy.addActionListener(e -> updateLevelInfo("EASY"));
        tMed.addActionListener(e -> updateLevelInfo("MEDIUM"));
        tHard.addActionListener(e -> updateLevelInfo("HARD"));

        bg.add(tEasy); bg.add(tMed); bg.add(tHard);

        btnStart = new IconButton("/ui/start/start_btn.png");
        btnStart.setOnClick(this::handleStart);
        bg.add(btnStart);

        btnBack = new IconButton("/ui/icons/back.png");
        btnBack.setOnClick(() -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (util.ExitConfirmHelper.confirmExit(w)) {
                listener.onBackToMenu();
            }
        });
        bg.add(btnBack);

        btnLanguage = new IconButton("/ui/menu/lang_btn.png", true);
        btnLanguage.setOnClick(this::handleLanguageSwitch);
        bg.add(btnLanguage);

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

        updateLevelInfo("EASY");
        updateUIText();

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutByRatio();
            }
        });
        SwingUtilities.invokeLater(this::layoutByRatio);

        new Thread(() -> new IconButton(THINKING_ICON, true)).start();
    }

    private void handleLanguageSwitch() {
        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null);

        new Thread(() -> {
            try {
                cycleLanguage();
                GameController.getInstance().getQuestionManager().switchLanguageFromCache();
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    updateUIText();
                    updateLevelInfo(currentDifficulty);
                    showLanguageToast();
                    btnLanguage.setIconPath("/ui/menu/lang_btn.png");
                    btnLanguage.setOnClick(this::handleLanguageSwitch);
                    revalidate();
                    repaint();
                });
            }
        }).start();
    }

    private void applyCharacterLimit(JTextField textField, int limit) {
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                int overLimit = (currentLength + text.length()) - length - limit;
                if (overLimit > 0) text = text.substring(0, text.length() - overLimit);
                if (text.length() > 0) super.replace(fb, offset, length, text, attrs);
            }
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
        });
    }

    private void updateUIText() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        lblLevel.setText(LanguageManager.get("level", lang));
        lblPlayer1.setText(LanguageManager.get("player1", lang));
        lblPlayer2.setText(LanguageManager.get("player2", lang));
        updateButtonImages(lang);
    }

    private void updateButtonImages(LanguageManager.Language lang) {
        String langPrefix = switch (lang) {
            case EN -> "";
            case HE -> "hebrew";
            case AR -> "arabic";
            case RU -> "russian";
            case ES -> "spanish";
        };

        String startPath, easyPath, medPath, hardPath;

        if (lang == LanguageManager.Language.EN) {
            startPath = "/ui/start/start_btn.png";
            easyPath = "/ui/start/easy_btn.png";
            medPath = "/ui/start/medium_btn.png";
            hardPath = "/ui/start/hard_btn.png";
        } else {
            startPath = "/ui/start/" + langPrefix + "StartGameBtn.png";
            easyPath = "/ui/start/" + langPrefix + "EasyBtn.png";
            medPath = "/ui/start/" + langPrefix + "MediumBtn.png";
            hardPath = "/ui/start/" + langPrefix + "HardBtn.png";
        }

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
            case "EASY" -> { sizeStr = "9x9"; lives = 10; mines = 10; questions = 6; surprises = 2; activeColor = colorEasy; }
            case "MEDIUM" -> { sizeStr = "13x13"; lives = 8; mines = 26; questions = 7; surprises = 3; activeColor = colorMed; }
            case "HARD" -> { sizeStr = "16x16"; lives = 6; mines = 44; questions = 11; surprises = 4; activeColor = colorHard; }
            default -> { sizeStr = "9x9"; lives = 10; mines = 10; questions = 6; surprises = 2; activeColor = colorEasy; }
        }

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String hexColor = String.format("#%02x%02x%02x", activeColor.getRed(), activeColor.getGreen(), activeColor.getBlue());

        String sharedLivesText = LanguageManager.get("shared_lives", lang);
        String boardText = LanguageManager.get("board", lang);
        String minesText = LanguageManager.get("mines_per_player", lang);
        String questionsText = LanguageManager.get("questions_count", lang);
        String surprisesText = LanguageManager.get("surprises_count", lang);

        String infoHtml;
        if (isRTL) {
            infoHtml = "<html><div style='text-align: center; direction: rtl; color: " + hexColor + "; font-family: Arial; font-size: 13px;'>" +
                    "<b>" + lives + " " + sharedLivesText + "</b><br>" +
                    "<b>" + boardText + " " + sizeStr + "</b><br>" +
                    "<b>" + mines + " " + minesText + "</b><br>" +
                    "<span style='font-size: 10px;'>" + surprises + " " + surprisesText + " | " + questions + " " + questionsText + "</span></div></html>";
        } else {
            String line1 = boardText + " " + sizeStr + " | " + lives + " " + sharedLivesText;
            String line2 = mines + " " + minesText;
            String line3 = questions + " " + questionsText + " | " + surprises + " " + surprisesText;
            infoHtml = "<html><div style='text-align: center; color: " + hexColor + "; font-family: Arial; font-size: 13px;'>" +
                    "<b>" + line1 + "</b><br>" + "<b>" + line2 + "</b><br>" + "<span style='font-size: 10px;'>" + line3 + "</span></div></html>";
        }
        lblLevelInfo.setText(infoHtml);
        lblLevelInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(activeColor, 1), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        if (bg != null && bg.getWidth() > 0) layoutByRatio();
    }

    private void layoutByRatio() {
        int W = bg.getWidth(); int H = bg.getHeight();
        if (W <= 0 || H <= 0) return;
        setBoundsRatio(lblPlayer1, 0.19, 0.28, 0.30, 0.06, W, H);
        setBoundsRatio(lblPlayer2, 0.51, 0.28, 0.30, 0.06, W, H);
        setBoundsRatio(p1Field, 0.20, 0.33, 0.28, 0.12, W, H);
        setBoundsRatio(p2Field, 0.52, 0.33, 0.28, 0.12, W, H);
        setBoundsRatio(lblLevel, 0.41, 0.45, 0.18, 0.06, W, H);
        int btnW = (int) (W * 0.21); int btnH = (int) (H * 0.10); int gap = (int) (W * 0.02);
        int totalButtonsWidth = btnW * 3 + gap * 2; int startXButtons = (W - totalButtonsWidth) / 2; int yButtons = (int) (H * 0.52);
        tEasy.setBounds(startXButtons, yButtons, btnW, btnH);
        tMed.setBounds(startXButtons + btnW + gap, yButtons, btnW, btnH);
        tHard.setBounds(startXButtons + (btnW + gap) * 2, yButtons, btnW, btnH);
        int targetX;
        if ("MEDIUM".equals(currentDifficulty)) targetX = tMed.getX();
        else if ("HARD".equals(currentDifficulty)) targetX = tHard.getX();
        else targetX = tEasy.getX();
        Dimension prefSize = lblLevelInfo.getPreferredSize();
        int infoW = prefSize.width + 10; int infoH = prefSize.height; int infoX = targetX + (btnW - infoW) / 2; int infoY = yButtons + btnH + 10;
        lblLevelInfo.setBounds(infoX, infoY, infoW, infoH);
        int startBtnW = (int) (W * 0.30); int startBtnH = (int)(H*0.30); int startBtnX = (W - startBtnW) / 2; int startBtnY = (int) (H * 0.63);
        btnStart.setBounds(startBtnX, startBtnY, startBtnW, startBtnH);
        setBoundsRatio(btnBack, 0.03, 0.92, 0.04, 0.06, W, H);
        int langBtnSize = 50; int langMargin = 75; int langX = W - langMargin; int langY = H - langMargin;
        btnLanguage.setBounds(langX, langY, langBtnSize, langBtnSize);
        if (toastLabel.isVisible()) {
            int lblW = toastLabel.getWidth(); int lblH = toastLabel.getHeight();
            toastLabel.setBounds(langX + (langBtnSize - lblW) / 2, langY - lblH - 10, lblW, lblH);
        }
        bg.revalidate(); bg.repaint();
    }

    private void setBoundsRatio(JComponent c, double x, double y, double w, double h, int W, int H) {
        c.setBounds((int) (x * W), (int) (y * H), (int) (w * W), (int) (h * H));
    }

    private void cycleLanguage() {
        GameController gc = GameController.getInstance();
        LanguageManager.Language current = gc.getCurrentLanguage();
        LanguageManager.Language next = LanguageManager.getNextLanguage(current);
        gc.setCurrentLanguage(next);
    }

    private void showLanguageToast() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        toastLabel.setText(LanguageManager.getDisplayName(lang));
        Dimension prefSize = toastLabel.getPreferredSize();
        int lblW = prefSize.width + 20; int lblH = 30;
        toastLabel.setSize(lblW, lblH);
        int W = bg.getWidth(); int H = bg.getHeight();
        int langBtnSize = 50; int langMargin = 75; int langX = W - langMargin; int langY = H - langMargin;
        toastLabel.setBounds(langX + (langBtnSize - lblW) / 2, langY - lblH - 10, lblW, lblH);
        toastLabel.setVisible(true);
        toastTimer.restart();
    }

    private void handleStart() {
        String p1 = p1Field.getText().trim();
        String p2 = p2Field.getText().trim();

        if (p1.isEmpty() || p2.isEmpty()) {
            LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
            String msg = LanguageManager.get("missing_names", lang);
            String title = LanguageManager.get("missing_names_title", lang);
            JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
            return;
        }
        listener.onStartGame(p1, p2, currentDifficulty);
    }

    public void resetFields() {
        p1Field.setText(""); p2Field.setText(""); tEasy.setSelected(true); updateLevelInfo("EASY"); updateUIText();
    }

    public void refreshLanguage() {
        updateUIText();
        updateLevelInfo(currentDifficulty);
        revalidate();
        repaint();
    }

    private void addTypingSound(JTextField tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            private long last = 0;
            private void ping() {
                long now = System.currentTimeMillis();
                if (now - last < 50) return;
                last = now;
                SoundManager.typeKey();
            }
            @Override public void insertUpdate(DocumentEvent e) { ping(); }
            @Override public void removeUpdate(DocumentEvent e) { }
            @Override public void changedUpdate(DocumentEvent e) { }
        });
    }
}