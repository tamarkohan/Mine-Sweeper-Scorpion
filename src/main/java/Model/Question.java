package Model;

import java.util.Arrays;
import java.util.List;

/**
 * Simple question model used by the Question System.
 * Schema aligns with questions.csv:
 * id, text, optionA, optionB, optionC, optionD, correctOption, difficultyLevel
 */
public class Question {
    private final int id;
    private final String text;
    private final List<String> options; // expected size 4
    private final char correctOption;   // 'A'/'B'/'C'/'D'
    private final String difficultyLevel; // free text or maps to game difficulty

    public Question(int id, String text, List<String> options, char correctOption, String difficultyLevel) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctOption = Character.toUpperCase(correctOption);
        this.difficultyLevel = difficultyLevel;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public char getCorrectOption() {
        return correctOption;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    /**
     * Convenience factory to create from CSV row.
     */
    public static Question fromCsvRow(String[] cols) {
        if (cols.length < 8) {
            throw new IllegalArgumentException("Question row must have 8 columns");
        }
        int id = Integer.parseInt(cols[0].trim());
        String text = cols[1].trim();
        List<String> opts = Arrays.asList(cols[2].trim(), cols[3].trim(), cols[4].trim(), cols[5].trim());
        char correct = cols[6].trim().isEmpty() ? 'A' : cols[6].trim().toUpperCase().charAt(0);
        String diff = cols[7].trim();
        return new Question(id, text, opts, correct, diff);
    }

    public String toCsvRow() {
        String optionA = options.size() > 0 ? options.get(0) : "";
        String optionB = options.size() > 1 ? options.get(1) : "";
        String optionC = options.size() > 2 ? options.get(2) : "";
        String optionD = options.size() > 3 ? options.get(3) : "";
        return id + "," + escape(text) + "," + escape(optionA) + "," + escape(optionB) + "," +
                escape(optionC) + "," + escape(optionD) + "," + correctOption + "," + difficultyLevel;
    }

    private String escape(String s) {
        if (s == null) return "";
        // basic CSV escape: wrap with quotes if contains comma or quote
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}

