package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddQuestionDialog extends JDialog {

    private final JTextField txtTitle = new JTextField();
    private final JComboBox<String> cmbDifficulty =
            new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
    private final JTextArea txtQuestion = new JTextArea(6, 30);
    private final JTextField txtAnswer = new JTextField();

    public AddQuestionDialog(JFrame owner) {
        super(owner, "Add Question", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(NeonUI.BG);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel header = new JLabel("➕ Add New Question");
        header.setForeground(NeonUI.ACCENT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(NeonUI.PANEL);
        card.setBorder(NeonUI.neonLine(NeonUI.ACCENT));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Row 0: Title
        addRow(card, gc, 0, "Title", txtTitle);

        // Row 1: Difficulty
        addRow(card, gc, 1, "Difficulty", cmbDifficulty);

        // Row 2: Question (TextArea)
        JLabel qLabel = new JLabel("Question");
        NeonUI.styleLabel(qLabel);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        card.add(qLabel, gc);

        NeonUI.styleArea(txtQuestion);
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(txtQuestion);
        scroll.setBorder(NeonUI.neonLine(NeonUI.ACCENT));
        scroll.getViewport().setBackground(NeonUI.PANEL);

        gc.gridx = 1;
        gc.gridy = 2;
        gc.weightx = 1;
        card.add(scroll, gc);

        // Row 3: Answer
        addRow(card, gc, 3, "Answer", txtAnswer);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(NeonUI.BG);

        JButton btnSave = NeonUI.neonButton("Save");
        JButton btnCancel = NeonUI.neonButton("Cancel");

        // Optional: make Cancel a different neon
        btnCancel.setBorder(NeonUI.neonLine(NeonUI.ACCENT_2));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        buttons.add(btnCancel);
        buttons.add(btnSave);

        root.add(header, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel card, GridBagConstraints gc, int row, String label, JComponent input) {
        JLabel l = new JLabel(label);
        NeonUI.styleLabel(l);

        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        card.add(l, gc);

        if (input instanceof JTextField tf) NeonUI.styleField(tf);
        if (input instanceof JComboBox<?> cb) NeonUI.styleCombo(cb);

        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 1;
        card.add(input, gc);
    }

    private void onSave() {
        // Example validation:
        if (txtTitle.getText().trim().isEmpty() || txtAnswer.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Title and Answer are required.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        dispose();
    }
}
