package View;

import javax.swing.*;
import java.awt.*;

public class GlowTextField extends JTextField {

    private Color glowColor = new Color(255, 80, 80);

    public GlowTextField(int columns) {
        super(columns);
        setOpaque(false);
        setBorder(null);
        setCaretColor(Color.WHITE);

        setForeground(glowColor);

        // Optional: nicer selection on dark bg
        setSelectionColor(new Color(255, 255, 255, 60));
        setSelectedTextColor(Color.WHITE);
    }

    public void setGlowColor(Color c) {
        glowColor = c;
        setForeground(c);   // main text color = glow color
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 1) paint glow behind the text
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        String text = getText();
        if (text == null) text = "";

        if (!text.isEmpty()) {
            Insets in = getInsets();
            FontMetrics fm = g2.getFontMetrics(getFont());
            int x = in.left;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            // Softer glow (less intense than before)
            for (int i = 5; i >= 1; i--) {
                int a = 14; // glow alpha (lower = softer)
                g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), a));

                g2.drawString(text, x - i, y);
                g2.drawString(text, x + i, y);
                g2.drawString(text, x, y - i);
                g2.drawString(text, x, y + i);
            }
        }

        g2.dispose();

        // 2) let Swing paint the normal text + caret + selection ON TOP
        super.paintComponent(g);
    }
}
