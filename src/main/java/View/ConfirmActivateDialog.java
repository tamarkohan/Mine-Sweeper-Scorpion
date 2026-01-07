package View;

import javax.swing.*;
import java.awt.*;

public class ConfirmActivateDialog extends JDialog {

    private boolean accepted = false;

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color BORDER = new Color(65, 255, 240);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(225, 230, 255);

    private ConfirmActivateDialog(Window owner, String titleText, String bodyText) {
        super(owner, titleText, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout(0, 0));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        TitleCard card = new TitleCard(titleText);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel body = new JLabel("<html><div style='text-align:center;'>" + bodyText + "</div></html>");
        body.setForeground(MUTED);
        body.setFont(new Font("Arial", Font.BOLD, 16));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.setBorder(BorderFactory.createEmptyBorder(14, 10, 10, 10));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");
        styleButton(no, false);
        styleButton(yes, true);

        no.addActionListener(e -> {
            accepted = false;
            dispose();
        });

        yes.addActionListener(e -> {
            accepted = true;
            dispose();
        });

        actions.add(no);
        actions.add(yes);

        content.add(card);
        content.add(body);
        content.add(actions);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public static boolean show(Window owner, String titleText, String bodyText) {
        ConfirmActivateDialog dlg = new ConfirmActivateDialog(owner, titleText, bodyText);
        dlg.setVisible(true);
        return dlg.accepted;
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
            setPreferredSize(new Dimension(520, 78));
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
            g2.setColor(BORDER);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            g2.setColor(TEXT);
            g2.setFont(new Font("Arial", Font.BOLD, 22));

            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }
}
