package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class IconToggleButton extends JToggleButton {
    private BufferedImage img;
    private final Color glowColor;
    private final boolean cropTransparent;

    public IconToggleButton(String imgPath, Color glowColor) {
        this(imgPath, glowColor, true); // Default to cropping transparent padding
    }

    public IconToggleButton(String imgPath, Color glowColor, boolean cropTransparent) {
        this.glowColor = glowColor;
        this.cropTransparent = cropTransparent;
        loadImage(imgPath);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadImage(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Image not found: " + path);
                return;
            }
            BufferedImage loadedImg = ImageIO.read(url);

            // Crop transparent padding to maximize visible content
            if (cropTransparent) {
                this.img = cropTransparentPadding(loadedImg);
            } else {
                this.img = loadedImg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Crops transparent padding from around the image content
     */
    private BufferedImage cropTransparentPadding(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;

        // Find the actual content bounds (non-transparent pixels)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;

                // If pixel is not fully transparent
                if (alpha > 10) { // Threshold for near-transparent
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // If no content found, return original
        if (maxX < minX || maxY < minY) {
            return src;
        }

        // Add small padding back
        int pad = 2;
        int realX = Math.max(0, minX - pad);
        int realY = Math.max(0, minY - pad);
        int realMaxX = Math.min(w - 1, maxX + pad);
        int realMaxY = Math.min(h - 1, maxY + pad);

        int newW = realMaxX - realX + 1;
        int newH = realMaxY - realY + 1;

        return src.getSubimage(realX, realY, newW, newH);
    }

    public void setIconPath(String path) {
        loadImage(path);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // Draw the image to fill the entire button
        if (img != null) {
            g2.drawImage(img, 0, 0, w, h, null);
        }

        // Draw selection glow
        if (isSelected()) {
            g2.setColor(glowColor);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(2, 2, w - 5, h - 5);

            g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
            g2.fillRect(4, 4, w - 8, h - 8);
        }

        g2.dispose();
    }
}