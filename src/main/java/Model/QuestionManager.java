package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuestionManager {

    private static final String RESOURCE_CSV = "/questions.csv";

    // IDE path
    private static final String DEV_RES_PATH = "src/main/resources/questions.csv";

    // fallback writable path
    private static final String APP_FOLDER = "ScorpionMinesweeper";
    private static final String FILE_NAME = "questions.csv";

    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();
    private final Set<Integer> usedQuestionIdsThisGame = new HashSet<>();

    private final File csvFile;   // the ONE file we will read+write

    public QuestionManager() {
        this.csvFile = resolveCsvFile();
        debugWhere();
    }

    /**
     * Choose where we read/write:
     * - If running in IDE and src/main/resources/questions.csv exists -> use it
     * - Else -> use user-home/ScorpionMinesweeper/questions.csv
     */
    private File resolveCsvFile() {
        File dev = new File(DEV_RES_PATH);
        if (dev.exists() && dev.isFile()) {
            return dev;
        }
        File dir = new File(System.getProperty("user.home"), APP_FOLDER);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, FILE_NAME);
    }

    private void debugWhere() {
        System.out.println("=== QuestionManager CSV ===");
        System.out.println("Using FILE: " + csvFile.getAbsolutePath());
        System.out.println("Exists? " + csvFile.exists());
        System.out.println("user.dir = " + System.getProperty("user.dir"));
        System.out.println("===========================");
    }


    public void resetForNewGame() {
        usedQuestionIdsThisGame.clear();
    }

    public void loadQuestions() {
        questions.clear();

        // If the chosen file exists -> load it
        if (csvFile.exists() && csvFile.isFile()) {
            loadFromFile(csvFile);
            System.out.println("Loaded " + questions.size() + " questions from FILE: " + csvFile.getAbsolutePath());
            return;
        }

        // If file doesn't exist (first run in user-home mode) -> load from resource then create file
        loadFromResource();
        System.out.println("Loaded " + questions.size() + " questions from RESOURCE: " + RESOURCE_CSV);

        if (!questions.isEmpty()) {
            saveQuestions();
            System.out.println("Created FILE at: " + csvFile.getAbsolutePath());
        }
    }

    private void loadFromFile(File file) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                List<String> cols = parseCsvLine(line);
                try {
                    Question q = Question.fromCsvRow(cols.toArray(new String[0]));

                    //  normalize correct option (if CSV has "1"/"2"/"3"/"4")
                    q = normalizeCorrectOptionIfNeeded(q);

                    questions.add(q);
                } catch (Exception ex) {
                    System.out.println("Bad row in FILE: " + line);
                }
            }

        } catch (Exception e) {
            System.out.println("Error reading FILE questions.csv: " + e.getMessage());
        }
    }

    private void loadFromResource() {
        InputStream in = getClass().getResourceAsStream(RESOURCE_CSV);
        if (in == null) {
            System.out.println("questions.csv not found in resources: " + RESOURCE_CSV);
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                List<String> cols = parseCsvLine(line);
                try {
                    Question q = Question.fromCsvRow(cols.toArray(new String[0]));
                    q = normalizeCorrectOptionIfNeeded(q);
                    questions.add(q);
                } catch (Exception ex) {
                    System.out.println("Bad row in RESOURCE: " + line);
                }
            }

        } catch (Exception e) {
            System.out.println("Error reading RESOURCE questions.csv: " + e.getMessage());
        }
    }

    /**
     * If Question.fromCsvRow loaded correctOption as '1'..'4', convert to 'A'..'D'
     */
    private Question normalizeCorrectOptionIfNeeded(Question q) {
        char c = q.getCorrectOption();
        if (c >= '1' && c <= '4') {
            char fixed = (char) ('A' + (c - '1'));
            return new Question(q.getId(), q.getText(), new ArrayList<>(q.getOptions()), fixed, q.getDifficultyLevel());
        }
        return q;
    }

    public void saveQuestions() {
        File parent = csvFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile, false), StandardCharsets.UTF_8))) {

            for (Question q : questions) {
                bw.write(q.toCsvRow());
                bw.newLine();
            }

            System.out.println("Saved questions to FILE: " + csvFile.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("Error writing questions.csv: " + e.getMessage());
        }
    }

    public List<Question> getAllQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public void addOrReplaceQuestion(Question q) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId() == q.getId()) {
                questions.set(i, q);
                usedQuestionIdsThisGame.remove(q.getId());
                return;
            }
        }
        questions.add(q);
    }

    public void deleteQuestion(int id) {
        questions.removeIf(q -> q.getId() == id);
        usedQuestionIdsThisGame.remove(id);
    }

    public Question getRandomUnusedQuestionByLevel(Game.QuestionLevel level) {
        if (questions.isEmpty()) return null;

        List<Question> pool = new ArrayList<>();
        for (Question q : questions) {
            if (q.getQuestionLevel() == level && !usedQuestionIdsThisGame.contains(q.getId())) {
                pool.add(q);
            }
        }
        if (pool.isEmpty()) return null;

        Question chosen = pool.get(random.nextInt(pool.size()));
        usedQuestionIdsThisGame.add(chosen.getId());
        return chosen;
    }

    public Question getRandomUnusedQuestionAnyLevel() {
        if (questions.isEmpty()) return null;

        List<Question> pool = new ArrayList<>();
        for (Question q : questions) {
            if (!usedQuestionIdsThisGame.contains(q.getId())) pool.add(q);
        }
        if (pool.isEmpty()) return null;

        Question chosen = pool.get(random.nextInt(pool.size()));
        usedQuestionIdsThisGame.add(chosen.getId());
        return chosen;
    }

    private List<String> parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') inQuotes = !inQuotes;
            else if (ch == ',' && !inQuotes) {
                cols.add(sb.toString());
                sb.setLength(0);
            } else sb.append(ch);
        }
        cols.add(sb.toString());
        return cols;
    }
}
