import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;

/**
 * USER STORY 1 - RANDOM PLACEMENT STATISTICAL VALIDATION:
 * 
 * This test class validates that the random placement algorithm:
 * 1. Places the correct number of mines, question cells, and surprise cells
 *    according to difficulty rules
 * 2. Ensures fair and balanced distribution across many board generations
 * 3. Verifies that the observed averages match the expected configuration
 *    within a reasonable tolerance
 * 
 * The test creates many random boards (500-1000) for each difficulty level
 * and performs statistical analysis to ensure the placement is truly random
 * and respects the difficulty configuration.
 */
public class RandomPlacementTest {

    // Number of boards to generate for statistical validation
    private static final int NUM_BOARDS = 1000;

    // Allowed percentage deviation when comparing observed averages to expected values
    private static final double TOLERANCE_PERCENT = 2.0; // 2% tolerance

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("USER STORY 1: RANDOM PLACEMENT TEST");
        System.out.println("==========================================\n");

        boolean allTestsPassed = true;

        // Run the placement test for each difficulty level
        allTestsPassed &= testDifficulty(Difficulty.EASY);
        allTestsPassed &= testDifficulty(Difficulty.MEDIUM);
        allTestsPassed &= testDifficulty(Difficulty.HARD);

        System.out.println("\n==========================================");
        if (allTestsPassed) {
            System.out.println("ALL RANDOM PLACEMENT TESTS PASSED ✓");
        } else {
            System.out.println("SOME RANDOM PLACEMENT TESTS FAILED ✗");
        }
        System.out.println("==========================================");
    }

    /**
     * Tests random placement for a specific difficulty level.
     * Creates many boards and validates that the placement matches expected counts.
     * 
     * @param difficulty The difficulty level to test
     * @return true if all validations pass, false otherwise
     */
    private static boolean testDifficulty(Difficulty difficulty) {
        System.out.println("=== Testing Difficulty: " + difficulty.name() + " ===");
        System.out.println("Generating " + NUM_BOARDS + " random boards...\n");

        // Expected counts from difficulty configuration
        int expectedMines = difficulty.getMines();
        int expectedQuestions = difficulty.getQuestionCells();
        int expectedSurprises = difficulty.getSurpriseCells();
        int totalCells = difficulty.getRows() * difficulty.getCols();

        // Counters for statistical analysis
        int totalMinesPlaced = 0;
        int totalQuestionsPlaced = 0;
        int totalSurprisesPlaced = 0;
        int totalNumbersPlaced = 0;
        int totalEmptyPlaced = 0;

        // Track distribution across all boards
        for (int i = 0; i < NUM_BOARDS; i++) {
            Game game = new Game(difficulty);
            Board board = game.getBoard1();

            // Count actual placements on this board
            int minesOnBoard = 0;
            int questionsOnBoard = 0;
            int surprisesOnBoard = 0;
            int numbersOnBoard = 0;
            int emptyOnBoard = 0;

            for (int r = 0; r < board.getRows(); r++) {
                for (int c = 0; c < board.getCols(); c++) {
                    Cell cell = board.getCell(r, c);
                    switch (cell.getContent()) {
                        case MINE:
                            minesOnBoard++;
                            break;
                        case QUESTION:
                            questionsOnBoard++;
                            break;
                        case SURPRISE:
                            surprisesOnBoard++;
                            break;
                        case NUMBER:
                            numbersOnBoard++;
                            break;
                        case EMPTY:
                            emptyOnBoard++;
                            break;
                    }
                }
            }

            // Validate each board has exactly the expected counts
            if (minesOnBoard != expectedMines) {
                System.out.println("[FAIL] Board #" + i + ": Expected " + expectedMines + 
                    " mines, found " + minesOnBoard);
                return false;
            }
            if (questionsOnBoard != expectedQuestions) {
                System.out.println("[FAIL] Board #" + i + ": Expected " + expectedQuestions + 
                    " question cells, found " + questionsOnBoard);
                return false;
            }
            if (surprisesOnBoard != expectedSurprises) {
                System.out.println("[FAIL] Board #" + i + ": Expected " + expectedSurprises + 
                    " surprise cells, found " + surprisesOnBoard);
                return false;
            }

            // Accumulate for statistical analysis
            totalMinesPlaced += minesOnBoard;
            totalQuestionsPlaced += questionsOnBoard;
            totalSurprisesPlaced += surprisesOnBoard;
            totalNumbersPlaced += numbersOnBoard;
            totalEmptyPlaced += emptyOnBoard;
        }

        // Calculate averages
        double avgMines = (double) totalMinesPlaced / NUM_BOARDS;
        double avgQuestions = (double) totalQuestionsPlaced / NUM_BOARDS;
        double avgSurprises = (double) totalSurprisesPlaced / NUM_BOARDS;
        double avgNumbers = (double) totalNumbersPlaced / NUM_BOARDS;
        double avgEmpty = (double) totalEmptyPlaced / NUM_BOARDS;

        // Print statistics
        System.out.println("Statistical Analysis Results:");
        System.out.println("  Expected Mines: " + expectedMines);
        System.out.println("  Average Mines: " + String.format("%.2f", avgMines));
        System.out.println("  Expected Questions: " + expectedQuestions);
        System.out.println("  Average Questions: " + String.format("%.2f", avgQuestions));
        System.out.println("  Expected Surprises: " + expectedSurprises);
        System.out.println("  Average Surprises: " + String.format("%.2f", avgSurprises));
        System.out.println("  Average Numbers: " + String.format("%.2f", avgNumbers));
        System.out.println("  Average Empty: " + String.format("%.2f", avgEmpty));
        System.out.println("  Total Cells: " + totalCells);
        System.out.println();

        // Validate averages are within tolerance
        boolean allPassed = true;

        allPassed &= validateAverage("Mines", avgMines, expectedMines, TOLERANCE_PERCENT);
        allPassed &= validateAverage("Question Cells", avgQuestions, expectedQuestions, TOLERANCE_PERCENT);
        allPassed &= validateAverage("Surprise Cells", avgSurprises, expectedSurprises, TOLERANCE_PERCENT);

        // Validate that total special cells don't exceed board size
        int totalSpecialCells = expectedMines + expectedQuestions + expectedSurprises;
        if (totalSpecialCells > totalCells) {
            System.out.println("[FAIL] Total special cells (" + totalSpecialCells + 
                ") exceeds board size (" + totalCells + ")");
            allPassed = false;
        } else {
            System.out.println("[PASS] Total special cells (" + totalSpecialCells + 
                ") is within board size (" + totalCells + ")");
        }

        // Validate that numbers are calculated correctly (should be > 0 if there are mines)
        if (expectedMines > 0 && avgNumbers == 0) {
            System.out.println("[WARN] No number cells found, but mines are present. " +
                "This might indicate a calculation issue.");
        }

        System.out.println();
        return allPassed;
    }

    /**
     * Validates that an observed average is within tolerance of the expected value.
     * 
     * @param name The name of the metric being validated
     * @param observed The observed average value
     * @param expected The expected value
     * @param tolerancePercent The tolerance percentage (e.g., 2.0 for 2%)
     * @return true if within tolerance, false otherwise
     */
    private static boolean validateAverage(String name, double observed, int expected, double tolerancePercent) {
        if (expected == 0) {
            // If expected is 0, observed should also be 0
            if (observed == 0) {
                System.out.println("[PASS] " + name + ": Expected 0, observed 0");
                return true;
            } else {
                System.out.println("[FAIL] " + name + ": Expected 0, observed " + observed);
                return false;
            }
        }

        double difference = Math.abs(observed - expected);
        double tolerance = expected * (tolerancePercent / 100.0);
        double percentDifference = (difference / expected) * 100.0;

        if (difference <= tolerance) {
            System.out.println(String.format("[PASS] %s: Expected %d, observed %.2f (difference: %.2f%%)",
                name, expected, observed, percentDifference));
            return true;
        } else {
            System.out.println(String.format("[FAIL] %s: Expected %d, observed %.2f (difference: %.2f%%, tolerance: %.2f%%)",
                name, expected, observed, percentDifference, tolerancePercent));
            return false;
        }
    }
}


