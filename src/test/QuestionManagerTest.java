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
        questionManager = new QuestionManager();

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
            assertTrue(questionManager.getAllQuestions().contains(result),
                    "Returned question should exist in the manager's questions list");
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
    @DisplayName("Multiple calls return valid questions until exhausted")
    void testGetRandomQuestion_MultipleCalls_AllReturnValidQuestions() {
        List<Question> results = new ArrayList<>();

        for (int i = 0; i < testQuestions.size(); i++) {
            Question result = questionManager.getRandomUnusedQuestionAnyLevel();
            results.add(result);
        }

        for (int i = 0; i < results.size(); i++) {
            Question result = results.get(i);
            assertNotNull(result, "Result at index " + i + " should not be null");
            assertTrue(questionManager.getAllQuestions().contains(result),
                    "Result at index " + i + " should exist in the questions list");
        }

        // After all questions are used, the next call should return null
        Question afterExhaust = questionManager.getRandomUnusedQuestionAnyLevel();
        assertNull(afterExhaust, "Should return null after all questions are used in this game");
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
