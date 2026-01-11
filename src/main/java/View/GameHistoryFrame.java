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

/**
 * "Games History" + "Players History" screen.
 * Uses the specific "Scorpion Minesweeper" background image from resources.
 *
 * MVC note:
 * This frame does NOT navigate by itself. It uses a callback (Runnable) that is provided
 * by whoever opened it (MainFrame), so we don't couple View->View.
 */
public class GameHistoryFrame extends JFrame {

    private final GameController controller;
    private final Runnable onExitToMenu;   // ✅ callback to main menu

    // Models
    private final DefaultTableModel gamesModel;
    private final DefaultTableModel playersModel;

    // Filters / search
    private final JComboBox<String> difficultyFilter;
    private final JComboBox<String> resultFilter;
    private final NeonInputField searchField;

    private static final String DIFF_ALL = "All";
    private static final String RES_ALL  = "All";

    // Colors
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255); // Cyan neon

    // Semi-transparent backgrounds
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30, 240);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20, 220);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80, 200);
    private static final Color HEADER_HOVER_BG = new Color(45, 45, 45, 255);

    public GameHistoryFrame(GameController controller, Runnable onExitToMenu) {
        super("Game & Players History");
        this.controller = controller;
        this.onExitToMenu = onExitToMenu;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Set window icon (optional)
        try {
            URL iconUrl = getClass().getResource("/ui/icons/img_1.png");
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Ignore icon errors
        }

        gamesModel = new DefaultTableModel(new String[]{
                "Players", "Date / Time", "Difficulty", "Result", "Final Score",
                "Remaining Lives", "Correct Answers", "Accuracy", "Duration"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 4, 5 -> Integer.class;   // Final Score, Remaining Lives
                    default -> String.class;      // others are strings
                };
            }
        };



        playersModel = new DefaultTableModel(new String[]{
                "Player", "Total Games", "Best Score", "Average Accuracy", "Preferred Difficulty"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 1, 2 -> Integer.class;  // Total Games, Best Score
                    default -> String.class;     // Average Accuracy is "33%" or "-"
                };
            }
        };


        JTable gamesTable = createStyledTable(gamesModel);
        JTable playersTable = createStyledTable(playersModel);

// 1) enable sorting FIRST (creates the sorter)
        gamesTable.setAutoCreateRowSorter(true);
        playersTable.setAutoCreateRowSorter(true);

// 2) now sorter exists, so listeners are safe
        gamesTable.getRowSorter().addRowSorterListener(e -> gamesTable.getTableHeader().repaint());
        playersTable.getRowSorter().addRowSorterListener(e -> playersTable.getTableHeader().repaint());

// 3) now you can cast and set comparators safely
        TableRowSorter<DefaultTableModel> sorter =
                (TableRowSorter<DefaultTableModel>) gamesTable.getRowSorter();

        sorter.setComparator(6, (a, b) -> Integer.compare(parseCorrectAnswers(a), parseCorrectAnswers(b)));
        sorter.setComparator(7, (a, b) -> Integer.compare(parsePercent(a), parsePercent(b)));

// Date / Time column index = 1 ("dd/MM/yy HH:mm")
        sorter.setComparator(1, (a, b) -> {
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

// Duration column index = 8 ("mm:ss")
        sorter.setComparator(8, (a, b) -> {
            try {
                String[] pa = a.toString().split(":");
                String[] pb = b.toString().split(":");
                int sa = Integer.parseInt(pa[0]) * 60 + Integer.parseInt(pa[1]);
                int sb = Integer.parseInt(pb[0]) * 60 + Integer.parseInt(pb[1]);
                return Integer.compare(sa, sb);
            } catch (Exception e) {
                return a.toString().compareTo(b.toString());
            }
        });

        TableRowSorter<DefaultTableModel> pSorter =
                (TableRowSorter<DefaultTableModel>) playersTable.getRowSorter();

        pSorter.setComparator(3, (a, b) -> Integer.compare(parsePercent(a), parsePercent(b)));


        // ====== FILTER / SEARCH BAR (Top) ======
        JPanel filterPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        filterPanel.setBackground(new Color(0, 0, 0, 200)); // Semi-transparent black
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ✅ Exit / Back button (LEFT)
        JButton btnBack = createStyledButton("Back to Menu");
        btnBack.addActionListener(e -> {
            dispose();
            if (onExitToMenu != null) onExitToMenu.run();
        });

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backPanel.setOpaque(false);
        backPanel.add(btnBack);
        filterPanel.setPreferredSize(new Dimension(1000, 70));
        filterPanel.setMinimumSize(new Dimension(1000, 70));

        // --- FIX ENDS HERE ---

        // Left: difficulty + result combos
        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leftFilters.setOpaque(false);

        difficultyFilter = createStyledComboBox(new String[]{DIFF_ALL, "EASY", "MEDIUM", "HARD"});
        resultFilter = createStyledComboBox(new String[]{RES_ALL, "WON", "LOST"});

        JLabel diffLabel = new JLabel("Difficulty:");
        diffLabel.setForeground(TEXT_COLOR);
        diffLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel resLabel = new JLabel("Result:");
        resLabel.setForeground(TEXT_COLOR);
        resLabel.setFont(new Font("Arial", Font.BOLD, 14));

        leftFilters.add(diffLabel);
        leftFilters.add(difficultyFilter);
        leftFilters.add(resLabel);
        leftFilters.add(resultFilter);

        // Center/Right: search box + button
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(TEXT_COLOR);
        searchLbl.setFont(new Font("Arial", Font.BOLD, 14));

        searchField = new NeonInputField(ACCENT_COLOR);
        searchField.setFieldWidth(260);
        searchField.setDisplayMode(false);

// style the INNER text field (this is the real JTextField)
        searchField.textField.setText("");
        searchField.textField.setHorizontalAlignment(SwingConstants.LEFT);
        searchField.textField.setFont(new Font("Arial", Font.BOLD, 16));
        searchField.textField.setGlowColor(ACCENT_COLOR);
        searchField.textField.setCaretColor(ACCENT_COLOR);

        JButton styledSearchButton = createStyledButton("Search");
        styledSearchButton.setPreferredSize(new Dimension(110, 34));

        searchPanel.add(searchLbl, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(styledSearchButton, BorderLayout.EAST);

        // Compose left side: back button + filters
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 5));
        leftGroup.setOpaque(false);
        leftGroup.add(backPanel);
        leftGroup.add(leftFilters);

        filterPanel.add(leftGroup, BorderLayout.WEST);
        filterPanel.add(searchPanel, BorderLayout.EAST);

        // Events
        difficultyFilter.addActionListener(e -> reloadTables());
        resultFilter.addActionListener(e -> reloadTables());
        styledSearchButton.addActionListener(e -> reloadTables());
        searchField.textField.addActionListener(e -> reloadTables());

        // ====== CENTER CONTENT (Background + Tables) ======
        BackgroundPanel content = new BackgroundPanel("/ui/menu/backgroundGameHistory.png");
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        JScrollPane gamesScroll = createStyledScrollPane(gamesTable);
        JScrollPane playersScroll = createStyledScrollPane(playersTable);

        // Spacer to push tables down below title
        content.add(Box.createVerticalStrut(160));

        // Add Tables
        content.add(gamesScroll);
        content.add(Box.createVerticalStrut(30)); // Space between tables
        content.add(playersScroll);

        // ====== FRAME LAYOUT ======
        setLayout(new BorderLayout());
        add(filterPanel, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        setSize(1000, 750);
        setLocationRelativeTo(null);

        reloadTables();
    }

    // =======================
    //   STYLING HELPERS
    // =======================

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(TABLE_ROW_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(new Color(50, 50, 50));
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setReorderingAllowed(false);
        table.setOpaque(false);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.setOpaque(true);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Arial", Font.BOLD, 14));
                lbl.setForeground(ACCENT_COLOR);
                lbl.setOpaque(true);

                // Base background
                lbl.setBackground(TABLE_HEADER_BG);

                // Always show "sortable" indicator
                String text = value == null ? "" : value.toString();
                text += "  ↕";   // <-- makes sorting obvious

                // If this column is actively sorted → show direction
                RowSorter<?> sorter = table.getRowSorter();
                if (sorter != null && !sorter.getSortKeys().isEmpty()) {
                    var key = sorter.getSortKeys().get(0);
                    if (key.getColumn() == table.convertColumnIndexToModel(column)) {
                        if (key.getSortOrder() == SortOrder.ASCENDING) {
                            text = value + "  ▲";
                        } else if (key.getSortOrder() == SortOrder.DESCENDING) {
                            text = value + "  ▼";
                        }

                        // Glow underline for active column
                        lbl.setBorder(BorderFactory.createMatteBorder(
                                0, 0, 2, 0,
                                new Color(0, 255, 255, 180)
                        ));
                    } else {
                        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                    }
                }

                lbl.setText(text);
                return lbl;
            }
        });


        // Center cell content
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(TABLE_ROW_BG);
        centerRenderer.setForeground(TEXT_COLOR);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255, 100)));
        return scroll;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setBackground(Color.WHITE);
        box.setForeground(Color.BLACK);
        box.setFont(new Font("Arial", Font.PLAIN, 14));
        return box;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(ACCENT_COLOR);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 60, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(40, 40, 40));
            }
        });
        return btn;
    }

    // =======================
    //   DATA LOADING
    // =======================

    private void reloadTables() {
        gamesModel.setRowCount(0);
        playersModel.setRowCount(0);

        String diffSelected = (String) difficultyFilter.getSelectedItem();
        String resSelected = (String) resultFilter.getSelectedItem();
        String search = searchField.getText().trim();

        List<GameHistoryRow> games =
                controller.getGameHistory(diffSelected, resSelected, search);

        for (GameHistoryRow r : games) {
            gamesModel.addRow(new Object[]{
                    r.players, r.dateTime, r.difficulty, r.result, r.finalScore,
                    r.remainingLives, r.correctAnswers, r.accuracy, r.duration
            });

        }

        List<PlayerHistoryRow> players =
                controller.getPlayersHistory(diffSelected, resSelected, search);

        for (PlayerHistoryRow r : players) {
            playersModel.addRow(new Object[]{
                    r.player, r.totalGames, r.bestScore,
                    r.averageAccuracy, r.preferredDifficulty
            });
        }
    }

    // =======================
    //   BACKGROUND PANEL
    // =======================
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String resourcePath) {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            } else {
                System.err.println("ERROR: Could not find background image at: " + resourcePath);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.RED);
                g.drawString("Background not found", 10, 20);
            }
        }


    }
    // =======================
//   SORT HELPERS
// =======================
    private static int parsePercent(Object o) {
        if (o == null) return -1;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("-")) return -1;
        s = s.replace("%", "").trim();
        try { return Integer.parseInt(s); }
        catch (Exception e) { return -1; }
    }

    private static int parseCorrectAnswers(Object o) {
        if (o == null) return -1;
        String s = o.toString().trim();
        if (s.isEmpty() || s.equals("-")) return -1;
        try {
            String[] parts = s.split("/");
            return Integer.parseInt(parts[0].trim());
        } catch (Exception e) {
            return -1;
        }
    }

}
