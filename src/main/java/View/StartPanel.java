package View;

import javax.swing.*;
import java.awt.*;

/**
 * Start screen: gets player names and difficulty from the user.
 */
public class StartPanel extends JPanel {

    public interface StartGameListener {
        void onStartGame(String player1Name, String player2Name, String difficultyKey);
    }

    private final StartGameListener listener;

    private JTextField txtPlayer1;
    private JTextField txtPlayer2;
    private JRadioButton rbEasy;
    private JRadioButton rbMedium;
    private JRadioButton rbHard;
    private JButton btnStart;

    public StartPanel(StartGameListener listener) {
        this.listener = listener;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());   // center everything
        setBackground(Color.WHITE);       // white background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // ================= Title =================
        JLabel title = new JLabel("SCORPION MINESWEEPER");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.BLACK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;

        // ================= Names Row =================
        JLabel lblP1 = new JLabel("Player 1:");
        lblP1.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPlayer1 = new JTextField(10);

        JLabel lblP2 = new JLabel("Player 2:");
        lblP2.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPlayer2 = new JTextField(10);

        gbc.gridy = 1;
        gbc.gridx = 0;
        add(lblP1, gbc);

        gbc.gridx = 1;
        add(txtPlayer1, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        add(lblP2, gbc);

        gbc.gridx = 1;
        add(txtPlayer2, gbc);

        // ================= Difficulty =================
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
        add(diffPanel, gbc);

        // ================= Button =================
        btnStart = new JButton("START");
        btnStart.setFont(new Font("Arial", Font.BOLD, 18));
        btnStart.setPreferredSize(new Dimension(160, 40));

        gbc.gridy = 4;
        add(btnStart, gbc);

        btnStart.addActionListener(e -> handleStart());
    }

    private void handleStart() {
        String p1 = txtPlayer1.getText().trim();
        String p2 = txtPlayer2.getText().trim();

        if (p1.isEmpty()) p1 = "Player 1";
        if (p2.isEmpty()) p2 = "Player 2";

        String difficultyKey = "EASY";
        if (rbMedium.isSelected()) difficultyKey = "MEDIUM";
        else if (rbHard.isSelected()) difficultyKey = "HARD";

        listener.onStartGame(p1, p2, difficultyKey);
    }
}
