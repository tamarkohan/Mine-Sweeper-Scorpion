package View;

import Controller.GameController;
import util.LanguageManager;

import javax.swing.*;

import java.awt.*;

/**
 * Modal confirmation dialog for game actions (exit, restart, activate, etc.).
 *
 * Purpose:
 * - Ask the user to confirm or cancel an action
 * - Block interaction with the main window until a decision is made
 * - Return the user's choice as a boolean result
 * - Supports Hebrew and English with proper RTL handling
 */
public class ConfirmDialog extends JDialog {

    // Stores the user's choice (true = Yes, false = No / closed)
    private boolean accepted = false;

    // Background gradient colors
    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    // UI color palette
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(225, 230, 255);

    // Accent color for this instance
    private final Color accentColor;

    /**
     * Private constructor.
     * Use the static show(...) methods to display this dialog.
     */
    private ConfirmDialog(Window owner, String titleText, String bodyText, Color accentColor, boolean isHebrew) {
        super(owner, titleText, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);

        this.accentColor = accentColor;

        // Root panel with custom gradient background
        BackgroundPanel root = new BackgroundPanel(accentColor);
        root.setLayout(new BorderLayout(0, 0));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Main vertical content container
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Title card displaying the dialog title
        TitleCard card = new TitleCard(titleText, accentColor);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Body text (supports multiline via HTML)
        String alignment = isHebrew ? "right" : "center";
        String direction = isHebrew ? "rtl" : "ltr";
        JLabel body = new JLabel("<html><div style='text-align:" + alignment + "; direction:" + direction + ";'>" + bodyText.replace("\n", "<br>") + "</div></html>");
        body.setForeground(MUTED);
        body.setFont(new Font("Arial", Font.BOLD, 16));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.setBorder(BorderFactory.createEmptyBorder(14, 10, 10, 10));
        if (isHebrew) {
            body.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }

        // Button container (Yes / No)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        String yesText = isHebrew ? "כן" : "Yes";
        String noText = isHebrew ? "לא" : "No";

        JButton yes = new JButton(yesText);
        JButton no = new JButton(noText);

        // Apply visual styling to buttons
        styleButton(no, false, accentColor);
        styleButton(yes, true, accentColor);

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

        // In Hebrew, "No" should come first (right side visually)
        if (isHebrew) {
            actions.add(no);
            actions.add(yes);
        } else {
            actions.add(yes);
            actions.add(no);
        }

        // Assemble dialog layout
        content.add(card);
        content.add(body);
        content.add(actions);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        // ESC key closes dialog (as "No")
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Default button is "No" for safety
        getRootPane().setDefaultButton(no);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Displays the dialog with custom accent color and language support.
     *
     * @return true if the user confirmed, false otherwise
     */
    public static boolean show(Window owner, String titleText, String bodyText, Color accentColor, boolean isHebrew) {
        ConfirmDialog dlg = new ConfirmDialog(owner, titleText, bodyText, accentColor, isHebrew);
        dlg.setVisible(true);
        return dlg.accepted;
    }

    /**
     * Displays the dialog using current game language settings.
     * Uses default cyan accent color.
     *
     * @return true if the user confirmed, false otherwise
     */
    public static boolean show(Window owner, String titleText, String bodyText) {
        boolean isHebrew = GameController.getInstance().getCurrentLanguage() == LanguageManager.Language.HE;
        Color defaultAccent = new Color(65, 255, 240); // Cyan
        return show(owner, titleText, bodyText, defaultAccent, isHebrew);
    }

    /**
     * Applies consistent styling to dialog buttons.
     *
     * @param primary true for primary action button, false for secondary
     */
    private static void styleButton(JButton b, boolean primary, Color accentColor) {
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? accentColor : new Color(210, 220, 255), 2, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        b.setBackground(primary ? new Color(15, 40, 80) : new Color(20, 30, 60));

        // Hover effect
        Color hoverBg = primary ? new Color(25, 60, 110) : new Color(35, 50, 85);
        Color normalBg = b.getBackground();
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(normalBg);
            }
        });
    }

    /**
     * Custom panel that paints a vertical gradient background with accent border.
     */
    private static class BackgroundPanel extends JPanel {
        private final Color accentColor;

        BackgroundPanel(Color accentColor) {
            this.accentColor = accentColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Gradient background
            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOTTOM);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 20, 20);

            // Accent border
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(accentColor);
            g2.drawRoundRect(1, 1, w - 3, h - 3, 20, 20);

            g2.dispose();
        }
    }

    /**
     * Rounded title panel with border and centered text.
     */
    private static class TitleCard extends JPanel {

        private final String text;
        private final Color accentColor;

        TitleCard(String text, Color accentColor) {
            this.text = text == null ? "" : text;
            this.accentColor = accentColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            setPreferredSize(new Dimension(420, 70));
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

            // Neon border using accent color
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(accentColor);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            // Title text
            g2.setColor(TEXT);
            g2.setFont(new Font("Arial", Font.BOLD, 20));

            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }
}