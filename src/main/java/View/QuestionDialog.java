
        package View;

import Controller.GameController;
import util.LanguageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class QuestionDialog extends JDialog {

    private GameController.QuestionAnswerResult result = GameController.QuestionAnswerResult.SKIPPED;

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color QUESTION_BORDER = new Color(65, 255, 240);
    private static final Color OPTION_BORDER = new Color(210, 220, 255);

    private static final Color TURQUOISE = new Color(65, 255, 240);
    private static final Color FRAME_GREY = new Color(140, 150, 160);

    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_MUTED = new Color(225, 230, 255);

    private JPanel resultOverlay;
    private JLabel resultTitle;
    private JLabel resultDetails;
    private String selectedAnswerText;
    private String correctAnswerText;

    private QuestionDialog(Window owner, GameController.QuestionDTO question) {
        super(owner, "", ModalityType.APPLICATION_MODAL);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        setTitle(LanguageManager.get("question", lang));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                result = GameController.QuestionAnswerResult.SKIPPED;
            }
        });

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        QuestionCard questionCard = new QuestionCard(question.text);
        questionCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 14));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        ButtonGroup group = new ButtonGroup();
        List<String> opts = question.options;
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

        JButton submit = new JButton(LanguageManager.get("submit", lang));
        JButton cancel = new JButton(LanguageManager.get("cancel", lang));
        styleActionButton(cancel, false);
        styleActionButton(submit, true);

        cancel.addActionListener(e -> {
            result = GameController.QuestionAnswerResult.SKIPPED;
            dispose();
        });

        submit.addActionListener(e -> {
            ButtonModel sel = group.getSelection();
            if (sel == null) {
                LanguageManager.Language currentLang = GameController.getInstance().getCurrentLanguage();
                JOptionPane.showMessageDialog(
                        this,
                        LanguageManager.get("no_answer_selected", currentLang),
                        LanguageManager.get("no_answer_title", currentLang),
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            for (OptionButton btn : buttons) {
                if (btn.isSelected()) {
                    selectedAnswerText = btn.text;
                    break;
                }
            }

            int correctIndex = normalize(question.correctOption) - 'A';
            if (correctIndex >= 0 && correctIndex < opts.size()) {
                correctAnswerText = opts.get(correctIndex);
            }

            char selectedChar = normalize(group.getSelection().getActionCommand().charAt(0));
            char correctChar = normalize(question.correctOption);

            boolean isCorrect = (selectedChar == correctChar);
            result = isCorrect ? GameController.QuestionAnswerResult.CORRECT
                    : GameController.QuestionAnswerResult.WRONG;

            dispose();
        });

        actions.add(cancel);
        actions.add(submit);

        content.add(questionCard);
        content.add(grid);
        content.add(actions);

        root.add(content);
        setContentPane(root);

        applyRtlIfHebrew(question.text, buttons);
        pack();

        setMinimumSize(new Dimension(760, 480));
        setSize(new Dimension(820, 520));
        setResizable(false);

        root.setPreferredSize(new Dimension(900, 560));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void showResult(boolean isCorrect) {
        if (resultOverlay == null) {
            resultOverlay = new JPanel(new GridBagLayout());
            resultOverlay.setOpaque(true);
            resultOverlay.setBackground(new Color(0, 0, 0, 170));

            JPanel card = new JPanel();
            card.setOpaque(true);
            card.setBackground(new Color(10, 18, 55));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

            Color accent = isCorrect ? new Color(80, 255, 120) : new Color(255, 90, 90);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accent, 3, true),
                    BorderFactory.createEmptyBorder(18, 22, 18, 22)
            ));

            resultTitle = new JLabel("", SwingConstants.CENTER);
            resultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultTitle.setFont(new Font("Arial", Font.BOLD, 22));
            resultTitle.setForeground(Color.WHITE);

            resultDetails = new JLabel("", SwingConstants.CENTER);
            resultDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultDetails.setFont(new Font("Arial", Font.PLAIN, 14));
            resultDetails.setForeground(new Color(225, 230, 255));

            LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
            JButton ok = new JButton(LanguageManager.get("ok", lang));
            styleActionButton(ok, true);
            ok.setAlignmentX(Component.CENTER_ALIGNMENT);
            ok.addActionListener(e -> dispose());

            card.add(resultTitle);
            card.add(Box.createVerticalStrut(10));
            card.add(resultDetails);
            card.add(Box.createVerticalStrut(16));
            card.add(ok);

            resultOverlay.add(card);
            setGlassPane(resultOverlay);
        }

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        resultTitle.setText(isCorrect ? LanguageManager.get("correct", lang) :
                LanguageManager.get("wrong", lang));

        String picked = (selectedAnswerText == null) ? "-" : selectedAnswerText;
        String correct = (correctAnswerText == null) ? "-" : correctAnswerText;

        resultDetails.setText("<html><div style='text-align:center;'>"
                + "<b>" + LanguageManager.get("your_answer", lang) + "</b> " + escapeHtml(picked) + "<br>"
                + "<b>" + LanguageManager.get("correct_answer", lang) + "</b> " + escapeHtml(correct)
                + "</div></html>");

        resultOverlay.setVisible(true);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static GameController.QuestionAnswerResult showQuestionDialog(
            Window owner, GameController.QuestionDTO question) {
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
            int arc = 50;

            boolean selected = isSelected();

            g2.setColor(new Color(0, 0, 0, 115));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (selected) {
                for (int i = 18; i >= 1; i--) {
                    g2.setStroke(new BasicStroke(i));
                    g2.setColor(new Color(TURQUOISE.getRed(), TURQUOISE.getGreen(), TURQUOISE.getBlue(), 14));
                    g2.drawRoundRect(
                            2 - i / 3,
                            2 - i / 3,
                            w - 4 + i / 2,
                            h - 4 + i / 2,
                            arc + i * 5 / 2,
                            arc + i * 5 / 2
                    );
                }

                g2.setStroke(new BasicStroke(2.5f));
                g2.setColor(TURQUOISE);
                g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            } else {
                g2.setStroke(new BasicStroke(2.2f));
                g2.setColor(FRAME_GREY);
                g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);
            }

            int circleR = 40;
            int circleX = rtl ? 18 : (w - 18 - circleR);
            int circleY = (h - circleR) / 2;

            if (selected) {
                g2.setColor(new Color(TURQUOISE.getRed(), TURQUOISE.getGreen(), TURQUOISE.getBlue(), 35));
                g2.fillOval(circleX - 6, circleY - 6, circleR + 12, circleR + 12);

                g2.setColor(TURQUOISE);
                g2.fillOval(circleX, circleY, circleR, circleR);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(0, 0, 0, 90));
                g2.drawOval(circleX, circleY, circleR, circleR);
            } else {
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillOval(circleX, circleY, circleR, circleR);
            }g2.setColor(new Color(10, 18, 55));
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            drawCentered(g2, String.valueOf(letter), new Rectangle(circleX, circleY, circleR, circleR));

            g2.setColor(selected ? TEXT : TEXT_MUTED);
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
    }}