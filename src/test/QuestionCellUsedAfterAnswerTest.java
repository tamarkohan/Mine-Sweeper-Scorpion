import Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that QUESTION cells are marked as USED after answering
 * and cannot be triggered again.
 * <p>
 * Test ID: TC-BB-QUESTION-01 (extended)
 * Branch: feature/question-cell-used-after-answer
 */
public class QuestionCellUsedAfterAnswerTest {

    private Game game;
    private Board board;
    private QuestionManager questionManager;
    private StubQuestionPresenter questionPresenter;
    private Cell questionCell;

    @BeforeEach
    void setup() {
        // Initialize game with MEDIUM difficulty
        game = new Game(Difficulty.MEDIUM);
        board = game.getBoard1();

        // Set up QuestionManager with a test question
        questionManager = new QuestionManager();
        Question testQuestion = new Question(
                1,
                "What is 2+2?",
                Arrays.asList("3", "4", "5", "6"),
                'B',
                "EASY"
        );
        // Manually add question to manager (bypassing CSV)
        questionManager.addOrReplaceQuestion(testQuestion);
        game.setQuestionManager(questionManager);

        // Set up stub presenter that simulates user answering
        questionPresenter = new StubQuestionPresenter();
        game.setQuestionPresenter(questionPresenter);

        // Ensure we have enough score to pay activation cost (8 for MEDIUM)
        game.setSharedScore(20);

        // Set up a QUESTION cell at a known position
        questionCell = board.getCell(0, 0);
        questionCell.setContent(Cell.CellContent.QUESTION);
        questionCell.setState(Cell.CellState.HIDDEN);
        questionCell.setUsed(false);
    }

    @Test
    @DisplayName("QUESTION cell marked as used after correct answer and cannot be reactivated")
    void questionCellUsedAfterCorrectAnswer() {
        // Verify initial state
        assertFalse(questionCell.isUsed(), "Cell should not be used initially");
        assertFalse(questionCell.isRevealed(), "Cell should be hidden initially");

        // Step 1: Reveal the QUESTION cell
        board.revealCell(0, 0);
        assertTrue(questionCell.isRevealed(), "Cell should be revealed");
        assertFalse(questionCell.isUsed(), "Cell should not be used yet (only revealed)");

        // Step 2: Activate the QUESTION cell (simulates user clicking on revealed Q cell)
        questionPresenter.setNextAnswer(true); // Simulate correct answer
        boolean firstActivation = board.activateSpecialCell(0, 0);

        assertTrue(firstActivation, "First activation should succeed");
        assertTrue(questionCell.isUsed(), "Cell should be marked as used after activation");

        // Step 3: Try to activate the same cell again
        questionPresenter.setNextAnswer(true); // Set answer (but shouldn't be called)
        boolean secondActivation = board.activateSpecialCell(0, 0);

        assertFalse(secondActivation, "Second activation should fail (cell is already used)");
        assertTrue(questionCell.isUsed(), "Cell should remain used");

        // Verify game state is still running
        assertEquals(GameState.RUNNING, game.getGameState(), "Game should remain running");
    }

    @Test
    @DisplayName("QUESTION cell marked as used after wrong answer and cannot be reactivated")
    void questionCellUsedAfterWrongAnswer() {
        // Verify initial state
        assertFalse(questionCell.isUsed(), "Cell should not be used initially");

        // Step 1: Reveal the QUESTION cell
        board.revealCell(0, 0);
        assertTrue(questionCell.isRevealed(), "Cell should be revealed");

        // Step 2: Activate with wrong answer
        questionPresenter.setNextAnswer(false); // Simulate wrong answer
        boolean firstActivation = board.activateSpecialCell(0, 0);

        assertTrue(firstActivation, "First activation should succeed even with wrong answer");
        assertTrue(questionCell.isUsed(), "Cell should be marked as used after activation (even if wrong)");

        // Step 3: Try to activate again
        boolean secondActivation = board.activateSpecialCell(0, 0);

        assertFalse(secondActivation, "Second activation should fail (cell is already used)");
        assertTrue(questionCell.isUsed(), "Cell should remain used");

        // Verify game state is still running
        assertEquals(GameState.RUNNING, game.getGameState(), "Game should remain running");
    }

    @Test
    @DisplayName("Multiple QUESTION cells can be activated independently")
    void multipleQuestionCellsIndependent() {
        // Set up second QUESTION cell
        Cell questionCell2 = board.getCell(0, 1);
        questionCell2.setContent(Cell.CellContent.QUESTION);
        questionCell2.setState(Cell.CellState.HIDDEN);
        questionCell2.setUsed(false);

        // Activate first cell
        board.revealCell(0, 0);
        questionPresenter.setNextAnswer(true);
        boolean activation1 = board.activateSpecialCell(0, 0);
        assertTrue(activation1, "First cell activation should succeed");
        assertTrue(questionCell.isUsed(), "First cell should be used");

        // Activate second cell (should work independently)
        board.revealCell(0, 1);
        questionPresenter.setNextAnswer(true);
        boolean activation2 = board.activateSpecialCell(0, 1);
        assertTrue(activation2, "Second cell activation should succeed");
        assertTrue(questionCell2.isUsed(), "Second cell should be used");

        // First cell should still be used and not reactivatable
        boolean reactivation1 = board.activateSpecialCell(0, 0);
        assertFalse(reactivation1, "First cell should not be reactivatable");
    }

    @Test
    @DisplayName("Game remains stable when trying to activate used QUESTION cell")
    void gameStableAfterUsedCellActivation() {
        int initialScore = game.getSharedScore();
        int initialLives = game.getSharedLives();

        // Activate QUESTION cell once
        board.revealCell(0, 0);
        questionPresenter.setNextAnswer(true);
        board.activateSpecialCell(0, 0);

        int scoreAfterFirst = game.getSharedScore();
        int livesAfterFirst = game.getSharedLives();

        // Try to activate again (should fail silently)
        boolean secondAttempt = board.activateSpecialCell(0, 0);
        assertFalse(secondAttempt, "Second activation should fail");

        // Verify game state unchanged
        assertEquals(scoreAfterFirst, game.getSharedScore(),
                "Score should not change on failed reactivation");
        assertEquals(livesAfterFirst, game.getSharedLives(),
                "Lives should not change on failed reactivation");
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");

        // Verify no exceptions were thrown
        assertDoesNotThrow(() -> board.activateSpecialCell(0, 0),
                "Should not throw exception when activating used cell");
    }

    /**
     * Stub implementation of QuestionPresenter for testing without UI.
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

