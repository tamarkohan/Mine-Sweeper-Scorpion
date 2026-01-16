package Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuestionManager.getRandomUnusedQuestionAnyLevel() Tests - TC-WB-QUESTION-SELECT-001")
public class QuestionManagerTest {

    private QuestionManager questionManager;
    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        // 1. Use Singleton instance (constructor is private)
        questionManager = QuestionManager.getInstance();

        // 2. Disable saving to avoid overwriting your real CSV files
        questionManager.setPersistenceEnabled(false);

        // 3. Clear any old data to ensure a clean test state
        questionManager.clearQuestionsForTesting();

        testQuestions = new ArrayList<>();
        testQuestions.add(new Question(1, "What is 2+2?",
                Arrays.asList("3", "4", "5", "6"), 'B', "EASY"));
        testQuestions.add(new Question(2, "What is the capital of France?",
                Arrays.asList("London", "Paris", "Berlin", "Madrid"), 'B', "MEDIUM"));
        testQuestions.add(new Question(3, "What is the square root of 16?",
                Arrays.asList("2", "4", "6", "8"), 'B', "HARD"));
        testQuestions.add(new Question(4, "What is 10*10?",
                Arrays.asList("90", "100", "110", "120"), 'B', "EASY"));
        testQuestions.add(new Question(5, "What is the largest planet?",
                Arrays.asList("Earth", "Jupiter", "Saturn", "Neptune"), 'B', "EXPERT"));

        for (Question question : testQuestions) {
            questionManager.addOrReplaceQuestion(question);
        }

        // Ensure unused-tracking starts clean for each test
        questionManager.resetForNewGame();
    }

    @Test
    @DisplayName("Returns non-null when questions list is not empty")
    void testGetRandomQuestion_ReturnsNotNull_WhenQuestionsListNotEmpty() {
        Question result = questionManager.getRandomUnusedQuestionAnyLevel();
        assertNotNull(result, "Should return a non-null question when list is not empty");
    }

    @Test
    @DisplayName("Returned question exists in the questions list")
    void testGetRandomQuestion_ReturnsQuestionFromList() {
        for (int i = 0; i < testQuestions.size(); i++) {
            Question result = questionManager.getRandomUnusedQuestionAnyLevel();
            assertNotNull(result, "Returned question should not be null");

            // We check by ID to ensure it's the same question logically
            boolean exists = questionManager.getAllQuestions().stream()
                    .anyMatch(q -> q.getId() == result.getId());

            assertTrue(exists, "Returned question should exist in the manager's questions list");
        }
    }

    @Test
    @DisplayName("Does not throw exception when questions.size() > 0")
    void testGetRandomQuestion_NoException_WhenQuestionsSizeGreaterThanZero() {
        assertTrue(questionManager.getAllQuestions().size() > 0,
                "Questions list should have size > 0");

        assertDoesNotThrow(() -> {
            Question result = questionManager.getRandomUnusedQuestionAnyLevel();
            assertNotNull(result, "Result should not be null");
        });
    }

    @Test
    @DisplayName("Recycles questions after all are used (Game Loop behavior)")
    void testGetRandomQuestion_Recycles_AfterExhaustion() {
        List<Question> results = new ArrayList<>();

        // 1. Consume all available questions
        for (int i = 0; i < testQuestions.size(); i++) {
            Question result = questionManager.getRandomUnusedQuestionAnyLevel();
            assertNotNull(result, "Should return question " + i);
            results.add(result);
        }

        // 2. The next call should NOT return null.
        // The QuestionManager is designed to reset (recycle) the pool if empty.
        Question recycleResult = questionManager.getRandomUnusedQuestionAnyLevel();

        assertNotNull(recycleResult, "Should recycle questions instead of returning null");
        assertTrue(questionManager.getAllQuestions().stream().anyMatch(q -> q.getId() == recycleResult.getId()),
                "Recycled question should be one of the original questions");
    }

    @Test
    @DisplayName("Returns valid Question object with required properties")
    void testGetRandomQuestion_ReturnsValidQuestionObject() {
        Question result = questionManager.getRandomUnusedQuestionAnyLevel();

        assertNotNull(result, "Result should not be null");
        assertTrue(result.getId() > 0, "Question ID should be positive");
        assertNotNull(result.getText(), "Question text should not be null");
        assertFalse(result.getText().isEmpty(), "Question text should not be empty");
        assertNotNull(result.getOptions(), "Question options should not be null");
        assertFalse(result.getOptions().isEmpty(), "Question options should not be empty");
        assertNotNull(result.getDifficultyLevel(), "Question difficulty level should not be null");
    }
}