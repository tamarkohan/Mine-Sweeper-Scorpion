package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame.
 * Manages navigation between:
 *  - MainMenuPanel (home screen)
 *  - StartPanel    (enter players + difficulty)
 *  - GamePanel     (actual game)
 *
 * Communicates with the Model layer only through GameController (MVC).
 */
public class MainFrame extends JFrame
        implements StartPanel.StartGameListener,
        MainMenuPanel.MainMenuListener {

    private final GameController controller;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private MainMenuPanel mainMenuPanel;
    private StartPanel startPanel;
    private GamePanel gamePanel;   // created when starting a game

    public MainFrame() {
        super("Scorpion Minesweeper");

        this.controller = GameController.getInstance();
        this.cardLayout = new CardLayout();
        this.cardPanel  = new JPanel(cardLayout);

        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Admin/debug menu for question management & history
        setJMenuBar(buildMenuBar());

        // ===== create screens (cards) =====
        mainMenuPanel = new MainMenuPanel(this);   // first screen with 4 buttons
        startPanel    = new StartPanel(this);      // existing screen

        cardPanel.add(mainMenuPanel, "MENU");
        cardPanel.add(startPanel,    "START");
        // "GAME" card will be added later when game starts

        setContentPane(cardPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // show first screen
        cardLayout.show(cardPanel, "MENU");
        setVisible(true);
    }

    // =================================================================
    //  Callbacks from StartPanel (StartGameListener)
    // =================================================================

    /**
     * Callback from StartPanel when the user starts a new game.
     * Initializes the game in the controller and switches to the GamePanel.
     */
    @Override
    public void onStartGame(String player1Name, String player2Name, String difficultyKey) {
        controller.startNewGame(difficultyKey);

        gamePanel = new GamePanel(controller, player1Name, player2Name);
        cardPanel.add(gamePanel, "GAME");
        cardLayout.show(cardPanel, "GAME");
    }

    /**
     * Callback from StartPanel when the user presses the BACK button.
     * Returns to the main menu screen.
     */
    @Override
    public void onBackToMenu() {
        cardLayout.show(cardPanel, "MENU");
    }

    // =================================================================
    //  Callbacks from MainMenuPanel (MainMenuListener)
    // =================================================================

    /** User pressed "START GAME" on the main menu. */
    @Override
    public void onStartGameClicked() {
        cardLayout.show(cardPanel, "START");
    }

    /** User pressed "GAMES HISTORY" on the main menu. */
    @Override
    public void onHistoryClicked() {
        GameHistoryFrame historyFrame = new GameHistoryFrame(controller);
        historyFrame.setVisible(true);
    }

    /** User pressed "HOW TO PLAY" on the main menu. */
    @Override
    public void onHowToPlayClicked() {
        showHowToPlayDialog();
    }

    /** User pressed "QUESTION MANAGEMENT (ADMIN)" on the main menu. */
    @Override
    public void onManageQuestionsClicked() {
        handleAdminQuestionManagement();
    }

    // =================================================================
    //  Menu bar
    // =================================================================

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // === Game menu ===
        JMenu gameMenu = new JMenu("Game");
        JMenuItem historyItem = new JMenuItem("Game History");

        historyItem.addActionListener(e -> onHistoryClicked());
        gameMenu.add(historyItem);
        bar.add(gameMenu);

        // === Admin menu ===
        JMenu admin = new JMenu("Admin");
        JMenuItem manageQuestions = new JMenuItem("Question Management");

        manageQuestions.addActionListener(e -> handleAdminQuestionManagement());
        admin.add(manageQuestions);
        bar.add(admin);

        return bar;
    }

    // =================================================================
    //  Helpers: How to play + Admin access
    // =================================================================

    private void showHowToPlayDialog() {
        String msg =
                "HOW TO PLAY\n\n" +
                        "Two players, each has a board.\n" +
                        "You share lives and score.\n\n" +
                        "Your turn:\n" +
                        "Left click = reveal a cell.\n" +
                        "Right click = flag a cell you think is a mine.\n" +
                        "• After your move, the turn switches.\n\n" +
                        "Cell types:\n" +
                        "Mine – losing a life if revealed.\n" +
                        "Number – tells how many mines around.\n" +
                        "Question (Q) – " +
                        "after reveal, you can pay points and answer a quiz\n" +
                        "(correct gives bonus, wrong can hurt).\n" +
                        "Surprise (S) – after reveal, you can pay points for random good/bad effect.\n\n" +
                        "Win / Lose:\n" +
                        "Win = all safe cells cleared.\n" +
                        "Lose = shared lives reach 0.\n" +
                        "Remaining lives turn into extra score at the end.\n";

        JOptionPane.showMessageDialog(
                this,
                msg,
                "How to Play",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Simple admin gate for Question Management.
     * You can change the password or later replace it with a real login.
     */
    private void handleAdminQuestionManagement() {
        JPasswordField pwd = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(
                this,
                pwd,
                "Admin password:",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String input = new String(pwd.getPassword());

        if (!"ADMIN".equals(input)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Access denied.",
                    "Wrong password",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // open your existing QuestionManagementFrame
        QuestionManagementFrame frame =
                new QuestionManagementFrame(controller.getQuestionManager());
        frame.setVisible(true);
    }

    public void showMainMenu() {
        cardLayout.show(cardPanel, "MENU");
    }


    // =================================================================
    //  Application entry point
    // =================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }


}
