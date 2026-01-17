package View;

import Controller.GameController;
import Controller.GameController.GameHistoryRow;
import Controller.GameController.PlayerHistoryRow;
import util.LanguageManager;
import util.SoundToggleOverlay;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
    private JLabel lblSearch, lblDiff, lblResult;
    private JLabel lblSortHint;
    private JButton searchBtn;

    private JPanel topBar;
    private JPanel filtersPanel;
    private JTable gamesTable;
    private JTable playersTable;

    private final IconButton btnLanguage;
    private final JLabel toastLabel;
    private final Timer toastTimer;
    private static final String THINKING_ICON = "/ui/icons/thinking.png";

    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255);
    private static final Color HINT_COLOR = new Color(180, 180, 180);
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30, 255);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80, 200);

    public GameHistoryFrame(GameController controller, Runnable onExitToMenu) {
        super("Game History");

        this.controller = controller;
        this.onExitToMenu = onExitToMenu;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EXIT");
        getRootPane().getActionMap().put("EXIT", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                if (onExitToMenu != null) onExitToMenu.run();
            }
        });

        gamesModel = new DefaultTableModel(0, 9) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        playersModel = new DefaultTableModel(0, 5) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        gamesTable = createStyledTable(gamesModel);
        playersTable = createStyledTable(playersModel);

        setupSorters(gamesTable, playersTable);

        JScrollPane gamesScroll = createScroll(gamesTable);
        JScrollPane playersScroll = createScroll(playersTable);

        topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(80, 20, 10, 20));

        lblSearch = label("Search:");
        searchBox = new JTextField(15);
        styleSearchField(searchBox);
        searchBtn = createButton("Search");
        searchBtn.setPreferredSize(new Dimension(80, 34));

        topBar.add(lblSearch);
        topBar.add(searchBox);
        topBar.add(searchBtn);

        lblDiff = label("Difficulty:");
        lblResult = label("Result:");
        difficultyFilter = createCombo();
        resultFilter = createCombo();

        // Sort hint label
        lblSortHint = new JLabel();
        lblSortHint.setForeground(HINT_COLOR);
        lblSortHint.setFont(new Font("Arial", Font.ITALIC, 12));

        filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtersPanel.setOpaque(false);
        filtersPanel.add(lblDiff);
        filtersPanel.add(difficultyFilter);
        filtersPanel.add(lblResult);
        filtersPanel.add(resultFilter);

        JPanel tables = new JPanel();
        tables.setOpaque(false);
        tables.setLayout(new BoxLayout(tables, BoxLayout.Y_AXIS));

        // Hint panel (centered)
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hintPanel.setOpaque(false);
        hintPanel.add(lblSortHint);

        tables.add(Box.createVerticalStrut(20));
        tables.add(filtersPanel);
        tables.add(Box.createVerticalStrut(5));
        tables.add(hintPanel);
        tables.add(Box.createVerticalStrut(10));
        tables.add(gamesScroll);
        tables.add(Box.createVerticalStrut(20));
        tables.add(playersScroll);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        IconButton exitBtn = new IconButton("/ui/icons/back.png");
        exitBtn.setPreferredSize(new Dimension(46, 46));
        exitBtn.setSafePadPx(2);
        exitBtn.setOnClick(() -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

        btnLanguage = new IconButton("/ui/icons/language.png", true);
        btnLanguage.setPreferredSize(new Dimension(46, 46));
        btnLanguage.setOnClick(this::handleLanguageSwitch);

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottom.setOpaque(false);
        leftBottom.add(exitBtn);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottom.setOpaque(false);
        rightBottom.add(btnLanguage);

        bottomBar.add(leftBottom, BorderLayout.WEST);
        bottomBar.add(rightBottom, BorderLayout.EAST);

        JPanel root = new BackgroundPanel("/ui/menu/game_history_bg.png");
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        root.add(topBar, BorderLayout.NORTH);

        JPanel tablesWrapper = new JPanel(new BorderLayout());
        tablesWrapper.setOpaque(false);
        tablesWrapper.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        tablesWrapper.add(tables, BorderLayout.CENTER);

        root.add(tablesWrapper, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setOpaque(true);
        toastLabel.setBackground(new Color(0, 0, 0, 180));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toastLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 1));
        toastLabel.setVisible(false);

        setContentPane(root);
        SoundToggleOverlay.attach(this);

        difficultyFilter.addActionListener(e -> reload());
        resultFilter.addActionListener(e -> reload());
        searchBtn.addActionListener(e -> reload());
        searchBox.addActionListener(e -> reload());

        toastTimer = new Timer(2000, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);

        updateUIText();
        updateComboItems();
        reload();

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private void handleLanguageSwitch() {
        btnLanguage.setIconPath(THINKING_ICON);
        btnLanguage.setOnClick(null);

        new Thread(() -> {
            try {
                GameController gc = GameController.getInstance();
                if (gc.getCurrentLanguage() == LanguageManager.Language.EN) {
                    gc.setCurrentLanguage(LanguageManager.Language.HE);
                } else {
                    gc.setCurrentLanguage(LanguageManager.Language.EN);
                }
                gc.getQuestionManager().switchLanguageFromCache();
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    updateUIText();
                    updateComboItems();
                    reload();
                    showLanguageToast();
                    btnLanguage.setIconPath("/ui/icons/language.png");
                    btnLanguage.setOnClick(this::handleLanguageSwitch);
                    revalidate();
                    repaint();
                });
            }
        }).start();
    }

    private void updateUIText() {
        LanguageManager.Language lang = controller.getCurrentLanguage();
        boolean isHe = (lang == LanguageManager.Language.HE);

        setTitle(isHe ? "היסטוריית משחקים" : "Game History");
        lblSearch.setText(isHe ? "חיפוש:" : "Search:");
        lblDiff.setText(isHe ? "רמת קושי:" : "Difficulty:");
        lblResult.setText(isHe ? "תוצאה:" : "Result:");
        searchBtn.setText(isHe ? "חפש" : "Search");

        // Sort hint text - use text instead of emoji
        lblSortHint.setText(isHe ? "טיפ: ניתן ללחוץ על כותרות העמודות כדי למיין" : "Tip: Click on column headers to sort");

        FlowLayout topBarLayout = (FlowLayout) topBar.getLayout();
        topBarLayout.setAlignment(isHe ? FlowLayout.RIGHT : FlowLayout.LEFT);

        FlowLayout filtersLayout = (FlowLayout) filtersPanel.getLayout();
        filtersLayout.setAlignment(isHe ? FlowLayout.RIGHT : FlowLayout.LEFT);

        topBar.removeAll();
        if (isHe) {
            topBar.add(searchBtn);
            topBar.add(searchBox);
            topBar.add(lblSearch);
        } else {
            topBar.add(lblSearch);
            topBar.add(searchBox);
            topBar.add(searchBtn);
        }

        filtersPanel.removeAll();
        if (isHe) {
            filtersPanel.add(resultFilter);
            filtersPanel.add(lblResult);
            filtersPanel.add(difficultyFilter);
            filtersPanel.add(lblDiff);
        } else {
            filtersPanel.add(lblDiff);
            filtersPanel.add(difficultyFilter);
            filtersPanel.add(lblResult);
            filtersPanel.add(resultFilter);
        }

        ComponentOrientation orientation = isHe ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT;
        gamesTable.setComponentOrientation(orientation);
        playersTable.setComponentOrientation(orientation);

        JTableHeader gh = gamesTable.getTableHeader();
        JTableHeader ph = playersTable.getTableHeader();
        gh.setComponentOrientation(orientation);
        ph.setComponentOrientation(orientation);

        gh.resizeAndRepaint();
        ph.resizeAndRepaint();

        topBar.revalidate();
        topBar.repaint();
        filtersPanel.revalidate();
        filtersPanel.repaint();

        String[] gHeaders = isHe
                ? new String[]{"שחקנים", "תאריך / שעה", "רמה", "תוצאה", "ניקוד", "חיים", "תשובות נכונות", "דיוק", "משך זמן"}
                : new String[]{"Players", "Date / Time", "Difficulty", "Result", "Final Score", "Lives Left", "Correct Ans", "Accuracy", "Duration"};

        String[] pHeaders = isHe
                ? new String[]{"שחקן", "סה\"כ משחקים", "תוצאה טובה", "דיוק ממוצע", "רמה מועדפת"}
                : new String[]{"Player", "Total Games", "Best Score", "Avg Accuracy", "Pref Difficulty"};

        gamesModel.setColumnIdentifiers(gHeaders);
        playersModel.setColumnIdentifiers(pHeaders);
        setupSorters(gamesTable, playersTable);
    }

    private void updateComboItems() {
        int diffIdx = difficultyFilter.getSelectedIndex();
        int resIdx = resultFilter.getSelectedIndex();

        difficultyFilter.removeAllItems();
        resultFilter.removeAllItems();

        boolean isHe = (controller.getCurrentLanguage() == LanguageManager.Language.HE);

        if (isHe) {
            difficultyFilter.addItem("הכל");
            difficultyFilter.addItem("קל");
            difficultyFilter.addItem("בינוני");
            difficultyFilter.addItem("קשה");
            resultFilter.addItem("הכל");
            resultFilter.addItem("ניצחון");
            resultFilter.addItem("הפסד");
        } else {
            difficultyFilter.addItem("All");
            difficultyFilter.addItem("EASY");
            difficultyFilter.addItem("MEDIUM");
            difficultyFilter.addItem("HARD");
            resultFilter.addItem("All");
            resultFilter.addItem("WON");
            resultFilter.addItem("LOST");
        }

        if (diffIdx >= 0 && diffIdx < difficultyFilter.getItemCount()) difficultyFilter.setSelectedIndex(diffIdx);
        else difficultyFilter.setSelectedIndex(0);

        if (resIdx >= 0 && resIdx < resultFilter.getItemCount()) resultFilter.setSelectedIndex(resIdx);
        else resultFilter.setSelectedIndex(0);
    }

    private void showLanguageToast() {
        JLayeredPane lp = getLayeredPane();
        if (toastLabel.getParent() != null) lp.remove(toastLabel);

        boolean isHe = controller.getCurrentLanguage() == LanguageManager.Language.HE;
        toastLabel.setText(isHe ? "עברית" : "English");

        Dimension size = toastLabel.getPreferredSize();
        int w = size.width + 30;
        int h = 30;

        Point btnLoc = SwingUtilities.convertPoint(btnLanguage.getParent(), btnLanguage.getLocation(), lp);
        int x = btnLoc.x + (btnLanguage.getWidth() - w) / 2;
        int y = btnLoc.y - h - 10;

        toastLabel.setBounds(x, y, w, h);
        lp.add(toastLabel, JLayeredPane.POPUP_LAYER);
        toastLabel.setVisible(true);
        toastTimer.restart();
    }

    private void reload() {
        gamesModel.setRowCount(0);
        playersModel.setRowCount(0);

        String dSel = (String) difficultyFilter.getSelectedItem();
        String rSel = (String) resultFilter.getSelectedItem();
        String search = searchBox.getText().trim();

        String dKey = mapToEnglishKey(dSel);
        String rKey = mapToEnglishKey(rSel);

        boolean isHe = (controller.getCurrentLanguage() == LanguageManager.Language.HE);

        List<GameHistoryRow> g = controller.getGameHistory(dKey, rKey, search);

        for (GameHistoryRow r : g) {
            gamesModel.addRow(new Object[]{
                    r.players,
                    r.dateTime,
                    translateData(r.difficulty, isHe),
                    translateData(r.result, isHe),
                    r.finalScore,
                    r.remainingLives,
                    r.correctAnswers,
                    r.accuracy,
                    r.duration
            });
        }

        for (PlayerHistoryRow r : controller.getPlayersHistory(dKey, rKey, search)) {
            playersModel.addRow(new Object[]{
                    r.player,
                    r.totalGames,
                    r.bestScore,
                    r.averageAccuracy,
                    translateData(r.preferredDifficulty, isHe)
            });
        }

        if (gamesTable != null) {
            gamesTable.revalidate();
            gamesTable.repaint();
        }
    }

    private String mapToEnglishKey(String uiValue) {
        if (uiValue == null) return "All";
        return switch (uiValue) {
            case "הכל", "All" -> "All";
            case "קל", "EASY" -> "EASY";
            case "בינוני", "MEDIUM" -> "MEDIUM";
            case "קשה", "HARD" -> "HARD";
            case "ניצחון", "WON" -> "WON";
            case "הפסד", "LOST" -> "LOST";
            default -> "All";
        };
    }

    private String translateData(String data, boolean toHebrew) {
        if (data == null) return "";
        if (!toHebrew) return data;

        return switch (data.toUpperCase()) {
            case "EASY" -> "קל";
            case "MEDIUM" -> "בינוני";
            case "HARD" -> "קשה";
            case "WON" -> "ניצחון";
            case "LOST" -> "הפסד";
            default -> data;
        };
    }

    private void styleSearchField(JTextField box) {
        box.setPreferredSize(new Dimension(180, 34));
        box.setMinimumSize(new Dimension(180, 34));
        box.setMaximumSize(new Dimension(180, 34));
        box.setBackground(new Color(0, 0, 0, 180));
        box.setForeground(TEXT_COLOR);
        box.setCaretColor(ACCENT_COLOR);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_COLOR);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        return l;
    }

    private JComboBox<String> createCombo() {
        JComboBox<String> c = new JComboBox<>();
        c.setFont(new Font("Arial", Font.PLAIN, 14));
        c.setPreferredSize(new Dimension(100, 30));
        return c;
    }

    private JButton createButton(String t) {
        JButton b = new JButton(t);
        b.setForeground(ACCENT_COLOR);
        b.setBackground(new Color(40, 40, 40));
        b.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        h.setOpaque(false);
        h.setFont(new Font("Arial", Font.BOLD, 14));
        h.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        h.setDefaultRenderer(new HeaderRenderer());

        DefaultTableCellRenderer c = new DefaultTableCellRenderer();
        c.setHorizontalAlignment(JLabel.CENTER);
        c.setForeground(TEXT_COLOR);
        c.setBackground(new Color(20, 20, 20));

        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(c);
        }
        return t;
    }

    private void setupSorters(JTable gTable, JTable pTable) {
        TableRowSorter<DefaultTableModel> gSorter = new TableRowSorter<>(gamesModel);
        gTable.setRowSorter(gSorter);
        gSorter.setComparator(4, (a, b) -> parseInt(a) - parseInt(b));
        gSorter.setComparator(5, (a, b) -> parseInt(a) - parseInt(b));
        gSorter.setComparator(7, (a, b) -> parsePercent(a) - parsePercent(b));
        gSorter.setComparator(8, (a, b) -> parseDuration(a) - parseDuration(b));

        TableRowSorter<DefaultTableModel> pSorter = new TableRowSorter<>(playersModel);
        pTable.setRowSorter(pSorter);
        pSorter.setComparator(1, (a, b) -> parseInt(a) - parseInt(b));
        pSorter.setComparator(2, (a, b) -> parseInt(a) - parseInt(b));
        pSorter.setComparator(3, (a, b) -> parsePercent(a) - parsePercent(b));
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, v, s, f, r, c);
            l.setHorizontalAlignment(CENTER);
            l.setForeground(ACCENT_COLOR);
            l.setBackground(TABLE_HEADER_BG);
            String base = v == null ? "" : v.toString();
            String txt = base + "  ↕";
            RowSorter<?> rs = table.getRowSorter();
            if (rs != null && !rs.getSortKeys().isEmpty()) {
                RowSorter.SortKey k = rs.getSortKeys().get(0);
                if (k.getColumn() == table.convertColumnIndexToModel(c)) {
                    txt = base + (k.getSortOrder() == SortOrder.ASCENDING ? "  ▲" : "  ▼");
                }
            }
            l.setText(txt);
            return l;
        }
    }

    private static int parseInt(Object o) {
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }
    private static int parsePercent(Object o) {
        try { return Integer.parseInt(o.toString().replace("%", "")); } catch (Exception e) { return 0; }
    }
    private static int parseDuration(Object o) {
        try {
            String[] p = o.toString().split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) { return 0; }
    }

    private static class BackgroundPanel extends JPanel {
        private final Image img;
        BackgroundPanel(String p) {
            URL u = getClass().getResource(p);
            img = u != null ? new ImageIcon(u).getImage() : null;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}