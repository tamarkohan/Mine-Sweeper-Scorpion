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
    private Runnable onClick;

    // If true, removes black padding around the button (perfect for your menu PNGs)
    private final boolean cropBlackPadding;

    public IconButton(String imgPath) {
        this(imgPath, false);
    }

    public IconButton(String imgPath, boolean cropBlackPadding) {
        this.cropBlackPadding = cropBlackPadding;

        try {
            img = ImageIO.read(getClass().getResource(imgPath));
            if (img == null) throw new RuntimeException("Image is null: " + imgPath);

            if (cropBlackPadding) {
                img = cropNearBlack(img, 18); // threshold (0..255). 18 works well for “almost black”
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed loading: " + imgPath, e);
        }

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
        });
    }

    public void setOnClick(Runnable r) { this.onClick = r; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // glow on hover (slightly larger behind)
        if (hover) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.20f));
            g2.drawImage(img, -8, -8, w + 16, h + 16, null);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // draw full fill
        g2.drawImage(img, 0, 0, w, h, null);

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

        // If everything is black (or failed), return original
        if (maxX < minX || maxY < minY) return src;

        // small padding so we don't cut the glow frame
        int pad = 6;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(w - 1, maxX + pad);
        maxY = Math.min(h - 1, maxY + pad);

        return src.getSubimage(minX, minY, (maxX - minX + 1), (maxY - minY + 1));
    }
}
