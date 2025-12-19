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
            this.img = cropTransparent(raw, 0); // remove uneven transparent padding
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

        int w = getWidth();
        int h = getHeight();

        boolean hover = getModel().isRollover();
        boolean selected = isSelected();

        // glow strength (brightness only, not size)
        float a = selected ? 0.28f : (hover ? 0.18f : 0.0f);

        // constant glow thickness (prevents “selected looks bigger”)
        int glowPx  = 9;
        int glowPx2 = 5;

        // consistent inner drawing area (makes all buttons look same size)
        int inset = 2; // or 0
        int drawX = inset;
        int drawY = inset;
        int drawW = w - inset * 2;
        int drawH = h - inset * 2;

        // --- glow (aligned to the same inset area) ---
        if (a > 0f) {
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

        // --- main image ---
        g2.drawImage(img, drawX, drawY, drawW, drawH, null);

        g2.dispose();
    }

    // Tint the PNG (preserves alpha) so the glow matches the requested color
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

    // Crop transparent padding around the visible pixels (normalizes different PNG margins)
    private static BufferedImage cropTransparent(BufferedImage src, int pad) {
        int w = src.getWidth(), h = src.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                if (a > 0) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) return src;

        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(w - 1, maxX + pad);
        maxY = Math.min(h - 1, maxY + pad);

        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
