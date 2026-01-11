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
        lblLevel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 26)); // no loading
        lblLevel.setOpaque(false);

        bg.add(lblLevel);


        p1Field = new NeonInputField("PLAYER 1", new Color(255, 80, 80));
        p2Field = new NeonInputField("PLAYER 2", new Color(80, 180, 255));

        bg.add(p1Field);
        bg.add(p2Field);



        tEasy = new IconToggleButton(
                "/ui/start/easy_btn.png",
                new Color(120, 255, 170)   // green
        );

        tMed = new IconToggleButton(
                "/ui/start/medium_btn.png",
                new Color(80, 180, 255)    // blue
        );

        tHard = new IconToggleButton(
                "/ui/start/hard_btn.png",
                new Color(255, 80, 80)     // red
        );



        Font diffFont = new Font("Arial Black", Font.PLAIN, 28);
        tEasy.setFont(diffFont);
        tMed.setFont(diffFont);
        tHard.setFont(diffFont);

        ButtonGroup group = new ButtonGroup();
        group.add(tEasy);
        group.add(tMed);
        group.add(tHard);
        tEasy.setSelected(true);

        btnStart = new IconButton("/ui/start/start_btnnn.png");
        btnStart.setOnClick(this::handleStart);
        bg.add(btnStart);



        // BACK icon (must exist in resources)
        ImageIcon backIcon = new ImageIcon(
                java.util.Objects.requireNonNull(
                        getClass().getResource("/ui/icons/back.png"),
                        "Missing /ui/icons/back.png in resources"
                )
        );

        btnBack = new IconButton("/ui/icons/back.png");
        btnBack.setOnClick(() -> listener.onBackToMenu());
        bg.add(btnBack);

        // Add to bg (order matters: later = on top)
        bg.add(p1Field);
        bg.add(p2Field);
        bg.add(tEasy);
        bg.add(tMed);
        bg.add(tHard);
        bg.add(btnStart);

        bg.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutByRatio();
            }
        });

        SwingUtilities.invokeLater(this::layoutByRatio);
    }

    private void layoutByRatio() {
        int W = bg.getWidth();
        int H = bg.getHeight();
        setBoundsRatio(lblPlayer1, 0.19, 0.28, 0.30, 0.06, W, H);
        setBoundsRatio(lblPlayer2, 0.51, 0.28, 0.30, 0.06, W, H);

        setBoundsRatio(p1Field, 0.20, 0.33, 0.28, 0.12, W, H);
        setBoundsRatio(p2Field, 0.52, 0.33, 0.28, 0.12, W, H);

        // --- Difficulty buttons: same size, same Y, one line ---
        // --- Difficulty buttons: same size, same Y, one line ---
        // ===== LEVEL BUTTONS (PIXEL CONTROL – LIKE MAIN MENU) =====
        int btnW = (int)(W * 0.23);   // width
        int btnH = (int)(H * 0.23);   // height
        int gap  = (int)(W * 0.015);  // small gap between buttons

        int total = btnW * 3 + gap * 2;
        int startX = (W - total) / 2;
        int y = (int)(H * 0.50);

        tEasy.setBounds(startX,               y, btnW, btnH);
        tMed .setBounds(startX + btnW + gap,  y, btnW, btnH);
        tHard.setBounds(startX + (btnW + gap)*2, y, btnW, btnH);








        setBoundsRatio(btnStart, 0.31, 0.63, 0.40, 0.41, W, H);

        setBoundsRatio(btnBack, 0.03, 0.92, 0.04, 0.06, W, H);

        setBoundsRatio(lblLevel, 0.41, 0.48, 0.18, 0.06, W, H); // centered above toggles

        bg.revalidate();
        bg.repaint();
    }

    private void setBoundsRatio(JComponent c, double x, double y, double w, double h, int W, int H) {
        c.setBounds((int)(x * W), (int)(y * H), (int)(w * W), (int)(h * H));
    }

    private void handleStart() {
        String p1 = p1Field.getText().trim();
        String p2 = p2Field.getText().trim();


        if (p1.isEmpty() && p2.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name for Player 1 and Player 2.",
                    "Missing Player Names",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (p1.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name for Player 1.",
                    "Missing Player 1",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (p2.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name for Player 2.",
                    "Missing Player 2",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String difficultyKey = "EASY";
        if (tMed.isSelected()) difficultyKey = "MEDIUM";
        else if (tHard.isSelected()) difficultyKey = "HARD";

        listener.onStartGame(p1, p2, difficultyKey);
    }

    private void styleTitleLabel(JLabel lbl, Color c) {
        lbl.setForeground(c);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
    }
    public void resetFields() {
        // clear text
        p1Field.setText("");
        p2Field.setText("");

        // reset difficulty to default
        tEasy.setSelected(true);
    }


}
