package View;

import Model.Question;
import Model.QuestionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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

        JTable table = createStyledTable(model);
        JScrollPane scroll = createStyledScrollPane(table);

        JButton btnAdd = createStyledButton("Add");
        JButton btnEdit = createStyledButton("Edit");
        JButton btnDelete = createStyledButton("Delete");
        JButton btnSave = createStyledButton("Save");


        JButton btnExit = createStyledButton("Back to Menu");

        btnAdd.addActionListener(e -> addQuestion());
        btnEdit.addActionListener(e -> editQuestion(table.getSelectedRow()));
        btnDelete.addActionListener(e -> deleteQuestion(table.getSelectedRow()));
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
        BackgroundPanel content = new BackgroundPanel("/ui/menu/bg.png");
        content.setLayout(new BorderLayout());

        // Increased top padding to 100 to push table down
        content.setBorder(BorderFactory.createEmptyBorder(100, 20, 10, 20));

        content.add(scroll, BorderLayout.CENTER);
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

    private void loadTable() {
        model.setRowCount(0);
        for (Question q : manager.getAllQuestions()) {
            List<String> opts = new ArrayList<>(q.getOptions());
            while (opts.size() < 4) opts.add("");
            model.addRow(new Object[]{
                    q.getId(), q.getText(),
                    opts.get(0), opts.get(1), opts.get(2), opts.get(3),
                    String.valueOf(q.getCorrectOption()),
                    q.getDifficultyLevel()
            });
        }
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
        char correct = model.getValueAt(row, 6).toString().charAt(0);
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
     */
    private Question promptForQuestion(Question existing) {
        JTextField idField = new JTextField(existing == null ? "" : String.valueOf(existing.getId()));
        JTextField textField = new JTextField(existing == null ? "" : existing.getText());

        // safe get options
        List<String> exOpts = existing == null ? List.of("", "", "", "") : existing.getOptions();
        while (exOpts.size() < 4) exOpts = new ArrayList<>(exOpts) {{ add(""); }};

        JTextField aField = new JTextField(existing == null ? "" : exOpts.get(0));
        JTextField bField = new JTextField(existing == null ? "" : exOpts.get(1));
        JTextField cField = new JTextField(existing == null ? "" : exOpts.get(2));
        JTextField dField = new JTextField(existing == null ? "" : exOpts.get(3));

        JTextField correctField = new JTextField(existing == null ? "A" : String.valueOf(existing.getCorrectOption()));
        JTextField diffField = new JTextField(existing == null ? "EASY" : existing.getDifficultyLevel());

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Text:"));
        panel.add(textField);
        panel.add(new JLabel("Option A:"));
        panel.add(aField);
        panel.add(new JLabel("Option B:"));
        panel.add(bField);
        panel.add(new JLabel("Option C:"));
        panel.add(cField);
        panel.add(new JLabel("Option D:"));
        panel.add(dField);
        panel.add(new JLabel("Correct (A-D):"));
        panel.add(correctField);
        panel.add(new JLabel("Difficulty (EASY/MEDIUM/HARD/EXPERT):"));
        panel.add(diffField);

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

            String corr = correctField.getText().trim().toUpperCase();
            if (corr.isEmpty()) throw new IllegalArgumentException("Correct option is empty.");
            char correct = corr.charAt(0);
            if (correct < 'A' || correct > 'D') throw new IllegalArgumentException("Correct must be A-D.");

            String diff = diffField.getText().trim();
            return new Question(id, text, opts, correct, diff);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
}
