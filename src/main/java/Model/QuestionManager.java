package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * QuestionManager: loads questions from questions.csv and provides
 * accessors for game logic and admin UI.
 */
public class QuestionManager {

    private static final String DEFAULT_CSV = "questions.csv";
    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();
    private String csvPath = DEFAULT_CSV;

    public QuestionManager() {
    }

    public QuestionManager(String csvPath) {
        this.csvPath = csvPath;
    }

    /**
     * Loads questions from the configured CSV file.
     * Schema: id,text,optionA,optionB,optionC,optionD,correctOption,difficultyLevel
     */
    public void loadQuestions() {
        questions.clear();
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) {
            System.out.println("questions.csv not found. Using empty question list.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Simple CSV split (supports quoted commas by replacing)
                List<String> cols = parseCsvLine(line);
                try {
                    Question q = Question.fromCsvRow(cols.toArray(new String[0]));
                    questions.add(q);
                } catch (Exception ex) {
                    System.out.println("Failed to parse question row: " + line + " reason: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading questions.csv: " + e.getMessage());
        }
    }

    /**
     * Saves current questions to CSV.
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

    /**
     * Returns a random question filtered by game difficulty.
     * If no exact difficulty match exists, returns any available question.
     */
    public Question getRandomQuestionForDifficulty(Difficulty difficulty) {
        if (questions.isEmpty()) return null;
        String diffKey = difficulty.name().toUpperCase();

        List<Question> filtered = questions.stream()
                .filter(q -> q.getDifficultyLevel() != null &&
                        q.getDifficultyLevel().equalsIgnoreCase(diffKey))
                .collect(Collectors.toList());

        List<Question> pool = filtered.isEmpty() ? questions : filtered;
        return pool.get(random.nextInt(pool.size()));
    }

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
    }

    // --- Basic CSV parser for the simple schema (handles quoted commas) ---
    private List<String> parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
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

