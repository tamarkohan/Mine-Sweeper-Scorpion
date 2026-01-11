package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Main application frame.
 * Manages navigation between:
 * - MainMenuPanel (home screen)
 * - StartPanel    (enter players + difficulty)
 * - HowToPlayPanel (game instructions)
 * - GamePanel     (actual game)
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
    private HowToPlayPanel howToPlayPanel;
    private GamePanel gamePanel;   // created when starting a game

    // Styling constants
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255); // Cyan neon

    public MainFrame() {
        super("Scorpion Minesweeper");

        this.controller = GameController.getInstance();
        this.cardLayout = new CardLayout();
        this.cardPanel  = new JPanel(cardLayout);

        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Set window icon
        try {
            URL iconUrl = getClass().getResource("/ui/icons/img_1.png");
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                setIconImage(icon.getImage());
            } else {
                System.err.println("Icon image not found at /ui/icons/img_1.png");
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }


        // ===== create screens (cards) =====
        mainMenuPanel = new MainMenuPanel(this);   // first screen with 4 buttons
        startPanel    = new StartPanel(this);      // existing screen
        howToPlayPanel = new HowToPlayPanel(this::onBackToMenu); // How to play panel

        cardPanel.add(mainMenuPanel, "MENU");
        cardPanel.add(startPanel,    "START");
        cardPanel.add(howToPlayPanel, "HOWTOPLAY");
        // "GAME" card will be added later when game starts

        setContentPane(cardPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        cardLayout.show(cardPanel, "MENU");
        setVisible(true);
    }

    // =================================================================
    //  Callbacks from StartPanel (StartGameListener)
    // =================================================================
    @Override
    public void onStartGame(String player1Name, String player2Name, String difficultyKey) {
        controller.startNewGame(difficultyKey);
        controller.registerQuestionPresenter(q -> {
            GameController.QuestionDTO dto = controller.buildQuestionDTO(q);

            GameController.QuestionAnswerResult ans =
                    QuestionDialog.showQuestionDialog(this, dto);

            return switch (ans) {
                case CORRECT -> Model.QuestionResult.CORRECT;
                case WRONG   -> Model.QuestionResult.WRONG;
                default      -> Model.QuestionResult.SKIPPED;
            };
        });

        if (gamePanel != null) {
            cardPanel.remove(gamePanel);
        }

        gamePanel = new GamePanel(
                controller,
                player1Name,
                player2Name,
                () -> {
                    controller.endGame();     // clear model
                    startPanel.resetFields(); // clear names + reset difficulty
                    cardLayout.show(cardPanel, "MENU");
                }
        );


        cardPanel.add(gamePanel, "GAME");
        cardLayout.show(cardPanel, "GAME");
    }

    @Override
    public void onBackToMenu() {
        startPanel.resetFields();
        cardLayout.show(cardPanel, "MENU");
    }

    // =================================================================
    //  Callbacks from MainMenuPanel (MainMenuListener)
    // =================================================================

    @Override
    public void onStartGameClicked() {
        cardLayout.show(cardPanel, "START");
    }

    @Override
    public void onHistoryClicked() {
        // ✅ pass a callback so GameHistoryFrame can return to menu without coupling
        GameHistoryFrame historyFrame = new GameHistoryFrame(controller, this::showMainMenu);
        historyFrame.setVisible(true);
    }

    @Override
    public void onHowToPlayClicked() {
        cardLayout.show(cardPanel, "HOWTOPLAY");
    }

    @Override
    public void onManageQuestionsClicked() {
        handleAdminQuestionManagement();
    }

    // =================================================================
    //  Menu bar
    // =================================================================

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem historyItem = new JMenuItem("Game History");

        historyItem.addActionListener(e -> onHistoryClicked());
        gameMenu.add(historyItem);
        bar.add(gameMenu);

        JMenu admin = new JMenu("Admin");
        JMenuItem manageQuestions = new JMenuItem("Question Management");

        manageQuestions.addActionListener(e -> handleAdminQuestionManagement());
        admin.add(manageQuestions);
        bar.add(admin);

        return bar;
    }

    // =================================================================
    //  Helpers: Admin access
    // =================================================================

    /**
     * Simple admin gate for Question Management.
     * Uses a custom styled dialog.
     */
    private void handleAdminQuestionManagement() {
        JDialog dialog = new JDialog(this, "Admin Access", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(BG_COLOR);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(30, 20, 20, 20)
        ));

        JLabel lbl = new JLabel("Enter Admin Password:", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));

        JPasswordField pwd = new JPasswordField(15);
        pwd.setBackground(new Color(20, 20, 20));
        pwd.setForeground(ACCENT_COLOR);
        pwd.setCaretColor(ACCENT_COLOR);
        pwd.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        pwd.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel pwdPanel = new JPanel();
        pwdPanel.setBackground(BG_COLOR);
        pwdPanel.add(pwd);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(BG_COLOR);

        JButton btnOk = createStyledButton("OK");
        JButton btnCancel = createStyledButton("Cancel");

        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);

        content.add(lbl, BorderLayout.NORTH);
        content.add(pwdPanel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnOk.addActionListener(e -> {
            String input = new String(pwd.getPassword());
            if ("ADMIN".equals(input)) {
                dialog.dispose();
                QuestionManagementFrame frame =
                        new QuestionManagementFrame(controller.getQuestionManager());
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Access denied.",
                        "Wrong password",
                        JOptionPane.ERROR_MESSAGE);
                pwd.setText("");
            }
        });

        dialog.getRootPane().setDefaultButton(btnOk);
        dialog.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(ACCENT_COLOR);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 60, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(40, 40, 40));
            }
        });
        return btn;
    }

    public void showMainMenu() {
        cardLayout.show(cardPanel, "MENU");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
