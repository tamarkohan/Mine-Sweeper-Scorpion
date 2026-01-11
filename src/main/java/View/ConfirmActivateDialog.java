package View;

import javax.swing.*;
import java.awt.*;

/**
 * Modal confirmation dialog used before activating a special action.
 *
 * Purpose:
 * - Ask the user to confirm or cancel an activation
 * - Block interaction with the main window until a decision is made
 * - Return the user's choice as a boolean result
 */
public class ConfirmActivateDialog extends JDialog {

    // Stores the user's choice (true = Yes, false = No / closed)
    private boolean accepted = false;

    // Background gradient colors
    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    // UI color palette
    private static final Color BORDER = new Color(65, 255, 240);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(225, 230, 255);

    /**
     * Private constructor.
     * Use the static show(...) method to display this dialog.
     */
    private ConfirmActivateDialog(Window owner, String titleText, String bodyText) {
        super(owner, titleText, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Root panel with custom gradient background
        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout(0, 0));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Main vertical content container
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Title card displaying the dialog title
        TitleCard card = new TitleCard(titleText);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Body text (supports multiline via HTML)
        JLabel body = new JLabel("<html><div style='text-align:center;'>" + bodyText + "</div></html>");
        body.setForeground(MUTED);
        body.setFont(new Font("Arial", Font.BOLD, 16));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.setBorder(BorderFactory.createEmptyBorder(14, 10, 10, 10));

        // Button container (Yes / No)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");

        // Apply visual styling to buttons
        styleButton(no, false);
        styleButton(yes, true);

        // Cancel action
        no.addActionListener(e -> {
            accepted = false;
            dispose();
        });

        // Confirm action
        yes.addActionListener(e -> {
            accepted = true;
            dispose();
        });

        actions.add(no);
        actions.add(yes);

        // Assemble dialog layout
        content.add(card);
        content.add(body);
        content.add(actions);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Displays the dialog and blocks until the user responds.
     *
     * @return true if the user confirmed, false otherwise
     */
    public static boolean show(Window owner, String titleText, String bodyText) {
        ConfirmActivateDialog dlg = new ConfirmActivateDialog(owner, titleText, bodyText);
        dlg.setVisible(true);
        return dlg.accepted;
    }

    /**
     * Applies consistent styling to dialog buttons.
     *
     * @param primary true for primary action button, false for secondary
     */
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

    /**
     * Custom panel that paints a vertical gradient background.
     */
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

    /**
     * Rounded title panel with border and centered text.
     */
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

            // Semi-transparent background
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Neon border
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(BORDER);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            // Title text
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
