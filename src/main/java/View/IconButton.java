package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class IconButton extends JComponent {
    private BufferedImage img;
    private boolean hover = false;
    private boolean pressed = false;
    private Runnable onClick;

    // If true, removes black padding around the button (perfect for your menu PNGs)
    private final boolean cropBlackPadding;

    // Glow tuning
    private float hoverGlowAlpha   = 0.22f;
    private float pressedGlowAlpha = 0.60f;
    private int   hoverGlowPx      = 10;
    private int   pressedGlowPx    = 18;

    // Pop tuning
    private double pressedScale = 1.03;

    // Safe padding so pop never clips corners
    private int safePadPx = 0;

    public IconButton(String imgPath) {
        this(imgPath, false);
    }

    public IconButton(String imgPath, boolean cropBlackPadding) {
        this.cropBlackPadding = cropBlackPadding;

        try {
            img = ImageIO.read(getClass().getResource(imgPath));
            if (img == null) throw new RuntimeException("Image is null: " + imgPath);

            if (cropBlackPadding) {
                img = cropNearBlack(img, 18);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed loading: " + imgPath, e);
        }

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; pressed = false; repaint(); }

            @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) {
                boolean inside = contains(e.getPoint());
                pressed = false;
                repaint();
                if (inside && onClick != null) onClick.run();
            }
        });
    }

    public void setOnClick(Runnable r) { this.onClick = r; }

    // Optional setters if you want to tweak later
    public void setSafePadPx(int px) { this.safePadPx = Math.max(0, px); repaint(); }
    public void setPressedScale(double s) { this.pressedScale = Math.max(1.0, s); repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || img == null) {
            g2.dispose();
            return;
        }

        // Scale (pop) around center, but we draw inside a safe padding
        double scale = pressed ? pressedScale : 1.0;

        int pad = safePadPx;
        int iw = Math.max(1, w - pad * 2);
        int ih = Math.max(1, h - pad * 2);

        // ---- Draw glow (based on padded rect) ----
        if (pressed) {
            g2.setComposite(AlphaComposite.SrcOver.derive(pressedGlowAlpha));
            g2.drawImage(img,
                    pad - pressedGlowPx, pad - pressedGlowPx,
                    iw + pressedGlowPx * 2, ih + pressedGlowPx * 2,
                    null);
            g2.setComposite(AlphaComposite.SrcOver);
        } else if (hover) {
            g2.setComposite(AlphaComposite.SrcOver.derive(hoverGlowAlpha));
            g2.drawImage(img,
                    pad - hoverGlowPx, pad - hoverGlowPx,
                    iw + hoverGlowPx * 2, ih + hoverGlowPx * 2,
                    null);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // ---- Pop transform ----
        // (we apply scaling around center, but because we draw inside pad, corners won't clip)
        g2.translate(w / 2.0, h / 2.0);
        g2.scale(scale, scale);
        g2.translate(-w / 2.0, -h / 2.0);

        // ---- Main image (inside padding) ----
        g2.drawImage(img, pad, pad, iw, ih, null);

        g2.dispose();
    }

    // Crops the “almost black” outer padding.
    private static BufferedImage cropNearBlack(BufferedImage src, int threshold) {
        int w = src.getWidth();
        int h = src.getHeight();

        int minX = w, minY = h, maxX = -1, maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = (argb) & 0xFF;

                boolean nearBlack = r <= threshold && g <= threshold && b <= threshold;
                if (!nearBlack) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) return src;

        int pad = 6;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(w - 1, maxX + pad);
        maxY = Math.min(h - 1, maxY + pad);

        return src.getSubimage(minX, minY, (maxX - minX + 1), (maxY - minY + 1));
    }
}
