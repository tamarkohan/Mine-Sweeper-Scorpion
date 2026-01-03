package View;

import javax.swing.*;
import java.awt.*;

public class NeonInputField extends JComponent {

    public final GlowTextField textField;
    private final Color neonColor;

    private boolean active = false;
    private int customWidth = 420; // default width

    // ✅ OLD SIGNATURE (keeps StartPanel working)
    public NeonInputField(String title, Color neonColor) {
        this(neonColor);
        // title currently not drawn (your StartPanel uses NeonTextLabel already)
        // but we keep it so old code compiles.
    }

    // ✅ NEW SIGNATURE (for GamePanel boxes)
    public NeonInputField(Color neonColor) {
        this.neonColor = neonColor;

        setLayout(null);
        setOpaque(false);

        textField = new GlowTextField(8);
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        textField.setGlowColor(neonColor);

        textField.setFont(new Font("Arial Black", Font.PLAIN, 22));
        textField.setCaretColor(Color.WHITE);

        // looks like your start panel
        textField.setHorizontalAlignment(SwingConstants.CENTER);

        add(textField);
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public void setText(String s) {
        textField.setText(s);
    }

    public String getText() {
        return textField.getText();
    }

    /** Make it look like a name box: not editable, no caret, no focus. */
    public void setDisplayMode(boolean displayMode) {
        textField.setEditable(!displayMode);
        textField.setFocusable(!displayMode);
        textField.setHighlighter(null);
        textField.setCaretPosition(0);
    }

    /** Set custom width for the field (useful for making it narrower) */
    public void setFieldWidth(int width) {
        this.customWidth = width;
        revalidate();
        repaint();
    }

    @Override
    public void doLayout() {
        int pad = 12;

        int x = pad + 6;
        int y = pad + 4;
        int w = getWidth() - (pad * 2) - 16;
        int h = getHeight() - (pad * 2) - 12;

        textField.setBounds(x, y, w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 0;
        int pad = 12;

        int glowAlpha = active ? 28 : 14;
        float borderWidth = active ? 2.6f : 2.0f;

        // glow
        for (int i = 8; i >= 1; i--) {
            g2.setColor(new Color(
                    neonColor.getRed(),
                    neonColor.getGreen(),
                    neonColor.getBlue(),
                    glowAlpha
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

        // inner glass
        g2.setColor(new Color(0, 0, 0, active ? 175 : 160));
        g2.fillRoundRect(pad, pad, getWidth() - 2 * pad, getHeight() - 2 * pad, arc, arc);

        // main border
        g2.setColor(neonColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRoundRect(pad, pad, getWidth() - 2 * pad, getHeight() - 2 * pad, arc, arc);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(customWidth, 72); // uses custom width
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize(); //  prevents stretching
    }
}