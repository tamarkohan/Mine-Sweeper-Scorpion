package View;

import Controller.GameController;
import util.LanguageManager;

import javax.swing.*;
import java.awt.*;

public class ResultMessageDialog extends JDialog {

    private static final Color BG_COLOR = new Color(20, 25, 40);
    private static final Color TEXT_COLOR = Color.WHITE;

    private ResultMessageDialog(Window owner, String title, String message, Color accentColor, boolean isRTL) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createLineBorder(accentColor, 3, true));

        // === HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 100));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(accentColor);
        int titleFontSize = LanguageManager.getAdjustedFontSize(18, lang);
        titleLabel.setFont(new Font("Arial", Font.BOLD, titleFontSize));

        JLabel closeBtn = new JLabel(" X ");
        closeBtn.setOpaque(true);
        closeBtn.setBackground(new Color(100, 30, 30));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { closeBtn.setBackground(new Color(180, 50, 50)); }
            public void mouseExited(java.awt.event.MouseEvent e) { closeBtn.setBackground(new Color(100, 30, 30)); }
            public void mouseClicked(java.awt.event.MouseEvent e) { dispose(); }
        });

        if (isRTL) {
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

        String[] lines = message.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            JLabel lineLabel = new JLabel(line);
            lineLabel.setForeground(TEXT_COLOR);
            int lineFontSize = LanguageManager.getAdjustedFontSize(14, lang);
            lineLabel.setFont(new Font("Arial", Font.PLAIN, lineFontSize));

            if (isRTL) {
                lineLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                lineLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            } else {
                lineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            }

            body.add(lineLabel);
            body.add(Box.createVerticalStrut(4));
        }

        // === FOOTER ===
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 12, 10));

        String okText = LanguageManager.get("ok", lang);
        JButton okBtn = new JButton(okText);
        okBtn.setBackground(accentColor);
        okBtn.setForeground(Color.WHITE);
        int btnFontSize = LanguageManager.getAdjustedFontSize(13, lang);
        okBtn.setFont(new Font("Arial", Font.BOLD, btnFontSize));
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dispose());
        footer.add(okBtn);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(body, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        pack();
        int minWidth = 350;
        int maxWidth = 550;
        int width = Math.max(minWidth, Math.min(getWidth() + 40, maxWidth));
        setSize(width, getHeight());
        setLocationRelativeTo(owner);
    }

    public static void show(Window owner, String title, String message, Color accentColor, boolean isRTL) {
        new ResultMessageDialog(owner, title, message, accentColor, isRTL).setVisible(true);
    }

    public static void show(Window owner, String title, String message, Color accentColor) {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        boolean isRTL = LanguageManager.isRTL(lang);
        show(owner, title, message, accentColor, isRTL);
    }
}