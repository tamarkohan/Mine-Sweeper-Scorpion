package View;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class StartPanel extends JPanel {

    public interface StartGameListener {
        void onStartGame(String player1Name, String player2Name, String difficultyKey);
        void onBackToMenu();
    }

    private final StartGameListener listener;

    private BackgroundPanel bg;

    private NeonInputField p1Field;
    private NeonInputField p2Field;

    private DifficultyToggle tEasy;
    private DifficultyToggle tMed;
    private DifficultyToggle tHard;

    private IconButton btnStart;
    private IconButton btnBack;
    private NeonTextLabel lblPlayer1;
    private NeonTextLabel lblPlayer2;



    public StartPanel(StartGameListener listener) {
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        bg = new BackgroundPanel("/ui/start/bg.png");
        bg.setLayout(null);
        add(bg, BorderLayout.CENTER);
        lblPlayer1 = new NeonTextLabel("PLAYER 1", new Color(255, 80, 80));
        lblPlayer2 = new NeonTextLabel("PLAYER 2", new Color(80, 180, 255));

        bg.add(lblPlayer1);
        bg.add(lblPlayer2);


        p1Field = new NeonInputField("PLAYER 1", new Color(255, 80, 80));
        p2Field = new NeonInputField("PLAYER 2", new Color(80, 180, 255));

        bg.add(p1Field);
        bg.add(p2Field);



        tEasy = new DifficultyToggle("EASY");
        tMed  = new DifficultyToggle("MEDIUM");
        tHard = new DifficultyToggle("HARD");

        Font diffFont = new Font("Arial Black", Font.PLAIN, 28);
        tEasy.setFont(diffFont);
        tMed.setFont(diffFont);
        tHard.setFont(diffFont);

        ButtonGroup group = new ButtonGroup();
        group.add(tEasy);
        group.add(tMed);
        group.add(tHard);
        tEasy.setSelected(true);

        btnStart = new IconButton("/ui/start/btn_start.png");
        btnStart.setOnClick(this::handleStart);

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
        setBoundsRatio(lblPlayer1, 0.20, 0.30, 0.30, 0.06, W, H);
        setBoundsRatio(lblPlayer2, 0.51, 0.30, 0.30, 0.06, W, H);

        setBoundsRatio(p1Field, 0.20, 0.36, 0.28, 0.12, W, H);
        setBoundsRatio(p2Field, 0.52, 0.36, 0.28, 0.12, W, H);

        setBoundsRatio(tEasy, 0.26, 0.63, 0.16, 0.09, W, H);
        setBoundsRatio(tMed,  0.42, 0.63, 0.20, 0.09, W, H);
        setBoundsRatio(tHard, 0.62, 0.63, 0.16, 0.09, W, H);


        setBoundsRatio(btnStart, 0.20, 0.70, 0.64, 0.42, W, H);

        setBoundsRatio(btnBack, 0.03, 0.90, 0.06, 0.08, W, H);


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

}
