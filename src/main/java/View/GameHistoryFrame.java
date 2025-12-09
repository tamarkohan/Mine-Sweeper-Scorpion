package View;

import Controller.GameController;
import Controller.GameController.GameHistoryRow;
import Controller.GameController.PlayerHistoryRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * "Games History" + "Players History" screen.
 * Simple basic Swing UI, no custom styling.
 */
public class GameHistoryFrame extends JFrame {

    private final GameController controller;

    // Tables + models
    private final DefaultTableModel gamesModel;
    private final DefaultTableModel playersModel;
    private final JTable gamesTable;
    private final JTable playersTable;

    // Filters / search
    private final JComboBox<String> difficultyFilter;
    private final JComboBox<String> resultFilter;
    private final JTextField searchField;

    private static final String DIFF_ALL = "All";
    private static final String RES_ALL  = "All";

    public GameHistoryFrame(GameController controller) {
        super("Game & Players History");
        this.controller = controller;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ====== TABLE MODELS ======
        gamesModel = new DefaultTableModel(new String[]{
                "Players",
                "Date / Time",
                "Difficulty",
                "Final Score",
                "Remaining Lives",
                "Correct Answers",
                "Accuracy",
                "Duration"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        playersModel = new DefaultTableModel(new String[]{
                "Player",
                "Total Games",
                "Best Score",
                "Average Accuracy",
                "Preferred Difficulty"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gamesTable = new JTable(gamesModel);
        playersTable = new JTable(playersModel);

        // ====== FILTER / SEARCH BAR (top area) ======
        JPanel filterPanel = new JPanel(new BorderLayout());

        // left: difficulty + result combos
        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        difficultyFilter = new JComboBox<>(new String[]{DIFF_ALL, "EASY", "MEDIUM", "HARD"});
        resultFilter = new JComboBox<>(new String[]{RES_ALL, "WON", "LOST"});

        JLabel diffLabel = new JLabel("Difficulty:");
        JLabel resLabel = new JLabel("Result:");

        leftFilters.add(diffLabel);
        leftFilters.add(difficultyFilter);
        leftFilters.add(resLabel);
        leftFilters.add(resultFilter);

        // center/right: search box + button
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));

        searchField = new JTextField();
        searchField.setToolTipText("Search player name...");

        JButton searchButton = new JButton("Search");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        filterPanel.add(leftFilters, BorderLayout.WEST);
        filterPanel.add(searchPanel, BorderLayout.CENTER);

        // events
        difficultyFilter.addActionListener(e -> reloadTables());
        resultFilter.addActionListener(e -> reloadTables());
        searchButton.addActionListener(e -> reloadTables());
        searchField.addActionListener(e -> reloadTables()); // Enter key

        // ====== CENTER CONTENT (titles + tables) ======
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel gamesTitle = new JLabel("Games History", SwingConstants.CENTER);
        JLabel playersTitle = new JLabel("Players History", SwingConstants.CENTER);

        // center titles inside boxlayout
        gamesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        playersTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane gamesScroll = new JScrollPane(gamesTable);
        JScrollPane playersScroll = new JScrollPane(playersTable);

        content.add(Box.createVerticalStrut(5));
        content.add(gamesTitle);
        content.add(Box.createVerticalStrut(5));
        content.add(gamesScroll);
        content.add(Box.createVerticalStrut(10));
        content.add(playersTitle);
        content.add(Box.createVerticalStrut(5));
        content.add(playersScroll);
        content.add(Box.createVerticalStrut(5));

        // ====== FRAME LAYOUT ======
        setLayout(new BorderLayout(5, 5));
        add(filterPanel, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        setSize(900, 600);
        setLocationRelativeTo(null);

        reloadTables();
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

        // ---- GAMES TABLE ----
        List<GameHistoryRow> games =
                controller.getGameHistory(diffSelected, resSelected, search);

        for (GameHistoryRow r : games) {
            gamesModel.addRow(new Object[]{
                    r.players,
                    r.dateTime,
                    r.difficulty,
                    r.finalScore,
                    r.remainingLives,
                    r.correctAnswers,
                    r.accuracy,
                    r.duration
            });
        }

        // ---- PLAYERS TABLE ----
        List<PlayerHistoryRow> players =
                controller.getPlayersHistory(diffSelected, resSelected, search);

        for (PlayerHistoryRow r : players) {
            playersModel.addRow(new Object[]{
                    r.player,
                    r.totalGames,
                    r.bestScore,
                    r.averageAccuracy,
                    r.preferredDifficulty
            });
        }
    }
}
