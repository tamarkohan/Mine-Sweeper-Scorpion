package View;

import Model.Question;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import Model.QuestionResult;

public class QuestionDialog extends JDialog {
    private QuestionResult result = QuestionResult.SKIPPED;

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

        // If user clicks X, treat as SKIPPED (do not mark wrong)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                result = QuestionResult.SKIPPED;
            }
        });

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        QuestionCard questionCard = new QuestionCard(question.getText());
        questionCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 14));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        ButtonGroup group = new ButtonGroup();
        List<String> opts = question.getOptions();
        char[] letters = new char[]{'A', 'B', 'C', 'D'};

        OptionButton[] buttons = new OptionButton[4];
        for (int i = 0; i < 4; i++) {
            String txt = (i < opts.size()) ? opts.get(i) : "";
            OptionButton ob = new OptionButton(letters[i], txt);
            group.add(ob);
            buttons[i] = ob;
            grid.add(ob);
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton submit = new JButton("Submit");
        JButton cancel = new JButton("Cancel");
        styleActionButton(cancel, false);
        styleActionButton(submit, true);

        cancel.addActionListener(e -> {
            result = QuestionResult.SKIPPED;
            dispose();
        });


        submit.addActionListener(e -> {
            if (group.getSelection() == null) {
                JOptionPane.showMessageDialog(this, "Please select an answer.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            char selectedChar = normalize(group.getSelection().getActionCommand().charAt(0));
            char correctChar = normalize(question.getCorrectOption());

            result = (selectedChar == correctChar) ? QuestionResult.CORRECT : QuestionResult.WRONG;
            dispose();
        });


        actions.add(cancel);
        actions.add(submit);

        content.add(questionCard);
        content.add(grid);
        content.add(actions);

        root.add(content);
        setContentPane(root);
        buildResultOverlay();

        applyRtlIfHebrew(question.getText(), buttons);
        pack(); // let it compute preferred sizes FIRST

        setMinimumSize(new Dimension(760, 480));   // prevent tiny dialog
        setSize(new Dimension(820, 520));          // final size
        setResizable(false);                       // keep fixed (or true if you want)

        // Bigger dialog
        root.setPreferredSize(new Dimension(900, 560));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

    }

    public static QuestionResult showQuestionDialog(Window owner, Question question) {
        QuestionDialog dlg = new QuestionDialog(owner, question);
        dlg.setVisible(true);
        return dlg.result;
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
        b.setOpaque(true);
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

    private static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
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
            setPreferredSize(new Dimension(620, 120));

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

        OptionButton(char letter, String text) {
            this.letter = letter;
            this.text = text == null ? "" : text;

            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setActionCommand(String.valueOf(letter));

            setPreferredSize(new Dimension(320, 110));
            setFont(new Font("Arial", Font.BOLD, 14));
            setForeground(TEXT_MUTED);

            addChangeListener(e -> repaint());
        }

        void applyRightToLeft() {
            rtl = true;
        }

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

            int circleR = 40;
            int circleX = rtl ? 18 : (w - 18 - circleR);
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

            int textX = rtl ? (circleX + circleR + gap) : padding;
            int textW = rtl ? (w - textX - padding) : (w - padding - gap - circleR - padding);

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

            int maxLines = 4;
            if (lines.size() > maxLines) {
                lines = new ArrayList<>(lines.subList(0, maxLines));
                String last = lines.get(maxLines - 1);
                while (fm.stringWidth(last + "…") > r.width && last.length() > 0) {
                    last = last.substring(0, last.length() - 1);
                }
                lines.set(maxLines - 1, last + "…");
            }

            int lineHeight = fm.getHeight();
            int totalHeight = lines.size() * lineHeight;
            int y = (r.height - totalHeight) / 2 + fm.getAscent();

            for (String line : lines) {
                g2.drawString(line, r.x, y);
                y += lineHeight;
            }
        }
    }
    private void buildResultOverlay() {
        resultOverlay = new JPanel(new GridBagLayout());
        resultOverlay.setOpaque(true);
        resultOverlay.setBackground(new Color(0, 0, 0, 150)); // dark transparent cover
        resultOverlay.setVisible(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255,255,255,70), 2, true),
                BorderFactory.createEmptyBorder(18, 26, 18, 26)
        ));
        card.setBackground(new Color(8, 14, 40));

        resultTitle = new JLabel("RESULT", SwingConstants.CENTER);
        resultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultTitle.setFont(new Font("Arial", Font.BOLD, 26));
        resultTitle.setForeground(Color.WHITE);

        resultDetails = new JLabel("", SwingConstants.CENTER);
        resultDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultDetails.setFont(new Font("Arial", Font.PLAIN, 15));
        resultDetails.setForeground(new Color(230, 235, 255));

        card.add(resultTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(resultDetails);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        resultOverlay.add(card, gbc);

        // IMPORTANT: put overlay on top of everything
        getLayeredPane().add(resultOverlay, JLayeredPane.POPUP_LAYER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                resultOverlay.setBounds(0, 0, getWidth(), getHeight());
            }
        });
    }

    private void showResult(boolean isCorrect) {
        Color good = new Color(50, 220, 140);
        Color bad  = new Color(255, 80, 80);

        resultTitle.setText(isCorrect ? "✅ CORRECT!" : "❌ WRONG!");
        resultTitle.setForeground(isCorrect ? good : bad);

        String html =
                "<html><div style='text-align:center; width:520px;'>"
                        + "<b>Your answer:</b> " + (selectedAnswerText == null ? "-" : selectedAnswerText)
                        + "<br><b>Correct answer:</b> " + (correctAnswerText == null ? "-" : correctAnswerText)
                        + "</div></html>";

        resultDetails.setText(html);

        // show overlay in the middle
        resultOverlay.setBounds(0, 0, getWidth(), getHeight());
        resultOverlay.setVisible(true);

        // close after short delay
        Timer t = new Timer(1200, e -> {
            ((Timer) e.getSource()).stop();
            dispose();
        });
        t.setRepeats(false);
        t.start();
    }


}
