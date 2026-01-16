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
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QuestionManagementFrame extends JFrame {

    private final QuestionManager manager;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JScrollPane tableScroll;

    // Filter controls
    private JComboBox<String> difficultyFilter;
    private JComboBox<String> correctAnswerFilter;
    private JTextField idFilter;
    private JLabel diffLabel;
    private JLabel corrLabel;
    private JLabel idLabel;
    private JButton applyBtn;
    private JButton clearBtn;
    private JPanel filterPanel;

    // Action buttons
    private final JButton btnAdd;
    private final JButton btnEdit;
    private final JButton btnDelete;
    private final JButton btnSave;

    private final IconButton btnLanguage;
    private final JLabel toastLabel;
    private final Timer toastTimer;
    private static final String THINKING_ICON = "/ui/icons/thinking.png";

    // Colors & Styles
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);
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

        // ESC Key Binding
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
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = createStyledTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Custom Sorters
        sorter.setComparator(0, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));
        sorter.setComparator(6, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));

        filterPanel = createFilterPanel();
        tableScroll = createStyledScrollPane(table);

        // Initialize Buttons
        btnAdd = createStyledButton("Add");
        btnEdit = createStyledButton("Edit");
        btnDelete = createStyledButton("Delete");
        btnSave = createStyledButton("Save");

        IconButton btnExit = new IconButton("/ui/icons/back.png");
        btnExit.setPreferredSize(new Dimension(46, 46));
        btnExit.setSafePadPx(2);
        btnExit.setOnClick(() -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

        btnLanguage = new IconButton("/ui/icons/language.png", true);
        btnLanguage.setPreferredSize(new Dimension(46, 46));
        btnLanguage.setOnClick(this::handleLanguageSwitch);

        // Action Listeners
        btnAdd.addActionListener(e -> addQuestion());
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) editQuestion(table.convertRowIndexToModel(selectedRow));
        });
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) deleteQuestion(table.convertRowIndexToModel(selectedRow));
        });
        btnSave.addActionListener(e -> saveQuestions());

        // Layout - Button Panel
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false); left.add(btnExit);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false); center.add(btnAdd); center.add(btnEdit); center.add(btnDelete); center.add(btnSave);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false); right.add(btnLanguage);

        btnPanel.add(left, BorderLayout.WEST);
        btnPanel.add(center, BorderLayout.CENTER);
        btnPanel.add(right, BorderLayout.EAST);

        // Layout - Main Content
        BackgroundPanel content = new BackgroundPanel("/ui/menu/question_management_bg.png");
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(120, 20, 10, 20));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        centerWrapper.add(filterPanel, BorderLayout.NORTH);
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
        // Uses standard add logic
        NeonQuestionDialog dialog = new NeonQuestionDialog(this, null, nextId);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            manager.addOrReplaceQuestion(dialog.getResult());
            manager.saveQuestions();
            manager.loadQuestions();
            loadTable();
        }
    }

    private void editQuestion(int row) {
        if (row < 0) return;
        Question existing = buildQuestionFromRow(row);
        NeonQuestionDialog dialog = new NeonQuestionDialog(this, existing, -1);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            manager.addOrReplaceQuestion(dialog.getResult());
            manager.saveQuestions();
            manager.loadQuestions();
            loadTable();
        }
    }

    private void deleteQuestion(int row) {
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        manager.deleteQuestion(id);
        manager.saveQuestions();
        manager.loadQuestions();
        loadTable();
        applyFilters();
    }

    private void saveQuestions() {
        manager.saveQuestions();
        boolean isHe = (GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE);
        String msg = isHe ? "השאלות נשמרו לקובץ CSV." : "Questions saved to CSV.";
        String title = isHe ? "נשמר" : "Saved";
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

        private final boolean isHebrew;

        NeonQuestionDialog(JFrame owner, Question existing, int autoId) {
            super(owner, existing == null ? "Add Question" : "Edit Question", true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            isHebrew = (GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE);

            setTitle(existing == null
                    ? (isHebrew ? "הוסף שאלה חדשה" : "Add Question")
                    : (isHebrew ? "ערוך שאלה" : "Edit Question"));

            if (isHebrew) {
                correctCombo.addItem("א"); correctCombo.addItem("ב"); correctCombo.addItem("ג"); correctCombo.addItem("ד");
                diffCombo.addItem("קל"); diffCombo.addItem("בינוני"); diffCombo.addItem("קשה"); diffCombo.addItem("מומחה");
            } else {
                correctCombo.addItem("A"); correctCombo.addItem("B"); correctCombo.addItem("C"); correctCombo.addItem("D");
                diffCombo.addItem("EASY"); diffCombo.addItem("MEDIUM"); diffCombo.addItem("HARD"); diffCombo.addItem("EXPERT");
            }

            int fieldW = 360;
            Dimension fieldMax = new Dimension(fieldW, 36);
            for (JTextField f : new JTextField[]{idField, textField, aField, bField, cField, dField}) {
                f.setPreferredSize(new Dimension(fieldW, 36));
                f.setMaximumSize(fieldMax);
            }

            JPanel root = new JPanel(new BorderLayout(14, 14));
            root.setBackground(DIALOG_BG);
            root.setBorder(new EmptyBorder(16, 16, 16, 16));

            if (isHebrew) root.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            String headerText = existing == null
                    ? (isHebrew ? "הוסף שאלה חדשה" : "Add New Question")
                    : (isHebrew ? "ערוך שאלה" : "Edit Question");

            JLabel header = new JLabel(headerText);
            header.setForeground(ACCENT_COLOR);
            header.setFont(new Font("Segoe UI", Font.BOLD, 22));
            if (isHebrew) {
                header.setHorizontalAlignment(SwingConstants.RIGHT);
                header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            root.add(header, BorderLayout.NORTH);

            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(DIALOG_PANEL);
            card.setBorder(neonBorder(ACCENT_COLOR));
            if (isHebrew) card.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(8, 8, 8, 8);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1;

            if (existing != null) {
                idField.setText(String.valueOf(existing.getId()));
                idField.setEditable(false);
                textField.setText(existing.getText());

                List<String> exOpts = new ArrayList<>(existing.getOptions());
                while (exOpts.size() < 4) exOpts.add("");

                aField.setText(exOpts.get(0)); bField.setText(exOpts.get(1));
                cField.setText(exOpts.get(2)); dField.setText(exOpts.get(3));

                char correctChar = Character.toUpperCase(existing.getCorrectOption());
                int correctIndex = correctChar - 'A';
                if (correctIndex >= 0 && correctIndex < 4) correctCombo.setSelectedIndex(correctIndex);

                String diff = existing.getDifficultyLevel().toUpperCase();
                int diffIndex = switch (diff) {
                    case "EASY" -> 0; case "MEDIUM" -> 1; case "HARD" -> 2; case "EXPERT" -> 3; default -> 0;
                };
                diffCombo.setSelectedIndex(diffIndex);
            } else {
                idField.setText(String.valueOf(autoId));
                idField.setEditable(false);
                correctCombo.setSelectedIndex(0);
                diffCombo.setSelectedIndex(0);
            }

            styleNeonField(idField); styleNeonField(textField);
            styleNeonField(aField); styleNeonField(bField);
            styleNeonField(cField); styleNeonField(dField);
            styleNeonCombo(correctCombo); styleNeonCombo(diffCombo);

            String lblId = isHebrew ? "מזהה" : "ID";
            String lblText = isHebrew ? "טקסט" : "Text";
            String lblOptA = isHebrew ? "אפשרות א" : "Option A";
            String lblOptB = isHebrew ? "אפשרות ב" : "Option B";
            String lblOptC = isHebrew ? "אפשרות ג" : "Option C";
            String lblOptD = isHebrew ? "אפשרות ד" : "Option D";
            String lblCorrect = isHebrew ? "נכונה" : "Correct";
            String lblDiff = isHebrew ? "רמת קושי" : "Difficulty";

            addRow(card, gc, 0, lblId, idField);
            addRow(card, gc, 1, lblText, textField);
            addRow(card, gc, 2, lblOptA, aField);
            addRow(card, gc, 3, lblOptB, bField);
            addRow(card, gc, 4, lblOptC, cField);
            addRow(card, gc, 5, lblOptD, dField);
            addRow(card, gc, 6, lblCorrect, correctCombo);
            addRow(card, gc, 7, lblDiff, diffCombo);

            root.add(card, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(isHebrew ? FlowLayout.LEFT : FlowLayout.RIGHT, 10, 0));
            buttons.setBackground(DIALOG_BG);

            JButton btnCancel = createStyledButton(isHebrew ? "ביטול" : "Cancel");
            JButton btnSave = createStyledButton(isHebrew ? "שמור" : "Save");

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> onSave(existing));

            if (isHebrew) {
                buttons.add(btnSave); buttons.add(btnCancel);
            } else {
                buttons.add(btnCancel); buttons.add(btnSave);
            }
            root.add(buttons, BorderLayout.SOUTH);

            setContentPane(root);
            pack();
            setSize(750, 650);
            setResizable(true);
            setMinimumSize(new Dimension(600, 500));
            setLocationRelativeTo(owner);
        }

        private void styleNeonField(JTextField f) {
            f.setBackground(DIALOG_PANEL);
            f.setForeground(TEXT_COLOR);
            f.setCaretColor(ACCENT_COLOR);
            f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            f.setBorder(neonBorder(ACCENT_COLOR));
            if(isHebrew) f.setHorizontalAlignment(JTextField.RIGHT);
            else f.setHorizontalAlignment(JTextField.LEFT);
            if (!f.isEditable()) f.setForeground(Color.GRAY);
        }

        private void addRow(JPanel card, GridBagConstraints gc, int row, String label, JComponent input) {
            JLabel l = new JLabel(label + ":");
            styleNeonLabel(l);

            if (isHebrew) {
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.anchor = GridBagConstraints.EAST;
                card.add(l, gc);
                gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.anchor = GridBagConstraints.WEST;
                card.add(input, gc);
            } else {
                gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.anchor = GridBagConstraints.WEST;
                card.add(l, gc);
                gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.anchor = GridBagConstraints.EAST;
                card.add(input, gc);
            }
        }

        private void onSave(Question existing) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String text = textField.getText().trim();
                if (text.isEmpty()) throw new IllegalArgumentException(isHebrew ? "הטקסט ריק." : "Text is empty.");

                List<String> opts = new ArrayList<>();
                opts.add(aField.getText().trim()); opts.add(bField.getText().trim());
                opts.add(cField.getText().trim()); opts.add(dField.getText().trim());

                int correctIndex = correctCombo.getSelectedIndex();
                char correct = (char) ('A' + correctIndex);

                int diffIndex = diffCombo.getSelectedIndex();
                String diff = switch (diffIndex) {
                    case 0 -> "EASY"; case 1 -> "MEDIUM"; case 2 -> "HARD"; case 3 -> "EXPERT"; default -> "EASY";
                };

                result = new Question(id, text, opts, correct, diff);
                dispose();
            } catch (Exception ex) {
                String errTitle = isHebrew ? "שגיאה" : "Error";
                String errMsg = isHebrew ? "קלט לא תקין: " : "Invalid input: ";
                JOptionPane.showMessageDialog(this, errMsg + ex.getMessage(), errTitle, JOptionPane.ERROR_MESSAGE);
            }
        }

        public Question getResult() { return result; }
    }

    // ================= HELPER CLASS =================
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String resourcePath) {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
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

    // ================= STANDARD UI METHODS =================

    private void handleLanguageSwitch() {
        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null);
        new Thread(() -> {
            try {
                GameController gc = GameController.getInstance();
                if (gc.getCurrentLanguage() == LanguageManager.Language.EN)
                    gc.setCurrentLanguage(LanguageManager.Language.HE);
                else
                    gc.setCurrentLanguage(LanguageManager.Language.EN);
                gc.getQuestionManager().switchLanguageFromCache();
                Thread.sleep(300);
            } catch (Exception e) { e.printStackTrace(); }
            SwingUtilities.invokeLater(() -> {
                updateUIText();
                updateFilterComboItems();
                loadTable();
                showLanguageToast();
                btnLanguage.setIconPath("/ui/icons/language.png");
                btnLanguage.setOnClick(this::handleLanguageSwitch);
            });
        }).start();
    }

    private void updateUIText() {
        boolean isHe = (GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE);
        setTitle(isHe ? "ניהול שאלות" : "Question Management");
        btnAdd.setText(isHe ? "הוסף" : "Add");
        btnEdit.setText(isHe ? "ערוך" : "Edit");
        btnDelete.setText(isHe ? "מחק" : "Delete");
        btnSave.setText(isHe ? "שמור" : "Save");

        diffLabel.setText(isHe ? "רמת קושי:" : "Difficulty:");
        corrLabel.setText(isHe ? "תשובה נכונה:" : "Correct:");
        idLabel.setText(isHe ? "מזהה:" : "ID:");
        applyBtn.setText(isHe ? "החל" : "Apply");
        clearBtn.setText(isHe ? "נקה" : "Clear");

        String[] headers = isHe
                ? new String[]{"מזהה", "טקסט", "א", "ב", "ג", "ד", "נכונה", "רמה"}
                : new String[]{"ID", "Text", "A", "B", "C", "D", "Correct", "Difficulty"};
        model.setColumnIdentifiers(headers);

        ComponentOrientation o = isHe ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT;
        table.setComponentOrientation(o);
        table.getTableHeader().setComponentOrientation(o);
        if (tableScroll != null) {
            tableScroll.setComponentOrientation(o);
            tableScroll.getViewport().setComponentOrientation(o);
        }
        filterPanel.setComponentOrientation(o);
        rebuildFilterPanel(isHe);
        table.getTableHeader().resizeAndRepaint();
    }

    private void rebuildFilterPanel(boolean isHe) {
        filterPanel.removeAll();
        FlowLayout layout = (FlowLayout) filterPanel.getLayout();
        layout.setAlignment(isHe ? FlowLayout.RIGHT : FlowLayout.LEFT);

        if (isHe) {
            filterPanel.add(diffLabel); filterPanel.add(difficultyFilter);
            filterPanel.add(corrLabel); filterPanel.add(correctAnswerFilter);
            filterPanel.add(idLabel); filterPanel.add(idFilter);
            filterPanel.add(applyBtn); filterPanel.add(clearBtn);
        } else {
            filterPanel.add(diffLabel); filterPanel.add(difficultyFilter);
            filterPanel.add(corrLabel); filterPanel.add(correctAnswerFilter);
            filterPanel.add(idLabel); filterPanel.add(idFilter);
            filterPanel.add(applyBtn); filterPanel.add(clearBtn);
        }
        filterPanel.revalidate(); filterPanel.repaint();
    }

    private void updateFilterComboItems() {
        int diffIdx = difficultyFilter.getSelectedIndex();
        int corrIdx = correctAnswerFilter.getSelectedIndex();
        difficultyFilter.removeAllItems();
        correctAnswerFilter.removeAllItems();

        boolean isHe = (GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE);
        if (isHe) {
            difficultyFilter.addItem("הכל"); difficultyFilter.addItem("קל");
            difficultyFilter.addItem("בינוני"); difficultyFilter.addItem("קשה"); difficultyFilter.addItem("מומחה");
        } else {
            difficultyFilter.addItem("All"); difficultyFilter.addItem("EASY");
            difficultyFilter.addItem("MEDIUM"); difficultyFilter.addItem("HARD"); difficultyFilter.addItem("EXPERT");
        }
        correctAnswerFilter.addItem(isHe ? "הכל" : "All");
        correctAnswerFilter.addItem("1"); correctAnswerFilter.addItem("2");
        correctAnswerFilter.addItem("3"); correctAnswerFilter.addItem("4");

        if (diffIdx >= 0 && diffIdx < difficultyFilter.getItemCount()) difficultyFilter.setSelectedIndex(diffIdx);
        else difficultyFilter.setSelectedIndex(0);
        if (corrIdx >= 0 && corrIdx < correctAnswerFilter.getItemCount()) correctAnswerFilter.setSelectedIndex(corrIdx);
        else correctAnswerFilter.setSelectedIndex(0);
    }

    private void showLanguageToast() {
        boolean isHe = GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE;
        toastLabel.setText(isHe ? "עברית" : "English");
        Dimension size = toastLabel.getPreferredSize();
        int w = size.width + 30; int h = 30;
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
                if (isSel) { l.setBackground(TABLE_SELECTION_BG); l.setForeground(TEXT_COLOR); }
                else { l.setBackground(TABLE_ROW_BG); l.setForeground(TEXT_COLOR); }
                return l;
            }
        });
        return table;
    }

    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 40)); btn.setForeground(ACCENT_COLOR);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_COLOR), new EmptyBorder(5, 15, 5, 15)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(60, 60, 60)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(40, 40, 40)); }
        });
        return btn;
    }

    private Border neonBorder(Color c) {
        return new CompoundBorder(new LineBorder(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200), 2, true),
                new EmptyBorder(10, 12, 10, 12));
    }

    private void styleNeonLabel(JLabel l) {
        l.setForeground(TEXT_COLOR); l.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void styleNeonField(JTextField f) {
        f.setBackground(DIALOG_PANEL); f.setForeground(TEXT_COLOR); f.setCaretColor(ACCENT_COLOR);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14)); f.setBorder(neonBorder(ACCENT_COLOR));
    }

    private void styleNeonCombo(JComboBox<?> c) {
        c.setBackground(DIALOG_PANEL); c.setForeground(TEXT_COLOR);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14)); c.setBorder(neonBorder(ACCENT_COLOR));
    }

    private void loadTable() {
        model.setRowCount(0);
        boolean isHe = (GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE);
        for (Question q : manager.getAllQuestions()) {
            List<String> o = new ArrayList<>(q.getOptions());
            while (o.size() < 4) o.add("");
            model.addRow(new Object[]{q.getId(), q.getText(), o.get(0), o.get(1), o.get(2), o.get(3),
                    (char) ('A' + (q.getCorrectOption() - 'A')), translateDifficulty(q.getDifficultyLevel(), isHe)});
        }
        applyFilters();
    }

    private String translateDifficulty(String diff, boolean toHebrew) {
        if (diff == null) return "";
        if (!toHebrew) return diff;
        return switch (diff.toUpperCase()) {
            case "EASY" -> "קל"; case "MEDIUM" -> "בינוני"; case "HARD" -> "קשה"; case "EXPERT" -> "מומחה"; default -> diff;
        };
    }

    private String translateDifficultyToEnglish(String diff) {
        if (diff == null) return "EASY";
        return switch (diff) {
            case "קל" -> "EASY"; case "בינוני" -> "MEDIUM"; case "קשה" -> "HARD"; case "מומחה" -> "EXPERT"; default -> diff.toUpperCase();
        };
    }

    private Question buildQuestionFromRow(int row) {
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String text = model.getValueAt(row, 1).toString();
        String a = model.getValueAt(row, 2).toString(); String b = model.getValueAt(row, 3).toString();
        String c = model.getValueAt(row, 4).toString(); String d = model.getValueAt(row, 5).toString();
        char correct = numberToChar(Integer.parseInt(model.getValueAt(row, 6).toString()));
        String diff = translateDifficultyToEnglish(model.getValueAt(row, 7).toString());
        List<String> opts = new ArrayList<>(); opts.add(a); opts.add(b); opts.add(c); opts.add(d);
        return new Question(id, text, opts, correct, diff);
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setOpaque(false);
        diffLabel = new JLabel("Difficulty:"); diffLabel.setForeground(TEXT_COLOR);
        difficultyFilter = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD", "EXPERT"});
        styleCombo(difficultyFilter);
        corrLabel = new JLabel("Correct:"); corrLabel.setForeground(TEXT_COLOR);
        correctAnswerFilter = new JComboBox<>(new String[]{"All", "1", "2", "3", "4"});
        styleCombo(correctAnswerFilter);
        idLabel = new JLabel("ID:"); idLabel.setForeground(TEXT_COLOR);
        idFilter = new JTextField(8); idFilter.setFont(new Font("Arial", Font.PLAIN, 14));
        applyBtn = createStyledButton("Apply"); clearBtn = createStyledButton("Clear");

        applyBtn.addActionListener(e -> applyFilters());
        clearBtn.addActionListener(e -> {
            difficultyFilter.setSelectedIndex(0); correctAnswerFilter.setSelectedIndex(0);
            idFilter.setText(""); applyFilters();
        });
        idFilter.addActionListener(e -> applyFilters());
        difficultyFilter.addActionListener(e -> applyFilters());
        correctAnswerFilter.addActionListener(e -> applyFilters());

        p.add(diffLabel); p.add(difficultyFilter); p.add(corrLabel); p.add(correctAnswerFilter);
        p.add(idLabel); p.add(idFilter); p.add(applyBtn); p.add(clearBtn);
        return p;
    }

    private void styleCombo(JComboBox<String> box) {
        box.setBackground(Color.WHITE); box.setForeground(Color.BLACK);
        box.setFont(new Font("Arial", Font.PLAIN, 14)); box.setFocusable(false);
    }

    private void applyFilters() {
        if (sorter == null) return;
        String diff = (String) difficultyFilter.getSelectedItem();
        String corr = (String) correctAnswerFilter.getSelectedItem();
        String idText = idFilter.getText().trim();
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String diffFilter = mapDifficultyToFilter(diff);
        if (diffFilter != null && !"All".equalsIgnoreCase(diffFilter) && !"הכל".equals(diffFilter)) {
            boolean isHe = (GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE);
            String pattern = isHe ? diffFilter : diffFilter;
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(pattern) + "$", 7));
        }
        String corrFilter = mapCorrectToFilter(corr);
        if (corrFilter != null && !"All".equalsIgnoreCase(corrFilter) && !"הכל".equals(corrFilter))
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(corrFilter) + "$", 6));
        if (!idText.isEmpty()) filters.add(RowFilter.regexFilter(java.util.regex.Pattern.quote(idText), 0));

        if (filters.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private String mapDifficultyToFilter(String value) {
        if (value == null) return "All";
        boolean isHe = (GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE);
        return switch (value) {
            case "הכל", "All" -> "All";
            case "קל", "EASY" -> isHe ? "קל" : "EASY";
            case "בינוני", "MEDIUM" -> isHe ? "בינוני" : "MEDIUM";
            case "קשה", "HARD" -> isHe ? "קשה" : "HARD";
            case "מומחה", "EXPERT" -> isHe ? "מומחה" : "EXPERT";
            default -> "All";
        };
    }

    private String mapCorrectToFilter(String value) {
        if (value == null) return "All";
        if ("הכל".equals(value) || "All".equals(value)) return "All";
        return value;
    }

    private static int parseIntSafe(Object o) {
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return Integer.MIN_VALUE; }
    }

    private char numberToChar(int number) {
        return switch (number) { case 1 -> 'A'; case 2 -> 'B'; case 3 -> 'C'; case 4 -> 'D'; default -> 'A'; };
    }
}