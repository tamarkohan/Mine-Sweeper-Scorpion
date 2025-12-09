package View;

import javax.swing.*;
import java.awt.*;

/**
 * First screen with 4 basic buttons.
 * Talks to MainFrame only via MainMenuListener.
 */
public class MainMenuPanel extends JPanel {

    public interface MainMenuListener {
        void onStartGameClicked();
        void onHistoryClicked();
        void onHowToPlayClicked();
        void onManageQuestionsClicked();
    }

    private final MainMenuListener listener;

    public MainMenuPanel(MainMenuListener listener) {
        this.listener = listener;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        JLabel title = new JLabel("Scorpion Minesweeper", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        gbc.gridy = 0;
        add(title, gbc);

        JButton btnStartGame = new JButton("Start New Game");
        JButton btnHistory   = new JButton("View Game History");
        JButton btnHowToPlay = new JButton("How To Play");
        JButton btnManageQ   = new JButton("Question Management (Admin)");

        gbc.gridy = 1;
        add(btnStartGame, gbc);
        gbc.gridy = 2;
        add(btnHistory, gbc);
        gbc.gridy = 3;
        add(btnHowToPlay, gbc);
        gbc.gridy = 4;
        add(btnManageQ, gbc);

        // listeners
        btnStartGame.addActionListener(e -> {
            if (listener != null) listener.onStartGameClicked();
        });
        btnHistory.addActionListener(e -> {
            if (listener != null) listener.onHistoryClicked();
        });
        btnHowToPlay.addActionListener(e -> {
            if (listener != null) listener.onHowToPlayClicked();
        });
        btnManageQ.addActionListener(e -> {
            if (listener != null) listener.onManageQuestionsClicked();
        });
    }
}
