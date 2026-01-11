package View;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;

public class NeonHeart extends JComponent {

    private boolean active = true;

    private final Color heartColor;
    private final Color glowColor;

    public NeonHeart(Color heartColor, Color glowColor) {
        this.heartColor = heartColor;
        this.glowColor = glowColor;
        setOpaque(false);
        setPreferredSize(new Dimension(40, 40));
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = new Font("Dialog", Font.BOLD, 28);
        String heart = "\u2665";

        GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), heart);
        Shape base = gv.getOutline();                 // at (0,0)

        Rectangle b = base.getBounds();

        float x = (getWidth() - b.width) / 2f - b.x;
        float y = (getHeight() - b.height) / 2f - b.y;

        Shape shape = java.awt.geom.AffineTransform
                .getTranslateInstance(x, y)
                .createTransformedShape(base);

        if (active) {
            // --- tight halo behind the heart ---
            for (int i = 6; i >= 1; i--) {
                float alpha = 0.05f * i;             // subtle
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setColor(glowColor);

                // expand shape slightly to fake blur (without stroke fuzz)
                Shape glowShape = java.awt.geom.AffineTransform
                        .getTranslateInstance(-0.5 * i, -0.5 * i)
                        .createTransformedShape(shape);

                g2.fill(glowShape);
            }

            // --- crisp fill ---
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(heartColor);
            g2.fill(shape);

        } else {
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(new Color(80, 80, 80, 220));
            g2.fill(shape);
        }

        g2.dispose();
    }
}
