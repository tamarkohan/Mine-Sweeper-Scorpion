package View;

import Model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Simple modal dialog to present a multiple-choice question and return correctness.
 */
public class QuestionDialog extends JDialog {

    private boolean correct = false;

    private QuestionDialog(Window owner, Question question) {
        super(owner, "Question", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblText = new JLabel("<html><body style='width:320px'>" + question.getText() + "</body></html>");
        lblText.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblText);
        panel.add(Box.createVerticalStrut(10));

        ButtonGroup group = new ButtonGroup();
        List<String> opts = question.getOptions();

        JRadioButton[] radios = new JRadioButton[opts.size()];
        char optionChar = 'A';
        for (int i = 0; i < opts.size(); i++) {
            String text = (i < opts.size()) ? opts.get(i) : "";
            JRadioButton rb = new JRadioButton(optionChar + ". " + text);
            rb.setActionCommand(String.valueOf(optionChar));
            group.add(rb);
            panel.add(rb);
            optionChar++;
            radios[i] = rb;
        }

        panel.add(Box.createVerticalStrut(10));

        JButton btnOk = new JButton("Submit");
        btnOk.addActionListener(e -> {
            String selected = group.getSelection() != null ? group.getSelection().getActionCommand() : null;
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select an answer.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            correct = selected.charAt(0) == question.getCorrectOption();
            dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnOk);
        panel.add(btnPanel);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Shows the dialog and returns true if the answer was correct.
     */
    public static boolean showQuestionDialog(Window owner, Question question) {
        QuestionDialog dlg = new QuestionDialog(owner, question);
        dlg.setVisible(true);
        return dlg.correct;
    }
}
