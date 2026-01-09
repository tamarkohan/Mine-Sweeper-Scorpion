package View;

import javax.swing.*;
import java.awt.*;

public class SpecialCellConfirmDialog extends JDialog {

    public enum Choice { YES, NO }
    private Choice choice = Choice.NO;

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color BORDER_CYAN = new Color(65, 255, 240);
    private static final Color BORDER_WHITE = new Color(210, 220, 255);

    private static final Color TEXT = Color.WHITE;
    private static final Color TEXT_MUTED = new Color(225, 230, 255);

    private SpecialCellConfirmDialog(Window owner, String titleText, String bodyText, Color accent) {
        super(owner, titleText, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout(0, 0));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel card = new RoundedCard(accent, 3f);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setForeground(TEXT);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel body = new JLabel("<html><div style='text-align:center; width:520px;'>" + bodyText + "</div></html>");
        body.setForeground(TEXT_MUTED);
        body.setFont(new Font("Arial", Font.BOLD, 15));
        body.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setOpaque(false);

        JButton no = new JButton("No");
        JButton yes = new JButton("Yes");

        styleActionButton(no, false, accent);
        styleActionButton(yes, true, accent);

        no.addActionListener(e -> {
            choice = Choice.NO;
            dispose();
        });

        yes.addActionListener(e -> {
            choice = Choice.YES;
            dispose();
        });

        actions.add(no);
        actions.add(yes);

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        setResizable(false);
        pack();
        setSize(new Dimension(720, 320));
        setLocationRelativeTo(owner);
    }

    public static boolean showConfirm(Window owner, boolean isQuestion) {
        String t = isQuestion ? "Question Cell" : "Surprise Cell";
        String body = isQuestion
                ? "This is a Question cell.<br/>Do you want to activate it?"
                : "This is a Surprise cell.<br/>Do you want to activate it?";

        Color accent = isQuestion ? new Color(65, 255, 240) : new Color(255, 80, 80);

        SpecialCellConfirmDialog dlg = new SpecialCellConfirmDialog(owner, t, body, accent);
        dlg.setVisible(true);
        return dlg.choice == Choice.YES;
    }

    private static void styleActionButton(JButton b, boolean primary, Color accent) {
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color border = primary ? accent : BORDER_WHITE;
        Color bg = primary ? new Color(15, 40, 80) : new Color(20, 30, 60);

        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 2, true),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)
        ));
        b.setBackground(bg);
        b.setOpaque(true);
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

    private static class RoundedCard extends JPanel {
        private final Color accent;
        private final float stroke;

        RoundedCard(Color accent, float stroke) {
            this.accent = accent;
            this.stroke = stroke;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 34;

            g2.setColor(new Color(0, 0, 0, 135));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setStroke(new BasicStroke(stroke));
            g2.setColor(accent);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            g2.dispose();
        }
    }
}
