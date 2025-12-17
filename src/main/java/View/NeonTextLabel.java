package View;

import javax.swing.*;
import java.awt.*;

public class NeonTextLabel extends JComponent {

    private final String text;
    private final Color baseColor;

    public NeonTextLabel(String text, Color baseColor) {
        this.text = text;
        this.baseColor = baseColor;

        setOpaque(false);
        setFont(new Font("Arial", Font.BOLD, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

        // ---- soft glow (same vibe as EASY/MEDIUM/HARD) ----
        for (int i = 3; i >= 1; i--) {
            g2.setColor(new Color(
                    baseColor.getRed(),
                    baseColor.getGreen(),
                    baseColor.getBlue(),
                    25
            ));
            g2.drawString(text, x - i, y);
            g2.drawString(text, x + i, y);
        }

        // ---- main text ----
        g2.setColor(baseColor);
        g2.drawString(text, x, y);

        g2.dispose();
    }
}
