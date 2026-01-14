package View;

import javax.swing.*;
import java.awt.*;

public class NeonInputField extends JPanel {

    public final JTextField textField;
    private final Color neonColor;
    private boolean active = false;
    private int customWidth = 420;

    public NeonInputField(String title, Color neonColor) {
        this(neonColor);
    }

    public NeonInputField(Color neonColor) {
        this.neonColor = neonColor;
        setOpaque(false);
        setLayout(new BorderLayout());

        textField = new JTextField();
        textField.setOpaque(false);
        textField.setForeground(neonColor);
        textField.setCaretColor(Color.WHITE);

        // Arial supports English, Hebrew, and Arabic correctly
        textField.setFont(new Font("Arial", Font.BOLD, 22));
        textField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        textField.setHorizontalAlignment(JTextField.LEADING);

        add(textField, BorderLayout.CENTER);
    }

    public void setFieldWidth(int width) {
        this.customWidth = width;
        revalidate();
        repaint();
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public void setText(String s) { textField.setText(s); }
    public String getText() { return textField.getText(); }

    public void setDisplayMode(boolean dm) {
        textField.setEditable(!dm);
        textField.setFocusable(!dm);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pad = 10;
        int w = getWidth() - (pad * 2);
        int h = getHeight() - (pad * 2);

        g2.setColor(new Color(0, 0, 0, active ? 220 : 180));
        g2.fillRect(pad, pad, w, h);

        g2.setColor(neonColor);
        g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
        g2.drawRect(pad, pad, w, h);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        // Restored to stable height
        return new Dimension(customWidth, 72);
    }
}