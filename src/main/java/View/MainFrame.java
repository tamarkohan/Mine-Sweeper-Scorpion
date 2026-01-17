package View;

import Controller.GameController;
import util.LanguageManager;
import util.SoundManager;
import util.SoundToggleOverlay;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.net.URL;

public class MainFrame extends JFrame
        implements StartPanel.StartGameListener,
        MainMenuPanel.MainMenuListener {

    private final GameController controller;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private MainMenuPanel mainMenuPanel;
    private StartPanel startPanel;
    private GamePanel gamePanel;   // created when starting a game

    // Styling constants
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255); // Cyan neon

    public MainFrame() {
        super("Scorpion Minesweeper");

        this.controller = GameController.getInstance();
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

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
        mainMenuPanel = new MainMenuPanel(this);
        startPanel = new StartPanel(this);

        cardPanel.add(mainMenuPanel, "MENU");
        cardPanel.add(startPanel, "START");

        setContentPane(cardPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        // Show first screen
        cardLayout.show(cardPanel, "MENU");

        // Attach global sound toggle overlay (shows on all cards)
        SoundToggleOverlay.attach(this);

        // Window close cleanup
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                SoundManager.stop();
            }
        });

        setVisible(true);

        // Start global background music
        SoundManager.playLoop("/audio/bg_music.wav");
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
                case WRONG -> Model.QuestionResult.WRONG;
                default -> Model.QuestionResult.SKIPPED;
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
                    controller.endGame();
                    // Go to START panel (level/name selection) instead of MENU
                    cardLayout.show(cardPanel, "START");
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
        GameHistoryFrame historyFrame = new GameHistoryFrame(controller, this::showMainMenu);
        historyFrame.setVisible(true);
    }

    @Override
    public void onHowToPlayClicked() {
        showHowToPlayDialog();
    }

    @Override
    public void onManageQuestionsClicked() {
        handleAdminQuestionManagement();
    }

    @Override
    public void onLanguageToggle() {
        // The panels now handle the switching in background threads.
        // We just need to update other panels that might be listening.
        if (startPanel != null) {
            startPanel.resetFields();
        }
        revalidate();
        repaint();
    }

    // =================================================================
    //  Helpers
    // =================================================================

    private void showHowToPlayDialog() {
        JDialog dialog = new JDialog(this, "How to Play", true);
        dialog.setUndecorated(true);
        dialog.setSize(700, 520);
        dialog.setLocationRelativeTo(this);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isHe = (lang == LanguageManager.Language.HE);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.BLACK);

        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title
        String titleText = isHe ? "הוראות משחק" : "HOW TO PLAY";
        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Use JTextPane for better RTL support
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBackground(Color.BLACK);

        // Set up HTML editor kit with custom styles
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { color: white; font-family: Arial; font-size: 12px; background-color: black; }");
        styleSheet.addRule("p { margin-top: 8px; margin-bottom: 8px; }");
        textPane.setEditorKit(kit);

        String htmlContent;
        if (isHe) {
            // HEBREW CONTENT (RTL)
            textPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            htmlContent = "<html><body dir='rtl' style='text-align: right;'>" +
                    "<p><b>שני שחקנים, לכל אחד לוח משלו.</b><br>" +
                    "אתם חולקים חיים וניקוד משותפים.</p>" +
                    "<p><b>התור שלך:</b><br>" +
                    "קליק שמאלי = חשיפת תא.<br>" +
                    "קליק ימני = סימון דגל על חשד למוקש.<br>" +
                    "• בסיום המהלך, התור עובר לשחקן השני.</p>" +
                    "<p><b>סוגי תאים:</b><br>" +
                    "<span style='color: #FF5050;'>מוקש</span> – איבוד חיים בעת חשיפה.<br>" +
                    "<span style='color: #50B4FF;'>מספר</span> – מציין כמה מוקשים יש מסביב.<br>" +
                    "<span style='color: #FFFF00;'>שאלה (Q)</span> – בחשיפה, ניתן לשלם נקודות ולענות על חידה (תשובה נכונה נותנת בונוס).<br>" +
                    "<span style='color: #FF00FF;'>הפתעה (S)</span> – בחשיפה, ניתן לשלם נקודות ולקבל אפקט אקראי (טוב או רע).</p>" +
                    "<p><b>ניצחון / הפסד:</b><br>" +
                    "ניצחון = כל התאים הבטוחים נחשפו.<br>" +
                    "הפסד = החיים המשותפים הגיעו ל-0.<br>" +
                    "חיים שנותרו מומרים לניקוד בונוס בסוף.</p>" +
                    "</body></html>";
        } else {
            // ENGLISH CONTENT (LTR)
            textPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            htmlContent = "<html><body>" +
                    "<p><b>Two players, each has a board.</b><br>" +
                    "You share lives and score.</p>" +
                    "<p><b>Your turn:</b><br>" +
                    "Left click = reveal a cell.<br>" +
                    "Right click = flag a cell you think is a mine.<br>" +
                    "• After your move, the turn switches.</p>" +
                    "<p><b>Cell types:</b><br>" +
                    "<span style='color: #FF5050;'>Mine</span> – losing a life if revealed.<br>" +
                    "<span style='color: #50B4FF;'>Number</span> – tells how many mines around.<br>" +
                    "<span style='color: #FFFF00;'>Question (Q)</span> – after reveal, you can pay points and answer a quiz (correct gives bonus, wrong can hurt).<br>" +
                    "<span style='color: #FF00FF;'>Surprise (S)</span> – after reveal, you can pay points for random good/bad effect.</p>" +
                    "<p><b>Win / Lose:</b><br>" +
                    "Win = all safe cells cleared.<br>" +
                    "Lose = shared lives reach 0.<br>" +
                    "Remaining lives turn into extra score at the end.</p>" +
                    "</body></html>";
        }

        textPane.setText(htmlContent);
        textPane.setCaretPosition(0);

        // Wrap in a panel with padding
        JPanel textWrapper = new JPanel(new BorderLayout());
        textWrapper.setOpaque(false);
        textWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        textWrapper.add(textPane, BorderLayout.CENTER);

        contentPanel.add(textWrapper, BorderLayout.CENTER);

        // Button
        String btnText = isHe ? "אישור" : "OK";
        JButton closeBtn = createStyledButton(btnText);
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPanel);
        dialog.setVisible(true);
    }

    private void handleAdminQuestionManagement() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isHe = (lang == LanguageManager.Language.HE);

        JDialog dialog = new JDialog(this, isHe ? "גישת מנהל" : "Admin Access", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(BG_COLOR);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(30, 20, 20, 20)
        ));

        String labelText = isHe ? "הזן סיסמת מנהל:" : "Enter Admin Password:";
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
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

        String okText = isHe ? "אישור" : "OK";
        String cancelText = isHe ? "ביטול" : "Cancel";

        JButton btnOk = createStyledButton(okText);
        JButton btnCancel = createStyledButton(cancelText);

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
                QuestionManagementFrame frame = new QuestionManagementFrame(controller.getQuestionManager());
                frame.setVisible(true);
            } else {
                String errMsg = isHe ? "הגישה נדחתה." : "Access denied.";
                String errTitle = isHe ? "סיסמה שגויה" : "Wrong password";
                JOptionPane.showMessageDialog(dialog,
                        errMsg,
                        errTitle,
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