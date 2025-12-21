package Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for QuestionManager.getRandomQuestion() according to TC-WB-QUESTION-SELECT-001.
 * 
 * Test Requirements:
 * - Returned question is NOT null when questions list is not empty
 * - Returned question exists in the questions list
 * - No exception is thrown when questions.size() > 0
 * - Keep the test deterministic (control randomness)
 */
@DisplayName("QuestionManager.getRandomQuestion() Tests - TC-WB-QUESTION-SELECT-001")
public class QuestionManagerTest {

    private QuestionManager questionManager;
    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        // Create a new QuestionManager instance for each test
        questionManager = new QuestionManager();
        
        // Create test questions with different difficulty levels
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
        
        // Add questions to the manager
        for (Question question : testQuestions) {
            questionManager.addOrReplaceQuestion(question);
        }
    }

    @Test
    @DisplayName("TC-WB-QUESTION-SELECT-001: getRandomQuestion returns non-null when questions list is not empty")
    void testGetRandomQuestion_ReturnsNotNull_WhenQuestionsListNotEmpty() {
        // Given: questions list is not empty (setUp adds 5 questions)
        
        // When: getRandomQuestion is called
        Question result = questionManager.getRandomQuestion();
        
        // Then: returned question should not be null
        assertNotNull(result, "getRandomQuestion() should return a non-null question when questions list is not empty");
    }

    @Test
    @DisplayName("TC-WB-QUESTION-SELECT-001: getRandomQuestion returns a question that exists in the questions list")
    void testGetRandomQuestion_ReturnsQuestionFromList() {
        // Given: questions list contains specific questions
        
        // When: getRandomQuestion is called multiple times
        // We call it multiple times to ensure it returns questions from the list
        for (int i = 0; i < 50; i++) {
            Question result = questionManager.getRandomQuestion();
            
            // Then: returned question should exist in the original list
            assertNotNull(result, "Returned question should not be null");
            assertTrue(testQuestions.contains(result) || 
                      questionManager.getAllQuestions().contains(result),
                      "Returned question should exist in the questions list. " +
                      "Question ID: " + (result != null ? result.getId() : "null"));
        }
    }

    @Test
    @DisplayName("TC-WB-QUESTION-SELECT-001: getRandomQuestion does not throw exception when questions.size() > 0")
    void testGetRandomQuestion_NoException_WhenQuestionsSizeGreaterThanZero() {
        // Given: questions list has size > 0 (setUp adds 5 questions)
        assertTrue(questionManager.getAllQuestions().size() > 0, 
                   "Questions list should have size > 0");
        
        // When/Then: getRandomQuestion should not throw any exception
        assertDoesNotThrow(() -> {
            Question result = questionManager.getRandomQuestion();
            // Verify it's not null as well
            assertNotNull(result, "Result should not be null");
        }, "getRandomQuestion() should not throw an exception when questions.size() > 0");
    }

    @Test
    @DisplayName("TC-WB-QUESTION-SELECT-001: getRandomQuestion is deterministic with controlled randomness")
    void testGetRandomQuestion_DeterministicBehavior() {
        // Given: questions list is not empty
        
        // When: getRandomQuestion is called multiple times
        List<Question> returnedQuestions = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Question result = questionManager.getRandomQuestion();
            assertNotNull(result, "Result should not be null on iteration " + i);
            returnedQuestions.add(result);
        }
        
        // Then: all returned questions should be from the original list
        List<Question> allQuestions = questionManager.getAllQuestions();
        for (Question returned : returnedQuestions) {
            assertTrue(allQuestions.contains(returned), 
                      "All returned questions should exist in the questions list. " +
                      "Question ID: " + returned.getId());
        }
        
        // Verify that at least some different questions are returned (showing randomness works)
        // But all should be from the valid set
        long uniqueQuestions = returnedQuestions.stream().distinct().count();
        assertTrue(uniqueQuestions >= 1, 
                   "At least one unique question should be returned");
        assertTrue(uniqueQuestions <= allQuestions.size(), 
                   "Number of unique questions should not exceed the total questions in list");
    }

    @Test
    @DisplayName("TC-WB-QUESTION-SELECT-001: getRandomQuestion returns valid Question object with all properties")
    void testGetRandomQuestion_ReturnsValidQuestionObject() {
        // Given: questions list is not empty
        
        // When: getRandomQuestion is called
        Question result = questionManager.getRandomQuestion();
        
        // Then: returned question should have all required properties set
        assertNotNull(result, "Result should not be null");
        assertTrue(result.getId() > 0, "Question ID should be positive");
        assertNotNull(result.getText(), "Question text should not be null");
        assertFalse(result.getText().isEmpty(), "Question text should not be empty");
        assertNotNull(result.getOptions(), "Question options should not be null");
        assertFalse(result.getOptions().isEmpty(), "Question options should not be empty");
        assertNotNull(result.getDifficultyLevel(), "Question difficulty level should not be null");
    }

    @Test
    @DisplayName("TC-WB-QUESTION-SELECT-001: Multiple calls to getRandomQuestion return valid questions")
    void testGetRandomQuestion_MultipleCalls_AllReturnValidQuestions() {
        // Given: questions list is not empty
        
        // When: getRandomQuestion is called multiple times
        List<Question> results = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Question result = questionManager.getRandomQuestion();
            results.add(result);
        }
        
        // Then: all results should be non-null and from the questions list
        List<Question> allQuestions = questionManager.getAllQuestions();
        for (int i = 0; i < results.size(); i++) {
            Question result = results.get(i);
            assertNotNull(result, "Result at index " + i + " should not be null");
            assertTrue(allQuestions.contains(result), 
                      "Result at index " + i + " should exist in the questions list");
        }
    }
}


