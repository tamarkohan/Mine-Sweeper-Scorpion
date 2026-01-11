package View;

import Model.Question;
import Model.QuestionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple admin/debug screen to view/add/edit/delete questions and persist to CSV.
 * Styled with the "black neon" theme.
 */
public class QuestionManagementFrame extends JFrame {

    private final QuestionManager manager;
    private final DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    // Filter controls
    private JComboBox<String> difficultyFilter;
    private JComboBox<String> correctAnswerFilter;
    private JTextField idFilter;

    private final Runnable onExitToMenu;

    // Colors (Same as GameHistoryFrame)
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255); // Cyan neon
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80);

    public QuestionManagementFrame(QuestionManager manager) {
        this(manager, null);
    }

    public QuestionManagementFrame(QuestionManager manager, Runnable onExitToMenu) {
        super("Question Management");
        this.manager = manager;
        this.onExitToMenu = onExitToMenu;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Set window icon
        try {
            URL iconUrl = getClass().getResource("/ui/icons/img_1.png");
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }

        String[] cols = {"ID", "Text", "A", "B", "C", "D", "Correct", "Difficulty"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // edit via dialog to keep consistency
            }
        };

        table = createStyledTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Create filter panel
        JPanel filterPanel = createFilterPanel();

        JScrollPane scroll = createStyledScrollPane(table);

        JButton btnAdd = createStyledButton("Add");
        JButton btnEdit = createStyledButton("Edit");
        JButton btnDelete = createStyledButton("Delete");
        JButton btnSave = createStyledButton("Save");
        JButton btnExit = createStyledButton("Back to Menu");

        btnAdd.addActionListener(e -> addQuestion());
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                editQuestion(table.convertRowIndexToModel(selectedRow));
            }
        });
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                deleteQuestion(table.convertRowIndexToModel(selectedRow));
            }
        });
        btnSave.addActionListener(e -> saveQuestions());

        btnExit.addActionListener(e -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

        // ===== Buttons panel (bottom) =====
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(BG_COLOR);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: exit
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(BG_COLOR);
        left.add(btnExit);

        // Right: actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(BG_COLOR);
        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(btnSave);

        btnPanel.add(left, BorderLayout.WEST);
        btnPanel.add(right, BorderLayout.EAST);

        // Use BackgroundPanel for the main content
        BackgroundPanel content = new BackgroundPanel("/ui/menu/question_management_bg.png");
        content.setLayout(new BorderLayout());

        // Increased top padding to 100 to push table down
        content.setBorder(BorderFactory.createEmptyBorder(100, 20, 10, 20));

        // Add filter panel above the table + table in center
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(filterPanel, BorderLayout.NORTH);
        centerWrapper.add(scroll, BorderLayout.CENTER);

        content.add(centerWrapper, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);

        loadTable();

        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    // =======================
    //   STYLING HELPERS
    // =======================

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(TABLE_ROW_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(new Color(50, 50, 50));
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setReorderingAllowed(false);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(ACCENT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Center cell content
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(TABLE_ROW_BG);
        centerRenderer.setForeground(TEXT_COLOR);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        return scroll;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(ACCENT_COLOR);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        // Hover effect
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

    // =======================
    //   TABLE DATA
    // =======================

    private void loadTable() {
        model.setRowCount(0);
        for (Question q : manager.getAllQuestions()) {
            List<String> opts = new ArrayList<>(q.getOptions());
            while (opts.size() < 4) opts.add("");
            model.addRow(new Object[]{
                    q.getId(), q.getText(),
                    opts.get(0), opts.get(1), opts.get(2), opts.get(3),
                    String.valueOf(charToNumber(q.getCorrectOption())),
                    q.getDifficultyLevel()
            });
        }
        applyFilters(); // Apply filters after loading
    }

    private void addQuestion() {
        Question q = promptForQuestion(null);
        if (q != null) {
            manager.addOrReplaceQuestion(q);
            loadTable();
        }
    }

    private void editQuestion(int row) {
        if (row < 0) return;
        Question existing = buildQuestionFromRow(row);
        Question updated = promptForQuestion(existing);
        if (updated != null) {
            manager.addOrReplaceQuestion(updated);
            loadTable();
        }
    }

    private void deleteQuestion(int row) {
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        manager.deleteQuestion(id);
        loadTable();
        applyFilters(); // Reapply filters after deletion
    }

    private void saveQuestions() {
        manager.saveQuestions();
        JOptionPane.showMessageDialog(this, "Questions saved to CSV.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private Question buildQuestionFromRow(int row) {
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String text = model.getValueAt(row, 1).toString();
        String a = model.getValueAt(row, 2).toString();
        String b = model.getValueAt(row, 3).toString();
        String c = model.getValueAt(row, 4).toString();
        String d = model.getValueAt(row, 5).toString();
        String correctStr = model.getValueAt(row, 6).toString();
        char correct = numberToChar(Integer.parseInt(correctStr));
        String diff = model.getValueAt(row, 7).toString();

        List<String> opts = new ArrayList<>();
        opts.add(a);
        opts.add(b);
        opts.add(c);
        opts.add(d);

        return new Question(id, text, opts, correct, diff);
    }

    /**
     * Simple prompt dialog to add/edit a question.
     * Uses combo boxes for correct answer (1-4). Difficulty is typed (EASY/MEDIUM/HARD/EXPERT).
     */
    private Question promptForQuestion(Question existing) {
        JTextField idField = new JTextField(existing == null ? "" : String.valueOf(existing.getId()));
        JTextField textField = new JTextField(existing == null ? "" : existing.getText());

        List<String> exOpts = (existing == null)
                ? new ArrayList<>(List.of("", "", "", ""))
                : new ArrayList<>(existing.getOptions());
        while (exOpts.size() < 4) exOpts.add("");

        JTextField aField = new JTextField(exOpts.get(0));
        JTextField bField = new JTextField(exOpts.get(1));
        JTextField cField = new JTextField(exOpts.get(2));
        JTextField dField = new JTextField(exOpts.get(3));

        // Correct answer dropdown (1-4)
        Integer[] options = {1, 2, 3, 4};
        JComboBox<Integer> correctComboBox = new JComboBox<>(options);
        if (existing != null) {
            correctComboBox.setSelectedItem(charToNumber(existing.getCorrectOption()));
        } else {
            correctComboBox.setSelectedItem(1); // default A
        }

// Difficulty dropdown
        String[] diffs = {"EASY", "MEDIUM", "HARD", "EXPERT"};
        JComboBox<String> diffComboBox = new JComboBox<>(diffs);

// preselect when editing
        if (existing != null && existing.getDifficultyLevel() != null) {
            diffComboBox.setSelectedItem(existing.getDifficultyLevel().toUpperCase());
        } else {
            diffComboBox.setSelectedItem("EASY");
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("ID:")); panel.add(idField);
        panel.add(new JLabel("Text:")); panel.add(textField);
        panel.add(new JLabel("Option A:")); panel.add(aField);
        panel.add(new JLabel("Option B:")); panel.add(bField);
        panel.add(new JLabel("Option C:")); panel.add(cField);
        panel.add(new JLabel("Option D:")); panel.add(dField);
        panel.add(new JLabel("Correct Answer (1–4):")); panel.add(correctComboBox);
        panel.add(new JLabel("Difficulty:")); panel.add(diffComboBox);

        int res = JOptionPane.showConfirmDialog(
                this,
                panel,
                existing == null ? "Add Question" : "Edit Question",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) return null;

        try {
            int id = Integer.parseInt(idField.getText().trim());
            String text = textField.getText().trim();
            String diff = ((String) diffComboBox.getSelectedItem()).toUpperCase();

            List<String> opts = new ArrayList<>();
            opts.add(aField.getText().trim());
            opts.add(bField.getText().trim());
            opts.add(cField.getText().trim());
            opts.add(dField.getText().trim());

            int selectedNumber = (Integer) correctComboBox.getSelectedItem();
            char correct = numberToChar(selectedNumber);

            return new Question(id, text, opts, correct, diff);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // =======================
    //   BACKGROUND PANEL
    // =======================
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

    /**
     * Converts a number (1-4) to the corresponding char ('A'-'D').
     */
    private char numberToChar(int number) {
        switch (number) {
            case 1: return 'A';
            case 2: return 'B';
            case 3: return 'C';
            case 4: return 'D';
            default: return 'A';
        }
    }

    /**
     * Converts a char ('A'-'D') to the corresponding number (1-4).
     */
    private int charToNumber(char ch) {
        switch (Character.toUpperCase(ch)) {
            case 'A': return 1;
            case 'B': return 2;
            case 'C': return 3;
            case 'D': return 4;
            default: return 1;
        }
    }

    // =======================
    //   FILTER UI + LOGIC
    // =======================

    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setOpaque(false);

        JLabel diffLbl = new JLabel("Difficulty:");
        diffLbl.setForeground(TEXT_COLOR);

        difficultyFilter = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD", "EXPERT"});
        styleCombo(difficultyFilter);

        JLabel corrLbl = new JLabel("Correct:");
        corrLbl.setForeground(TEXT_COLOR);

        correctAnswerFilter = new JComboBox<>(new String[]{"All", "1", "2", "3", "4"});
        styleCombo(correctAnswerFilter);

        JLabel idLbl = new JLabel("ID:");
        idLbl.setForeground(TEXT_COLOR);

        idFilter = new JTextField(8);
        idFilter.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton applyBtn = createStyledButton("Apply");
        JButton clearBtn = createStyledButton("Clear");

        applyBtn.addActionListener(e -> applyFilters());
        clearBtn.addActionListener(e -> {
            difficultyFilter.setSelectedIndex(0);
            correctAnswerFilter.setSelectedIndex(0);
            idFilter.setText("");
            applyFilters();
        });

        // Apply on Enter in the ID field
        idFilter.addActionListener(e -> applyFilters());

        // Apply instantly on combo change
        difficultyFilter.addActionListener(e -> applyFilters());
        correctAnswerFilter.addActionListener(e -> applyFilters());

        p.add(diffLbl);
        p.add(difficultyFilter);
        p.add(corrLbl);
        p.add(correctAnswerFilter);
        p.add(idLbl);
        p.add(idFilter);
        p.add(applyBtn);
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
        String idText = (idFilter.getText() == null) ? "" : idFilter.getText().trim();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Column indexes: 0 ID, 6 Correct, 7 Difficulty
        if (diff != null && !"All".equalsIgnoreCase(diff)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(diff) + "$", 7));
        }

        if (corr != null && !"All".equalsIgnoreCase(corr)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(corr) + "$", 6));
        }

        if (!idText.isEmpty()) {
            // allow partial id match
            filters.add(RowFilter.regexFilter(java.util.regex.Pattern.quote(idText), 0));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }
}
