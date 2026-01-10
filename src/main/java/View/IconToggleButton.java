package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class IconToggleButton extends JToggleButton {

    private final BufferedImage img;   // cropped base image
    private final Color glow;

    public IconToggleButton(String imagePath, Color glow) {
        try {
            BufferedImage raw = ImageIO.read(getClass().getResource(imagePath));
            if (raw == null) throw new RuntimeException("Image is null: " + imagePath);
            // Crop out empty transparent space (tolerance 20 removes faint shadows)
            this.img = cropTransparent(raw, 20);
        } catch (Exception e) {
            throw new RuntimeException("Failed loading: " + imagePath, e);
        }

        this.glow = glow;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int compW = getWidth();
        int compH = getHeight();

        // --- 1. Calculate Scale to Maintain Aspect Ratio ---
        // This prevents the "Ruined" stretched look.
        // We fit the image inside the component bounds while keeping its shape.
        double imgAspect = (double) img.getWidth() / img.getHeight();
        double compAspect = (double) compW / compH;

        int drawW, drawH;
        int margin = 4; // Space for the border

        int availW = compW - (margin * 2);
        int availH = compH - (margin * 2);

        if (compAspect > imgAspect) {
            // Component is wider than image -> constrain by height
            drawH = availH;
            drawW = (int) (availH * imgAspect);
        } else {
            // Component is taller than image -> constrain by width
            drawW = availW;
            drawH = (int) (availW / imgAspect);
        }

        // --- 2. Center the Image ---
        int drawX = (compW - drawW) / 2;
        int drawY = (compH - drawH) / 2;

        boolean hover = getModel().isRollover();
        boolean selected = isSelected();
        float a = selected ? 0.6f : (hover ? 0.2f : 0.0f);

        // --- 3. Draw Glow (Behind) ---
        if (a > 0f) {
            int glowPx = 9;
            int glowPx2 = 5;
            BufferedImage glowImg = tinted(img, glow);

            g2.setComposite(AlphaComposite.SrcOver.derive(a));
            g2.drawImage(glowImg,
                    drawX - glowPx, drawY - glowPx,
                    drawW + glowPx * 2, drawH + glowPx * 2,
                    null);

            g2.setComposite(AlphaComposite.SrcOver.derive(a * 0.65f));
            g2.drawImage(glowImg,
                    drawX - glowPx2, drawY - glowPx2,
                    drawW + glowPx2 * 2, drawH + glowPx2 * 2,
                    null);

            g2.setComposite(AlphaComposite.SrcOver);
        }

        // --- 4. Draw Main Image ---
        g2.drawImage(img, drawX, drawY, drawW, drawH, null);

        // --- 5. Draw Selection Border ---
        if (selected) {
            g2.setColor(glow);
            g2.setStroke(new BasicStroke(3f));

            // Draw the rectangle exactly around the image calculated above
            // +/- adjustments allow the border to sit just outside the pixels
            g2.drawRoundRect(drawX - 2, drawY - 2, drawW + 4, drawH + 4, 15, 15);
        }

        g2.dispose();
    }

    // Helper: Tint the PNG
    private static BufferedImage tinted(BufferedImage src, Color c) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = out.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        gg.drawImage(src, 0, 0, null);
        gg.setComposite(AlphaComposite.SrcAtop);
        gg.setColor(c);
        gg.fillRect(0, 0, out.getWidth(), out.getHeight());
        gg.dispose();
        return out;
    }

    // Helper: Crop transparent padding
    private static BufferedImage cropTransparent(BufferedImage src, int tolerance) {
        int w = src.getWidth(), h = src.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                if (a > tolerance) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) return src;

        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}