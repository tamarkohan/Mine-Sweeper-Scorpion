package View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NeonMessageDialog extends JDialog {

    public enum Type {SUCCESS, ERROR, INFO}

    // ===== Theme =====
    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color CYAN = new Color(65, 255, 240);
    private static final Color CYAN_SOFT = new Color(65, 255, 240, 140);

    private static final Color RED = new Color(255, 70, 70);
    private static final Color RED_SOFT = new Color(255, 70, 70, 140);

    private static final Color TEXT = new Color(245, 245, 255);
    private static final Color MUTED = new Color(200, 210, 240);

    private static final Color CARD_BG = new Color(0, 0, 0, 115);

    private static final Color BTN_BG = new Color(18, 26, 60);
    private static final Color BTN_BG_H = new Color(24, 38, 88);

    private NeonMessageDialog(Window owner, String title, Type type,
                              String mainLine,
                              List<Row> rows,
                              String footerNote) {
        super(owner, title == null ? "Message" : title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new GradientPanel();
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setLayout(new BorderLayout(0, 12));

        Color accent = (type == Type.ERROR) ? RED : CYAN;
        Color accentSoft = (type == Type.ERROR) ? RED_SOFT : CYAN_SOFT;

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel(iconFor(type));
        icon.setPreferredSize(new Dimension(36, 36));

        JLabel ttl = new JLabel(title == null ? "Message" : title);
        ttl.setForeground(TEXT);
        ttl.setFont(new Font("Arial", Font.BOLD, 18));

        header.add(icon, BorderLayout.WEST);
        header.add(ttl, BorderLayout.CENTER);

        JPanel underline = line(accentSoft);
        underline.setPreferredSize(new Dimension(1, 6));

        // ===== Card =====
        JPanel card = new RoundedCard(accent);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel main = new JLabel(mainLine == null ? "" : mainLine);
        main.setForeground(TEXT);
        main.setFont(new Font("Arial", Font.BOLD, 15));

        JPanel rowsPanel = new JPanel();
        rowsPanel.setOpaque(false);
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

        for (Row r : rows) {
            rowsPanel.add(makeRow(r.label, r.value, r.badgeType, accent));
            rowsPanel.add(Box.createVerticalStrut(8));
        }

        if (footerNote != null && !footerNote.isBlank()) {
            JLabel note = new JLabel(footerNote);
            note.setForeground(MUTED);
            note.setFont(new Font("Arial", Font.PLAIN, 12));
            rowsPanel.add(Box.createVerticalStrut(4));
            rowsPanel.add(note);
        }

        JScrollPane scroll = new JScrollPane(rowsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        scroll.setPreferredSize(new Dimension(720, 260));

        card.add(main, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);


        // ===== Actions =====
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton ok = makeButton("OK", true, accent);
        ok.addActionListener(e -> dispose());
        actions.add(ok);

        // ===== Assemble =====
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(underline, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        setResizable(false);
        setSize(620, 320);
        setLocationRelativeTo(owner);
    }



    // Public helper: build structured (best option)
    public static void show(Window owner, String title, Type type, String mainLine, List<Row> rows, String footerNote) {
        NeonMessageDialog dlg = new NeonMessageDialog(owner, title, type, mainLine, rows, footerNote);
        dlg.setVisible(true);
    }

    // ===== Row model =====
    public enum BadgeType {NONE, POSITIVE, NEGATIVE, NEUTRAL}

    public static class Row {
        public final String label;
        public final String value;
        public final BadgeType badgeType;

        public Row(String label, String value, BadgeType badgeType) {
            this.label = label;
            this.value = value;
            this.badgeType = badgeType;
        }
    }

    // ===== UI helpers =====
    private static JPanel makeRow(String label, String value, BadgeType badgeType, Color accent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(MUTED);
        l.setFont(new Font("Arial", Font.PLAIN, 13));

        JComponent v;
        if (badgeType == BadgeType.NONE) {
            JLabel val = new JLabel(value == null ? "" : value);
            val.setForeground(TEXT);
            val.setFont(new Font("Arial", Font.BOLD, 13));
            v = val;
        } else {
            v = badge(value, badgeType, accent);
        }

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private static JComponent badge(String text, BadgeType type, Color accent) {
        Color bg;
        Color border;
        if (type == BadgeType.POSITIVE) {
            bg = new Color(0, 255, 170, 35);
            border = new Color(0, 255, 170, 160);
        } else if (type == BadgeType.NEGATIVE) {
            bg = new Color(255, 70, 70, 35);
            border = new Color(255, 70, 70, 160);
        } else { // NEUTRAL
            bg = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28);
            border = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160);
        }

        JLabel b = new JLabel(text == null ? "" : text);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        b.setOpaque(true);
        b.setBackground(bg);
        return b;
    }

    private JButton makeButton(String text, boolean primary, Color accent) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setBackground(BTN_BG);
        b.setOpaque(true);
        b.setContentAreaFilled(true);

        Color border = primary ? accent : new Color(210, 220, 255, 160);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, primary ? 2 : 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(BTN_BG_H);
            else b.setBackground(BTN_BG);
        });
        return b;
    }

    private static JPanel line(Color c) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillRoundRect(0, getHeight() / 2 - 1, getWidth(), 2, 8, 8);
                g2.dispose();
            }
        };
    }

    private static Icon iconFor(Type type) {
        return switch (type) {
            case SUCCESS -> UIManager.getIcon("OptionPane.informationIcon"); // you can replace with your own icon
            case ERROR -> UIManager.getIcon("OptionPane.errorIcon");
            case INFO -> UIManager.getIcon("OptionPane.informationIcon");
        };
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOTTOM));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    private static class RoundedCard extends JPanel {
        private final Color accent;

        RoundedCard(Color accent) {
            this.accent = accent;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int arc = 28;

            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
            g2.drawRoundRect(1, 1, w - 2, h - 2, arc, arc);

            g2.dispose();
        }
    }

    // ===== Basic parser so you can keep your existing message strings =====
    private static class Parsed {
        String mainLine = "";
        List<Row> rows = new ArrayList<>();
        String footer = "";
    }

    private static Parsed parse(String raw) {
        Parsed p = new Parsed();
        if (raw == null) return p;

        String[] lines = raw.replace("\r", "").split("\n");
        List<String> clean = new ArrayList<>();
        for (String s : lines) {
            s = s.trim();
            if (s.isEmpty()) continue;
            clean.add(s);
        }

        if (!clean.isEmpty()) p.mainLine = clean.get(0);

        for (int i = 1; i < clean.size(); i++) {
            String s = clean.get(i);

            // heuristics for common rows
            if (s.toLowerCase().startsWith("activation cost")) {
                p.rows.add(new Row("Activation cost", afterColon(s), BadgeType.NEUTRAL));
            } else if (s.toLowerCase().startsWith("special effect")) {
                p.rows.add(new Row("Special effect", afterColon(s), BadgeType.NEUTRAL));
            } else if (s.toLowerCase().startsWith("score")) {
                p.rows.add(new Row("Score", afterColon(s), BadgeType.NEUTRAL));
            } else if (s.toLowerCase().startsWith("lives")) {
                p.rows.add(new Row("Lives", afterColon(s), BadgeType.NEUTRAL));
            } else if (s.toLowerCase().startsWith("penalty")) {
                p.rows.add(new Row("Penalty", afterColon(s), BadgeType.NEGATIVE));
            } else if (s.toLowerCase().startsWith("reward")) {
                p.rows.add(new Row("Reward", afterColon(s), BadgeType.POSITIVE));
            } else if (s.toLowerCase().contains("bad")) {
                p.rows.add(new Row("Result", s, BadgeType.NEGATIVE));
            } else if (s.toLowerCase().contains("good") || s.toLowerCase().contains("correct")) {
                p.rows.add(new Row("Result", s, BadgeType.POSITIVE));
            } else {
                // put unknown lines into footer to keep card clean
                if (p.footer.isBlank()) p.footer = s;
                else p.footer += " • " + s;
            }
        }

        return p;
    }

    private static String afterColon(String s) {
        int idx = s.indexOf(':');
        if (idx >= 0 && idx + 1 < s.length()) return s.substring(idx + 1).trim();
        return s;
    }
}
