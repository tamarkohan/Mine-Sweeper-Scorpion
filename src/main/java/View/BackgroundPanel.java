package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class BackgroundPanel extends JPanel {
    private BufferedImage bg;

    public BackgroundPanel(String resourcePath) {
        setOpaque(false);
        setBackground(Color.BLACK);
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in != null) bg = ImageIO.read(in);
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bg == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        g2.dispose();
    }
}
