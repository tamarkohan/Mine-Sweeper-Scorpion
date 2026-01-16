package Model;

import Controller.GameController;
import util.LanguageManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuestionManager {

    // --- Singleton ---
    private static QuestionManager instance;
    private QuestionManager() {}
    public static QuestionManager getInstance() {
        if (instance == null) instance = new QuestionManager();
        return instance;
    }

    // --- Data ---
    private final List<Question> allQuestions = new ArrayList<>();
    private final Set<Integer> usedQuestionIdsThisGame = new HashSet<>();
    private final Random random = new Random();

    // --- TESTING HELPERS ---
    private boolean persistenceEnabled = true;

    public void setPersistenceEnabled(boolean enabled) {
        this.persistenceEnabled = enabled;
    }

    // --- Caching ---
    private final List<Question> cacheEn = new ArrayList<>();
    private final List<Question> cacheHe = new ArrayList<>();
    private boolean isCacheLoaded = false;

    /**
     * Loads the questions for the current language.
     */
    public void loadQuestions() {
        ensureCacheLoaded();
        allQuestions.clear();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> source = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;

        if (source != null && !source.isEmpty()) {
            allQuestions.addAll(source);
        } else {
            // Fallback to English if Hebrew is missing/empty, just to prevent crashes
            if (lang == LanguageManager.Language.HE && !cacheEn.isEmpty()) {
                allQuestions.addAll(cacheEn);
            }
        }
    }

    /**
     * FAST language switch - swaps active list from cache.
     */
    public void switchLanguageFromCache() {
        // Same logic as loadQuestions, just ensuring cache is ready
        loadQuestions();
    }

    public void forceReloadQuestions() {
        switchLanguageFromCache();
    }

    private void ensureCacheLoaded() {
        if (isCacheLoaded) return;
        cacheEn.clear();
        cacheHe.clear();

        loadListFromFile("questions.csv", cacheEn);
        loadListFromFile("questions_he.csv", cacheHe);
        isCacheLoaded = true;
    }

    public void preloadAllCaches() {
        ensureCacheLoaded();
    }

    private void loadListFromFile(String fileName, List<Question> targetList) {
        File file = new File("src/main/resources/" + fileName);
        if (file.exists()) {
            loadFromStream(targetList, () -> new FileInputStream(file));
        } else {
            loadFromStream(targetList, () -> getClass().getResourceAsStream("/" + fileName));
        }
    }

    private interface InputStreamSupplier { InputStream get() throws Exception; }

    private void loadFromStream(List<Question> targetList, InputStreamSupplier supplier) {
        try (InputStream is = supplier.get()) {
            if (is == null) return;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                reader.readLine(); // Skip header
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        List<String> cols = parseCsvLineFast(line);
                        Question q = Question.fromCsvRow(cols.toArray(new String[0]));
                        q = normalizeCorrectOption(q);
                        targetList.add(q);
                    } catch (Exception e) { /* skip bad rows */ }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> parseCsvLineFast(String line) {
        if (line.startsWith("\"") && line.endsWith("\"") && line.length() > 2) {
            line = line.substring(1, line.length() - 1);
            line = line.replace("\"\"", "\"");
        }
        List<String> cols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') inQuotes = !inQuotes;
            else if (ch == ',' && !inQuotes) {
                cols.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        cols.add(sb.toString().trim());
        return cols;
    }

    private Question normalizeCorrectOption(Question q) {
        char c = q.getCorrectOption();
        if (c >= '1' && c <= '4') {
            char fixed = (char) ('A' + (c - '1'));
            return new Question(q.getId(), q.getText(), new ArrayList<>(q.getOptions()), fixed, q.getDifficultyLevel());
        }
        return q;
    }

    public void resetForNewGame() {
        usedQuestionIdsThisGame.clear();
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(allQuestions);
    }

    /**
     * --- SINGLE LANGUAGE ADD ---
     * Adds the question ONLY to the current active language list.
     * Does NOT touch the other language file.
     */
    public void addOrReplaceQuestion(Question q) {
        ensureCacheLoaded();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> targetCache = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;

        // 1. Update the specific cache
        targetCache.removeIf(existing -> existing.getId() == q.getId());
        targetCache.add(q);

        // 2. Update active memory
        allQuestions.clear();
        allQuestions.addAll(targetCache);

        // 3. Save ONLY the relevant file
        saveQuestions();
    }

    public void deleteQuestion(int id) {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> targetCache = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;

        targetCache.removeIf(q -> q.getId() == id);
        allQuestions.removeIf(q -> q.getId() == id);

        saveQuestions();
    }

    /**
     * --- SINGLE FILE SAVE ---
     * Saves only the file corresponding to the current language.
     */
    public void saveQuestions() {
        if (!persistenceEnabled) return; // For tests

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();

        if (lang == LanguageManager.Language.HE) {
            saveListToFile(cacheHe, "questions_he.csv");
        } else {
            saveListToFile(cacheEn, "questions.csv");
        }
    }

    private void saveListToFile(List<Question> list, String fileName) {
        File file = new File("src/main/resources/" + fileName);

        // Sort by ID before saving
        list.sort(Comparator.comparingInt(Question::getId));

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.println("id,text,optionA,optionB,optionC,optionD,correctOption,difficultyLevel");
            for (Question q : list) {
                out.println(q.toCsvRow());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Question getRandomUnusedQuestionAnyLevel() {
        if (allQuestions.isEmpty()) return null;
        List<Question> pool = new ArrayList<>();
        for (Question q : allQuestions) {
            if (!usedQuestionIdsThisGame.contains(q.getId())) pool.add(q);
        }
        if (pool.isEmpty()) {
            usedQuestionIdsThisGame.clear();
            pool.addAll(allQuestions);
        }
        if (pool.isEmpty()) return null;
        Question chosen = pool.get(random.nextInt(pool.size()));
        usedQuestionIdsThisGame.add(chosen.getId());
        return chosen;
    }

    /**
     * --- NEXT ID ---
     * Calculates the next ID based ONLY on the current language list.
     */
    public int getNextId() {
        ensureCacheLoaded();
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> source = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;

        int maxId = source.stream().mapToInt(Question::getId).max().orElse(0);
        return maxId + 1;
    }

    // --- Helper for Unit Tests ---
    public void clearQuestionsForTesting() {
        allQuestions.clear();
        usedQuestionIdsThisGame.clear();
        cacheEn.clear();
        cacheHe.clear();
    }
}