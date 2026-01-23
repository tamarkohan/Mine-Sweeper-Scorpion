package View;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for showing game results (correct/wrong answers, surprise outcomes).
 * Blocks interaction with the game until the user closes it.
 */
public class ResultMessageDialog extends JDialog {

    private static final Color BG_COLOR = new Color(20, 25, 40);
    private static final Color TEXT_COLOR = Color.WHITE;

    private ResultMessageDialog(Window owner, String title, String message, Color accentColor, boolean isHebrew) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);

        // Main panel with border
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createLineBorder(accentColor, 3, true));

        // === HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 100));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(accentColor);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // X close button
        JLabel closeBtn = new JLabel(" X ");
        closeBtn.setOpaque(true);
        closeBtn.setBackground(new Color(100, 30, 30));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setBackground(new Color(180, 50, 50));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setBackground(new Color(100, 30, 30));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
            }
        });

        if (isHebrew) {
            header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            header.add(titleLabel, BorderLayout.CENTER);
            header.add(closeBtn, BorderLayout.WEST);
        } else {
            header.add(titleLabel, BorderLayout.CENTER);
            header.add(closeBtn, BorderLayout.EAST);
        }

        // === BODY ===
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 15, 18));

        // Parse message lines
        String[] lines = message.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            JLabel lineLabel = new JLabel(line);
            lineLabel.setForeground(TEXT_COLOR);
            lineLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            if (isHebrew) {
                lineLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                lineLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            } else {
                lineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            }

            body.add(lineLabel);
            body.add(Box.createVerticalStrut(4));
        }

        // === FOOTER with OK button ===
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 12, 10));

        JButton okBtn = new JButton("OK");
        okBtn.setBackground(accentColor);
        okBtn.setForeground(Color.WHITE);
        okBtn.setFont(new Font("Arial", Font.BOLD, 13));
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dispose());

        footer.add(okBtn);

        // Assemble
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(body, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Size and position
        pack();
        int minWidth = 350;
        int maxWidth = 500;
        int width = Math.max(minWidth, Math.min(getWidth() + 30, maxWidth));
        int height = getHeight();
        setSize(width, height);
        setLocationRelativeTo(owner);
    }

    /**
     * Shows a modal result dialog and blocks until closed.
     */
    public static void show(Window owner, String title, String message, Color accentColor, boolean isHebrew) {
        ResultMessageDialog dialog = new ResultMessageDialog(owner, title, message, accentColor, isHebrew);
        dialog.setVisible(true);
    }
}