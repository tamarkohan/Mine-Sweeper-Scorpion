package View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class OutcomeDialog extends JDialog {

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(225, 230, 255);

    private static final Color ACCENT_GREEN = new Color(80, 255, 120);
    private static final Color ACCENT_RED   = new Color(255, 90, 90);

    private OutcomeDialog(Window owner, String title, String message, Color accent) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        TitleCard header = new TitleCard(title, accent);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        BodyCard body = new BodyCard(message, accent);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton ok = new JButton("OK");
        styleButton(ok, accent);
        ok.addActionListener(e -> dispose());
        actions.add(ok);

        content.add(header);
        content.add(body);
        content.add(actions);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        root.setPreferredSize(new Dimension(820, 360));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // ---------- Public API ----------
    public static void showWrong(Window owner, String message) {
        new OutcomeDialog(owner, "Wrong!", message, ACCENT_RED).setVisible(true);
    }

    public static void showCorrect(Window owner, String message) {
        new OutcomeDialog(owner, "Correct!", message, ACCENT_GREEN).setVisible(true);
    }

    public static void showQuestionOutcome(Window owner,
                                           boolean isCorrect,
                                           String yourAnswer,
                                           String correctAnswer,
                                           String details) {
        Color accent = isCorrect ? ACCENT_GREEN : ACCENT_RED;
        String title = isCorrect ? " CORRECT!" : " WRONG!";

        String msg =
                "Your answer: " + (yourAnswer == null ? "-" : yourAnswer) + "\n" +
                        "Correct answer: " + (correctAnswer == null ? "-" : correctAnswer) + "\n\n" +
                        (details == null ? "" : details);

        new OutcomeDialog(owner, title, msg, accent).setVisible(true);
    }



    public static void show(Window owner, String title, String message) {
        // default: cyan-ish
        new OutcomeDialog(owner, title, message, new Color(65, 255, 240)).setVisible(true);
    }

    // ---------- Styling ----------
    private static void styleButton(JButton b, Color accent) {
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        b.setBackground(new Color(15, 40, 80));
        b.setOpaque(true);
    }

    private static List<String> lines(String msg) {
        if (msg == null) return List.of();
        String[] raw = msg.split("\\r?\\n");
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    // ---------- Inner classes ----------
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

    private static class TitleCard extends JPanel {
        private final String text;
        private final Color accent;

        TitleCard(String text, Color accent) {
            this.text = text == null ? "" : text;
            this.accent = accent;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            setPreferredSize(new Dimension(520, 80));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Only draw the title text (no card, no border)
            g2.setColor(accent); //  title is red/green/cyan
            g2.setFont(new Font("Arial", Font.BOLD, 44)); // adjust size if you want

            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);

            g2.dispose();
        }


    }

    private static class BodyCard extends JPanel {
        private final List<String> lines;
        private final Color accent;

        BodyCard(String msg, Color accent) {
            this.lines = lines(msg);
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(520, Math.max(120, 38 + this.lines.size() * 24)));
            setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 40;

            // glass card
            g2.setColor(new Color(0, 0, 0, 115));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // neon border
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(accent);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            // message lines
            int y = 34;
            for (String line : lines) {
                boolean important =
                        line.startsWith("Score:") ||
                                line.startsWith("Lives:") ||
                                line.startsWith("Activation cost:") ||
                                line.toLowerCase().startsWith("special effect:");

                g2.setFont(new Font("Arial", important ? Font.BOLD : Font.PLAIN, important ? 16 : 15));
                g2.setColor(important ? TEXT : MUTED);

                String printed = (important ? "• " : "  ") + line;
                g2.drawString(printed, 18, y);
                y += 24;
            }

            g2.dispose();
        }
    }
        public static void showQuestionOutcomeFromMessage(Window owner, String outcomeMessage) {
            if (outcomeMessage == null) return;

            String lower = outcomeMessage.toLowerCase();

            boolean isCorrect = lower.contains("correct");
            boolean isWrong = lower.contains("wrong");
            boolean isSkipped = lower.contains("didn't answer") || lower.contains("did not answer") || lower.contains("skipped");

            Color accent;
            String title;

            if (isCorrect) {
                accent = ACCENT_GREEN;
                title = "CORRECT!";
            } else if (isWrong) {
                accent = ACCENT_RED;
                title = "WRONG!";
            } else if (isSkipped) {
                // keep cyan for skipped / neutral
                accent = new Color(65, 255, 240);
                title = "SKIPPED";
            } else {
                // fallback if message doesn't match
                accent = new Color(65, 255, 240);
                title = "OUTCOME";
            }

            // optional: remove leading "Correct!/Wrong!" if your model puts it
            String cleaned = outcomeMessage
                    .replaceFirst("(?i)^\\s*correct!\\s*\\R", "")
                    .replaceFirst("(?i)^\\s*wrong!\\s*\\R", "");

            new OutcomeDialog(owner, title, cleaned, accent).setVisible(true);
        }

    public static void showSurpriseOutcomeFromMessage(Window owner, String outcomeMessage) {
        if (outcomeMessage == null) return;

        String lower = outcomeMessage.toLowerCase();
        boolean good = lower.contains("good");
        boolean bad  = lower.contains("bad");

        Color accent = good ? ACCENT_GREEN : (bad ? ACCENT_RED : new Color(65, 255, 240));
        String title = good ? "SURPRISE: GOOD!" : (bad ? "SURPRISE: BAD!" : "SURPRISE!");

        new OutcomeDialog(owner, title, outcomeMessage, accent).setVisible(true);
    }


}
