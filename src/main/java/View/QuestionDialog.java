package View;

import Model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDialog extends JDialog {

    private boolean correct = false;

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color QUESTION_BORDER = new Color(65, 255, 240);
    private static final Color OPTION_BORDER = new Color(210, 220, 255);
    private static final Color OPTION_BORDER_SELECTED = new Color(65, 255, 240);

    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_MUTED = new Color(225, 230, 255);

    private QuestionDialog(Window owner, Question question) {
        super(owner, "Question", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        QuestionCard questionCard = new QuestionCard(question.getText());
        questionCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        ButtonGroup group = new ButtonGroup();
        List<String> opts = question.getOptions();
        char[] letters = new char[]{'A', 'B', 'C', 'D'};

        OptionButton[] buttons = new OptionButton[4];

        // ====== UPDATED: 2x2 GridBagConstraints placement ======
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        for (int i = 0; i < 4; i++) {
            String txt = (i < opts.size()) ? opts.get(i) : "";
            OptionButton ob = new OptionButton(letters[i], txt);
            group.add(ob);
            buttons[i] = ob;

            gc.gridx = i % 2;   // 0,1
            gc.gridy = i / 2;   // 0,0,1,1

            // bottom gap only for first row; right gap only for left column
            gc.insets = new Insets(0, 0, (gc.gridy == 0 ? 14 : 0), (gc.gridx == 0 ? 18 : 0));

            grid.add(ob, gc);
        }
        // =======================================================

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton submit = new JButton("Submit");
        JButton cancel = new JButton("Cancel");
        styleActionButton(cancel, false);
        styleActionButton(submit, true);

        cancel.addActionListener(e -> {
            correct = false;
            dispose();
        });

        submit.addActionListener(e -> {
            String selected = (group.getSelection() == null) ? null : group.getSelection().getActionCommand();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select an answer.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            char selectedChar = normalize(selected.charAt(0));
            char correctChar = normalize(question.getCorrectOption());
            correct = (selectedChar == correctChar);
            dispose();
        });

        actions.add(cancel);
        actions.add(submit);

        content.add(questionCard);
        content.add(grid);
        content.add(actions);

        root.add(content);
        setContentPane(root);

        // RTL only for Hebrew content
        applyRtlIfHebrew(question.getText(), buttons);

        pack();                 // let layout compute correct height
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public static boolean showQuestionDialog(Window owner, Question question) {
        QuestionDialog dlg = new QuestionDialog(owner, question);
        dlg.setVisible(true);
        return dlg.correct;
    }

    private static void styleActionButton(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? QUESTION_BORDER : OPTION_BORDER, 2, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.setBackground(primary ? new Color(15, 40, 80) : new Color(20, 30, 60));
    }

    private static void applyRtlIfHebrew(String text, OptionButton[] buttons) {
        if (text != null && text.matches(".*[\\u0590-\\u05FF].*")) {
            for (OptionButton b : buttons) {
                b.applyRightToLeft();
            }
        }
    }

    private static char normalize(char ch) {
        ch = Character.toUpperCase(ch);
        if (ch >= '1' && ch <= '4') return (char) ('A' + (ch - '1'));
        return ch;
    }

    static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;

        String[] words = text.trim().split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(test) <= maxWidth) {
                line = new StringBuilder(test);
            } else {
                if (line.length() > 0) lines.add(line.toString());
                line = new StringBuilder(w);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOTTOM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    private static class QuestionCard extends JPanel {
        private final String text;

        QuestionCard(String text) {
            this.text = text == null ? "" : text;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            setPreferredSize(new Dimension(520, 95));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 40;

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(QUESTION_BORDER);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            g2.setColor(TEXT);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            drawWrappedCentered(g2, text, new Rectangle(28, 12, w - 56, h - 24));

            g2.dispose();
        }

        private static void drawWrappedCentered(Graphics2D g2, String text, Rectangle r) {
            FontMetrics fm = g2.getFontMetrics();
            List<String> lines = QuestionDialog.wrap(text, fm, r.width);

            int lineHeight = fm.getHeight();
            int totalHeight = lines.size() * lineHeight;
            int y = r.y + (r.height - totalHeight) / 2 + fm.getAscent();

            for (String line : lines) {
                int x = r.x + (r.width - fm.stringWidth(line)) / 2;
                g2.drawString(line, x, y);
                y += lineHeight;
            }
        }
    }

    private static class OptionButton extends JToggleButton {
        private final char letter;
        private final String text;
        private boolean rtl = false;

        // ===== UPDATED: dynamic height based on content =====
        private static final int BTN_W = 250;
        private static final int MIN_H = 70;

        OptionButton(char letter, String text) {
            this.letter = letter;
            this.text = text == null ? "" : text;

            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActionCommand(String.valueOf(letter));

            setFont(new Font("Arial", Font.BOLD, 14));
            setForeground(TEXT_MUTED);

            // start size and then expand if needed
            setPreferredSize(new Dimension(BTN_W, MIN_H));
            setMinimumSize(new Dimension(BTN_W, MIN_H));
            updatePreferredHeight();

            addChangeListener(e -> repaint());
        }

        void applyRightToLeft() {
            rtl = true;
            repaint();
        }

        private void updatePreferredHeight() {
            FontMetrics fm = getFontMetrics(getFont());

            int circleR = 40;
            int padding = 22;
            int gap = 14;

            // text width (same logic as paint)
            int textW = BTN_W - padding - gap - circleR - padding;

            List<String> lines = QuestionDialog.wrap(text, fm, textW);

            int lineHeight = fm.getHeight();
            int textBlockH = lines.size() * lineHeight;

            // +padding, keep at least circle height
            int needed = Math.max(MIN_H, textBlockH + 28);

            setPreferredSize(new Dimension(BTN_W, needed));
            setMinimumSize(new Dimension(BTN_W, needed));
        }
        // ====================================================

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 36;

            g2.setColor(new Color(0, 0, 0, 115));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(isSelected() ? OPTION_BORDER_SELECTED : OPTION_BORDER);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            // English (default): circle on RIGHT
            // Hebrew (rtl): circle on LEFT
            boolean english = !rtl;

            int circleR = 40;
            int circleX = english ? (w - 18 - circleR) : 18;
            int circleY = (h - circleR) / 2;

            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillOval(circleX, circleY, circleR, circleR);

            g2.setColor(new Color(10, 18, 55));
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            drawCentered(g2, String.valueOf(letter), new Rectangle(circleX, circleY, circleR, circleR));

            g2.setColor(isSelected() ? TEXT : TEXT_MUTED);
            g2.setFont(getFont());

            int padding = 22;
            int gap = 14;

            int textX = english ? padding : (circleX + circleR + gap);
            int textW = english ? (w - padding - gap - circleR - padding) : (w - textX - padding);

            Rectangle textRect = new Rectangle(textX, 0, textW, h);
            drawWrappedLeft(g2, text, textRect);

            g2.dispose();
        }

        private static void drawCentered(Graphics2D g2, String s, Rectangle r) {
            FontMetrics fm = g2.getFontMetrics();
            int x = r.x + (r.width - fm.stringWidth(s)) / 2;
            int y = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(s, x, y);
        }

        private static void drawWrappedLeft(Graphics2D g2, String text, Rectangle r) {
            FontMetrics fm = g2.getFontMetrics();
            List<String> lines = QuestionDialog.wrap(text, fm, r.width);

            int lineHeight = fm.getHeight();
            int totalHeight = lines.size() * lineHeight;

            int y = (r.height - totalHeight) / 2 + fm.getAscent();
            for (String line : lines) {
                g2.drawString(line, r.x, y);
                y += lineHeight;
            }
        }
    }
}
