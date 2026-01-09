package View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OutcomeDialog extends JDialog {

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color BORDER = new Color(65, 255, 240);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(225, 230, 255);

    private OutcomeDialog(Window owner, String message) {
        super(owner, "Message", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        TitleCard card = new TitleCard("Message");
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel bodyCard = new BodyCard(message);
        bodyCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        bodyCard.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton ok = new JButton("OK");
        styleButton(ok, true);
        ok.addActionListener(e -> dispose());

        actions.add(ok);

        content.add(card);
        content.add(bodyCard);
        content.add(actions);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public static void show(Window owner, String message) {
        OutcomeDialog dlg = new OutcomeDialog(owner, message);
        dlg.setVisible(true);
    }

    private static void styleButton(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? BORDER : new Color(210, 220, 255), 2, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        b.setBackground(primary ? new Color(15, 40, 80) : new Color(20, 30, 60));
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

        TitleCard(String text) {
            this.text = text == null ? "" : text;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            setPreferredSize(new Dimension(520, 70));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 38;

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(BORDER);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            g2.setColor(TEXT);
            g2.setFont(new Font("Arial", Font.BOLD, 20));

            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }

    private static class BodyCard extends JPanel {
        private final List<String> lines;

        BodyCard(String msg) {
            this.lines = lines(msg);
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
            int arc = 34;

            g2.setColor(new Color(0, 0, 0, 115));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(new Color(255, 90, 90));
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.setColor(MUTED);

            int y = 34;
            for (String line : lines) {
                g2.drawString(line, 18, y);
                y += 24;
            }

            g2.dispose();
        }
    }
}
