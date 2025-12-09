package View;

import javax.swing.*;
import java.awt.*;

/**
 * Start screen UI.
 * Collects player names and difficulty level, then notifies MainFrame to start the game.
 */
public class StartPanel extends JPanel {

    public interface StartGameListener {
        void onStartGame(String player1Name, String player2Name, String difficultyKey);
        void onBackToMenu();  // <-- FIXED
    }

    private final StartGameListener listener;

    private JTextField txtPlayer1;
    private JTextField txtPlayer2;
    private JRadioButton rbEasy;
    private JRadioButton rbMedium;
    private JRadioButton rbHard;
    private JButton btnStart;
    private JButton btnBack;

    public StartPanel(StartGameListener listener) {
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout()); // instead of GridBag on whole panel
        setBackground(Color.WHITE);

        // ================= CENTER PANEL =================
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // ----- Title -----
        JLabel title = new JLabel("SCORPION MINESWEEPER");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.BLACK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerPanel.add(title, gbc);

        gbc.gridwidth = 1;

        // ----- Player names -----
        JLabel lblP1 = new JLabel("Player 1:");
        lblP1.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPlayer1 = new JTextField(10);

        JLabel lblP2 = new JLabel("Player 2:");
        lblP2.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPlayer2 = new JTextField(10);

        gbc.gridy = 1; gbc.gridx = 0;
        centerPanel.add(lblP1, gbc);
        gbc.gridx = 1;
        centerPanel.add(txtPlayer1, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        centerPanel.add(lblP2, gbc);
        gbc.gridx = 1;
        centerPanel.add(txtPlayer2, gbc);

        // ----- Difficulty -----
        JLabel lblDifficulty = new JLabel("Difficulty:");
        lblDifficulty.setFont(new Font("Arial", Font.PLAIN, 16));

        rbEasy = new JRadioButton("Easy", true);
        rbMedium = new JRadioButton("Medium");
        rbHard = new JRadioButton("Hard");

        ButtonGroup group = new ButtonGroup();
        group.add(rbEasy);
        group.add(rbMedium);
        group.add(rbHard);

        JPanel diffPanel = new JPanel(new FlowLayout());
        diffPanel.setBackground(Color.WHITE);
        diffPanel.add(lblDifficulty);
        diffPanel.add(rbEasy);
        diffPanel.add(rbMedium);
        diffPanel.add(rbHard);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        centerPanel.add(diffPanel, gbc);

        // ----- START button (center) -----
        btnStart = new JButton("START");
        btnStart.setFont(new Font("Arial", Font.BOLD, 18));
        btnStart.setPreferredSize(new Dimension(160, 40));
        btnStart.addActionListener(e -> handleStart());

        gbc.gridy = 4;
        centerPanel.add(btnStart, gbc);

        // Add the center panel to the layout
        add(centerPanel, BorderLayout.CENTER);

        // ================= BOTTOM PANEL =================
        btnBack = new JButton("BACK");
        btnBack.setPreferredSize(new Dimension(120, 35));
        btnBack.addActionListener(e -> listener.onBackToMenu());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnBack);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleStart() {
        String p1 = txtPlayer1.getText().trim();
        String p2 = txtPlayer2.getText().trim();

        if (p1.isEmpty() || p2.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name for both players.",
                    "Missing Names",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String difficultyKey = "EASY";
        if (rbMedium.isSelected()) difficultyKey = "MEDIUM";
        else if (rbHard.isSelected()) difficultyKey = "HARD";

        listener.onStartGame(p1, p2, difficultyKey);
    }
}
