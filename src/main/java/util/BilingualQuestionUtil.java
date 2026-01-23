package util;

import Model.Question;

import java.util.ArrayList;
import java.util.List;

public class BilingualQuestionUtil {

    /**
     * Check if string contains Hebrew characters
     */
    public static boolean containsHebrew(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= 0x0590 && ch <= 0x05FF) return true; // Hebrew block
        }
        return false;
    }

    /**
     * Check if string contains Arabic characters
     */
    public static boolean containsArabic(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            // Arabic Unicode blocks
            if ((ch >= 0x0600 && ch <= 0x06FF) ||  // Arabic
                    (ch >= 0x0750 && ch <= 0x077F) ||  // Arabic Supplement
                    (ch >= 0x08A0 && ch <= 0x08FF) ||  // Arabic Extended-A
                    (ch >= 0xFB50 && ch <= 0xFDFF) ||  // Arabic Presentation Forms-A
                    (ch >= 0xFE70 && ch <= 0xFEFF)) {  // Arabic Presentation Forms-B
                return true;
            }
        }
        return false;
    }

    /**
     * Check if string contains Cyrillic (Russian) characters
     */
    public static boolean containsCyrillic(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= 0x0400 && ch <= 0x04FF) ||  // Cyrillic
                    (ch >= 0x0500 && ch <= 0x052F)) {  // Cyrillic Supplement
                return true;
            }
        }
        return false;
    }

    /**
     * Check if string contains Spanish-specific characters (accented vowels, ñ, ¿, ¡)
     */
    public static boolean containsSpanishSpecific(String s) {
        if (s == null) return false;
        String spanishChars = "áéíóúüñÁÉÍÓÚÜÑ¿¡";
        for (int i = 0; i < s.length(); i++) {
            if (spanishChars.indexOf(s.charAt(i)) >= 0) return true;
        }
        return false;
    }

    /**
     * Check if a question appears to be in Hebrew
     */
    public static boolean questionLooksHebrew(Question q) {
        if (q == null) return false;
        if (containsHebrew(q.getText())) return true;
        for (String o : q.getOptions()) {
            if (containsHebrew(o)) return true;
        }
        return false;
    }

    /**
     * Check if a question appears to be in Arabic
     */
    public static boolean questionLooksArabic(Question q) {
        if (q == null) return false;
        if (containsArabic(q.getText())) return true;
        for (String o : q.getOptions()) {
            if (containsArabic(o)) return true;
        }
        return false;
    }

    /**
     * Check if a question appears to be in Russian
     */
    public static boolean questionLooksRussian(Question q) {
        if (q == null) return false;
        if (containsCyrillic(q.getText())) return true;
        for (String o : q.getOptions()) {
            if (containsCyrillic(o)) return true;
        }
        return false;
    }

    /**
     * Check if text is RTL (Hebrew or Arabic)
     */
    public static boolean isRTLText(String s) {
        return containsHebrew(s) || containsArabic(s);
    }

    /**
     * Detect the likely language of a question
     */
    public static LanguageManager.Language detectQuestionLanguage(Question q) {
        if (questionLooksHebrew(q)) return LanguageManager.Language.HE;
        if (questionLooksArabic(q)) return LanguageManager.Language.AR;
        if (questionLooksRussian(q)) return LanguageManager.Language.RU;
        // Spanish and English are both Latin-based, default to English unless Spanish-specific chars found
        if (containsSpanishSpecific(q.getText())) return LanguageManager.Language.ES;
        for (String o : q.getOptions()) {
            if (containsSpanishSpecific(o)) return LanguageManager.Language.ES;
        }
        return LanguageManager.Language.EN;
    }

    /**
     * Translate a question from one language to another
     */
    public static Question translateQuestion(TranslatorService ts, Question src, String from, String to) throws Exception {
        String text = ts.translate(src.getText(), from, to);

        List<String> opts = new ArrayList<>(src.getOptions());
        while (opts.size() < 4) opts.add("");

        List<String> out = new ArrayList<>();
        for (String o : opts) out.add(ts.translate(o, from, to));

        // keep id/correct/difficulty the same
        return new Question(src.getId(), text, out, src.getCorrectOption(), src.getDifficultyLevel());
    }

    /**
     * Get the language code for the TranslatorService
     */
    public static String getLanguageCode(LanguageManager.Language lang) {
        return switch (lang) {
            case EN -> "en";
            case HE -> "he";
            case AR -> "ar";
            case RU -> "ru";
            case ES -> "es";
        };
    }
}