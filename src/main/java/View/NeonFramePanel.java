package View;

import javax.swing.*;
import java.awt.*;

public class NeonFramePanel extends JPanel {
    private final Color glowColor;
    private final int padding;     // space between frame and board
    private final int arc;

    public NeonFramePanel(Color glowColor, int padding, int arc) {
        this.glowColor = glowColor;
        this.padding = padding;
        this.arc = arc;

        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int x = 3;
        int y = 3;
        int rw = w - 6;
        int rh = h - 6;

        // --- glow strokes (NO fill!) ---
        for (int i = 10; i >= 1; i--) {
            int alpha = 10; // very soft glow
            g2.setStroke(new BasicStroke(i));
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha));
            g2.drawRoundRect(x, y, rw, rh, arc, arc);
        }

        // main border
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 180));
        g2.drawRoundRect(x, y, rw, rh, arc, arc);

        g2.dispose();
    }
}
