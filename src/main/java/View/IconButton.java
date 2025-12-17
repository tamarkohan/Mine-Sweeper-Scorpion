package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class IconButton extends JComponent {
    private final BufferedImage img;
    private boolean hover = false;
    private Runnable onClick;

    public IconButton(String imgPath) {
        try {
            img = ImageIO.read(getClass().getResource(imgPath));
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

        // keep aspect ratio
        double iw = img.getWidth();
        double ih = img.getHeight();
        double cw = getWidth();
        double ch = getHeight();

        double scale = Math.min(cw / iw, ch / ih);
        int w = (int)(iw * scale);
        int h = (int)(ih * scale);
        int x = (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;

        // subtle glow on hover
        if (hover) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.20f));
            g2.drawImage(img, x - 6, y - 6, w + 12, h + 12, null);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        g2.drawImage(img, x, y, w, h, null);
        g2.dispose();
    }
}
