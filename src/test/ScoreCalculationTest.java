import Controller.GameController;
import Model.Difficulty;
import Model.Game;
import Model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScoreCalculationTest {

    private GameController controller;
    private Game game;

    @BeforeEach
    void setup() {
        controller = GameController.getInstance();
        controller.startNewGame("EASY");
        game = controller.getCurrentGame();
        assertNotNull(game, "Game should be initialized");
    }

    @Test
    @DisplayName("TC-WB-SCORES-001: Verify EXPERT question reward on EASY difficulty")
    void verifyExpertQuestionRewardOnEasyDifficulty() {
        // Setup: Initialize score and lives
        game.setSharedScore(20);
        game.setSharedLives(7);

        // Input: Process a correct EXPERT question
        game.processQuestionAnswer(Game.QuestionLevel.EXPERT, true);

        // Expected Output Verification
        assertEquals(35, game.getSharedScore(), "Score should be 20 + 15 = 35");
        assertEquals(9, game.getSharedLives(), "Lives should be 7 + 2 = 9");
        assertEquals(GameState.RUNNING, game.getGameState(), "Game should remain in RUNNING state");
    }
}
