
package View;

import Controller.GameController;
import Controller.GameController.GameHistoryRow;
import Controller.GameController.PlayerHistoryRow;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class GameHistoryFrame extends JFrame {

    private final GameController controller;
    private final Runnable onExitToMenu;

    private final DefaultTableModel gamesModel;
    private final DefaultTableModel playersModel;

    private JComboBox<String> difficultyFilter;
    private JComboBox<String> resultFilter;
    private JTextField searchBox;

    private static final String DIFF_ALL = "All";
    private static final String RES_ALL  = "All";

    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30, 240);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20, 220);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80, 200);

    public GameHistoryFrame(GameController controller, Runnable onExitToMenu) {
        super("Game & Players History");
        this.controller = controller;
        this.onExitToMenu = onExitToMenu;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ================= MODELS =================
        gamesModel = new DefaultTableModel(new String[]{
                "Players", "Date / Time", "Difficulty", "Result", "Final Score",
                "Remaining Lives", "Correct Answers", "Accuracy", "Duration"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        playersModel = new DefaultTableModel(new String[]{
                "Player", "Total Games", "Best Score", "Average Accuracy", "Preferred Difficulty"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable gamesTable = createStyledTable(gamesModel);
        JTable playersTable = createStyledTable(playersModel);

        // ================= SORTING (FIXED) =================
        TableRowSorter<DefaultTableModel> gSorter = new TableRowSorter<>(gamesModel);
        gamesTable.setRowSorter(gSorter);

        TableRowSorter<DefaultTableModel> pSorter = new TableRowSorter<>(playersModel);
        playersTable.setRowSorter(pSorter);

        // repaint header when sort changes (so ▲▼ updates)
        gSorter.addRowSorterListener(e -> gamesTable.getTableHeader().repaint());
        pSorter.addRowSorterListener(e -> playersTable.getTableHeader().repaint());

        // ---- GAMES table comparators ----
        // Final Score (col 4)
        gSorter.setComparator(4, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));
        // Remaining Lives (col 5)
        gSorter.setComparator(5, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));
        // Correct Answers "3/4" (col 6)
        gSorter.setComparator(6, (a, b) -> Integer.compare(parseCorrectAnswers(a), parseCorrectAnswers(b)));
        // Accuracy "75%" or "-" (col 7)
        gSorter.setComparator(7, (a, b) -> Integer.compare(parsePercent(a), parsePercent(b)));

        // Date/Time "dd/MM/yy HH:mm" (col 1)
        gSorter.setComparator(1, (a, b) -> {
            try {
                java.time.format.DateTimeFormatter fmt =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                java.time.LocalDateTime da = java.time.LocalDateTime.parse(a.toString(), fmt);
                java.time.LocalDateTime db = java.time.LocalDateTime.parse(b.toString(), fmt);
                return da.compareTo(db);
            } catch (Exception e) {
                return a.toString().compareTo(b.toString());
            }
        });

        // Duration "mm:ss" (col 8)
        gSorter.setComparator(8, (a, b) -> Integer.compare(parseDurationSeconds(a), parseDurationSeconds(b)));

        // ---- PLAYERS table comparators ----
        // Total Games (col 1)
        pSorter.setComparator(1, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));
        // Best Score (col 2)
        pSorter.setComparator(2, (a, b) -> Integer.compare(parseIntSafe(a), parseIntSafe(b)));
        // Average Accuracy (col 3)
        pSorter.setComparator(3, (a, b) -> Integer.compare(parsePercent(a), parsePercent(b)));

        JScrollPane gamesScroll = createStyledScrollPane(gamesTable);
        JScrollPane playersScroll = createStyledScrollPane(playersTable);

        // ================= TOP BAR =================
        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton backBtn = createBackButton();
        topBar.add(backBtn);
        topBar.add(Box.createHorizontalStrut(20));

        difficultyFilter = createCombo(new String[]{DIFF_ALL, "EASY", "MEDIUM", "HARD"});
        resultFilter = createCombo(new String[]{RES_ALL, "WON", "LOST"});

        Dimension comboSize = new Dimension(120, 26);
        difficultyFilter.setPreferredSize(comboSize);
        difficultyFilter.setMaximumSize(comboSize);
        resultFilter.setPreferredSize(comboSize);
        resultFilter.setMaximumSize(comboSize);

        topBar.add(label("Difficulty:"));
        topBar.add(difficultyFilter);
        topBar.add(Box.createHorizontalStrut(10));
        topBar.add(label("Result:"));
        topBar.add(resultFilter);

        topBar.add(Box.createHorizontalGlue());

        topBar.add(label("Search:"));

        searchBox = new JTextField();
        searchBox.setPreferredSize(new Dimension(260, 28));
        searchBox.setMaximumSize(new Dimension(260, 28));
        searchBox.setBackground(new Color(0, 0, 0, 180));
        searchBox.setForeground(Color.WHITE);
        searchBox.setCaretColor(ACCENT_COLOR);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        JButton searchBtn = createButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 28));
        searchBtn.setMaximumSize(new Dimension(90, 28));
        searchBtn.setMargin(new Insets(0, 10, 0, 10));

        topBar.add(searchBox);
        topBar.add(Box.createHorizontalStrut(8));
        topBar.add(searchBtn);

        // ================= ROOT =================
        JPanel root = new BackgroundPanel("/ui/menu/game_history_bg.png");
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        root.add(topBar, BorderLayout.NORTH);

        JPanel tables = new JPanel();
        tables.setOpaque(false);
        tables.setLayout(new BoxLayout(tables, BoxLayout.Y_AXIS));
        tables.add(Box.createVerticalStrut(120));
        tables.add(gamesScroll);
        tables.add(Box.createVerticalStrut(25));
        tables.add(playersScroll);

        root.add(tables, BorderLayout.CENTER);
        setContentPane(root);

        // ================= EVENTS =================
        difficultyFilter.addActionListener(e -> reloadTables());
        resultFilter.addActionListener(e -> reloadTables());
        searchBtn.addActionListener(e -> reloadTables());
        searchBox.addActionListener(e -> reloadTables());

        setSize(1000, 750);
        setLocationRelativeTo(null);
        reloadTables();
    }

    // ================= HELPERS =================

    private JButton createBackButton() {
        URL url = getClass().getResource("/ui/icons/back.png");
        if (url == null) {
            JButton fallback = new JButton("Back");
            fallback.addActionListener(e -> {
                dispose();
                if (onExitToMenu != null) onExitToMenu.run();
            });
            return fallback;
        }

        Image img = new ImageIcon(url).getImage();
        Image scaled = img.getScaledInstance(29, 29, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);

        JButton btn = new JButton(icon);
        btn.setPreferredSize(new Dimension(50, 50));
        btn.setMinimumSize(new Dimension(50, 50));
        btn.setMaximumSize(new Dimension(50, 50));

        btn.setBorder(null);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Back");

        btn.addActionListener(e -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

        return btn;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_COLOR);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        return l;
    }

    private JComboBox<String> createCombo(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Arial", Font.PLAIN, 14));
        return box;
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(ACCENT_COLOR);
        b.setBackground(new Color(40, 40, 40));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        return b;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);

        t.setOpaque(true);
        t.setBackground(new Color(20, 20, 20));
        t.setForeground(TEXT_COLOR);
        t.setSelectionBackground(TABLE_SELECTION_BG);
        t.setSelectionForeground(TEXT_COLOR);
        t.setGridColor(new Color(90, 90, 90));
        t.setRowHeight(25);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        t.getTableHeader().setReorderingAllowed(false);

        JTableHeader h = t.getTableHeader();
        h.setOpaque(true);
        h.setForeground(ACCENT_COLOR);
        h.setBackground(new Color(30, 30, 30));
        h.setFont(new Font("Arial", Font.BOLD, 14));
        h.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // header renderer with arrows
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);

                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Arial", Font.BOLD, 14));
                lbl.setForeground(ACCENT_COLOR);
                lbl.setOpaque(true);
                lbl.setBackground(TABLE_HEADER_BG);

                String base = (value == null) ? "" : value.toString();
                String text = base + "  ↕";

                RowSorter<?> sorter = table.getRowSorter();
                if (sorter != null && !sorter.getSortKeys().isEmpty()) {
                    RowSorter.SortKey key = sorter.getSortKeys().get(0);
                    int modelCol = table.convertColumnIndexToModel(col);

                    if (key.getColumn() == modelCol) {
                        if (key.getSortOrder() == SortOrder.ASCENDING) text = base + "  ▲";
                        else if (key.getSortOrder() == SortOrder.DESCENDING) text = base + "  ▼";
                    }
                }

                lbl.setText(text);
                return lbl;
            }
        });

        DefaultTableCellRenderer c = new DefaultTableCellRenderer();
        c.setHorizontalAlignment(JLabel.CENTER);
        c.setForeground(TEXT_COLOR);
        c.setBackground(new Color(20, 20, 20));
        c.setOpaque(true);

        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(c);
        }

        return t;
    }

    private JScrollPane createStyledScrollPane(JComponent v) {
        JScrollPane s = new JScrollPane(v);
        s.setOpaque(false);
        s.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255, 120)));
        s.getViewport().setOpaque(true);
        s.getViewport().setBackground(new Color(20, 20, 20));
        return s;
    }

    private void reloadTables() {
        gamesModel.setRowCount(0);
        playersModel.setRowCount(0);

        String diff = (String) difficultyFilter.getSelectedItem();
        String res  = (String) resultFilter.getSelectedItem();
        String search = searchBox.getText().trim();

        List<GameHistoryRow> games = controller.getGameHistory(diff, res, search);
        for (GameHistoryRow r : games) {
            gamesModel.addRow(new Object[]{
                    r.players, r.dateTime, r.difficulty, r.result, r.finalScore,
                    r.remainingLives, r.correctAnswers, r.accuracy, r.duration
            });
        }

        List<PlayerHistoryRow> players = controller.getPlayersHistory(diff, res, search);
        for (PlayerHistoryRow r : players) {
            playersModel.addRow(new Object[]{
                    r.player, r.totalGames, r.bestScore,
                    r.averageAccuracy, r.preferredDifficulty
            });
        }
    }

    private static class BackgroundPanel extends JPanel {
        private final Image img;
        BackgroundPanel(String path) {
            URL u = getClass().getResource(path);
            img = u != null ? new ImageIcon(u).getImage() : null;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // =======================
    // SORT HELPERS
    // =======================
    private static int parsePercent(Object o) {
        if (o == null) return Integer.MIN_VALUE;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("-")) return Integer.MIN_VALUE;
        s = s.replace("%", "").trim();
        try { return Integer.parseInt(s); }
        catch (Exception e) { return Integer.MIN_VALUE; }
    }

    private static int parseCorrectAnswers(Object o) {
        if (o == null) return Integer.MIN_VALUE;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("-")) return Integer.MIN_VALUE;
        try {
            String[] parts = s.split("/");
            return Integer.parseInt(parts[0].trim());
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }

    private static int parseIntSafe(Object o) {
        if (o == null) return Integer.MIN_VALUE;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("-")) return Integer.MIN_VALUE;
        try { return Integer.parseInt(s); }
        catch (Exception e) { return Integer.MIN_VALUE; }
    }

    private static int parseDurationSeconds(Object o) {
        if (o == null) return Integer.MIN_VALUE;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("-")) return Integer.MIN_VALUE;
        try {
            String[] p = s.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }
}
