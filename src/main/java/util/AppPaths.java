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
}
