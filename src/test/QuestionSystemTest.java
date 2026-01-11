import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;
import Model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black-box style test for the Question System flow.
 * <p>
 * Scenario (TC-BB-QUESTION-01):
 * - A QUESTION cell is activated during a running game.
 * - The QuestionManager would return a known question (simulated here).
 * - The player selects the CORRECT answer.
 * <p>
 * Expectations:
 * - The question is evaluated as correct (we simulate by calling processQuestionAnswer with true).
 * - Shared score increases by the configured reward for the chosen difficulty/level.
 * - Shared lives do NOT decrease (and may increase according to the reward rules).
 * - Game state remains RUNNING.
 */
public class QuestionSystemTest {

    private Game game;
    private Board board;

    @BeforeEach
    void setup() {
        // Use MEDIUM difficulty for predictable rewards:
        // Activation cost = 8, Question rewards for EASY = +8 points, +1 life
        game = new Game(Difficulty.MEDIUM);
        board = game.getBoard1();

        // Ensure we have enough score to pay activation cost
        game.setSharedScore(20);
    }

    @Test
    @DisplayName("TC-BB-QUESTION-01: QUESTION cell activated, correct answer rewards score and lives")
    void questionCellCorrectAnswerFlow() {
        // Arrange: put a QUESTION cell in a known position
        Cell questionCell = board.getCell(0, 0);
        questionCell.setContent(Cell.CellContent.QUESTION);
        questionCell.setState(Cell.CellState.HIDDEN);
        questionCell.setUsed(false);
        questionCell.setQuestionId(1); // simulated known question id

        int oldScore = game.getSharedScore();
        int oldLives = game.getSharedLives();

        int activationCost = game.getDifficulty().getActivationCost(); // 8 for MEDIUM
        int expectedQuestionRewardPoints = 8; // MEDIUM difficulty, QuestionLevel.EASY reward points
        int expectedQuestionRewardLives = 1;  // MEDIUM difficulty, QuestionLevel.EASY reward lives

        // Act: activate the QUESTION cell (deducts activation cost and reveals the cell)
        board.revealCell(0, 0);

        // Simulate the UI returning a correct answer from the question popup:
        // We call the existing reward/penalty logic directly.
        game.processQuestionAnswer(Game.QuestionLevel.EASY, true);

        // Assert: score increased by (-activationCost + 1 for reveal) + reward points
        int expectedScore =
                oldScore                         // starting score
                        - activationCost         // cost to activate question cell
                        + 1                      // +1 for revealing a safe cell
                        + expectedQuestionRewardPoints; // reward for correct answer

        int expectedLives =
                oldLives                         // starting lives
                        + expectedQuestionRewardLives; // reward gives +1 life (no decrease)

        assertEquals(expectedScore, game.getSharedScore(), "Score should increase by net reward after activation cost");
        assertEquals(expectedLives, game.getSharedLives(), "Lives should not decrease (and should gain per reward)");
        assertEquals(GameState.RUNNING, game.getGameState(), "Game should remain running after answering a question");
    }
}

