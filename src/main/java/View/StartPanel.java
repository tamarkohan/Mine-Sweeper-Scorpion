package View;

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

    private IconToggleButton tEasy;
    private IconToggleButton tMed;
    private IconToggleButton tHard;

    private IconButton btnStart;
    private IconButton btnBack;
    private NeonTextLabel lblPlayer1;
    private NeonTextLabel lblPlayer2;

    JLabel lblLevel = new JLabel("LEVEL:");
    private JLabel lblLevelInfo;
    private String currentDifficulty = "EASY";

    // Level Colors to match toggles
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

        lblPlayer1 = new NeonTextLabel("PLAYER 1", new Color(255, 80, 80));
        lblPlayer2 = new NeonTextLabel("PLAYER 2", new Color(80, 180, 255));

        bg.add(lblPlayer1);
        bg.add(lblPlayer2);

        lblLevel.setHorizontalAlignment(SwingConstants.CENTER);
        lblLevel.setForeground(Color.WHITE);
        lblLevel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 26));
        lblLevel.setOpaque(false);
        bg.add(lblLevel);

        // Initialize Level Info Label with Black background
        lblLevelInfo = new JLabel();
        lblLevelInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLevelInfo.setOpaque(true);
        lblLevelInfo.setBackground(Color.BLACK);
        bg.add(lblLevelInfo);

        p1Field = new NeonInputField("PLAYER 1", new Color(255, 80, 80));
        p2Field = new NeonInputField("PLAYER 2", new Color(80, 180, 255));

        bg.add(p1Field);
        bg.add(p2Field);

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

        // Action listeners to sync the rectangle
        tEasy.addActionListener(e -> updateLevelInfo("EASY"));
        tMed.addActionListener(e -> updateLevelInfo("MEDIUM"));
        tHard.addActionListener(e -> updateLevelInfo("HARD"));

        btnStart = new IconButton("/ui/start/start_btn.png");
        btnStart.setOnClick(this::handleStart);
        bg.add(btnStart);

        btnBack = new IconButton("/ui/icons/back.png");
        btnBack.setOnClick(() -> listener.onBackToMenu());
        bg.add(btnBack);

        bg.add(tEasy);
        bg.add(tMed);
        bg.add(tHard);

        updateLevelInfo("EASY"); // Set initial info

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutByRatio();
            }
        });

        SwingUtilities.invokeLater(this::layoutByRatio);
    }

    private void updateLevelInfo(String level) {
        this.currentDifficulty = level;
        String boardSize;
        int lives, mines, questions, surprises;
        Color activeColor;

        switch (level) {
            case "EASY" -> {
                boardSize = "9x9"; lives = 10; mines = 10; questions = 6; surprises = 2;
                activeColor = colorEasy;
            }
            case "MEDIUM" -> {
                boardSize = "13x13"; lives = 8; mines = 26; questions = 7; surprises = 3;
                activeColor = colorMed;
            }
            case "HARD" -> {
                boardSize = "16x16"; lives = 6; mines = 44; questions = 11; surprises = 4;
                activeColor = colorHard;
            }
            default -> {
                boardSize = "9x9"; lives = 10; mines = 10; questions = 6; surprises = 2;
                activeColor = colorEasy;
            }
        }

        String hexColor = String.format("#%02x%02x%02x", activeColor.getRed(), activeColor.getGreen(), activeColor.getBlue());

        // Updated Phrasing: Bold, colored text with "mines per player"
        String infoHtml = "<html><div style='text-align: center; color: " + hexColor + "; font-family: Arial; font-size: 13px;'>" +
                "<b>" + boardSize + " Board</b> &nbsp;|&nbsp; <b>" + lives + " Shared Lives</b><br>" +
                "<b>" + mines + " mines per player</b><br>" +
                "<span style='font-size: 10px;'>" + questions + " Questions &nbsp;|&nbsp; " + surprises + " Surprises</span>" +
                "</div></html>";

        lblLevelInfo.setText(infoHtml);

        lblLevelInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activeColor, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        if (bg != null && bg.getWidth() > 0) {
            layoutByRatio();
        }
    }

    private void layoutByRatio() {
        int W = bg.getWidth();
        int H = bg.getHeight();
        if (W <= 0 || H <= 0) return;

        setBoundsRatio(lblPlayer1, 0.19, 0.28, 0.30, 0.06, W, H);
        setBoundsRatio(lblPlayer2, 0.51, 0.28, 0.30, 0.06, W, H);

        setBoundsRatio(p1Field, 0.20, 0.33, 0.28, 0.12, W, H);
        setBoundsRatio(p2Field, 0.52, 0.33, 0.28, 0.12, W, H);

        int btnW = (int) (W * 0.23);
        int btnH = (int) (H * 0.23);
        int gap = (int) (W * 0.015);

        int totalButtonsWidth = btnW * 3 + gap * 2;
        int startXButtons = (W - totalButtonsWidth) / 2;
        int yButtons = (int) (H * 0.50);

        tEasy.setBounds(startXButtons, yButtons, btnW, btnH);
        tMed.setBounds(startXButtons + btnW + gap, yButtons, btnW, btnH);
        tHard.setBounds(startXButtons + (btnW + gap) * 2, yButtons, btnW, btnH);

        // Center under selected button
        int targetX;
        if ("MEDIUM".equals(currentDifficulty)) targetX = tMed.getX();
        else if ("HARD".equals(currentDifficulty)) targetX = tHard.getX();
        else targetX = tEasy.getX();

        Dimension prefSize = lblLevelInfo.getPreferredSize();
        int infoW = prefSize.width + 10;
        int infoH = prefSize.height;
        int infoX = targetX + (btnW - infoW) / 2;

        // Position very close to buttons
        int infoY = yButtons + btnH - (int)(H * 0.015);

        lblLevelInfo.setBounds(infoX, infoY, infoW, infoH);

        setBoundsRatio(btnStart, 0.31, 0.63, 0.40, 0.41, W, H);
        setBoundsRatio(btnBack, 0.03, 0.92, 0.04, 0.06, W, H);
        setBoundsRatio(lblLevel, 0.41, 0.48, 0.18, 0.06, W, H);

        bg.revalidate();
        bg.repaint();
    }

    private void setBoundsRatio(JComponent c, double x, double y, double w, double h, int W, int H) {
        c.setBounds((int) (x * W), (int) (y * H), (int) (w * W), (int) (h * H));
    }

    private void handleStart() {
        String p1 = p1Field.getText().trim();
        String p2 = p2Field.getText().trim();

        if (p1.isEmpty() || p2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter names for both players.", "Missing Names", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String difficultyKey = currentDifficulty;
        listener.onStartGame(p1, p2, difficultyKey);
    }

    public void resetFields() {
        p1Field.setText(""); p2Field.setText("");
        tEasy.setSelected(true); updateLevelInfo("EASY");
    }
}