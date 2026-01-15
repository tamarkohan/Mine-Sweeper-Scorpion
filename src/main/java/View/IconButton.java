package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

public class IconButton extends JComponent {
    private BufferedImage img;
    private boolean hover = false;
    private boolean pressed = false;
    private Runnable onClick;

    // Settings
    private final boolean cropBlackPadding;
    private float hoverGlowAlpha = 0.22f;
    private float pressedGlowAlpha = 0.30f;
    private int hoverGlowPx = 10;
    private int pressedGlowPx = 12;
    private double pressedScale = 1.03;
    private int safePadPx = 0;

    public IconButton(String imgPath) {
        this(imgPath, false);
    }

    public IconButton(String imgPath, boolean cropBlackPadding) {
        this.cropBlackPadding = cropBlackPadding;
        loadImage(imgPath);

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { hover = false; pressed = false; repaint(); }
            @Override
            public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
            @Override
            public void mouseReleased(MouseEvent e) {
                boolean inside = contains(e.getPoint());
                pressed = false;
                repaint();
                if (inside && onClick != null) onClick.run();
            }
        });
    }

    private void loadImage(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Image not found: " + path);
                return;
            }
            BufferedImage loadedImg = ImageIO.read(url);
            if (cropBlackPadding) {
                this.img = cropNearBlack(loadedImg, 18);
            } else {
                this.img = loadedImg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Methods restored for compatibility ---

    public void setImage(String path) {
        loadImage(path);
        repaint();
    }

    public void setIconPath(String path) {
        setImage(path);
    }

    public void setSafePadPx(int px) {
        this.safePadPx = Math.max(0, px);
        repaint();
    }

    public void setPressedScale(double s) {
        this.pressedScale = Math.max(1.0, s);
        repaint();
    }

    public void setOnClick(Runnable r) { this.onClick = r; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || img == null) { g2.dispose(); return; }

        double scale = pressed ? pressedScale : 1.0;
        int pad = safePadPx;
        int iw = Math.max(1, w - pad * 2);
        int ih = Math.max(1, h - pad * 2);

        if (pressed) {
            g2.setComposite(AlphaComposite.SrcOver.derive(pressedGlowAlpha));
            g2.drawImage(img, pad - pressedGlowPx, pad - pressedGlowPx, iw + pressedGlowPx * 2, ih + pressedGlowPx * 2, null);
        } else if (hover) {
            g2.setComposite(AlphaComposite.SrcOver.derive(hoverGlowAlpha));
            g2.drawImage(img, pad - hoverGlowPx, pad - hoverGlowPx, iw + hoverGlowPx * 2, ih + hoverGlowPx * 2, null);
        }

        g2.setComposite(AlphaComposite.SrcOver);
        g2.translate(w / 2.0, h / 2.0);
        g2.scale(scale, scale);
        g2.translate(-w / 2.0, -h / 2.0);
        g2.drawImage(img, pad, pad, iw, ih, null);
        g2.dispose();
    }

    private static BufferedImage cropNearBlack(BufferedImage src, int threshold) {
        int w = src.getWidth();
        int h = src.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                if (((argb >>> 24) & 0xFF) == 0) continue; // Skip transparent
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;

                // If NOT near black, extend bounds
                if (!(r <= threshold && g <= threshold && b <= threshold)) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // Return original if completely black or empty
        if (maxX < minX || maxY < minY) return src;

        // Safe padding calculation (Fixes RasterFormatException)
        int pad = 6;
        int realX = Math.max(0, minX - pad);
        int realY = Math.max(0, minY - pad);
        int realMaxX = Math.min(w - 1, maxX + pad);
        int realMaxY = Math.min(h - 1, maxY + pad);

        int newW = realMaxX - realX + 1;
        int newH = realMaxY - realY + 1;

        return src.getSubimage(realX, realY, newW, newH);
    }
}