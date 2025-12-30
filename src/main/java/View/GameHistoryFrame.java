package View;

import Controller.GameController;
import Controller.GameController.GameHistoryRow;
import Controller.GameController.PlayerHistoryRow;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URL;
import java.util.List;

/**
 * "Games History" + "Players History" screen.
 * Uses the specific "Scorpion Minesweeper" background image from resources.
 */
public class GameHistoryFrame extends JFrame {

    private final GameController controller;

    // Models need to be fields so we can update them in reloadTables()
    private final DefaultTableModel gamesModel;
    private final DefaultTableModel playersModel;

    // Filters / search
    private final JComboBox<String> difficultyFilter;
    private final JComboBox<String> resultFilter;
    private final GlowTextField searchField;

    private static final String DIFF_ALL = "All";
    private static final String RES_ALL  = "All";

    // Colors
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 255, 255); // Cyan neon

    // Semi-transparent backgrounds
    private static final Color TABLE_HEADER_BG = new Color(30, 30, 30, 240);
    private static final Color TABLE_ROW_BG = new Color(20, 20, 20, 220);
    private static final Color TABLE_SELECTION_BG = new Color(60, 60, 80, 200);

    public GameHistoryFrame(GameController controller) {
        super("Game & Players History");
        this.controller = controller;
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

        // ====== TABLE MODELS ======
        gamesModel = new DefaultTableModel(new String[]{
                "Players", "Date / Time", "Difficulty", "Final Score",
                "Remaining Lives", "Correct Answers", "Accuracy", "Duration"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        playersModel = new DefaultTableModel(new String[]{
                "Player", "Total Games", "Best Score", "Average Accuracy", "Preferred Difficulty"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable gamesTable = createStyledTable(gamesModel);
        JTable playersTable = createStyledTable(playersModel);

        // ====== FILTER / SEARCH BAR (Top) ======
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBackground(new Color(0, 0, 0, 200));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        searchField = new GlowTextField(15);
        JButton styledSearchButton = createStyledButton("Search");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(styledSearchButton, BorderLayout.EAST);

        filterPanel.add(leftFilters, BorderLayout.WEST);
        filterPanel.add(searchPanel, BorderLayout.EAST);

        // Events
        difficultyFilter.addActionListener(e -> reloadTables());
        resultFilter.addActionListener(e -> reloadTables());
        styledSearchButton.addActionListener(e -> reloadTables());
        searchField.addActionListener(e -> reloadTables());

        // ====== CENTER CONTENT (Background + Tables) ======

        // This path must match exactly where your image is in the resources folder
        // For example: src/main/resources/ui/menu/bg.png -> "/ui/menu/bg.png"
        BackgroundPanel content = new BackgroundPanel("/ui/menu/backgroundGameHistory.png");
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30)); // Added side padding

        JScrollPane gamesScroll = createStyledScrollPane(gamesTable);
        JScrollPane playersScroll = createStyledScrollPane(playersTable);

        // --- VERTICAL SPACER ADJUSTMENT ---
        // Changed from 130 to 160 to push the table down below the title
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
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(ACCENT_COLOR);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

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
                    r.players, r.dateTime, r.difficulty, r.finalScore,
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
            // Load from resources (classpath)
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

                // Debug text
                g.setColor(Color.RED);
                g.drawString("Background not found", 10, 20);
            }
        }
    }
}