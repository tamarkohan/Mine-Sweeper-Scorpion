import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that QUESTION cells are marked as USED after answering
 * and cannot be triggered again.
 * Test ID: TC-BB-QUESTION-01 (extended)
 */
public class QuestionCellUsedAfterAnswerTest {

    private Game game;
    private Board board;
    private QuestionManager questionManager;
    private StubQuestionPresenter questionPresenter;
    private Cell questionCell;

    @BeforeEach
    void setup() {
        // Initialize game
        game = new Game(Difficulty.MEDIUM);
        board = game.getBoard1();

        // --- FIXED SETUP ---
        questionManager = QuestionManager.getInstance();

        // 1. Disable saving to avoid overwriting your real CSV files
        questionManager.setPersistenceEnabled(false);

        // 2. Clear singleton state correctly
        questionManager.clearQuestionsForTesting();

        Question testQuestion = new Question(
                1,
                "What is 2+2?",
                Arrays.asList("3", "4", "5", "6"),
                'B',
                "EASY"
        );

        // Add to memory only (since persistence is false)
        questionManager.addOrReplaceQuestion(testQuestion);
        game.setQuestionManager(questionManager);

        // Stub presenter
        questionPresenter = new StubQuestionPresenter();
        game.setQuestionPresenter(questionPresenter);

        // Ensure score for activation
        game.setSharedScore(20);

        // Setup Board Cell
        questionCell = board.getCell(0, 0);
        questionCell.setContent(Cell.CellContent.QUESTION);
        questionCell.setState(Cell.CellState.HIDDEN);
        questionCell.setUsed(false);
    }

    @Test
    @DisplayName("QUESTION cell marked as used after correct answer")
    void questionCellUsedAfterCorrectAnswer() {
        assertFalse(questionCell.isUsed(), "Initially unused");

        board.revealCell(0, 0);

        questionPresenter.setNextAnswer(true); // Correct
        boolean success = board.activateSpecialCell(0, 0);

        assertTrue(success, "Activation succeeded");
        assertTrue(questionCell.isUsed(), "Cell is now used");

        // Try again
        boolean second = board.activateSpecialCell(0, 0);
        assertFalse(second, "Second activation blocked");
    }

    @Test
    @DisplayName("QUESTION cell marked as used after wrong answer")
    void questionCellUsedAfterWrongAnswer() {
        board.revealCell(0, 0);

        questionPresenter.setNextAnswer(false); // Wrong
        boolean success = board.activateSpecialCell(0, 0);

        assertTrue(success, "Activation succeeded (penalty applied internally)");
        assertTrue(questionCell.isUsed(), "Cell is used even if wrong");

        assertFalse(board.activateSpecialCell(0, 0), "Cannot reactivate");
    }

    @Test
    @DisplayName("Multiple QUESTION cells work independently")
    void multipleQuestionCellsIndependent() {
        Cell cell2 = board.getCell(0, 1);
        cell2.setContent(Cell.CellContent.QUESTION);
        cell2.setState(Cell.CellState.HIDDEN);

        // Use Cell 1
        board.revealCell(0, 0);
        questionPresenter.setNextAnswer(true);
        board.activateSpecialCell(0, 0);

        // Use Cell 2
        board.revealCell(0, 1);
        board.activateSpecialCell(0, 1);

        assertTrue(questionCell.isUsed());
        assertTrue(cell2.isUsed());
    }

    /**
     * Stub for testing UI interactions without GUI
     */
    private static class StubQuestionPresenter implements Game.QuestionPresenter {
        private QuestionResult nextAnswer = QuestionResult.CORRECT;

        public void setNextAnswer(boolean correct) {
            this.nextAnswer = correct ? QuestionResult.CORRECT : QuestionResult.WRONG;
        }

        @Override
        public QuestionResult presentQuestion(Question question) {
            return nextAnswer;
        }
    }
}