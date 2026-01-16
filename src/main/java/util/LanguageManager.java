package util;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    public enum Language { EN, HE }

    private static final Map<String, Map<Language, String>> translations = new HashMap<>();

    static {
        // Game Result Dialog
        add("you_won", "🎊 YOU WON! 🎊", "🎊 ניצחתם! 🎊");
        add("game_over", "💀 GAME OVER 💀", "💀 המשחק נגמר 💀");
        add("stat_score", "Score:", "ניקוד:");
        add("stat_lives", "Lives Remaining:", "חיים שנותרו:");
        add("stat_questions", "Questions Answered:", "שאלות שנענו:");
        add("stat_correct", "Correct Answers:", "תשובות נכונות:");
        add("stat_surprises", "Surprises Opened:", "הפתעות שנפתחו:");
        add("time", "Time", "זמן");
        add("stat_accuracy", "Accuracy:", "דיוק:");
        add("restart", "Restart", "התחל מחדש");
        add("exit", "Exit", "יציאה");

        // GamePanel
        add("score", "SCORE", "ניקוד");
        add("lives", "LIVES", "חיים");
        add("mines_left", "MINES LEFT", "מוקשים נותרו");
        add("wait_turn", "WAIT FOR YOUR TURN", "המתן לתורך");

        // QuestionDialog
        add("question", "Question", "שאלה");
        add("submit", "Submit", "שלח");
        add("cancel", "Cancel", "ביטול");
        add("no_answer_selected", "Please choose an answer first.", "אנא בחר תשובה תחילה.");
        add("no_answer_title", "No answer selected", "לא נבחרה תשובה");
        add("correct", "CORRECT ✓", "נכון ✓");
        add("wrong", "WRONG ✗", "שגוי ✗");
        add("your_answer", "Your answer:", "התשובה שלך:");
        add("correct_answer", "Correct answer:", "התשובה הנכונה:");
        add("ok", "OK", "אישור");

        // Language Toast
        add("lang_english", "English", "English");
        add("lang_hebrew", "עברית", "עברית");

        // ActivationConfirmDialog - Question Cell
        add("question_cell", "Question Cell", "תא שאלה");
        add("this_is_question_cell", "This is a Question cell", "זהו תא שאלה");
        add("do_you_want_to_activate", "Do you want to activate it?", "האם ברצונך להפעיל אותו?");
        add("activate", "Activate", "הפעל");

        // ActivationConfirmDialog - Surprise Cell
        add("surprise_cell", "Surprise Cell", "תא הפתעה");
        add("this_is_surprise_cell", "This is a Surprise cell", "זהו תא הפתעה");

        // Outcome Dialog - Question Results
        add("outcome_correct", "CORRECT!", "נכון!");
        add("outcome_wrong", "WRONG!", "שגוי!");
        add("outcome_skipped", "SKIPPED", "דילגת");
        add("outcome", "OUTCOME", "תוצאה");

        // Outcome Dialog - Surprise Results
        add("surprise_good", "SURPRISE: GOOD!", "הפתעה: טוב!");
        add("surprise_bad", "SURPRISE: BAD!", "הפתעה: רע!");
        add("surprise", "SURPRISE!", "הפתעה!");

        // Outcome message parts
        add("wrong_prefix", "Wrong", "שגוי");
        add("correct_prefix", "Correct", "נכון");
        add("activation_cost", "Activation cost", "עלות הפעלה");
        add("score_label", "Score", "ניקוד");
        add("lives_label", "Lives", "חיים");
        add("pts", "pts", "נק'");
        add("life", "life", "חיים");

        // Difficulty levels
        add("difficulty_easy", "EASY", "קל");
        add("difficulty_medium", "MEDIUM", "בינוני");
        add("difficulty_hard", "HARD", "קשה");
        add("difficulty_expert", "EXPERT", "מומחה");

        // Question Management Frame
        add("question_management", "Question Management", "ניהול שאלות");
        add("add", "Add", "הוסף");
        add("edit", "Edit", "ערוך");
        add("delete", "Delete", "מחק");
        add("save", "Save", "שמור");
        add("apply", "Apply", "החל");
        add("clear", "Clear", "נקה");
        add("difficulty", "Difficulty:", "רמת קושי:");
        add("correct_label", "Correct:", "תשובה נכונה:");
        add("id", "ID:", "מזהה:");
        add("all", "All", "הכל");
        add("saved_msg", "Questions saved to CSV.", "השאלות נשמרו לקובץ CSV.");
        add("saved_title", "Saved", "נשמר");

        // Add/Edit Question Dialog
        add("add_question", "Add Question", "הוסף שאלה");
        add("edit_question", "Edit Question", "ערוך שאלה");
        add("add_new_question", "Add New Question", "הוסף שאלה חדשה");
        add("text", "Text", "טקסט");
        add("option_a", "Option A", "אפשרות א");
        add("option_b", "Option B", "אפשרות ב");
        add("option_c", "Option C", "אפשרות ג");
        add("option_d", "Option D", "אפשרות ד");
        add("correct_option", "Correct", "נכונה");
        add("difficulty_level", "Difficulty", "רמה");
        add("text_empty", "Text is empty.", "הטקסט ריק.");
        add("invalid_input", "Invalid input: ", "קלט לא תקין: ");
        add("error", "Error", "שגיאה");

        // Admin Access Dialog
        add("admin_access", "Admin Access", "גישת מנהל");
        add("enter_admin_password", "Enter Admin Password:", "הזן סיסמת מנהל:");
        add("access_denied", "Access denied.", "הגישה נדחתה.");
        add("wrong_password", "Wrong password", "סיסמה שגויה");

        // Table headers for Question Management
        add("header_id", "ID", "מזהה");
        add("header_text", "Text", "טקסט");
        add("header_a", "A", "א");
        add("header_b", "B", "ב");
        add("header_c", "C", "ג");
        add("header_d", "D", "ד");
        add("header_correct", "Correct", "נכונה");
        add("header_difficulty", "Difficulty", "רמה");
    }

    private static void add(String key, String en, String he) {
        Map<Language, String> map = new HashMap<>();
        map.put(Language.EN, en);
        map.put(Language.HE, he);
        translations.put(key, map);
    }

    public static String get(String key, Language lang) {
        Map<Language, String> map = translations.get(key);
        if (map == null) return key;
        String result = map.get(lang);
        return result == null ? key : result;
    }
}