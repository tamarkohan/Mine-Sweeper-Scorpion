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

    // --- Caching for all 5 languages ---
    private final List<Question> cacheEn = new ArrayList<>();
    private final List<Question> cacheHe = new ArrayList<>();
    private final List<Question> cacheAr = new ArrayList<>();
    private final List<Question> cacheRu = new ArrayList<>();
    private final List<Question> cacheEs = new ArrayList<>();
    private boolean isCacheLoaded = false;


    private util.TranslatorService translator; // lazy

    /**
     * Loads the questions for the current language.
     */
    public void loadQuestions() {
        ensureCacheLoaded();
        allQuestions.clear();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> source = getCacheForLanguage(lang);

        if (source != null && !source.isEmpty()) {
            allQuestions.addAll(source);
        } else {
            // Fallback to English if target language is missing/empty
            if (!cacheEn.isEmpty()) {
                allQuestions.addAll(cacheEn);
            }
        }
    }

    /**
     * Get the cache for a specific language
     */
    private List<Question> getCacheForLanguage(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> cacheHe;
            case AR -> cacheAr;
            case RU -> cacheRu;
            case ES -> cacheEs;
            default -> cacheEn;
        };
    }

    /**
     * FAST language switch - swaps active list from cache.
     */
    public void switchLanguageFromCache() {
        loadQuestions();
    }

    public void forceReloadQuestions() {
        switchLanguageFromCache();
    }

    private void ensureCacheLoaded() {
        if (isCacheLoaded) return;
        cacheEn.clear();
        cacheHe.clear();
        cacheAr.clear();
        cacheRu.clear();
        cacheEs.clear();

        loadListFromFile("questions.csv", cacheEn, LanguageManager.Language.EN);
        loadListFromFile("questions_he.csv", cacheHe, LanguageManager.Language.HE);
        loadListFromFile("questions_ar.csv", cacheAr, LanguageManager.Language.AR);
        loadListFromFile("questions_ru.csv", cacheRu, LanguageManager.Language.RU);
        loadListFromFile("questions_es.csv", cacheEs, LanguageManager.Language.ES);
        isCacheLoaded = true;
    }

    public void preloadAllCaches() {
        ensureCacheLoaded();
    }

    private void loadListFromFile(String fileName, List<Question> targetList, LanguageManager.Language lang) {
        // 1) First try writable external location (works when running JAR)
        File external = getExternalFile(lang);

        if (external != null && external.exists()) {
            loadFromStream(targetList, () -> new FileInputStream(external));
            return;
        }

        // 2) Dev mode fallback (IntelliJ resources folder)
        File devFile = new File("src/main/resources/" + fileName);
        if (devFile.exists()) {
            loadFromStream(targetList, () -> new FileInputStream(devFile));
            return;
        }

        // 3) Packaged resources fallback
        loadFromStream(targetList, () -> getClass().getResourceAsStream("/" + fileName));
    }

    private File getExternalFile(LanguageManager.Language lang) {
        return switch (lang) {
            case HE -> util.AppPaths.questionsHeFile();
            case AR -> util.AppPaths.questionsArFile();
            case RU -> util.AppPaths.questionsRuFile();
            case ES -> util.AppPaths.questionsEsFile();
            default -> util.AppPaths.questionsEnFile();
        };
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
     */
    public void addOrReplaceQuestion(Question q) {
        ensureCacheLoaded();

        LanguageManager.Language lang = GameController.getInstance().getCurrentLanguage();
        List<Question> targetCache = getCacheForLanguage(lang);

        targetCache.removeIf(existing -> existing.getId() == q.getId());
        targetCache.add(q);

        allQuestions.clear();
        allQuestions.addAll(targetCache);

        saveQuestions();
    }

    public void deleteQuestion(int id) {
        ensureCacheLoaded();

        cacheEn.removeIf(q -> q.getId() == id);
        cacheHe.removeIf(q -> q.getId() == id);
        cacheAr.removeIf(q -> q.getId() == id);
        cacheRu.removeIf(q -> q.getId() == id);
        cacheEs.removeIf(q -> q.getId() == id);
        allQuestions.removeIf(q -> q.getId() == id);

        saveAllLanguages();
    }

    public void saveQuestions() {
        saveAllLanguages();
    }

    private void saveListToFile(List<Question> list, LanguageManager.Language lang) {
        File file = getExternalFile(lang);
        if (file == null) return;

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

    public int getNextId() {
        ensureCacheLoaded();
        int maxEn = cacheEn.stream().mapToInt(Question::getId).max().orElse(0);
        int maxHe = cacheHe.stream().mapToInt(Question::getId).max().orElse(0);
        int maxAr = cacheAr.stream().mapToInt(Question::getId).max().orElse(0);
        int maxRu = cacheRu.stream().mapToInt(Question::getId).max().orElse(0);
        int maxEs = cacheEs.stream().mapToInt(Question::getId).max().orElse(0);
        return Math.max(Math.max(Math.max(Math.max(maxEn, maxHe), maxAr), maxRu), maxEs) + 1;
    }

    public void clearQuestionsForTesting() {
        allQuestions.clear();
        usedQuestionIdsThisGame.clear();
        cacheEn.clear();
        cacheHe.clear();
        cacheAr.clear();
        cacheRu.clear();
        cacheEs.clear();
    }

    private util.TranslatorService getTranslator() {
        if (translator == null) translator = util.TranslatorService.fromEnvOrResource();
        return translator;
    }

    public void saveAllLanguages() {
        if (!persistenceEnabled) return;
        saveListToFile(cacheEn, LanguageManager.Language.EN);
        saveListToFile(cacheHe, LanguageManager.Language.HE);
        saveListToFile(cacheAr, LanguageManager.Language.AR);
        saveListToFile(cacheRu, LanguageManager.Language.RU);
        saveListToFile(cacheEs, LanguageManager.Language.ES);
    }

    public void saveBothLanguages() {
        saveAllLanguages();
    }

    public void addOrReplaceQuestionBilingual(Question input) throws Exception {
        ensureCacheLoaded();

        LanguageManager.Language sourceLang = util.BilingualQuestionUtil.detectQuestionLanguage(input);

        Question qEn, qHe, qAr, qRu, qEs;

        if (sourceLang == LanguageManager.Language.EN) {
            qEn = input;
            qHe = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "en", "he");
            qAr = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "en", "ar");
            qRu = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "en", "ru");
            qEs = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "en", "es");
        } else if (sourceLang == LanguageManager.Language.HE) {
            qHe = input;
            qEn = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "he", "en");
            qAr = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "he", "ar");
            qRu = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "he", "ru");
            qEs = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "he", "es");
        } else if (sourceLang == LanguageManager.Language.AR) {
            qAr = input;
            qEn = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ar", "en");
            qHe = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ar", "he");
            qRu = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ar", "ru");
            qEs = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ar", "es");
        } else if (sourceLang == LanguageManager.Language.RU) {
            qRu = input;
            qEn = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ru", "en");
            qHe = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ru", "he");
            qAr = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ru", "ar");
            qEs = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "ru", "es");
        } else {
            qEs = input;
            qEn = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "es", "en");
            qHe = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "es", "he");
            qAr = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "es", "ar");
            qRu = util.BilingualQuestionUtil.translateQuestion(getTranslator(), input, "es", "ru");
        }

        int id = input.getId();
        cacheEn.removeIf(q -> q.getId() == id);
        cacheHe.removeIf(q -> q.getId() == id);
        cacheAr.removeIf(q -> q.getId() == id);
        cacheRu.removeIf(q -> q.getId() == id);
        cacheEs.removeIf(q -> q.getId() == id);

        cacheEn.add(qEn);
        cacheHe.add(qHe);
        cacheAr.add(qAr);
        cacheRu.add(qRu);
        cacheEs.add(qEs);

        loadQuestions();
        saveAllLanguages();
    }
}