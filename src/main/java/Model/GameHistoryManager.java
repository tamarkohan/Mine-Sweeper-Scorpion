package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages persistence and access to game history records.
 *
 * Responsibilities:
 * - Load game history from CSV on startup
 * - Seed initial CSV from resources if no user file exists
 * - Store finished games in memory
 * - Persist history back to CSV after each addition
 *
 * Implemented as a Singleton to ensure a single source of truth.
 */
public class GameHistoryManager {

    // Singleton instance
    private static GameHistoryManager instance;

    // CSV bundled with the application (used for first-time seeding)
    private static final String RESOURCE_CSV = "/data/game_history.csv";

    // CSV file name stored on the user's machine
    private static final String HISTORY_FILE_NAME = "game_history.csv";

    // Default location under the user's home directory
    private static final String DEFAULT_CSV = new File(
            System.getProperty("user.home"),
            ".scorpion-minesweeper" + File.separator + "data" + File.separator + HISTORY_FILE_NAME
    ).getAbsolutePath();

    // Absolute path to the CSV file used by this instance
    private final String csvPath;

    // In-memory cache of all history entries
    private final List<GameHistoryEntry> entries = new ArrayList<>();

    // Private constructor to enforce Singleton usage
    private GameHistoryManager() {
        this(DEFAULT_CSV);
    }

    // Allows specifying a custom CSV path (used internally)
    private GameHistoryManager(String csvPath) {
        this.csvPath = csvPath;

        // Ensure a CSV file exists (seed from resources if missing)
        seedFromResourceIfMissing();

        // Load existing history into memory
        loadFromFile();
    }

    /**
     * Copies the bundled CSV resource into the user directory
     * only if no history file exists yet.
     */
    private void seedFromResourceIfMissing() {
        File outFile = new File(csvPath);
        if (outFile.exists()) return;

        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }

        try (InputStream in = getClass().getResourceAsStream(RESOURCE_CSV)) {
            if (in == null) return;
            try (OutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
            }
        } catch (Exception e) {
            System.out.println("Failed to seed history from resources: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton instance of the manager.
     */
    public static GameHistoryManager getInstance() {
        if (instance == null) {
            instance = new GameHistoryManager();
        }
        return instance;
    }

    /**
     * Returns an unmodifiable view of all stored history entries.
     * Prevents external modification of internal state.
     */
    public List<GameHistoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Adds a completed game entry and persists it immediately.
     */
    public void addEntry(GameHistoryEntry entry) {
        if (entry == null) return;

        // Debug information useful during development
        System.out.println("ADDING HISTORY ENTRY: " + entry);
        System.out.println("CSV PATH USED: " + csvPath);
        System.out.println("ABSOLUTE PATH: " + new java.io.File(csvPath).getAbsolutePath());
        System.out.println("CAN WRITE: " + new java.io.File(csvPath).canWrite());

        entries.add(entry);
        saveToFile();
    }

    // ========================
    // CSV persistence
    // ========================

    /**
     * Writes the entire history list to the CSV file.
     * Overwrites the file to keep it consistent with memory.
     */
    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvPath), StandardCharsets.UTF_8))) {

            // CSV header
            bw.write("timestamp,player1,player2,difficulty,result,finalScore,livesLeft,durationSeconds,totalQuestions,correctAnswers");
            bw.newLine();

            for (GameHistoryEntry e : entries) {
                bw.write(toCsvLine(e));
                bw.newLine();
            }

        } catch (IOException ex) {
            System.out.println("Failed to save history: " + ex.getMessage());
        }
    }

    /**
     * Loads history entries from the CSV file into memory.
     */
    private void loadFromFile() {
        entries.clear();

        File f = new File(csvPath);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {

            String line = br.readLine(); // skip header
            if (line == null) return;

            while ((line = br.readLine()) != null) {
                GameHistoryEntry entry = parseCsvLine(line);
                if (entry != null) entries.add(entry);
            }

        } catch (IOException ex) {
            System.out.println("Failed to load history: " + ex.getMessage());
        }
    }

    /**
     * Converts a GameHistoryEntry into a single CSV line.
     */
    private String toCsvLine(GameHistoryEntry e) {
        return escape(e.getTimestamp().toString()) + "," +
                escape(e.getPlayer1Name()) + "," +
                escape(e.getPlayer2Name()) + "," +
                escape(e.getDifficulty()) + "," +
                escape(e.getResult()) + "," +
                e.getFinalScore() + "," +
                e.getLivesLeft() + "," +
                e.getDurationSeconds() + "," +
                e.getTotalQuestions() + "," +
                e.getCorrectAnswers();
    }

    /**
     * Parses a CSV line back into a GameHistoryEntry object.
     */
    private GameHistoryEntry parseCsvLine(String line) {
        List<String> parts = splitCsv(line);
        if (parts.size() < 10) return null;

        try {
            LocalDateTime ts = LocalDateTime.parse(unescape(parts.get(0)));
            String p1 = unescape(parts.get(1));
            String p2 = unescape(parts.get(2));
            String diff = unescape(parts.get(3));
            String res = unescape(parts.get(4));
            int score = Integer.parseInt(parts.get(5));
            int lives = Integer.parseInt(parts.get(6));
            long dur = Long.parseLong(parts.get(7));
            int totalQ = Integer.parseInt(parts.get(8));
            int correctQ = Integer.parseInt(parts.get(9));

            return new GameHistoryEntry(ts, p1, p2, diff, res, score, lives, dur, totalQ, correctQ);

        } catch (Exception ex) {
            // Invalid or corrupted CSV line
            return null;
        }
    }

    /**
     * Escapes a string for safe CSV writing.
     */
    private String escape(String s) {
        if (s == null) s = "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    /**
     * Reverses CSV escaping back into the original string.
     */
    private String unescape(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    /**
     * Splits a CSV line while respecting quoted values.
     */
    private List<String> splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }

        out.add(cur.toString());
        return out;
    }
}
