package Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for correct-answer behavior for Question cells according to TC-BB-QUESTION-001.
 * <p>
 * Tests logic only, no UI.
 * <p>
 * Acceptance Criteria:
 * - Selected answer is evaluated as CORRECT
 * - Shared score increases according to score table
 * - Shared lives do NOT decrease
 * - Game state remains RUNNING
 */
@DisplayName("Question Correct Answer Tests - TC-BB-QUESTION-001")
public class QuestionCorrectAnswerTest {

    private Game game;
    private int initialScore;
    private int initialLives;
    private GameState initialState;

    @BeforeEach
    void setUp() {
        // Each test will set up its own game with specific difficulty
    }

    /**
     * Helper method to set up game state before processing answer
     */
    private void setupGameState(Difficulty difficulty) {
        game = new Game(difficulty);
        initialScore = game.getSharedScore();
        initialLives = game.getSharedLives();
        initialState = game.getGameState();

        // Verify initial state
        assertEquals(GameState.RUNNING, initialState, "Game should start in RUNNING state");
        assertTrue(initialLives > 0, "Game should start with positive lives");
    }

    // ============================================================
    // EASY GAME DIFFICULTY TESTS
    // ============================================================

    @Test
    @DisplayName("TC-BB-QUESTION-001: EASY game, EASY question - correct answer")
    void testEasyGame_EasyQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.EASY);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EASY;
        int expectedScoreIncrease = 3;
        int expectedLifeIncrease = 1;

        // When: Process correct answer
        game.processQuestionAnswer(qLevel, true);

        // Then: Verify all acceptance criteria
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING after correct answer");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease (initial: " + initialLives +
                        ", current: " + game.getSharedLives() + ")");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: EASY game, MEDIUM question - correct answer")
    void testEasyGame_MediumQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.EASY);
        Game.QuestionLevel qLevel = Game.QuestionLevel.MEDIUM;
        int expectedScoreIncrease = 6;
        int expectedLifeIncrease = 0;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should remain unchanged");
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: EASY game, HARD question - correct answer")
    void testEasyGame_HardQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.EASY);
        Game.QuestionLevel qLevel = Game.QuestionLevel.HARD;
        int expectedScoreIncrease = 10;
        int expectedLifeIncrease = 0;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should remain unchanged");
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: EASY game, EXPERT question - correct answer")
    void testEasyGame_ExpertQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.EASY);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EXPERT;
        int expectedScoreIncrease = 15;
        int expectedLifeIncrease = 2;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    // ============================================================
    // MEDIUM GAME DIFFICULTY TESTS
    // ============================================================

    @Test
    @DisplayName("TC-BB-QUESTION-001: MEDIUM game, EASY question - correct answer")
    void testMediumGame_EasyQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.MEDIUM);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EASY;
        int expectedScoreIncrease = 8;
        int expectedLifeIncrease = 1;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: MEDIUM game, MEDIUM question - correct answer")
    void testMediumGame_MediumQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.MEDIUM);
        Game.QuestionLevel qLevel = Game.QuestionLevel.MEDIUM;
        int expectedScoreIncrease = 10;
        int expectedLifeIncrease = 1;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: MEDIUM game, HARD question - correct answer")
    void testMediumGame_HardQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.MEDIUM);
        Game.QuestionLevel qLevel = Game.QuestionLevel.HARD;
        int expectedScoreIncrease = 15;
        int expectedLifeIncrease = 1;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: MEDIUM game, EXPERT question - correct answer")
    void testMediumGame_ExpertQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.MEDIUM);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EXPERT;
        int expectedScoreIncrease = 20;
        int expectedLifeIncrease = 2;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    // ============================================================
    // HARD GAME DIFFICULTY TESTS
    // ============================================================

    @Test
    @DisplayName("TC-BB-QUESTION-001: HARD game, EASY question - correct answer")
    void testHardGame_EasyQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.HARD);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EASY;
        int expectedScoreIncrease = 10;
        int expectedLifeIncrease = 1;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: HARD game, MEDIUM question - correct answer")
    void testHardGame_MediumQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.HARD);
        Game.QuestionLevel qLevel = Game.QuestionLevel.MEDIUM;
        // Note: MEDIUM question in HARD game has random life reward (1 or 2)
        // We test that lives don't decrease and score increases correctly
        int expectedScoreIncrease = 15;
        int minExpectedLifeIncrease = 1;
        int maxExpectedLifeIncrease = 2;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        int actualLifeIncrease = game.getSharedLives() - initialLives;
        assertTrue(actualLifeIncrease >= minExpectedLifeIncrease &&
                        actualLifeIncrease <= maxExpectedLifeIncrease,
                "Lives should increase by " + minExpectedLifeIncrease +
                        " or " + maxExpectedLifeIncrease + " (actual: " + actualLifeIncrease + ")");
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: HARD game, HARD question - correct answer")
    void testHardGame_HardQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.HARD);
        Game.QuestionLevel qLevel = Game.QuestionLevel.HARD;
        int expectedScoreIncrease = 20;
        int expectedLifeIncrease = 2;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: HARD game, EXPERT question - correct answer")
    void testHardGame_ExpertQuestion_CorrectAnswer() {
        // Given
        setupGameState(Difficulty.HARD);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EXPERT;
        int expectedScoreIncrease = 40;
        int expectedLifeIncrease = 3;

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        assertEquals(initialScore + expectedScoreIncrease, game.getSharedScore(),
                "Score should increase by " + expectedScoreIncrease + " points");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease");
        assertEquals(initialLives + expectedLifeIncrease, game.getSharedLives(),
                "Lives should increase by " + expectedLifeIncrease);
    }

    // ============================================================
    // EDGE CASES AND ADDITIONAL VALIDATIONS
    // ============================================================

    @Test
    @DisplayName("TC-BB-QUESTION-001: Multiple correct answers - score accumulates")
    void testMultipleCorrectAnswers_ScoreAccumulates() {
        // Given
        setupGameState(Difficulty.EASY);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EASY;
        int expectedScorePerAnswer = 3;
        int numberOfAnswers = 3;

        // When: Process multiple correct answers
        for (int i = 0; i < numberOfAnswers; i++) {
            game.processQuestionAnswer(qLevel, true);
        }

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING after multiple answers");
        assertEquals(initialScore + (expectedScorePerAnswer * numberOfAnswers),
                game.getSharedScore(),
                "Score should accumulate correctly");
        assertTrue(game.getSharedLives() >= initialLives,
                "Lives should NOT decrease after multiple correct answers");
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: Correct answer when at max lives - score conversion")
    void testCorrectAnswer_AtMaxLives_ScoreConversion() {
        // Given
        setupGameState(Difficulty.EASY);
        // Set lives to MAX_LIVES (10)
        game.setSharedLives(10);
        initialLives = game.getSharedLives();
        initialScore = game.getSharedScore();

        Game.QuestionLevel qLevel = Game.QuestionLevel.EASY;
        int expectedScoreIncrease = 3;
        int expectedLifeReward = 1; // This should convert to score

        // When
        game.processQuestionAnswer(qLevel, true);

        // Then
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
        // Score should increase by points + converted life value
        assertTrue(game.getSharedScore() > initialScore + expectedScoreIncrease,
                "Score should increase more than base points when life is converted");
        assertEquals(initialLives, game.getSharedLives(),
                "Lives should remain at max (should NOT decrease)");
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: Correct answer evaluation - isCorrect parameter")
    void testCorrectAnswer_Evaluation_IsCorrectTrue() {
        // Given
        setupGameState(Difficulty.EASY);
        Game.QuestionLevel qLevel = Game.QuestionLevel.MEDIUM;
        int initialScoreBefore = game.getSharedScore();
        int initialLivesBefore = game.getSharedLives();

        // When: Process with isCorrect = true
        game.processQuestionAnswer(qLevel, true);

        // Then: Verify it was treated as correct (score increased, no penalty)
        assertTrue(game.getSharedScore() > initialScoreBefore,
                "Score should increase when isCorrect=true");
        assertTrue(game.getSharedLives() >= initialLivesBefore,
                "Lives should NOT decrease when isCorrect=true");
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING");
    }

    @Test
    @DisplayName("TC-BB-QUESTION-001: Correct answer - game state check before processing")
    void testCorrectAnswer_GameStateCheck() {
        // Given: Game in RUNNING state
        setupGameState(Difficulty.EASY);
        Game.QuestionLevel qLevel = Game.QuestionLevel.EASY;

        // Verify initial state
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game should be in RUNNING state initially");

        // When: Process correct answer
        game.processQuestionAnswer(qLevel, true);

        // Then: State should still be RUNNING
        assertEquals(GameState.RUNNING, game.getGameState(),
                "Game state should remain RUNNING after correct answer");
    }
}

