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
    private GamePanel gamePanel;

    private static final Color BG_COLOR = Color.BLACK;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);

    public MainFrame() {
        super("Scorpion Minesweeper");
        SoundManager.init();
        this.controller = GameController.getInstance();
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        createAndShowGUI();
    }

    private void createAndShowGUI() {
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

        mainMenuPanel = new MainMenuPanel(this);
        startPanel = new StartPanel(this);

        cardPanel.add(mainMenuPanel, "MENU");
        cardPanel.add(startPanel, "START");

        setContentPane(cardPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        cardLayout.show(cardPanel, "MENU");

        SoundToggleOverlay.attach(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                SoundManager.stop();
            }
        });

        setVisible(true);

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
                    // Refresh StartPanel with current language before showing
                    startPanel.refreshLanguage();
                    cardLayout.show(cardPanel, "START");
                }
        );

        cardPanel.add(gamePanel, "GAME");
        cardLayout.show(cardPanel, "GAME");
        gamePanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        gamePanel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
    }

    @Override
    public void onBackToMenu() {
        startPanel.resetFields();
        // Refresh MainMenuPanel with current language before showing
        mainMenuPanel.refreshLanguage();
        cardLayout.show(cardPanel, "MENU");
    }

    // =================================================================
    //  Callbacks from MainMenuPanel (MainMenuListener)
    // =================================================================

    @Override
    public void onStartGameClicked() {
        // Refresh StartPanel with current language before showing
        startPanel.refreshLanguage();
        cardLayout.show(cardPanel, "START");
    }

    @Override
    public void onHistoryClicked() {
        GameHistoryFrame historyFrame = new GameHistoryFrame(controller, () -> {
            // When returning from history, refresh main menu language
            mainMenuPanel.refreshLanguage();
            showMainMenu();
        });
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
        // When language changes in MainMenu, also update StartPanel
        if (startPanel != null) {
            startPanel.refreshLanguage();
        }
        revalidate();
        repaint();
    }

    // =================================================================
    //  Helpers
    // =================================================================

    private void showHowToPlayDialog() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String dialogTitle = getHowToPlayTitle(lang);
        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setUndecorated(true);
        dialog.setSize(700, 520);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.BLACK);

        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        String titleText = getHowToPlayTitle(lang);
        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBackground(Color.BLACK);

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { color: white; font-family: Arial; font-size: 12px; background-color: black; }");
        styleSheet.addRule("p { margin-top: 8px; margin-bottom: 8px; }");
        textPane.setEditorKit(kit);

        String htmlContent = getHowToPlayContent(lang);

        if (isRTL) {
            textPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            textPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }

        textPane.setText(htmlContent);
        textPane.setCaretPosition(0);

        JPanel textWrapper = new JPanel(new BorderLayout());
        textWrapper.setOpaque(false);
        textWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        textWrapper.add(textPane, BorderLayout.CENTER);

        contentPanel.add(textWrapper, BorderLayout.CENTER);

        String btnText = LanguageManager.get("ok", lang);
        JButton closeBtn = createStyledButton(btnText);
        closeBtn.addActionListener(e -> {
            SoundManager.click();   // play click sound
            dialog.dispose();       // close
        });


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPanel);
        dialog.setVisible(true);
    }

    private String getHowToPlayTitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "הוראות משחק";
            case AR -> "كيفية اللعب";
            case RU -> "КАК ИГРАТЬ";
            case ES -> "CÓMO JUGAR";
            default -> "HOW TO PLAY";
        };
    }

    private String getHowToPlayContent(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "<html><body dir='rtl' style='text-align: right;'>" +
                    "<p><b>שני שחקנים, לכל אחד לוח משלו.</b><br>" +
                    "אתם חולקים חיים וניקוד.</p>" +
                    "<p><b>בתורך:</b><br>" +
                    "לחיצה שמאלית = חשיפת תא.<br>" +
                    "לחיצה ימנית = סימון תא כמוקש.<br>" +
                    "• לאחר המהלך, התור עובר לשחקן השני.</p>" +
                    "<p><b>סוגי תאים:</b><br>" +
                    "<span style='color: #FF5050;'>מוקש</span> – פגיעה בחיים אם נחשף.<br>" +
                    "<span style='color: #50B4FF;'>מספר</span> – מציין כמה מוקשים מסביב.<br>" +
                    "<span style='color: #FFFF00;'>שאלה (Q)</span> – לאחר חשיפה, ניתן לשלם נקודות ולענות על שאלה (תשובה נכונה נותנת בונוס, שגויה עלולה להזיק).<br>" +
                    "<span style='color: #FF00FF;'>הפתעה (S)</span> – לאחר חשיפה, ניתן לשלם נקודות לאפקט אקראי טוב/רע.</p>" +
                    "<p><b>ניצחון / הפסד:</b><br>" +
                    "ניצחון = כל התאים הבטוחים נחשפו.<br>" +
                    "הפסד = החיים המשותפים הגיעו ל-0.<br>" +
                    "חיים שנותרו הופכים לניקוד נוסף בסוף.</p>" +
                    "</body></html>";

            case AR -> "<html><body dir='rtl' style='text-align: right;'>" +
                    "<p><b>لاعبان، لكل منهما لوحة خاصة.</b><br>" +
                    "تتشاركون الأرواح والنقاط.</p>" +
                    "<p><b>في دورك:</b><br>" +
                    "النقر الأيسر = كشف خلية.<br>" +
                    "النقر الأيمن = وضع علامة على خلية تعتقد أنها لغم.<br>" +
                    "• بعد حركتك، ينتقل الدور للاعب الآخر.</p>" +
                    "<p><b>أنواع الخلايا:</b><br>" +
                    "<span style='color: #FF5050;'>لغم</span> – تخسر حياة إذا كُشف.<br>" +
                    "<span style='color: #50B4FF;'>رقم</span> – يُظهر عدد الألغام المحيطة.<br>" +
                    "<span style='color: #FFFF00;'>سؤال (Q)</span> – بعد الكشف، يمكنك دفع نقاط والإجابة على سؤال (الإجابة الصحيحة تعطي مكافأة، الخاطئة قد تضر).<br>" +
                    "<span style='color: #FF00FF;'>مفاجأة (S)</span> – بعد الكشف، يمكنك دفع نقاط لتأثير عشوائي جيد/سيء.</p>" +
                    "<p><b>الفوز / الخسارة:</b><br>" +
                    "الفوز = كشف جميع الخلايا الآمنة.<br>" +
                    "الخسارة = وصول الأرواح المشتركة إلى 0.<br>" +
                    "الأرواح المتبقية تتحول إلى نقاط إضافية في النهاية.</p>" +
                    "</body></html>";

            case RU -> "<html><body>" +
                    "<p><b>Два игрока, у каждого своё поле.</b><br>" +
                    "У вас общие жизни и очки.</p>" +
                    "<p><b>Ваш ход:</b><br>" +
                    "Левый клик = открыть ячейку.<br>" +
                    "Правый клик = пометить ячейку как мину с помощью флага.<br>" +
                    "• После хода очередь переходит другому игроку.</p>" +
                    "<p><b>Типы ячеек:</b><br>" +
                    "<span style='color: #FF5050;'>Мина</span> – потеря жизни при открытии.<br>" +
                    "<span style='color: #50B4FF;'>Число</span> – показывает количество мин вокруг.<br>" +
                    "<span style='color: #FFFF00;'>Вопрос (Q)</span> – после открытия вы платите очки и отвечаете на вопрос (правильный ответ даёт бонус, неправильный может навредить).<br>" +
                    "<span style='color: #FF00FF;'>Сюрприз (S)</span> – после открытия вам высвечиваеся рандомно бонус или штраф эффект с соответствующими последствиями.</p>" +
                    "<p><b>Победа / Поражение:</b><br>" +
                    "Победа = все мины распознаны.<br>" +
                    "Поражение = общие жизни достигли 0.<br>" +
                    "Оставшиеся жизни превращаются в дополнительные очки в конце.</p>" +
                    "</body></html>";

            case ES -> "<html><body>" +
                    "<p><b>Dos jugadores, cada uno tiene un tablero.</b><br>" +
                    "Comparten vidas y puntuación.</p>" +
                    "<p><b>Tu turno:</b><br>" +
                    "Clic izquierdo = revelar una celda.<br>" +
                    "Clic derecho = marcar una celda que crees que es una mina.<br>" +
                    "• Después de tu movimiento, el turno pasa al otro jugador.</p>" +
                    "<p><b>Tipos de celdas:</b><br>" +
                    "<span style='color: #FF5050;'>Mina</span> – pierdes una vida si se revela.<br>" +
                    "<span style='color: #50B4FF;'>Número</span> – indica cuántas minas hay alrededor.<br>" +
                    "<span style='color: #FFFF00;'>Pregunta (Q)</span> – después de revelar, puedes pagar puntos y responder una pregunta (respuesta correcta da bonificación, incorrecta puede perjudicar).<br>" +
                    "<span style='color: #FF00FF;'>Sorpresa (S)</span> – después de revelar, puedes pagar puntos por un efecto aleatorio bueno/malo.</p>" +
                    "<p><b>Ganar / Perder:</b><br>" +
                    "Ganar = todas las celdas seguras reveladas.<br>" +
                    "Perder = las vidas compartidas llegan a 0.<br>" +
                    "Las vidas restantes se convierten en puntos extra al final.</p>" +
                    "</body></html>";

            default -> "<html><body>" +
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
        };
    }

    private void handleAdminQuestionManagement() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String dialogTitle = getAdminAccessTitle(lang);
        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(BG_COLOR);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(30, 20, 20, 20)
        ));

        if (isRTL) {
            content.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }

        String labelText = getEnterPasswordText(lang);
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));

        JPasswordField pwd = new JPasswordField(15);
        final int cooldownMs = 35;
        final long[] last = {0L};

        pwd.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char ch = e.getKeyChar();
                if (Character.isISOControl(ch)) return;

                long now = System.currentTimeMillis();
                if (now - last[0] >= cooldownMs) {
                    SoundManager.typeKey();
                    last[0] = now;
                }
            }

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                        e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) {

                    long now = System.currentTimeMillis();
                    if (now - last[0] >= cooldownMs) {
                        SoundManager.typeKey();
                        last[0] = now;
                    }
                }
            }
        });
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

        String okText = LanguageManager.get("ok", lang);
        String cancelText = LanguageManager.get("cancel", lang);

        JButton btnOk = createStyledButton(okText);
        JButton btnCancel = createStyledButton(cancelText);
        btnOk.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                SoundManager.click();
            }
        });

        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                SoundManager.click();
            }
        });


        if (isRTL) {
            btnPanel.add(btnCancel);
            btnPanel.add(btnOk);
        } else {
            btnPanel.add(btnOk);
            btnPanel.add(btnCancel);
        }

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
                QuestionManagementFrame frame = new QuestionManagementFrame(controller.getQuestionManager(), () -> {
                    // When returning from question management, refresh main menu language
                    mainMenuPanel.refreshLanguage();
                    showMainMenu();
                });
                frame.setVisible(true);
            } else {
                String errMsg = getAccessDeniedMessage(lang);
                String errTitle = getWrongPasswordTitle(lang);
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

    private String getAdminAccessTitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "גישת מנהל";
            case AR -> "وصول المسؤول";
            case RU -> "Доступ администратора";
            case ES -> "Acceso de administrador";
            default -> "Admin Access";
        };
    }

    private String getEnterPasswordText(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "הזן סיסמת מנהל:";
            case AR -> "أدخل كلمة مرور المسؤول:";
            case RU -> "Введите пароль администратора:";
            case ES -> "Ingrese contraseña de administrador:";
            default -> "Enter Admin Password:";
        };
    }

    private String getAccessDeniedMessage(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "הגישה נדחתה.";
            case AR -> "تم رفض الوصول.";
            case RU -> "Доступ запрещён.";
            case ES -> "Acceso denegado.";
            default -> "Access denied.";
        };
    }

    private String getWrongPasswordTitle(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> "סיסמה שגויה";
            case AR -> "كلمة مرور خاطئة";
            case RU -> "Неверный пароль";
            case ES -> "Contraseña incorrecta";
            default -> "Wrong password";
        };
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
        // Refresh language before showing
        mainMenuPanel.refreshLanguage();
        cardLayout.show(cardPanel, "MENU");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}