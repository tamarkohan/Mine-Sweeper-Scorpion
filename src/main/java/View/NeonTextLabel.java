package View;

import javax.swing.*;
import java.awt.*;

public class NeonTextLabel extends JLabel {

    public NeonTextLabel(String text, Color baseColor) {
        super(text);

        // Simple White text for all labels to ensure they are visible
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 18));
        setHorizontalAlignment(SwingConstants.CENTER);

        // Optional: add a tiny border if you want it to look "boxy" like the inputs
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // By using the standard JLabel painting, Hebrew/Arabic will never "ghost"
        super.paintComponent(g);
    }
}