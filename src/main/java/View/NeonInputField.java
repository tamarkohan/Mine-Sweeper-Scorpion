package View;

import javax.swing.*;
import java.awt.*;

public class NeonInputField extends JComponent {

    public final GlowTextField textField;
    private final Color neonColor;
    private final String title;

    public NeonInputField(String title, Color neonColor) {
        this.title = title;
        this.neonColor = neonColor;

        setLayout(null);
        setOpaque(false);

        textField = new GlowTextField(8);
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        textField.setGlowColor(neonColor);

        textField.setFont(new Font("Arial Black", Font.PLAIN, 22));

        // caret can stay white (looks good)
        textField.setCaretColor(Color.WHITE);

        add(textField);
    }

    public String getText() {
        return textField.getText();
    }

    @Override
    public void doLayout() {
        int pad = 12;

        // Put the text nicely inside the glass area
        int x = pad + 18;
        int y = pad + 18;
        int w = getWidth() - (pad * 2) - 36;
        int h = getHeight() - (pad * 2) - 30;

        textField.setBounds(x, y, w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 0;
        int pad = 12;

        // --- glow ---
        for (int i = 8; i >= 1; i--) {
            g2.setColor(new Color(
                    neonColor.getRed(),
                    neonColor.getGreen(),
                    neonColor.getBlue(),
                    14
            ));
            g2.setStroke(new BasicStroke(i * 2f));
            g2.drawRoundRect(
                    pad - i,
                    pad - i,
                    getWidth() - 2 * pad + i * 2,
                    getHeight() - 2 * pad + i * 2,
                    arc, arc
            );
        }

        // --- inner glass ---
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(pad, pad, getWidth() - 2 * pad, getHeight() - 2 * pad, arc, arc);

        // --- main border ---
        g2.setColor(neonColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(pad, pad, getWidth() - 2 * pad, getHeight() - 2 * pad, arc, arc);



        g2.dispose();
    }
}
