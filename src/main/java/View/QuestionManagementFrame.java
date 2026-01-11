package View;
// Password: ADMIN (for tutors)
import Model.Question;
import Model.QuestionManager;

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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QuestionManagementFrame extends JFrame {

    private final QuestionManager manager;
    private final DefaultTableModel model;
    private final Runnable onExitToMenu;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    // Filter controls
    private JComboBox<String> difficultyFilter;
    private JComboBox<String> correctAnswerFilter;
    private JTextField idFilter;

    // Colors
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80);

    // Neon dialog palette
    private static final Color DIALOG_BG = new Color(5, 6, 10);
    private static final Color DIALOG_PANEL = new Color(11, 15, 26);

    public QuestionManagementFrame(QuestionManager manager) {
        this(manager, null);
    }

    public QuestionManagementFrame(QuestionManager manager, Runnable onExitToMenu) {
        super("Question Management");
        this.manager = manager;
        this.onExitToMenu = onExitToMenu;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
                return false;
            }
        };

        table = createStyledTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        // Disable sorting for text columns
        sorter.setSortable(1, false); // Text
        sorter.setSortable(2, false); // A
        sorter.setSortable(3, false); // B
        sorter.setSortable(4, false); // C
        sorter.setSortable(5, false); // D

        sorter.addRowSorterListener(e -> table.getTableHeader().repaint());
        // ID column (0) numeric
        sorter.setComparator(0, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));

// Correct column (6) numeric
        sorter.setComparator(6, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));

        // Create filter panel
        JPanel filterPanel = createFilterPanel();

        JScrollPane scroll = createStyledScrollPane(table);

        JButton btnAdd = createStyledButton("Add");
        JButton btnEdit = createStyledButton("Edit");
        JButton btnDelete = createStyledButton("Delete");
        JButton btnSave = createStyledButton("Save");
        IconButton btnExit = new IconButton("/ui/icons/back.png");
        btnExit.setPreferredSize(new Dimension(46, 46));  // adjust if needed
        btnExit.setSafePadPx(2);                          // optional (prevents clipping)
        btnExit.setOnClick(() -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

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

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false); //  let background show through
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);     //  transparent
        left.add(btnExit);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);    //  transparent
        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(btnSave);


        btnPanel.add(left, BorderLayout.WEST);
        btnPanel.add(right, BorderLayout.EAST);

        BackgroundPanel content = new BackgroundPanel("/ui/menu/question_management_bg.png");
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(120, 20, 10, 20));

        // Add filter panel above the table + table in center
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

// ⬇️ makes table thinner horizontally
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(
                0,   // top
                30,  // left  (≈ 0.8 cm)
                0,   // bottom
                30   // right (≈ 0.8 cm)
        ));

        centerWrapper.add(filterPanel, BorderLayout.NORTH);
        centerWrapper.add(scroll, BorderLayout.CENTER);


        content.add(centerWrapper, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
        manager.loadQuestions();   // refresh from file each time frame opens
        loadTable();


        setUndecorated(true);               // removes the top window strip
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);


    }

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

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);

                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Arial", Font.BOLD, 14));
                lbl.setForeground(ACCENT_COLOR);
                lbl.setOpaque(true);
                lbl.setBackground(TABLE_HEADER_BG);

                String base = (value == null) ? "" : value.toString();
                int modelCol = table.convertColumnIndexToModel(col);

// Default: no arrows
                String text = base;

// Only add arrows for sortable columns
                if (isSortableColumn(modelCol)) {
                    text = base + "  ↕";

                    RowSorter<?> rs = table.getRowSorter();
                    if (rs != null && !rs.getSortKeys().isEmpty()) {
                        RowSorter.SortKey key = rs.getSortKeys().get(0);
                        if (key.getColumn() == modelCol) {
                            if (key.getSortOrder() == SortOrder.ASCENDING) text = base + "  ▲";
                            else if (key.getSortOrder() == SortOrder.DESCENDING) text = base + "  ▼";
                        }
                    }
                }

                lbl.setText(text);

                return lbl;
            }
        });

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
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(new Color(0, 0, 0, 0)); // fully transparent
        scroll.setBorder(BorderFactory.createEmptyBorder());
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
        return new CompoundBorder(
                new LineBorder(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200), 2, true),
                new EmptyBorder(10, 12, 10, 12)
        );
    }

    private void styleNeonLabel(JLabel l) {
        l.setForeground(TEXT_COLOR);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void styleNeonField(JTextField f) {
        f.setBackground(DIALOG_PANEL);
        f.setForeground(TEXT_COLOR);
        f.setCaretColor(ACCENT_COLOR);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(neonBorder(ACCENT_COLOR));
    }

    private void styleNeonCombo(JComboBox<?> c) {
        c.setBackground(DIALOG_PANEL);
        c.setForeground(TEXT_COLOR);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(neonBorder(ACCENT_COLOR));
    }

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
            manager.saveQuestions();   // AUTO SAVE
            manager.loadQuestions();   // reload to ensure table matches file
            loadTable();
        }
    }


    private void editQuestion(int row) {
        if (row < 0) return;
        Question existing = buildQuestionFromRow(row);
        Question updated = promptForQuestion(existing);
        if (updated != null) {
            manager.addOrReplaceQuestion(updated);
            manager.saveQuestions();   // AUTO SAVE
            manager.loadQuestions();
            loadTable();
        }
    }


    private void deleteQuestion(int row) {
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        manager.deleteQuestion(id);
        manager.saveQuestions();       // AUTO SAVE
        manager.loadQuestions();
        loadTable();
        applyFilters();
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

    private Question promptForQuestion(Question existing) {
        NeonQuestionDialog dialog = new NeonQuestionDialog(this, existing);
        dialog.setVisible(true);
        return dialog.getResult();
    }

    // =======================
    //   NEON ADD/EDIT DIALOG
    // =======================
    private class NeonQuestionDialog extends JDialog {

        private Question result = null;

        private final JTextField idField = new JTextField();
        private final JTextField textField = new JTextField();
        private final JTextField aField = new JTextField();
        private final JTextField bField = new JTextField();
        private final JTextField cField = new JTextField();
        private final JTextField dField = new JTextField();

        private final JComboBox<String> correctCombo =
                new JComboBox<>(new String[]{"A", "B", "C", "D"});

        private final JComboBox<String> diffCombo =
                new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD", "EXPERT"});

        private boolean resizing = false;

        NeonQuestionDialog(JFrame owner, Question existing) {
            super(owner, existing == null ? "Add Question" : "Edit Question", true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // limit field width so they don't stretch too much
            int fieldW = 360;
            Dimension fieldMax = new Dimension(fieldW, 36);
            for (JTextField f : new JTextField[]{idField, textField, aField, bField, cField, dField}) {
                f.setPreferredSize(new Dimension(fieldW, 36));
                f.setMaximumSize(fieldMax);
            }

            JPanel root = new JPanel(new BorderLayout(14, 14));
            root.setBackground(DIALOG_BG);
            root.setBorder(new EmptyBorder(16, 16, 16, 16));

            JLabel header = new JLabel(existing == null ? " Add New Question" : " Edit Question");
            header.setForeground(ACCENT_COLOR);
            header.setFont(new Font("Segoe UI", Font.BOLD, 22));
            root.add(header, BorderLayout.NORTH);

            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(DIALOG_PANEL);
            card.setBorder(neonBorder(ACCENT_COLOR));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(8, 8, 8, 8);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1;

            if (existing != null) {
                idField.setText(String.valueOf(existing.getId()));
                textField.setText(existing.getText());

                List<String> exOpts = new ArrayList<>(existing.getOptions());
                while (exOpts.size() < 4) exOpts.add("");

                aField.setText(exOpts.get(0));
                bField.setText(exOpts.get(1));
                cField.setText(exOpts.get(2));
                dField.setText(exOpts.get(3));

                correctCombo.setSelectedItem(String.valueOf(existing.getCorrectOption()).toUpperCase());
                diffCombo.setSelectedItem(existing.getDifficultyLevel());
            } else {
                correctCombo.setSelectedItem("A");
                diffCombo.setSelectedItem("EASY");
            }

            styleNeonField(idField);
            styleNeonField(textField);
            styleNeonField(aField);
            styleNeonField(bField);
            styleNeonField(cField);
            styleNeonField(dField);
            styleNeonCombo(correctCombo);
            styleNeonCombo(diffCombo);

            addRow(card, gc, 0, "ID", idField);
            addRow(card, gc, 1, "Text", textField);
            addRow(card, gc, 2, "Option A", aField);
            addRow(card, gc, 3, "Option B", bField);
            addRow(card, gc, 4, "Option C", cField);
            addRow(card, gc, 5, "Option D", dField);
            addRow(card, gc, 6, "Correct", correctCombo);
            addRow(card, gc, 7, "Difficulty", diffCombo);

            // put card inside scroll so UI stays clean on square resizing
            JScrollPane sc = new JScrollPane(card);
            sc.setBorder(BorderFactory.createEmptyBorder());
            sc.getViewport().setBackground(DIALOG_BG);
            sc.setOpaque(false);

            root.add(sc, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setBackground(DIALOG_BG);

            JButton btnCancel = createStyledButton("Cancel");
            JButton btnSave = createStyledButton("Save");

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> onSave(existing));

            buttons.add(btnCancel);
            buttons.add(btnSave);
            root.add(buttons, BorderLayout.SOUTH);

            setContentPane(root);

            // Start square + allow resizing
            pack();
            int start = 560;
            setSize(start, start);
            setResizable(true);
            setMinimumSize(new Dimension(520, 520));
            setLocationRelativeTo(owner);

            // Keep it square while resizing
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (resizing) return;
                    resizing = true;

                    int s = Math.max(getWidth(), getHeight());
                    Dimension min = getMinimumSize();
                    s = Math.max(s, Math.max(min.width, min.height));
                    setSize(s, s);

                    resizing = false;
                }
            });
        }

        private void addRow(JPanel card, GridBagConstraints gc, int row, String label, JComponent input) {
            JLabel l = new JLabel(label + ":");
            styleNeonLabel(l);

            gc.gridx = 0;
            gc.gridy = row;
            gc.weightx = 0;
            card.add(l, gc);

            gc.gridx = 1;
            gc.gridy = row;
            gc.weightx = 1;
            card.add(input, gc);
        }

        private void onSave(Question existing) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String text = textField.getText().trim();
                if (text.isEmpty()) throw new IllegalArgumentException("Text is empty.");

                List<String> opts = new ArrayList<>();
                opts.add(aField.getText().trim());
                opts.add(bField.getText().trim());
                opts.add(cField.getText().trim());
                opts.add(dField.getText().trim());

                char correct = correctCombo.getSelectedItem().toString().charAt(0);
                String diff = diffCombo.getSelectedItem().toString().trim();

                result = new Question(id, text, opts, correct, diff);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public Question getResult() {
            return result;
        }
    }

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
            case 1:
                return 'A';
            case 2:
                return 'B';
            case 3:
                return 'C';
            case 4:
                return 'D';
            default:
                return 'A';
        }
    }

    /**
     * Converts a char ('A'-'D') to the corresponding number (1-4).
     */
    private int charToNumber(char ch) {
        switch (Character.toUpperCase(ch)) {
            case 'A':
                return 1;
            case 'B':
                return 2;
            case 'C':
                return 3;
            case 'D':
                return 4;
            default:
                return 1;
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

    private static int parseIntSafe(Object o) {
        if (o == null) return Integer.MIN_VALUE;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("-")) return Integer.MIN_VALUE;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }

    private boolean isSortableColumn(int modelCol) {
        return modelCol == 0 || modelCol == 6 || modelCol == 7;
    }

}
