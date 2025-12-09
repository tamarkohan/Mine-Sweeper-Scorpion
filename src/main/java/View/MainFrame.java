package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame.
 * Manages screen navigation between StartPanel and GamePanel using CardLayout.
 * Communicates with the Model layer only through GameController.
 */
public class MainFrame extends JFrame implements StartPanel.StartGameListener {

    private final GameController controller;
    private final CardLayout cardLayout;
    private JPanel cardPanel;

    public MainFrame() {
        super("Scorpion Minesweeper");

        this.controller = GameController.getInstance();
        this.cardLayout = new CardLayout();
        
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        this.cardPanel = new JPanel(cardLayout);

        // Admin/debug menu for question management
        setJMenuBar(buildMenuBar());

        // create screens
        final StartPanel startPanel = new StartPanel(this);
        cardPanel.add(startPanel, "START");

        setContentPane(cardPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Callback from StartPanel when the user starts a new game.
     * Initializes the game in the controller and switches to the GamePanel.
     */
    @Override
    public void onStartGame(String player1Name, String player2Name, String difficultyKey) {
        controller.startNewGame(difficultyKey);

        GamePanel gamePanel = new GamePanel(controller, player1Name, player2Name);
        cardPanel.add(gamePanel, "GAME");
        cardLayout.show(cardPanel, "GAME");
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu admin = new JMenu("Admin");
        JMenuItem manageQuestions = new JMenuItem("Question Management");
        manageQuestions.addActionListener(e -> {
            QuestionManagementFrame frame = new QuestionManagementFrame(controller.getQuestionManager());
            frame.setVisible(true);
        });
        admin.add(manageQuestions);
        bar.add(admin);
        return bar;
    }

    /**
     * Application entry point. Launches the main frame on the EDT.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}