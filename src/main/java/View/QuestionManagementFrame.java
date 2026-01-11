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

    // Colors (Same as GameHistoryFrame)
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255); // Cyan neon
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80);

    public QuestionManagementFrame(QuestionManager manager) {
        super("Question Management");
        this.manager = manager;

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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(BG_COLOR);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnSave);

        // Use BackgroundPanel for the main content
        BackgroundPanel content = new BackgroundPanel("/ui/menu/bg.png");
        content.setLayout(new BorderLayout());
        
        // Increased top padding to 100 to push table down
        content.setBorder(BorderFactory.createEmptyBorder(100, 20, 10, 20));

        // Add filter panel above the table
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(filterPanel, BorderLayout.NORTH);
        centerWrapper.add(scroll, BorderLayout.CENTER);

        // Removed the NeonTextLabel title so the background title is visible
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
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

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

    private void loadTable() {
        model.setRowCount(0);
        for (Question q : manager.getAllQuestions()) {
            List<String> opts = new ArrayList<>(q.getOptions());
            while (opts.size() < 4) opts.add("");
            model.addRow(new Object[]{
                    q.getId(), q.getText(),
                    opts.get(0), opts.get(1), opts.get(2), opts.get(3),
                    String.valueOf(convertCorrectOptionToInt(q.getCorrectOption())),
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
        char correct = convertIntToCorrectOption(Integer.parseInt(correctStr));
        String diff = model.getValueAt(row, 7).toString();
        List<String> opts = new ArrayList<>();
        opts.add(a); opts.add(b); opts.add(c); opts.add(d);
        return new Question(id, text, opts, correct, diff);
    }

    /**
     * Simple prompt dialog to add/edit a question.
     */
    private Question promptForQuestion(Question existing) {
        JTextField idField = new JTextField(existing == null ? "" : String.valueOf(existing.getId()));
        JTextField textField = new JTextField(existing == null ? "" : existing.getText());
        JTextField aField = new JTextField(existing == null ? "" : existing.getOptions().get(0));
        JTextField bField = new JTextField(existing == null ? "" : existing.getOptions().get(1));
        JTextField cField = new JTextField(existing == null ? "" : existing.getOptions().get(2));
        JTextField dField = new JTextField(existing == null ? "" : existing.getOptions().get(3));
        JTextField correctField = new JTextField(existing == null ? "1" : String.valueOf(convertCorrectOptionToInt(existing.getCorrectOption())));
        JTextField diffField = new JTextField(existing == null ? "EASY" : existing.getDifficultyLevel());

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("ID:")); panel.add(idField);
        panel.add(new JLabel("Text:")); panel.add(textField);
        panel.add(new JLabel("Option A:")); panel.add(aField);
        panel.add(new JLabel("Option B:")); panel.add(bField);
        panel.add(new JLabel("Option C:")); panel.add(cField);
        panel.add(new JLabel("Option D:")); panel.add(dField);
        panel.add(new JLabel("Correct (1-4):")); panel.add(correctField);
        panel.add(new JLabel("Difficulty (EASY/MEDIUM/HARD/EXPERT):")); panel.add(diffField);

        int res = JOptionPane.showConfirmDialog(this, panel,
                existing == null ? "Add Question" : "Edit Question",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return null;

        try {
            int id = Integer.parseInt(idField.getText().trim());
            String text = textField.getText().trim();
            List<String> opts = new ArrayList<>();
            opts.add(aField.getText().trim());
            opts.add(bField.getText().trim());
            opts.add(cField.getText().trim());
            opts.add(dField.getText().trim());
            int correctInt = Integer.parseInt(correctField.getText().trim());
            char correct = convertIntToCorrectOption(correctInt);
            String diff = diffField.getText().trim();
            return new Question(id, text, opts, correct, diff);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // =======================
    //   FILTER PANEL
    // =======================

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                        "Filters",
                        0,
                        0,
                        new Font("Arial", Font.BOLD, 12),
                        ACCENT_COLOR
                ),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // ID Filter
        JLabel idLabel = new JLabel("ID:");
        idLabel.setForeground(TEXT_COLOR);
        idLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        idFilter = createStyledTextField();
        idFilter.setPreferredSize(new Dimension(100, 25));
        idFilter.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        // Correct Answer Filter
        JLabel correctLabel = new JLabel("Correct Answer:");
        correctLabel.setForeground(TEXT_COLOR);
        correctLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        correctAnswerFilter = createStyledComboBox(new String[]{"ALL", "1", "2", "3", "4"});
        correctAnswerFilter.addActionListener(e -> applyFilters());

        // Difficulty Filter
        JLabel diffLabel = new JLabel("Difficulty:");
        diffLabel.setForeground(TEXT_COLOR);
        diffLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        difficultyFilter = createStyledComboBox(new String[]{"ALL", "EASY", "MEDIUM", "HARD", "EXPERT"});
        difficultyFilter.addActionListener(e -> applyFilters());

        panel.add(idLabel);
        panel.add(idFilter);
        panel.add(correctLabel);
        panel.add(correctAnswerFilter);
        panel.add(diffLabel);
        panel.add(difficultyFilter);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(new Color(20, 20, 20));
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(ACCENT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(new Color(20, 20, 20));
        combo.setForeground(TEXT_COLOR);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Style the combo box button
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setBackground(isSelected ? TABLE_SELECTION_BG : TABLE_ROW_BG);
                    label.setForeground(TEXT_COLOR);
                }
                return c;
            }
        });

        return combo;
    }

    private void applyFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // ID filter (partial match, numeric)
        String idText = idFilter.getText().trim();
        if (!idText.isEmpty()) {
            try {
                final String idFilterText = idText;
                filters.add(RowFilter.regexFilter(idFilterText, 0)); // Column 0 is ID
            } catch (java.util.regex.PatternSyntaxException e) {
                // Invalid regex, ignore
            }
        }

        // Correct Answer filter (numeric 1/2/3/4 matches table display)
        String correctValue = (String) correctAnswerFilter.getSelectedItem();
        if (correctValue != null && !"ALL".equals(correctValue)) {
            filters.add(RowFilter.regexFilter("^" + correctValue + "$", 6)); // Column 6 is Correct
        }

        // Difficulty filter
        String diffValue = (String) difficultyFilter.getSelectedItem();
        if (diffValue != null && !"ALL".equals(diffValue)) {
            filters.add(RowFilter.regexFilter("^" + diffValue + "$", 7)); // Column 7 is Difficulty
        }

        // Combine all filters with AND logic
        if (!filters.isEmpty()) {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        } else {
            sorter.setRowFilter(null); // No filter
        }
    }

    // =======================
    //   HELPER METHODS
    // =======================

    /**
     * Converts correct option from char ('A'/'B'/'C'/'D') to int (1/2/3/4) for display.
     */
    private int convertCorrectOptionToInt(char correctOption) {
        return switch (Character.toUpperCase(correctOption)) {
            case 'A' -> 1;
            case 'B' -> 2;
            case 'C' -> 3;
            case 'D' -> 4;
            default -> 1; // fallback
        };
    }

    /**
     * Converts correct option from int (1/2/3/4) to char ('A'/'B'/'C'/'D') for model storage.
     */
    private char convertIntToCorrectOption(int correctInt) {
        return switch (correctInt) {
            case 1 -> 'A';
            case 2 -> 'B';
            case 3 -> 'C';
            case 4 -> 'D';
            default -> 'A'; // fallback
        };
    }
}