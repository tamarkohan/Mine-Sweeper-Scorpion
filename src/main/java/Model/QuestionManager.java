package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * QuestionManager: loads questions from questions.csv and provides
 * accessors for game logic and admin UI.
 *
 * Fix: prevents repeating the same question multiple times in ONE game.
 */
public class QuestionManager {

    private static final String DEFAULT_CSV = "/questions.csv";

    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();

    private String csvPath = DEFAULT_CSV;

    // Track used questions per game (by ID)
    private final Set<Integer> usedQuestionIdsThisGame = new HashSet<>();

    public QuestionManager() {
    }

    public QuestionManager(String csvPath) {
        this.csvPath = csvPath;
    }

    /**
     * Call this at the start of every new game.
     * (GameController.startNewGame should call it)
     */
    public void resetForNewGame() {
        usedQuestionIdsThisGame.clear();
    }

    /**
     * Loads questions from the CSV resource inside the JAR.
     * Schema: id,text,optionA,optionB,optionC,optionD,correctOption,difficultyLevel
     */
    public void loadQuestions() {
        questions.clear();

        InputStream in = getClass().getResourceAsStream(DEFAULT_CSV);
        if (in == null) {
            System.out.println("questions.csv not found inside JAR.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                List<String> cols = parseCsvLine(line);
                try {
                    Question q = Question.fromCsvRow(cols.toArray(new String[0]));
                    questions.add(q);
                } catch (Exception ex) {
                    System.out.println("Failed to parse question row: " + line);
                }
            }

            System.out.println("Loaded " + questions.size() + " questions.");

        } catch (Exception e) {
            System.out.println("Error loading questions.csv: " + e.getMessage());
        }
    }

    /**
     * Saves current questions to CSV (file path, not JAR resource).
     * Used by your admin UI.
     */
    public void saveQuestions() {
        File csvFile = new File(csvPath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, StandardCharsets.UTF_8, false))) {
            for (Question q : questions) {
                bw.write(q.toCsvRow());
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error writing questions.csv: " + e.getMessage());
        }
    }

    // ============================================================
    //  Unique-per-game question picking (NO repeats)
    // ============================================================

    /**
     * Returns a random UNUSED question that matches the given level.
     * Returns null if there are no unused questions left for that level.
     */
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

    /**
     * Returns a random UNUSED question from ANY level.
     * Returns null if there are no unused questions left at all.
     */
    public Question getRandomUnusedQuestionAnyLevel() {
        if (questions.isEmpty()) return null;

        List<Question> pool = new ArrayList<>();
        for (Question q : questions) {
            if (!usedQuestionIdsThisGame.contains(q.getId())) {
                pool.add(q);
            }
        }

        if (pool.isEmpty()) return null;

        Question chosen = pool.get(random.nextInt(pool.size()));
        usedQuestionIdsThisGame.add(chosen.getId());
        return chosen;
    }

    // ============================================================
    // Existing accessors / admin helpers
    // ============================================================

    public List<Question> getAllQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public void addOrReplaceQuestion(Question q) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId() == q.getId()) {
                questions.set(i, q);
                return;
            }
        }
        questions.add(q);
    }

    public void deleteQuestion(int id) {
        questions.removeIf(q -> q.getId() == id);
        usedQuestionIdsThisGame.remove(id); // keep set clean too
    }

    // ============================================================
    // These allow repeats and can cause the bug you described.
    // ============================================================

    /** Old behavior: random by level but CAN repeat. Avoid using in game logic. */
    public Question getRandomQuestionByLevel(Game.QuestionLevel level) {
        if (questions.isEmpty()) return null;

        List<Question> filtered = questions.stream()
                .filter(q -> q.getQuestionLevel() == level)
                .toList();

        List<Question> pool = filtered.isEmpty() ? questions : filtered;
        return pool.get(random.nextInt(pool.size()));
    }

    /** Old behavior: random from all but CAN repeat. Avoid using in game logic. */
    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(random.nextInt(questions.size()));
    }

    // --- Basic CSV parser for the simple schema (handles quoted commas) ---
    private List<String> parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                cols.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }

        cols.add(sb.toString());
        return cols;
    }
}
