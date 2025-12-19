package View;

import javax.swing.*;
import java.awt.*;

public class IconToggleButton extends JToggleButton {

    private final Image image;
    private final Color glow;

    public IconToggleButton(String imagePath, Color glow) {
        image = new ImageIcon(
                getClass().getResource(imagePath)
        ).getImage();

        this.glow = glow;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // image
        g2.drawImage(image, 0, 0, w, h, this);

        // glow if selected
        if (isSelected()) {
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(
                        glow.getRed(),
                        glow.getGreen(),
                        glow.getBlue(),
                        22
                ));
                g2.setStroke(new BasicStroke(i));
                g2.drawRoundRect(
                        i / 2,
                        i / 2,
                        w - i,
                        h - i,
                        20,
                        20
                );
            }
        }

        g2.dispose();
    }
}
