package View;

import Controller.GameController;
import util.LanguageManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class OutcomeDialog extends JDialog {

    private static final Color BG_TOP = new Color(6, 10, 28);
    private static final Color BG_BOTTOM = new Color(10, 18, 55);

    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(225, 230, 255);

    private static final Color ACCENT_GREEN = new Color(80, 255, 120);
    private static final Color ACCENT_RED = new Color(255, 90, 90);

    private OutcomeDialog(Window owner, String title, String message, Color accent) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        TitleCard header = new TitleCard(title, accent, lang);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        BodyCard body = new BodyCard(message, accent, lang);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton ok = new JButton(LanguageManager.get("ok", lang));
        styleButton(ok, accent, lang);
        ok.addActionListener(e -> dispose());
        actions.add(ok);

        content.add(header);
        content.add(body);
        content.add(actions);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        root.setPreferredSize(new Dimension(820, 360));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public static void show(Window owner, String title, String message) {
        new OutcomeDialog(owner, title, message, new Color(65, 255, 240)).setVisible(true);
    }

    // ---------- Styling ----------
    private static void styleButton(JButton b, Color accent, LanguageManager.Language lang) {
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        int fontSize = LanguageManager.getAdjustedFontSize(13, lang);
        b.setFont(new Font("Dialog", Font.BOLD, fontSize));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        b.setBackground(new Color(15, 40, 80));
        b.setOpaque(true);
    }

    private static List<String> lines(String msg) {
        if (msg == null) return List.of();
        String[] raw = msg.split("\\r?\\n");
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    // ---------- Inner classes ----------
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOTTOM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    private static class TitleCard extends JPanel {
        private final String text;
        private final Color accent;
        private final LanguageManager.Language lang;

        TitleCard(String text, Color accent, LanguageManager.Language lang) {
            this.text = text == null ? "" : text;
            this.accent = accent;
            this.lang = lang;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            setPreferredSize(new Dimension(520, 80));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(accent);
            int fontSize = LanguageManager.getAdjustedFontSize(44, lang);
            g2.setFont(new Font("Dialog", Font.BOLD, fontSize));

            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }

    private static class BodyCard extends JPanel {
        private final List<String> lines;
        private final Color accent;
        private final LanguageManager.Language lang;

        BodyCard(String msg, Color accent, LanguageManager.Language lang) {
            this.lines = lines(msg);
            this.accent = accent;
            this.lang = lang;
            setOpaque(false);
            int lineHeight = LanguageManager.getAdjustedFontSize(24, lang);
            setPreferredSize(new Dimension(520, Math.max(120, 38 + this.lines.size() * lineHeight)));
            setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 40;

            // glass card
            g2.setColor(new Color(0, 0, 0, 115));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // neon border
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(accent);
            g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);

            // message lines
            int y = 34;
            boolean isRTL = LanguageManager.isRTL(lang);

            // Get translated keywords for detection
            String scoreKey = LanguageManager.get("score_label", lang);
            String livesKey = LanguageManager.get("lives_label", lang);
            String activationKey = LanguageManager.get("activation_cost", lang);
            String specialKey = LanguageManager.get("special_effect", lang);

            for (String line : lines) {
                boolean important =
                        line.contains("Score:") || line.contains(scoreKey) ||
                                line.contains("Lives:") || line.contains(livesKey) ||
                                line.contains("Activation cost:") || line.contains(activationKey) ||
                                line.toLowerCase().contains("special effect:") || line.contains(specialKey);

                int baseFontSize = important ? 16 : 15;
                int fontSize = LanguageManager.getAdjustedFontSize(baseFontSize, lang);
                g2.setFont(new Font("Dialog", important ? Font.BOLD : Font.PLAIN, fontSize));
                g2.setColor(important ? TEXT : MUTED);

                String printed = (important ? "• " : "  ") + line;

                if (isRTL) {
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(printed);
                    g2.drawString(printed, w - 18 - textWidth, y);
                } else {
                    g2.drawString(printed, 18, y);
                }
                int lineHeight = LanguageManager.getAdjustedFontSize(24, lang);
                y += lineHeight;
            }

            g2.dispose();
        }
    }

    public static void showQuestionOutcomeFromMessage(Window owner, String outcomeMessage) {
        if (outcomeMessage == null) return;

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        String lower = outcomeMessage.toLowerCase();

        boolean isCorrect = lower.contains("correct") || lower.contains("נכון") || lower.contains("صحيح") || lower.contains("правильно") || lower.contains("correcto");
        boolean isWrong = lower.contains("wrong") || lower.contains("שגוי") || lower.contains("خطأ") || lower.contains("неправильно") || lower.contains("incorrecto");
        boolean isSkipped = lower.contains("didn't answer") || lower.contains("did not answer")
                || lower.contains("skipped") || lower.contains("דילגת") || lower.contains("تم التخطي") || lower.contains("пропущен") || lower.contains("omitido");

        Color accent;
        String title;

        if (isCorrect) {
            accent = ACCENT_GREEN;
            title = LanguageManager.get("outcome_correct", lang);
        } else if (isWrong) {
            accent = ACCENT_RED;
            title = LanguageManager.get("outcome_wrong", lang);
        } else if (isSkipped) {
            accent = new Color(65, 255, 240);
            title = LanguageManager.get("outcome_skipped", lang);
        } else {
            accent = new Color(65, 255, 240);
            title = LanguageManager.get("result", lang);
        }

        // Translate the outcome message
        String translatedMessage = translateOutcomeMessage(outcomeMessage, lang);

        new OutcomeDialog(owner, title, translatedMessage, accent).setVisible(true);
    }

    public static void showSurpriseOutcomeFromMessage(Window owner, String outcomeMessage) {
        if (outcomeMessage == null) return;

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        String lower = outcomeMessage.toLowerCase();
        boolean good = lower.contains("good") || lower.contains("טוב") || lower.contains("جيد") || lower.contains("хорошо") || lower.contains("bueno");
        boolean bad = lower.contains("bad") || lower.contains("רע") || lower.contains("سيء") || lower.contains("плохо") || lower.contains("malo");

        Color accent = good ? ACCENT_GREEN : (bad ? ACCENT_RED : new Color(65, 255, 240));
        String title = good ? LanguageManager.get("surprise_good", lang)
                : (bad ? LanguageManager.get("surprise_bad", lang)
                : LanguageManager.get("surprise", lang));

        // Translate the outcome message
        String translatedMessage = translateOutcomeMessage(outcomeMessage, lang);

        new OutcomeDialog(owner, title, translatedMessage, accent).setVisible(true);
    }

    /**
     * Translates outcome message parts to the current language
     */
    private static String translateOutcomeMessage(String message, LanguageManager.Language lang) {
        if (message == null || lang == LanguageManager.Language.EN) {
            return message;
        }

        String result = message;

        // Get translations from LanguageManager
        String diffEasy = LanguageManager.get("difficulty_easy", lang);
        String diffMedium = LanguageManager.get("difficulty_medium", lang);
        String diffHard = LanguageManager.get("difficulty_hard", lang);
        String diffExpert = LanguageManager.get("difficulty_expert", lang);

        String wrongText = LanguageManager.get("wrong_prefix", lang);
        String correctText = LanguageManager.get("correct_prefix", lang);

        String activationCost = LanguageManager.get("activation_cost", lang);
        String scoreLabel = LanguageManager.get("score_label", lang);
        String livesLabel = LanguageManager.get("lives_label", lang);
        String specialEffect = LanguageManager.get("special_effect", lang);

        String ptsText = LanguageManager.get("pts", lang);
        String lifeText = LanguageManager.get("life", lang);

        String goodSurprise = LanguageManager.get("good_surprise_msg", lang);
        String badSurprise = LanguageManager.get("bad_surprise_msg", lang);
        String goodText = LanguageManager.get("good", lang);
        String badText = LanguageManager.get("bad", lang);

        String didntAnswer = LanguageManager.get("didnt_answer", lang);
        String costDeducted = LanguageManager.get("activation_cost_deducted", lang);

        // Translate difficulty levels
        result = result.replace("EASY", diffEasy);
        result = result.replace("MEDIUM", diffMedium);
        result = result.replace("HARD", diffHard);
        result = result.replace("EXPERT", diffExpert);

        // Translate outcome words
        result = result.replace("Wrong!", wrongText + "!");
        result = result.replace("Wrong", wrongText);
        result = result.replace("Correct!", correctText + "!");
        result = result.replace("Correct", correctText);

        // Translate stat labels
        result = result.replace("Activation cost:", activationCost);
        result = result.replace("Score:", scoreLabel);
        result = result.replace("Lives:", livesLabel);
        result = result.replace("Special effect:", specialEffect);

        // Translate units
        result = result.replace(" pts", " " + ptsText);
        result = result.replace(" life.", " " + lifeText + ".");
        result = result.replace(" life", " " + lifeText);
        result = result.replace(" lives", " " + lifeText);

        // RTL: تثبيت الإشارة قبل الرقم (+8 نقطة وليس 8+) بـ \u202A LTR embedding و \u202C
        if (LanguageManager.isRTL(lang)) {
            result = result.replaceAll("([+-]\\d+)", "\u202A$1\u202C");
        }

        // Translate surprise outcomes
        result = result.replace("Good surprise!", goodSurprise);
        result = result.replace("Bad surprise!", badSurprise);
        // Be careful with standalone Good/Bad - only if followed by specific patterns
        if (!result.contains(goodSurprise)) {
            result = result.replace("Good", goodText);
        }
        if (!result.contains(badSurprise)) {
            result = result.replace("Bad", badText);
        }

        // Translate common phrases
        result = result.replace("You didn't answer the question.", didntAnswer);
        result = result.replace("Activation cost was deducted.", costDeducted);

        // Arabic: عرض التغيير بصيغة قبل/بعد (النقاط: قبل 85، بعد 96)
        if (lang == LanguageManager.Language.AR) {
            result = result.replaceAll("(\\d+)\\s*->\\s*(\\d+)", "قبل $1، بعد $2");
        } else if (LanguageManager.isRTL(lang)) {
            result = result.replaceAll("(\\d+)\\s*->\\s*(\\d+)", "$2 \u2190 $1");
        }

        return result;
    }
}