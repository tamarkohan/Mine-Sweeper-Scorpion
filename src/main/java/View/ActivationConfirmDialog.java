package View;

import Controller.GameController;
import util.LanguageManager;

import javax.swing.*;
import java.awt.*;

public class ActivationConfirmDialog extends JDialog {

    public enum Choice {ACTIVATE, CANCEL}

    private Choice choice = Choice.CANCEL;

    // Theme
    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color CYAN = new Color(65, 255, 240);
    private static final Color CYAN_SOFT = new Color(65, 255, 240, 140);

    private static final Color TEXT = new Color(245, 245, 255);
    private static final Color MUTED = new Color(200, 210, 240);

    private static final Color BTN_BG = new Color(18, 26, 60);
    private static final Color BTN_BG_H = new Color(24, 38, 88);

    private ActivationConfirmDialog(Window owner, String cellType) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        // Determine if it's a question or surprise cell
        boolean isQuestion = cellType.equalsIgnoreCase("Question");

        // Get translated strings
        String titleText = isQuestion
                ? LanguageManager.get("question_cell", lang)
                : LanguageManager.get("surprise_cell", lang);

        String headerText = isQuestion
                ? LanguageManager.get("this_is_question_cell", lang)
                : LanguageManager.get("this_is_surprise_cell", lang);

        String questionText = LanguageManager.get("do_you_want_to_activate", lang);
        String cancelText = LanguageManager.get("cancel", lang);
        String activateText = LanguageManager.get("activate", lang);

        setTitle(titleText);

        JPanel root = new GradientPanel();
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.setLayout(new BorderLayout(0, 12));

        // ===== Header (icon + title) =====
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel(loadIconForType(cellType));
        icon.setPreferredSize(new Dimension(36, 36));

        JLabel title = new JLabel(headerText);
        title.setForeground(TEXT);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        // Apply RTL for Hebrew
        if (lang == LanguageManager.Language.HE) {
            header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            title.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            header.add(icon, BorderLayout.EAST);
            header.add(title, BorderLayout.CENTER);
        } else {
            header.add(icon, BorderLayout.WEST);
            header.add(title, BorderLayout.CENTER);
        }

        // underline
        JPanel underline = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CYAN_SOFT);
                g2.fillRoundRect(0, getHeight() / 2 - 1, getWidth(), 2, 8, 8);
                g2.dispose();
            }
        };
        underline.setOpaque(false);
        underline.setPreferredSize(new Dimension(1, 6));

        // ===== Body (single clean line) =====
        JLabel question = new JLabel(questionText);
        question.setForeground(MUTED);
        question.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        if (lang == LanguageManager.Language.HE) {
            question.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            body.add(question, BorderLayout.EAST);
        } else {
            body.add(question, BorderLayout.WEST);
        }

        // ===== Buttons =====
        JPanel buttons = new JPanel(new FlowLayout(
                lang == LanguageManager.Language.HE ? FlowLayout.LEFT : FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton cancel = makeButton(cancelText, false);
        JButton activate = makeButton(activateText, true);

        cancel.addActionListener(e -> {
            choice = Choice.CANCEL;
            dispose();
        });
        activate.addActionListener(e -> {
            choice = Choice.ACTIVATE;
            dispose();
        });

        // For Hebrew, reverse button order (primary on left)
        if (lang == LanguageManager.Language.HE) {
            buttons.add(activate);
            buttons.add(cancel);
        } else {
            buttons.add(cancel);
            buttons.add(activate);
        }

        // ===== Assemble =====
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(underline, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        setResizable(false);

        // compact size
        setSize(520, 190);
        setLocationRelativeTo(owner);
    }

    public static boolean show(Window owner, String cellType) {
        ActivationConfirmDialog dlg = new ActivationConfirmDialog(owner, cellType);
        dlg.setVisible(true);
        return dlg.choice == Choice.ACTIVATE;
    }

    private JButton makeButton(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setBackground(BTN_BG);
        b.setOpaque(true);
        b.setContentAreaFilled(true);

        Color border = primary ? CYAN : new Color(210, 220, 255, 160);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, primary ? 2 : 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        // hover
        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(BTN_BG_H);
            else b.setBackground(BTN_BG);
        });

        return b;
    }

    private Icon loadIconForType(String cellType) {

        String path = cellType.equalsIgnoreCase("Question")
                ? "/ui/cells/question.png"
                : "/ui/cells/surprise_btn.png";

        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(34, 34, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return UIManager.getIcon("OptionPane.questionIcon");
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
}