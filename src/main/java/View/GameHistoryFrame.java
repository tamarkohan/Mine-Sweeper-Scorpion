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
    private static final String RES_ALL = "All";

    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30, 240);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80, 200);

    public GameHistoryFrame(GameController controller, Runnable onExitToMenu) {
        super("Game History");

        this.controller = controller;
        this.onExitToMenu = onExitToMenu;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ================= MODELS =================
        gamesModel = new DefaultTableModel(new String[]{
                "Players", "Date / Time", "Difficulty", "Result",
                "Final Score", "Remaining Lives", "Correct Answers",
                "Accuracy", "Duration"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        playersModel = new DefaultTableModel(new String[]{
                "Player", "Total Games", "Best Score",
                "Average Accuracy", "Preferred Difficulty"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable gamesTable = createStyledTable(gamesModel);
        JTable playersTable = createStyledTable(playersModel);

        // ================= SORTING =================
        TableRowSorter<DefaultTableModel> gSorter = new TableRowSorter<>(gamesModel);
        gamesTable.setRowSorter(gSorter);
        gSorter.addRowSorterListener(e -> gamesTable.getTableHeader().repaint());

        gSorter.setComparator(4, (a, b) -> parseInt(a) - parseInt(b)); // score
        gSorter.setComparator(5, (a, b) -> parseInt(a) - parseInt(b)); // lives
        gSorter.setComparator(7, (a, b) -> parsePercent(a) - parsePercent(b)); // accuracy
        gSorter.setComparator(8, (a, b) -> parseDuration(a) - parseDuration(b)); // duration

        TableRowSorter<DefaultTableModel> pSorter = new TableRowSorter<>(playersModel);
        playersTable.setRowSorter(pSorter);
        pSorter.addRowSorterListener(e -> playersTable.getTableHeader().repaint());

        pSorter.setComparator(1, (a, b) -> parseInt(a) - parseInt(b));
        pSorter.setComparator(2, (a, b) -> parseInt(a) - parseInt(b));
        pSorter.setComparator(3, (a, b) -> parsePercent(a) - parsePercent(b));

        JScrollPane gamesScroll = createScroll(gamesTable);
        JScrollPane playersScroll = createScroll(playersTable);

        // ================= TOP BAR (SEARCH ONLY) =================
        // ================= TOP BAR (SEARCH ONLY) =================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

// Right side (search) - we can push it down safely
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        searchPanel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        searchBox = new JTextField(22);
        searchBox.setPreferredSize(new Dimension(260, 34)); // ⬅️ taller
        searchBox.setMinimumSize(new Dimension(260, 34));
        searchBox.setMaximumSize(new Dimension(260, 34));

        searchBox.setBackground(new Color(0, 0, 0, 180));
        searchBox.setForeground(TEXT_COLOR);
        searchBox.setCaretColor(ACCENT_COLOR);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(6, 8, 6, 8) // inner padding
        ));


        JButton searchBtn = createButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 34));
        searchBtn.setMinimumSize(new Dimension(90, 34));
        searchBtn.setMaximumSize(new Dimension(90, 34));

        searchPanel.add(label("Search:"));
        searchPanel.add(searchBox);
        searchPanel.add(searchBtn);

        topBar.add(searchPanel, BorderLayout.EAST);


        // ================= FILTER ROW =================
        difficultyFilter = createCombo(new String[]{DIFF_ALL, "EASY", "MEDIUM", "HARD"});
        resultFilter = createCombo(new String[]{RES_ALL, "WON", "LOST"});

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        filters.add(label("Difficulty:"));
        filters.add(difficultyFilter);
        filters.add(label("Result:"));
        filters.add(resultFilter);

        // ================= TABLES =================
        JPanel tables = new JPanel();
        tables.setOpaque(false);
        tables.setLayout(new BoxLayout(tables, BoxLayout.Y_AXIS));

        tables.add(Box.createVerticalStrut(50));
        tables.add(filters);
        tables.add(Box.createVerticalStrut(8));
        tables.add(gamesScroll);
        tables.add(Box.createVerticalStrut(20));
        tables.add(playersScroll);

        // ================= EXIT BUTTON =================
        IconButton exitBtn = new IconButton("/ui/icons/back.png");
        exitBtn.setPreferredSize(new Dimension(46, 46));
        exitBtn.setSafePadPx(2);
        exitBtn.setOnClick(() -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(exitBtn);

        // ================= ROOT =================
        JPanel root = new BackgroundPanel("/ui/menu/game_history_bg.png");
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        root.add(topBar, BorderLayout.NORTH);
        JPanel tablesWrapper = new JPanel(new BorderLayout());
        tablesWrapper.setOpaque(false);

// 1 cm ≈ 38 pixels
        tablesWrapper.setBorder(BorderFactory.createEmptyBorder(0, 38, 0, 38));

        tablesWrapper.add(tables, BorderLayout.CENTER);
        root.add(tablesWrapper, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        // ================= EVENTS =================
        difficultyFilter.addActionListener(e -> reload());
        resultFilter.addActionListener(e -> reload());
        searchBtn.addActionListener(e -> reload());
        searchBox.addActionListener(e -> reload());

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        reload();
    }

    // ================= HELPERS =================
    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_COLOR);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        return l;
    }

    private JComboBox<String> createCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Arial", Font.PLAIN, 14));
        return c;
    }

    private JButton createButton(String t) {
        JButton b = new JButton(t);
        b.setForeground(ACCENT_COLOR);
        b.setBackground(new Color(40, 40, 40));
        b.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        return b;
    }

    private JScrollPane createScroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.getViewport().setBackground(new Color(20, 20, 20));
        s.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255, 120)));
        return s;
    }

    private JTable createStyledTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(25);
        t.setBackground(new Color(20, 20, 20));
        t.setForeground(TEXT_COLOR);
        t.setSelectionBackground(TABLE_SELECTION_BG);
        t.setSelectionForeground(TEXT_COLOR);
        t.getTableHeader().setReorderingAllowed(false);

        JTableHeader h = t.getTableHeader();
        h.setForeground(ACCENT_COLOR);
        h.setBackground(TABLE_HEADER_BG);
        h.setFont(new Font("Arial", Font.BOLD, 14));
        h.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        h.setDefaultRenderer(new HeaderRenderer());

        DefaultTableCellRenderer c = new DefaultTableCellRenderer();
        c.setHorizontalAlignment(JLabel.CENTER);
        c.setForeground(TEXT_COLOR);
        c.setBackground(new Color(20, 20, 20));

        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(c);

        return t;
    }

    private void reload() {
        gamesModel.setRowCount(0);
        playersModel.setRowCount(0);

        List<GameHistoryRow> g =
                controller.getGameHistory(
                        (String) difficultyFilter.getSelectedItem(),
                        (String) resultFilter.getSelectedItem(),
                        searchBox.getText().trim()
                );

        for (GameHistoryRow r : g)
            gamesModel.addRow(new Object[]{
                    r.players, r.dateTime, r.difficulty, r.result,
                    r.finalScore, r.remainingLives,
                    r.correctAnswers, r.accuracy, r.duration
            });

        for (PlayerHistoryRow r :
                controller.getPlayersHistory(
                        (String) difficultyFilter.getSelectedItem(),
                        (String) resultFilter.getSelectedItem(),
                        searchBox.getText().trim()
                ))
            playersModel.addRow(new Object[]{
                    r.player, r.totalGames, r.bestScore,
                    r.averageAccuracy, r.preferredDifficulty
            });
    }

    // ================= HEADER ARROWS =================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object v, boolean s, boolean f, int r, int c) {

            JLabel l = (JLabel) super.getTableCellRendererComponent(table, v, s, f, r, c);
            l.setHorizontalAlignment(CENTER);
            l.setForeground(ACCENT_COLOR);
            l.setBackground(TABLE_HEADER_BG);

            String base = v == null ? "" : v.toString();
            String txt = base + "  ↕";

            RowSorter<?> rs = table.getRowSorter();
            if (rs != null && !rs.getSortKeys().isEmpty()) {
                RowSorter.SortKey k = rs.getSortKeys().get(0);
                if (k.getColumn() == table.convertColumnIndexToModel(c))
                    txt = base + (k.getSortOrder() == SortOrder.ASCENDING ? "  ▲" : "  ▼");
            }
            l.setText(txt);
            return l;
        }
    }

    // ================= PARSERS =================
    private static int parseInt(Object o) {
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parsePercent(Object o) {
        try {
            return Integer.parseInt(o.toString().replace("%", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseDuration(Object o) {
        try {
            String[] p = o.toString().split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    private static class BackgroundPanel extends JPanel {
        private final Image img;

        BackgroundPanel(String p) {
            URL u = getClass().getResource(p);
            img = u != null ? new ImageIcon(u).getImage() : null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
