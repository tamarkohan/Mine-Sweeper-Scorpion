package util;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    public enum Language { EN, HE }

    private static final Map<String, String> hebrewMap = new HashMap<>();

    static {
        hebrewMap.put("mines_left", "מוקשים שנותרו:");
        hebrewMap.put("score", "ניקוד:");
        hebrewMap.put("lives", "חיים:");
        hebrewMap.put("time", "זמן:");
        hebrewMap.put("restart", "משחק מחדש");
        hebrewMap.put("exit", "יציאה");
        hebrewMap.put("you_won", "ניצחתם!");
        hebrewMap.put("game_over", "המשחק נגמר");
    }

    public static String get(String key, Language lang) {
        if (lang == Language.HE && hebrewMap.containsKey(key)) {
            return hebrewMap.get(key);
        }
        return key;
    }
}