package View;

import javax.swing.*;
import java.awt.*;

public class DifficultyToggle extends JToggleButton {

    // Match your START vibe (soft neon green, not too intense)
    private static final Color SELECTED_GREEN = new Color(190, 255, 190); // softer than before
    private static final Color NORMAL_TEXT    = new Color(235, 235, 235);
    private static final Color HOVER_TEXT     = new Color(255, 255, 255);

    public DifficultyToggle(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(NORMAL_TEXT);

        // Bigger + bold + clean (no external fonts)
        setFont(new Font("Arial Black", Font.PLAIN, 26));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String text = getText();
        FontMetrics fm = g2.getFontMetrics(getFont());
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

        // base color
        Color c = NORMAL_TEXT;
        if (getModel().isRollover()) c = HOVER_TEXT;
        if (isSelected()) c = SELECTED_GREEN;

        // ✅ small, crisp glow only when selected (NO big blur block)
        if (isSelected()) {
            g2.setFont(getFont());
            g2.setColor(new Color(SELECTED_GREEN.getRed(), SELECTED_GREEN.getGreen(), SELECTED_GREEN.getBlue(), 80));
            g2.drawString(text, x - 1, y);
            g2.drawString(text, x + 1, y);
            g2.drawString(text, x, y - 1);
            g2.drawString(text, x, y + 1);
        }

        // main text
        g2.setColor(c);
        g2.drawString(text, x, y);

        g2.dispose();
    }
}
