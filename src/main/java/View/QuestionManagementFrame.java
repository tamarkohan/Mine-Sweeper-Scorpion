package View;

import Controller.GameController;
import Model.Question;
import Model.QuestionManager;
import View.IconButton;
import util.LanguageManager;
import util.SoundToggleOverlay;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import util.SoundManager;
import View.ConfirmDialog;

public class QuestionManagementFrame extends JFrame {

    private final QuestionManager manager;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JScrollPane tableScroll;

    // Filter controls
    private JComboBox<String> difficultyFilter;
    private JComboBox<String> correctAnswerFilter;
    private JLabel diffLabel;
    private JLabel corrLabel;
    private JLabel lblSortHint;
    private JButton clearBtn;
    private JPanel filterPanel;

    // Action buttons
    private final JButton btnAdd;
    private final JButton btnEdit;
    private final JButton btnDelete;

    private final IconButton btnLanguage;
    private final JLabel toastLabel;
    private final Timer toastTimer;
    private static final String THINKING_ICON = "/ui/icons/thinking.png";

    // Colors & Styles
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);
    private static final Color HINT_COLOR = new Color(180, 180, 180);
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80);
    private static final Color DIALOG_BG = new Color(5, 6, 10);
    private static final Color DIALOG_PANEL = new Color(11, 15, 26);

    // --- Constructor 1 ---
    public QuestionManagementFrame(QuestionManager manager) {
        this(manager, null);
    }

    // --- Constructor 2 ---
    public QuestionManagementFrame(QuestionManager manager, Runnable onExitToMenu) {
        super("Question Management");
        this.manager = manager;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ESC Key Binding - Direct close without confirmation
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "closeWindow");
        getRootPane().getActionMap().put("closeWindow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                if (onExitToMenu != null) onExitToMenu.run();
            }
        });


        // Setup Table Model
        String[] cols = {"ID", "Text", "A", "B", "C", "D", "Correct", "Difficulty"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class; // ID
                return String.class;
            }
        };


        table = createStyledTable(model);
        attachHeaderClickSound(table);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

        // Custom Sorters
        sorter.setComparator(0, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));
        // Column 6 (Correct) contains letters A-D, compare as strings
        sorter.setComparator(6, (a, b) -> {
            String sa = a == null ? "" : a.toString();
            String sb = b == null ? "" : b.toString();
            return sa.compareTo(sb);
        });

        filterPanel = createFilterPanel();
        tableScroll = createStyledScrollPane(table);

        // Initialize Buttons
        btnAdd = createStyledButton("Add");
        btnEdit = createStyledButton("Edit");
        btnDelete = createStyledButton("Delete");
        attachClickSound(btnAdd);
        attachClickSound(btnEdit);
        attachClickSound(btnDelete);

        IconButton btnExit = new IconButton("/ui/icons/back.png");
        btnExit.setPreferredSize(new Dimension(46, 46));
        btnExit.setSafePadPx(2);
        btnExit.setOnClick(() -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });


        btnLanguage = new IconButton("/ui/icons/language.png", true);
        btnLanguage.setPreferredSize(new Dimension(46, 46));
        // Changed: Use popup menu instead of toggle
        btnLanguage.setOnClick(this::showLanguagePopup);

        // Action Listeners
        btnAdd.addActionListener(e -> addQuestion());
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                editQuestion(table.convertRowIndexToModel(selectedRow));
            } else {
                LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
                JOptionPane.showMessageDialog(this,
                        LanguageManager.get("select_question_edit", lang),
                        LanguageManager.get("no_selection", lang),
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                deleteQuestion(table.convertRowIndexToModel(selectedRow));
            } else {
                LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
                JOptionPane.showMessageDialog(this,
                        LanguageManager.get("select_question_delete", lang),
                        LanguageManager.get("no_selection", lang),
                        JOptionPane.WARNING_MESSAGE);
            }
        });


        // Layout - Button Panel
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(btnExit);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false);
        center.add(btnAdd);
        center.add(btnEdit);
        center.add(btnDelete);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnLanguage);

        btnPanel.add(left, BorderLayout.WEST);
        btnPanel.add(center, BorderLayout.CENTER);
        btnPanel.add(right, BorderLayout.EAST);

        // Sort hint label
        lblSortHint = new JLabel();
        lblSortHint.setForeground(HINT_COLOR);
        lblSortHint.setFont(new Font("Arial", Font.ITALIC, 12));

        // Hint panel (centered)
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hintPanel.setOpaque(false);
        hintPanel.add(lblSortHint);

        // Layout - Main Content
        BackgroundPanel content = new BackgroundPanel("/ui/menu/question_management_bg.png");
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(120, 20, 10, 20));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        // Create a panel to hold filters and hint
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(filterPanel);
        topSection.add(Box.createVerticalStrut(5));
        topSection.add(hintPanel);

        centerWrapper.add(topSection, BorderLayout.NORTH);
        centerWrapper.add(tableScroll, BorderLayout.CENTER);

        content.add(centerWrapper, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        // Toast Notification
        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setOpaque(true);
        toastLabel.setBackground(new Color(0, 0, 0, 180));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toastLabel.setVisible(false);
        getLayeredPane().add(toastLabel, JLayeredPane.POPUP_LAYER);

        toastTimer = new Timer(2000, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);

        setContentPane(content);
        SoundToggleOverlay.attach(this);

        // Initial Load
        manager.loadQuestions();
        loadTable();
        updateUIText();

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    // ================= ACTIONS =================

    private void addQuestion() {
        int nextId = manager.getNextId();
        NeonQuestionDialog dialog = new NeonQuestionDialog(this, null, nextId);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            try {
                manager.addOrReplaceQuestionBilingual(dialog.getResult());
                manager.loadQuestions();
                loadTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
                JOptionPane.showMessageDialog(this,
                        LanguageManager.get("translation_failed", lang),
                        LanguageManager.get("error", lang),
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    private void editQuestion(int row) {
        if (row < 0) return;
        Question existing = buildQuestionFromRow(row);
        if (existing == null) {
            LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
            JOptionPane.showMessageDialog(this,
                    LanguageManager.get("could_not_load", lang),
                    LanguageManager.get("error", lang),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        NeonQuestionDialog dialog = new NeonQuestionDialog(this, existing, -1);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            try {
                manager.addOrReplaceQuestionBilingual(dialog.getResult());
                manager.loadQuestions();
                loadTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
                JOptionPane.showMessageDialog(this,
                        LanguageManager.get("translation_failed", lang),
                        LanguageManager.get("error", lang),
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    private void deleteQuestion(int row) {
        if (row < 0) return;

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        String title = LanguageManager.get("delete_question", lang);
        String msg = LanguageManager.get("delete_confirm", lang);

        // optional: play dialog-open sound (choose what you like)
        SoundManager.exitDialog(); // or SoundManager.specialCellDialog();

        // blue accent like restart dialog vibe
        Color accentBlue = new Color(0, 255, 255);

        boolean confirm = ConfirmDialog.show(this, title, msg, accentBlue, isRTL);
        if (!confirm) return;

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        manager.deleteQuestion(id);
        manager.saveQuestions();
        manager.loadQuestions();
        loadTable();
        applyFilters();
    }


    private void saveQuestions() {
        manager.saveQuestions();
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        String msg = LanguageManager.get("questions_saved", lang);
        String title = LanguageManager.get("saved", lang);
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // ================= DIALOG CLASS =================

    private class NeonQuestionDialog extends JDialog {

        private Question result = null;

        private final JTextField idField = new JTextField();
        private final JTextField textField = new JTextField();
        private final JTextField aField = new JTextField();
        private final JTextField bField = new JTextField();
        private final JTextField cField = new JTextField();
        private final JTextField dField = new JTextField();

        private final JComboBox<String> correctCombo = new JComboBox<>();
        private final JComboBox<String> diffCombo = new JComboBox<>();

        private final boolean isRTL;
        private final LanguageManager.Language currentLang;

        NeonQuestionDialog(JFrame owner, Question existing, int autoId) {
            super(owner, existing == null ? "Add Question" : "Edit Question", true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // Remove Java icon - set to parent's icon or transparent
            if (owner != null && owner.getIconImage() != null) {
                setIconImage(owner.getIconImage());
            } else {
                setIconImage(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB));
            }

            currentLang = GameController.getInstance().getCurrentLanguage();
            isRTL = LanguageManager.isRTL(currentLang);

            setTitle(existing == null
                    ? LanguageManager.get("add_new_question", currentLang)
                    : LanguageManager.get("edit_question", currentLang));

            // Setup combo boxes based on language
            setupComboBoxes();

            int fieldW = 360;
            Dimension fieldMax = new Dimension(fieldW, 36);
            for (JTextField f : new JTextField[]{idField, textField, aField, bField, cField, dField}) {
                f.setPreferredSize(new Dimension(fieldW, 36));
                f.setMaximumSize(fieldMax);
            }

            JPanel root = new JPanel(new BorderLayout(14, 14));
            root.setBackground(DIALOG_BG);
            root.setBorder(new EmptyBorder(16, 16, 16, 16));

            if (isRTL) root.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            String headerText = existing == null
                    ? LanguageManager.get("add_new_question", currentLang)
                    : LanguageManager.get("edit_question", currentLang);

            JLabel header = new JLabel(headerText);
            header.setForeground(ACCENT_COLOR);
            header.setFont(new Font("Segoe UI", Font.BOLD, 22));
            if (isRTL) {
                header.setHorizontalAlignment(SwingConstants.RIGHT);
                header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            root.add(header, BorderLayout.NORTH);

            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(DIALOG_PANEL);
            card.setBorder(neonBorder(ACCENT_COLOR));
            if (isRTL) card.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(8, 8, 8, 8);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1;

            if (existing != null) {
                // EDITING - ID is not editable
                idField.setText(String.valueOf(existing.getId()));
                idField.setEditable(false);
                textField.setText(existing.getText());

                List<String> exOpts = new ArrayList<>(existing.getOptions());
                while (exOpts.size() < 4) exOpts.add("");

                aField.setText(exOpts.get(0));
                bField.setText(exOpts.get(1));
                cField.setText(exOpts.get(2));
                dField.setText(exOpts.get(3));

                char correctChar = Character.toUpperCase(existing.getCorrectOption());
                int correctIndex = correctChar - 'A';
                if (correctIndex >= 0 && correctIndex < 4) {
                    correctCombo.setSelectedIndex(correctIndex);
                }

                String diff = existing.getDifficultyLevel().toUpperCase();
                int diffIndex = switch (diff) {
                    case "EASY" -> 0;
                    case "MEDIUM" -> 1;
                    case "HARD" -> 2;
                    case "EXPERT" -> 3;
                    default -> 0;
                };
                diffCombo.setSelectedIndex(diffIndex);
            } else {
                // ADDING - ID is auto-generated and not editable
                idField.setText(String.valueOf(autoId));
                idField.setEditable(false);
                correctCombo.setSelectedIndex(0);
                diffCombo.setSelectedIndex(0);
            }

            styleNeonField(idField);
            styleNeonField(textField);
            styleNeonField(aField);
            styleNeonField(bField);
            styleNeonField(cField);
            styleNeonField(dField);
            styleNeonCombo(correctCombo);
            styleNeonCombo(diffCombo);

            attachComboOpenClickSound(correctCombo);
            attachComboOpenClickSound(diffCombo);


            attachTypingSound(textField);
            attachTypingSound(aField);
            attachTypingSound(bField);
            attachTypingSound(cField);
            attachTypingSound(dField);
            // idField is not editable, so no need

            // Get labels in current language
            String lblId = LanguageManager.get("id", currentLang);
            String lblText = LanguageManager.get("question_text", currentLang);
            String lblOptA = LanguageManager.get("option_a", currentLang);
            String lblOptB = LanguageManager.get("option_b", currentLang);
            String lblOptC = LanguageManager.get("option_c", currentLang);
            String lblOptD = LanguageManager.get("option_d", currentLang);
            String lblCorrect = LanguageManager.get("correct_answer_label", currentLang);
            String lblDiff = LanguageManager.get("difficulty_label", currentLang);

            addRow(card, gc, 0, lblId, idField);
            addRow(card, gc, 1, lblText, textField);
            addRow(card, gc, 2, lblOptA, aField);
            addRow(card, gc, 3, lblOptB, bField);
            addRow(card, gc, 4, lblOptC, cField);
            addRow(card, gc, 5, lblOptD, dField);
            addRow(card, gc, 6, lblCorrect, correctCombo);
            addRow(card, gc, 7, lblDiff, diffCombo);

            root.add(card, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel(new FlowLayout(isRTL ? FlowLayout.LEFT : FlowLayout.RIGHT, 10, 0));
            buttonsPanel.setOpaque(false);

            JButton btnCancel = createStyledButton(LanguageManager.get("cancel", currentLang));
            JButton btnSave = createStyledButton(LanguageManager.get("save", currentLang));

            btnCancel.addActionListener(e -> {
                SoundManager.click();
                dispose();
            });

            btnSave.addActionListener(e -> {
                SoundManager.click();
                if (validateAndSave()) {
                    dispose();
                }
            });

            buttonsPanel.add(btnCancel);
            buttonsPanel.add(btnSave);

            root.add(buttonsPanel, BorderLayout.SOUTH);

            setContentPane(root);
            setSize(520, 560);
            setLocationRelativeTo(owner);
        }

        private void setupComboBoxes() {
            // Setup correct answer combo (always A-D)
            correctCombo.addItem("A");
            correctCombo.addItem("B");
            correctCombo.addItem("C");
            correctCombo.addItem("D");

            // Setup difficulty combo based on language
            switch (currentLang) {
                case HE -> {
                    diffCombo.addItem("קל");
                    diffCombo.addItem("בינוני");
                    diffCombo.addItem("קשה");
                    diffCombo.addItem("מומחה");
                }
                case AR -> {
                    diffCombo.addItem("سهل");
                    diffCombo.addItem("متوسط");
                    diffCombo.addItem("صعب");
                    diffCombo.addItem("خبير");
                }
                case RU -> {
                    diffCombo.addItem("Легко");
                    diffCombo.addItem("Средне");
                    diffCombo.addItem("Сложно");
                    diffCombo.addItem("Эксперт");
                }
                case ES -> {
                    diffCombo.addItem("Fácil");
                    diffCombo.addItem("Medio");
                    diffCombo.addItem("Difícil");
                    diffCombo.addItem("Experto");
                }
                default -> {
                    diffCombo.addItem("EASY");
                    diffCombo.addItem("MEDIUM");
                    diffCombo.addItem("HARD");
                    diffCombo.addItem("EXPERT");
                }
            }
        }

        private void addRow(JPanel card, GridBagConstraints gc, int row, String labelText, JComponent field) {
            JLabel lbl = new JLabel(labelText);
            styleNeonLabel(lbl);

            gc.gridy = row;
            gc.gridx = isRTL ? 1 : 0;
            gc.anchor = isRTL ? GridBagConstraints.EAST : GridBagConstraints.WEST;
            gc.weightx = 0;
            card.add(lbl, gc);

            gc.gridx = isRTL ? 0 : 1;
            gc.anchor = isRTL ? GridBagConstraints.WEST : GridBagConstraints.EAST;
            gc.weightx = 1;
            card.add(field, gc);
        }

        private boolean validateAndSave() {
            String text = textField.getText().trim();
            String a = aField.getText().trim();
            String b = bField.getText().trim();
            String c = cField.getText().trim();
            String d = dField.getText().trim();

            if (text.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            int id = Integer.parseInt(idField.getText().trim());
            char correct = (char) ('A' + correctCombo.getSelectedIndex());
            String difficultyValue = translateDifficultyToEnglish((String) diffCombo.getSelectedItem());

            List<String> opts = new ArrayList<>();
            opts.add(a);
            opts.add(b);
            opts.add(c);
            opts.add(d);

            result = new Question(id, text, opts, correct, difficultyValue);
            return true;
        }

        public Question getResult() {
            return result;
        }
    }

    // ================= BACKGROUND PANEL =================

    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        BackgroundPanel(String resourcePath) {
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                backgroundImage = new ImageIcon(resource).getImage();
            } else {
                System.err.println("ERROR: Could not find background image at: " + resourcePath);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    // ================= LANGUAGE METHODS =================

    /**
     * Shows language selection popup menu with all 5 languages
     */
    private void showLanguagePopup() {
        JPopupMenu langMenu = new JPopupMenu();
        langMenu.setBackground(new Color(11, 15, 26));
        langMenu.setBorder(BorderFactory.createLineBorder(new Color(0, 245, 255)));

        for (LanguageManager.Language lang : LanguageManager.Language.values()) {
            JMenuItem item = new JMenuItem(LanguageManager.getDisplayName(lang));
            item.setForeground(Color.WHITE);
            item.setBackground(new Color(11, 15, 26));
            item.setFont(new Font("Arial", Font.BOLD, 14));

            // Highlight current language
            if (lang == GameController.getInstance().getCurrentLanguage()) {
                item.setForeground(new Color(0, 245, 255));
            }

            item.addActionListener(e -> handleLanguageSelection(lang));
            langMenu.add(item);
        }

        // Show popup above the button
        Dimension size = langMenu.getPreferredSize();
        langMenu.show(btnLanguage, 0, -size.height);
    }

    /**
     * Handles language selection from popup menu
     */
    private void handleLanguageSelection(LanguageManager.Language lang) {
        // Skip if same language selected
        if (lang == GameController.getInstance().getCurrentLanguage()) {
            return;
        }

        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null);
        new Thread(() -> {
            try {
                GameController gc = GameController.getInstance();
                gc.setCurrentLanguage(lang);
                gc.getQuestionManager().switchLanguageFromCache();
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                updateUIText();
                updateFilterComboItems();
                loadTable();
                showLanguageToast();
                btnLanguage.setIconPath("/ui/icons/language.png");
                btnLanguage.setOnClick(this::showLanguagePopup);
            });
        }).start();
    }

    private void updateUIText() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);

        setTitle(LanguageManager.get("question_management", lang));
        btnAdd.setText(LanguageManager.get("add", lang));
        btnEdit.setText(LanguageManager.get("edit", lang));
        btnDelete.setText(LanguageManager.get("delete", lang));

        diffLabel.setText(LanguageManager.get("difficulty", lang));
        corrLabel.setText(LanguageManager.get("correct_label", lang));
        clearBtn.setText(LanguageManager.get("clear", lang));

        // Sort hint text
        lblSortHint.setText(LanguageManager.get("sort_hint", lang));

        // Update table headers
        String[] headers = getTableHeaders(lang);
        model.setColumnIdentifiers(headers);

        ComponentOrientation o = isRTL ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT;
        table.setComponentOrientation(o);
        table.getTableHeader().setComponentOrientation(o);
        if (tableScroll != null) {
            tableScroll.setComponentOrientation(o);
            tableScroll.getViewport().setComponentOrientation(o);
        }
        filterPanel.setComponentOrientation(o);
        rebuildFilterPanel(isRTL);
        table.getTableHeader().resizeAndRepaint();
    }

    /**
     * Get table headers for all 5 languages
     */
    private String[] getTableHeaders(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> new String[]{"מזהה", "טקסט", "א", "ב", "ג", "ד", "נכונה", "רמה"};
            case AR -> new String[]{"المعرف", "النص", "أ", "ب", "ج", "د", "الصحيحة", "الصعوبة"};
            case RU -> new String[]{"ID", "Текст", "A", "B", "C", "D", "Ответ", "Сложность"};
            case ES -> new String[]{"ID", "Texto", "A", "B", "C", "D", "Correcta", "Dificultad"};
            default -> new String[]{"ID", "Text", "A", "B", "C", "D", "Correct", "Difficulty"};
        };
    }

    private void rebuildFilterPanel(boolean isRTL) {
        filterPanel.removeAll();
        FlowLayout layout = (FlowLayout) filterPanel.getLayout();
        layout.setAlignment(isRTL ? FlowLayout.RIGHT : FlowLayout.LEFT);

        if (isRTL) {
            filterPanel.add(clearBtn);
            filterPanel.add(correctAnswerFilter);
            filterPanel.add(corrLabel);
            filterPanel.add(difficultyFilter);
            filterPanel.add(diffLabel);
        } else {
            filterPanel.add(diffLabel);
            filterPanel.add(difficultyFilter);
            filterPanel.add(corrLabel);
            filterPanel.add(correctAnswerFilter);
            filterPanel.add(clearBtn);
        }

        filterPanel.revalidate();
        filterPanel.repaint();
    }


    private void updateFilterComboItems() {
        int diffIdx = difficultyFilter.getSelectedIndex();
        int corrIdx = correctAnswerFilter.getSelectedIndex();
        difficultyFilter.removeAllItems();
        correctAnswerFilter.removeAllItems();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        // Add difficulty items based on language
        switch (lang) {
            case HE -> {
                difficultyFilter.addItem("הכל");
                difficultyFilter.addItem("קל");
                difficultyFilter.addItem("בינוני");
                difficultyFilter.addItem("קשה");
                difficultyFilter.addItem("מומחה");
            }
            case AR -> {
                difficultyFilter.addItem("الكل");
                difficultyFilter.addItem("سهل");
                difficultyFilter.addItem("متوسط");
                difficultyFilter.addItem("صعب");
                difficultyFilter.addItem("خبير");
            }
            case RU -> {
                difficultyFilter.addItem("Все");
                difficultyFilter.addItem("Легко");
                difficultyFilter.addItem("Средне");
                difficultyFilter.addItem("Сложно");
                difficultyFilter.addItem("Эксперт");
            }
            case ES -> {
                difficultyFilter.addItem("Todos");
                difficultyFilter.addItem("Fácil");
                difficultyFilter.addItem("Medio");
                difficultyFilter.addItem("Difícil");
                difficultyFilter.addItem("Experto");
            }
            default -> {
                difficultyFilter.addItem("All");
                difficultyFilter.addItem("EASY");
                difficultyFilter.addItem("MEDIUM");
                difficultyFilter.addItem("HARD");
                difficultyFilter.addItem("EXPERT");
            }
        }

        // Use letters A-D to match table display (universal)
        String allText = switch (lang) {
            case HE -> "הכל";
            case AR -> "الكل";
            case RU -> "Все";
            case ES -> "Todos";
            default -> "All";
        };
        correctAnswerFilter.addItem(allText);
        correctAnswerFilter.addItem("A");
        correctAnswerFilter.addItem("B");
        correctAnswerFilter.addItem("C");
        correctAnswerFilter.addItem("D");

        if (diffIdx >= 0 && diffIdx < difficultyFilter.getItemCount()) difficultyFilter.setSelectedIndex(diffIdx);
        else difficultyFilter.setSelectedIndex(0);
        if (corrIdx >= 0 && corrIdx < correctAnswerFilter.getItemCount()) correctAnswerFilter.setSelectedIndex(corrIdx);
        else correctAnswerFilter.setSelectedIndex(0);
    }

    private void showLanguageToast() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        toastLabel.setText(LanguageManager.getDisplayName(lang));
        Dimension size = toastLabel.getPreferredSize();
        int w = size.width + 30;
        int h = 30;
        Point btnLoc = SwingUtilities.convertPoint(btnLanguage.getParent(), btnLanguage.getLocation(), getLayeredPane());
        toastLabel.setBounds(btnLoc.x + (btnLanguage.getWidth() - w) / 2, btnLoc.y - h - 10, w, h);
        toastLabel.setVisible(true);
        toastTimer.restart();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(TABLE_ROW_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TEXT_COLOR);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, v, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(CENTER);
                if (isSel) {
                    l.setBackground(TABLE_SELECTION_BG);
                    l.setForeground(TEXT_COLOR);
                } else {
                    l.setBackground(TABLE_ROW_BG);
                    l.setForeground(TEXT_COLOR);
                }
                return l;
            }
        });
        return table;
    }

    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(ACCENT_COLOR);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_COLOR), new EmptyBorder(5, 15, 5, 15)));
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

    private Border neonBorder(Color c) {
        return new CompoundBorder(new LineBorder(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200), 2, true),
                new EmptyBorder(10, 12, 10, 12));
    }

    private void styleNeonLabel(JLabel l) {
        l.setForeground(TEXT_COLOR);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void styleNeonField(JTextField field) {
        field.setBackground(DIALOG_PANEL);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(neonBorder(ACCENT_COLOR));
    }

    private void styleNeonCombo(JComboBox<?> c) {
        c.setBackground(DIALOG_PANEL);
        c.setForeground(TEXT_COLOR);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(neonBorder(ACCENT_COLOR));
    }

    private void loadTable() {
        model.setRowCount(0);
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        for (Question q : manager.getAllQuestions()) {
            List<String> o = new ArrayList<>(q.getOptions());
            while (o.size() < 4) o.add("");
            // Store correct answer as letter A-D
            char correctLetter = Character.toUpperCase(q.getCorrectOption());
            model.addRow(new Object[]{
                    Integer.valueOf(q.getId()),
                    q.getText(),
                    o.get(0), o.get(1), o.get(2), o.get(3),
                    String.valueOf(correctLetter),
                    translateDifficulty(q.getDifficultyLevel(), lang)
            });

        }
        applyFilters();
    }

    /**
     * Translate difficulty for display in all 5 languages
     */
    private String translateDifficulty(String diff, LanguageManager.Language lang) {
        if (diff == null) return "";
        if (lang == LanguageManager.Language.EN) return diff.toUpperCase();

        return switch (lang) {
            case HE -> switch (diff.toUpperCase()) {
                case "EASY" -> "קל";
                case "MEDIUM" -> "בינוני";
                case "HARD" -> "קשה";
                case "EXPERT" -> "מומחה";
                default -> diff;
            };
            case AR -> switch (diff.toUpperCase()) {
                case "EASY" -> "سهل";
                case "MEDIUM" -> "متوسط";
                case "HARD" -> "صعب";
                case "EXPERT" -> "خبير";
                default -> diff;
            };
            case RU -> switch (diff.toUpperCase()) {
                case "EASY" -> "Легко";
                case "MEDIUM" -> "Средне";
                case "HARD" -> "Сложно";
                case "EXPERT" -> "Эксперт";
                default -> diff;
            };
            case ES -> switch (diff.toUpperCase()) {
                case "EASY" -> "Fácil";
                case "MEDIUM" -> "Medio";
                case "HARD" -> "Difícil";
                case "EXPERT" -> "Experto";
                default -> diff;
            };
            default -> diff;
        };
    }

    private String translateDifficultyToEnglish(String diff) {
        if (diff == null) return "EASY";
        return switch (diff) {
            // Hebrew
            case "קל" -> "EASY";
            case "בינוני" -> "MEDIUM";
            case "קשה" -> "HARD";
            case "מומחה" -> "EXPERT";
            // Arabic
            case "سهل" -> "EASY";
            case "متوسط" -> "MEDIUM";
            case "صعب" -> "HARD";
            case "خبير" -> "EXPERT";
            // Russian
            case "Легко" -> "EASY";
            case "Средне" -> "MEDIUM";
            case "Сложно" -> "HARD";
            case "Эксперт" -> "EXPERT";
            // Spanish
            case "Fácil" -> "EASY";
            case "Medio" -> "MEDIUM";
            case "Difícil" -> "HARD";
            case "Experto" -> "EXPERT";
            default -> diff.toUpperCase();
        };
    }

    private Question buildQuestionFromRow(int row) {
        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            String text = model.getValueAt(row, 1).toString();
            String a = model.getValueAt(row, 2).toString();
            String b = model.getValueAt(row, 3).toString();
            String c = model.getValueAt(row, 4).toString();
            String d = model.getValueAt(row, 5).toString();

            // Correct column contains letters A-D
            String correctStr = model.getValueAt(row, 6).toString().trim().toUpperCase();
            char correct = 'A';
            if (!correctStr.isEmpty()) {
                char firstChar = correctStr.charAt(0);
                if (firstChar >= 'A' && firstChar <= 'D') {
                    correct = firstChar;
                }
            }

            String diff = translateDifficultyToEnglish(model.getValueAt(row, 7).toString());
            List<String> opts = new ArrayList<>();
            opts.add(a);
            opts.add(b);
            opts.add(c);
            opts.add(d);
            return new Question(id, text, opts, correct, diff);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setOpaque(false);

        diffLabel = new JLabel("Difficulty:");
        diffLabel.setForeground(TEXT_COLOR);

        difficultyFilter = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD", "EXPERT"});
        styleCombo(difficultyFilter);
        attachComboClickSound(difficultyFilter);

        corrLabel = new JLabel("Correct:");
        corrLabel.setForeground(TEXT_COLOR);

        correctAnswerFilter = new JComboBox<>(new String[]{"All", "A", "B", "C", "D"});
        styleCombo(correctAnswerFilter);
        attachComboClickSound(correctAnswerFilter);

        clearBtn = createStyledButton("Clear");
        attachClickSound(clearBtn);

        clearBtn.addActionListener(e -> {
            difficultyFilter.setSelectedIndex(0);
            correctAnswerFilter.setSelectedIndex(0);
            applyFilters();
        });

        difficultyFilter.addActionListener(e -> applyFilters());
        correctAnswerFilter.addActionListener(e -> applyFilters());

        p.add(diffLabel);
        p.add(difficultyFilter);
        p.add(corrLabel);
        p.add(correctAnswerFilter);
        p.add(clearBtn);

        return p;
    }



    private void styleCombo(JComboBox<String> box) {
        box.setBackground(Color.WHITE);
        box.setForeground(Color.BLACK);
        box.setFont(new Font("Arial", Font.PLAIN, 14));
        box.setFocusable(false);
    }

    private void applyFilters() {
        if (sorter == null) return;

        String diff = (String) difficultyFilter.getSelectedItem();
        String corr = (String) correctAnswerFilter.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String diffFilter = mapDifficultyToFilter(diff);
        if (diffFilter != null && !"All".equalsIgnoreCase(diffFilter)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(diffFilter) + "$", 7));
        }

        // Handle "All" in different languages for correct filter
        if (corr != null && !isAllValue(corr)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(corr) + "$", 6));
        }

        if (filters.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private boolean isAllValue(String value) {
        if (value == null) return true;
        return switch (value) {
            case "All", "הכל", "الكل", "Все", "Todos" -> true;
            default -> false;
        };
    }


    private String mapDifficultyToFilter(String value) {
        if (value == null || isAllValue(value)) return "All";

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        // Map to the displayed value in the current language
        String englishValue = translateDifficultyToEnglish(value);
        return translateDifficulty(englishValue, lang);
    }

    private static int parseIntSafe(Object o) {
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }
    // --- Sounds helpers ---
    private void attachClickSound(AbstractButton btn) {
        btn.addActionListener(e -> SoundManager.click());
    }


    private void attachComboClickSound(JComboBox<?> combo) {
        final boolean[] initialized = {false};
        combo.addActionListener(e -> {
            if (!initialized[0]) { initialized[0] = true; return; } // skip init fire
            SoundManager.click();
        });
    }


    private void attachTypingSound(JTextField field) {
        final int cooldownMs = 35;
        final long[] last = {0L};

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void ping() {
                long now = System.currentTimeMillis();
                if (now - last[0] >= cooldownMs) {
                    SoundManager.typeKey();
                    last[0] = now;
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { ping(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { ping(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { ping(); }
        });
    }

    private void attachHeaderClickSound(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                SoundManager.click();
            }
        });
    }
    private void attachComboOpenClickSound(JComboBox<?> combo) {
        // 1) Click anywhere on the combo
        combo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                SoundManager.click();
            }
        });

        // 2) Click on the arrow button (Metal/Basic UI)
        java.awt.Component[] comps = combo.getComponents();
        for (java.awt.Component c : comps) {
            if (c instanceof AbstractButton b) {
                b.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        SoundManager.click();
                    }
                });
            }
        }
    }



}