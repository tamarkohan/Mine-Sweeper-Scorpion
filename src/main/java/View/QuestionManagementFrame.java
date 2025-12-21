package View;

import Model.Question;
import Model.QuestionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple admin/debug screen to view/add/edit/delete questions and persist to CSV.
 */
public class QuestionManagementFrame extends JFrame {

    private final QuestionManager manager;
    private final DefaultTableModel model;

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

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnSave = new JButton("Save");

        btnAdd.addActionListener(e -> addQuestion());
        btnEdit.addActionListener(e -> editQuestion(table.getSelectedRow()));
        btnDelete.addActionListener(e -> deleteQuestion(table.getSelectedRow()));
        btnSave.addActionListener(e -> saveQuestions());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnSave);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        loadTable();

        setSize(800, 400);
        setLocationRelativeTo(null);
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
        JTextField correctField = new JTextField(existing == null ? "A" : String.valueOf(existing.getCorrectOption()));
        JTextField diffField = new JTextField(existing == null ? "EASY" : existing.getDifficultyLevel());

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("ID:")); panel.add(idField);
        panel.add(new JLabel("Text:")); panel.add(textField);
        panel.add(new JLabel("Option A:")); panel.add(aField);
        panel.add(new JLabel("Option B:")); panel.add(bField);
        panel.add(new JLabel("Option C:")); panel.add(cField);
        panel.add(new JLabel("Option D:")); panel.add(dField);
        panel.add(new JLabel("Correct (A-D):")); panel.add(correctField);
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
            char correct = correctField.getText().trim().toUpperCase().charAt(0);
            String diff = diffField.getText().trim();
            return new Question(id, text, opts, correct, diff);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}

