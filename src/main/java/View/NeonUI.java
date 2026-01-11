package View;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class NeonUI {
    public static final Color BG = new Color(5, 6, 10);
    public static final Color PANEL = new Color(11, 15, 26);
    public static final Color ACCENT = new Color(0, 245, 255);
    public static final Color ACCENT_2 = new Color(180, 0, 255);
    public static final Color TEXT = new Color(234, 246, 255);
    public static final Color MUTED = new Color(140, 160, 180);

    public static Border neonLine(Color c) {
        return new CompoundBorder(
                new LineBorder(new Color(c.getRed(), c.getGreen(), c.getBlue(), 180), 2, true),
                new EmptyBorder(10, 12, 10, 12)
        );
    }

    public static void styleLabel(JLabel l) {
        l.setForeground(TEXT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    public static void styleField(JTextField f) {
        f.setBackground(PANEL);
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(neonLine(ACCENT));
    }

    public static void styleArea(JTextArea a) {
        a.setBackground(PANEL);
        a.setForeground(TEXT);
        a.setCaretColor(ACCENT);
        a.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        a.setBorder(neonLine(ACCENT));
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setBackground(PANEL);
        c.setForeground(TEXT);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(neonLine(ACCENT));
    }

    public static JButton neonButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(TEXT);
        b.setBackground(new Color(10, 20, 30));
        b.setBorder(neonLine(ACCENT));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
