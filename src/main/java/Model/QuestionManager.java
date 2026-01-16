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

    // --- Caching ---
    private final List<Question> cacheEn = new ArrayList<>();
    private final List<Question> cacheHe = new ArrayList<>();
    private boolean isCacheLoaded = false;

    /**
     * Loads the questions for the current language.
     * Guaranteed to switch lists instantly.
     */
    public void loadQuestions() {
        ensureCacheLoaded();

        allQuestions.clear();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> source = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;

        if (source != null && !source.isEmpty()) {
            allQuestions.addAll(source);
        } else {
            System.err.println("Warning: Target language cache is empty! " + lang);
            if (lang == LanguageManager.Language.HE && !cacheEn.isEmpty()) {
                allQuestions.addAll(cacheEn);
            }
        }

        System.out.println("Active Questions: " + allQuestions.size() + " (" + lang + ")");
    }

    /**
     * FAST language switch - just swaps the active list from pre-loaded cache.
     * No file I/O, no parsing - instant switch.
     */
    public void switchLanguageFromCache() {
        ensureCacheLoaded();

        allQuestions.clear();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> source = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;

        if (source != null && !source.isEmpty()) {
            allQuestions.addAll(source);
        } else {
            System.err.println("Warning: Target language cache is empty! " + lang);
            // Fallback to English if Hebrew is empty
            if (!cacheEn.isEmpty()) {
                allQuestions.addAll(cacheEn);
            }
        }

        System.out.println("Switched to " + lang + " - " + allQuestions.size() + " questions ready");
    }

    /**
     * Forces a complete reload of questions for the current language.
     * This clears the active list and reloads from the appropriate cache.
     */
    public void forceReloadQuestions() {
        // Just delegate to switchLanguageFromCache - it does the same thing but faster
        switchLanguageFromCache();
    }

    private void ensureCacheLoaded() {
        if (isCacheLoaded) return;

        System.out.println("Loading CSV files into cache...");
        long startTime = System.currentTimeMillis();

        cacheEn.clear();
        cacheHe.clear();

        // Load both language files into cache
        loadListFromFile("questions.csv", cacheEn);
        loadListFromFile("questions_he.csv", cacheHe);

        isCacheLoaded = true;

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Cache loaded in " + elapsed + "ms (EN: " + cacheEn.size() + ", HE: " + cacheHe.size() + ")");
    }

    /**
     * Pre-loads both language caches at startup.
     * Call this early (e.g., during splash screen) to avoid any lag later.
     */
    public void preloadAllCaches() {
        ensureCacheLoaded();
    }

    private void loadListFromFile(String fileName, List<Question> targetList) {
        File file = new File("src/main/resources/" + fileName);
        if (file.exists()) {
            loadFromStream(fileName, targetList, () -> new FileInputStream(file));
            return;
        }
        loadFromStream(fileName, targetList, () -> getClass().getResourceAsStream("/" + fileName));
    }

    private interface InputStreamSupplier { InputStream get() throws Exception; }

    private void loadFromStream(String sourceName, List<Question> targetList, InputStreamSupplier supplier) {
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
                    } catch (Exception e) {
                        // Silently skip bad rows
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OPTIMIZED: Fast CSV parser that handles Excel's double-quote format
     */
    private List<String> parseCsvLineFast(String line) {
        // Quick check: If line starts and ends with quotes, it's Excel format
        if (line.startsWith("\"") && line.endsWith("\"") && line.length() > 2) {
            line = line.substring(1, line.length() - 1);
            line = line.replace("\"\"", "\"");
        }

        List<String> cols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
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

    public void addOrReplaceQuestion(Question q) {
        allQuestions.removeIf(existing -> existing.getId() == q.getId());
        allQuestions.add(q);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> cache = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;
        cache.removeIf(existing -> existing.getId() == q.getId());
        cache.add(q);

        saveQuestions();
    }

    public void deleteQuestion(int id) {
        allQuestions.removeIf(q -> q.getId() == id);

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> cache = (lang == LanguageManager.Language.HE) ? cacheHe : cacheEn;
        cache.removeIf(q -> q.getId() == id);

        saveQuestions();
    }

    public void saveQuestions() {
        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        String fileName = (lang == LanguageManager.Language.HE) ? "questions_he.csv" : "questions.csv";
        File file = new File("src/main/resources/" + fileName);

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.println("id,text,optionA,optionB,optionC,optionD,correctOption,difficultyLevel");
            for (Question q : allQuestions) {
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
}