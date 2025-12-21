package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameHistoryManager {

    private static GameHistoryManager instance;

    //  writeable location (works in JAR + IDE)
    private static final String RESOURCE_CSV = "/game_history.csv";
    private static final String HISTORY_FILE_NAME = "game_history.csv";
    private static final String DEFAULT_CSV =
            System.getProperty("user.home") + File.separator + HISTORY_FILE_NAME;

    private final String csvPath;
    private final List<GameHistoryEntry> entries = new ArrayList<>();

    private GameHistoryManager() {
        this(DEFAULT_CSV);
    }

    public GameHistoryManager(String csvPath) {
        this.csvPath = csvPath;

        //if external file doesn't exist yet -> copy initial history from resources
        seedFromResourceIfMissing();

        loadFromFile();
    }

    private void seedFromResourceIfMissing() {
        File outFile = new File(csvPath);
        if (outFile.exists()) return;

        try (InputStream in = getClass().getResourceAsStream(RESOURCE_CSV)) {
            // אם אין קובץ בריסורסס בכלל – פשוט נתחיל ריק
            if (in == null) return;

            // יוצרים את הקובץ החיצוני ומעתיקים אליו את התוכן מהריסורסס
            try (OutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
            }
        } catch (Exception e) {
            System.out.println("Failed to seed history from resources: " + e.getMessage());
        }
    }



    public static GameHistoryManager getInstance() {
        if (instance == null) {
            instance = new GameHistoryManager();
        }
        return instance;
    }

    public List<GameHistoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void addEntry(GameHistoryEntry entry) {
        if (entry == null) return;
        entries.add(entry);
        saveToFile();     // save every time
    }

    // ========================
    // Persist to CSV
    // ========================

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvPath), StandardCharsets.UTF_8))) {

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
            return null;
        }
    }

    private String escape(String s) {
        if (s == null) s = "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private String unescape(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

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

    // optional: useful for debug
    public String getCsvPath() {
        return csvPath;
    }
}
