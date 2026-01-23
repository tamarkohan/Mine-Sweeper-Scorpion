package util;

import java.io.File;

public class AppPaths {

    public static File dataDir() {
        File dir = new File(System.getProperty("user.home"), ".scorpion-minesweeper");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File questionsEnFile() {
        return new File(dataDir(), "questions.csv");
    }

    public static File questionsHeFile() {
        return new File(dataDir(), "questions_he.csv");
    }

    public static File questionsArFile() {
        return new File(dataDir(), "questions_ar.csv");
    }

    public static File questionsRuFile() {
        return new File(dataDir(), "questions_ru.csv");
    }

    public static File questionsEsFile() {
        return new File(dataDir(), "questions_es.csv");
    }

    /**
     * Get the questions file for a specific language
     */
    public static File questionsFile(LanguageManager.Language lang) {
        return switch (lang) {
            case EN -> questionsEnFile();
            case HE -> questionsHeFile();
            case AR -> questionsArFile();
            case RU -> questionsRuFile();
            case ES -> questionsEsFile();
        };
    }
}