package util;

import Model.Question;

import java.util.ArrayList;
import java.util.List;

public class BilingualQuestionUtil {

    public static boolean containsHebrew(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= 0x0590 && ch <= 0x05FF) return true; // Hebrew block
        }
        return false;
    }

    public static boolean questionLooksHebrew(Question q) {
        if (q == null) return false;
        if (containsHebrew(q.getText())) return true;
        for (String o : q.getOptions()) {
            if (containsHebrew(o)) return true;
        }
        return false;
    }

    public static Question translateQuestion(TranslatorService ts, Question src, String from, String to) throws Exception {
        String text = ts.translate(src.getText(), from, to);

        List<String> opts = new ArrayList<>(src.getOptions());
        while (opts.size() < 4) opts.add("");

        List<String> out = new ArrayList<>();
        for (String o : opts) out.add(ts.translate(o, from, to));

        // keep id/correct/difficulty the same
        return new Question(src.getId(), text, out, src.getCorrectOption(), src.getDifficultyLevel());
    }
}
