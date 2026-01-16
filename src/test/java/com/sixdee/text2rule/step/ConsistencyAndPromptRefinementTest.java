package com.sixdee.text2rule.step;

import com.sixdee.text2rule.model.NodeData;
import com.sixdee.text2rule.model.RuleNode;
import com.sixdee.text2rule.model.RuleTree;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for ConsistencyStep and PromptRefinementStep.
 * Tests the consistency check logic and prompt refinement workflow.
 */
@DisplayName("Consistency Check and Prompt Refinement Tests")
public class ConsistencyAndPromptRefinementTest {

    @Mock
    private ChatLanguageModel mockLang4jClient;

    private ConsistencyStep consistencyStep;
    private PromptRefinementStep promptRefinementStep;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        consistencyStep = new ConsistencyStep(mockLang4jClient);
        promptRefinementStep = new PromptRefinementStep(mockLang4jClient);
    }

    // ==================== Consistency Check Tests ====================

    @Test
    @DisplayName("Should calculate high consistency score when children match original")
    public void testConsistencyCheck_HighScore() {
        // Initialize all variables before try block
        String mockResponse = null;
        String originalText = null;
        String childrenText = null;
        Double score = null;
        
        try {
            // Arrange
            mockResponse = "{\"similarity_score\": 0.95}";
            when(mockLang4jClient.generate(anyString())).thenReturn(mockResponse);

            originalText = "Run campaign on Mondays";
            childrenText = "Campaign runs on Mondays";

            // Act
            score = consistencyStep.calculateConsistencyScore(originalText, childrenText);

            // Assert
            assertNotNull(score, "Score should not be null");
            assertEquals(0.95, score, 0.01, "Score should be 0.95");
            verify(mockLang4jClient, times(1)).generate(anyString());
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should calculate low consistency score when children don't match original")
    public void testConsistencyCheck_LowScore() {
        // Initialize all variables before try block
        String mockResponse = null;
        String originalText = null;
        String childrenText = null;
        Double score = null;
        
        try {
            // Arrange
            mockResponse = "{\"similarity_score\": 0.45}";
            when(mockLang4jClient.generate(anyString())).thenReturn(mockResponse);

            originalText = "Run campaign on Mondays";
            childrenText = "Send emails on Fridays";

            // Act
            score = consistencyStep.calculateConsistencyScore(originalText, childrenText);

            // Assert
            assertNotNull(score, "Score should not be null");
            assertEquals(0.45, score, 0.01, "Score should be 0.45");
            assertTrue(score < 0.8, "Score should be below threshold");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle JSON response with markdown code blocks")
    public void testConsistencyCheck_WithMarkdownBlocks() {
        // Initialize all variables before try block
        String mockResponse = null;
        String originalText = null;
        String childrenText = null;
        Double score = null;
        
        try {
            // Arrange
            mockResponse = "```json\n{\"similarity_score\": 0.88}\n```";
            when(mockLang4jClient.generate(anyString())).thenReturn(mockResponse);

            originalText = "Test input";
            childrenText = "Test output";

            // Act
            score = consistencyStep.calculateConsistencyScore(originalText, childrenText);

            // Assert
            assertNotNull(score, "Score should not be null even with markdown");
            assertEquals(0.88, score, 0.01, "Score should be 0.88");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle malformed JSON response gracefully")
    public void testConsistencyCheck_MalformedJSON() {
        // Initialize all variables before try block
        String mockResponse = null;
        String originalText = null;
        String childrenText = null;
        Double score = null;
        
        try {
            // Arrange
            mockResponse = "This is not JSON at all";
            when(mockLang4jClient.generate(anyString())).thenReturn(mockResponse);

            originalText = "Test input";
            childrenText = "Test output";

            // Act
            score = consistencyStep.calculateConsistencyScore(originalText, childrenText);

            // Assert
            assertNull(score, "Score should be null for malformed JSON");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should set similarity score on tree root node")
    public void testCheckConsistency_SetsScoreOnTree() {
        // Initialize all variables before try block
        RuleTree<NodeData> tree = null;
        NodeData rootData = null;
        RuleNode<NodeData> root = null;
        NodeData child1Data = null;
        NodeData child2Data = null;
        String mockResponse = null;
        Double actualScore = null;
        
        try {
            // Arrange
            mockResponse = "{\"similarity_score\": 0.92}";
            when(mockLang4jClient.generate(anyString())).thenReturn(mockResponse);

            rootData = new NodeData("Original text", "grok");
            root = new RuleNode<>(rootData);
            tree = new RuleTree<>();
            tree.setRoot(root);

            child1Data = new NodeData("Child 1 text", "grok");
            child2Data = new NodeData("Child 2 text", "grok");
            root.addChild(new RuleNode<>(child1Data));
            root.addChild(new RuleNode<>(child2Data));

            // Act
            consistencyStep.checkConsistency(tree);

            // Assert
            actualScore = tree.getRoot().getData().getSimilarityScore();
            assertNotNull(actualScore, "Similarity score should be set on root");
            assertEquals(0.92, actualScore, 0.01, "Score should match LLM response");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle null tree gracefully")
    public void testCheckConsistency_NullTree() {
        try {
            // Act & Assert - should not throw exception
            consistencyStep.checkConsistency(null);
            // If we get here, test passed
            assertTrue(true, "Method should handle null gracefully");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    // ==================== Segment Consistency Tests ====================

    @Test
    @DisplayName("Should check segment consistency for Condition nodes")
    public void testSegmentConsistency_ConditionNode() {
        // Initialize all variables before try block
        RuleTree<NodeData> tree = null;
        NodeData rootData = null;
        RuleNode<NodeData> root = null;
        NodeData conditionData = null;
        RuleNode<NodeData> conditionNode = null;
        NodeData segment1Data = null;
        NodeData segment2Data = null;
        String mockResponse = null;
        Double actualScore = null;
        
        try {
            // Arrange
            mockResponse = "{\"similarity_score\": 0.87}";
            when(mockLang4jClient.generate(anyString())).thenReturn(mockResponse);

            rootData = new NodeData("Root", "grok");
            root = new RuleNode<>(rootData);
            tree = new RuleTree<>();
            tree.setRoot(root);

            conditionData = new NodeData("Condition", "grok");
            conditionData.setType("Condition");
            conditionNode = new RuleNode<>(conditionData);
            root.addChild(conditionNode);

            segment1Data = new NodeData("Segment 1", "grok");
            segment2Data = new NodeData("Segment 2", "grok");
            conditionNode.addChild(new RuleNode<>(segment1Data));
            conditionNode.addChild(new RuleNode<>(segment2Data));

            // Act
            consistencyStep.checkSegmentConsistency(tree);

            // Assert
            actualScore = conditionNode.getData().getSimilarityScore();
            assertNotNull(actualScore, "Segment consistency score should be set");
            assertEquals(0.87, actualScore, 0.01, "Score should match LLM response");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    // ==================== Prompt Refinement Tests ====================

    @Test
    @DisplayName("Should refine prompt based on feedback")
    public void testPromptRefinement_Success() {
        // Initialize all variables before try block
        String originalPrompt = null;
        String inputText = null;
        String previousOutput = null;
        String feedback = null;
        String mockRefinedPrompt = null;
        String result = null;
        
        try {
            // Arrange
            originalPrompt = "Decompose the following text into conditions";
            inputText = "Run campaign on Mondays";
            previousOutput = "Condition: Monday";
            feedback = "Missing time information";
            mockRefinedPrompt = "Decompose the following text into conditions, ensuring time details are captured";

            when(mockLang4jClient.generate(anyString())).thenReturn(mockRefinedPrompt);

            // Act
            result = promptRefinementStep.refinePrompt(originalPrompt, inputText, previousOutput, feedback);

            // Assert
            assertNotNull(result, "Refined prompt should not be null");
            assertEquals(mockRefinedPrompt, result, "Should return refined prompt");
            verify(mockLang4jClient, times(1)).generate(anyString());
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should fallback to original prompt on LLM error")
    public void testPromptRefinement_LLMError() {
        // Initialize all variables before try block
        String originalPrompt = null;
        String inputText = null;
        String previousOutput = null;
        String feedback = null;
        String result = null;
        
        try {
            // Arrange
            originalPrompt = "Original prompt";
            inputText = "Test input";
            previousOutput = "Test output";
            feedback = "Test feedback";

            when(mockLang4jClient.generate(anyString()))
                .thenThrow(new RuntimeException("LLM service error"));

            // Act
            result = promptRefinementStep.refinePrompt(originalPrompt, inputText, previousOutput, feedback);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(originalPrompt, result, "Should fallback to original prompt on error");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should fallback to original prompt when LLM returns empty")
    public void testPromptRefinement_EmptyResponse() {
        // Initialize all variables before try block
        String originalPrompt = null;
        String inputText = null;
        String previousOutput = null;
        String feedback = null;
        String result = null;
        
        try {
            // Arrange
            originalPrompt = "Original prompt";
            inputText = "Test input";
            previousOutput = "Test output";
            feedback = "Test feedback";

            when(mockLang4jClient.generate(anyString())).thenReturn("");

            // Act
            result = promptRefinementStep.refinePrompt(originalPrompt, inputText, previousOutput, feedback);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(originalPrompt, result, "Should fallback to original prompt when empty");
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle null feedback and previous output gracefully")
    public void testPromptRefinement_NullInputs() {
        // Initialize all variables before try block
        String originalPrompt = null;
        String inputText = null;
        String mockRefinedPrompt = null;
        String result = null;
        
        try {
            // Arrange
            originalPrompt = "Original prompt";
            inputText = "Test input";
            mockRefinedPrompt = "Refined prompt";

            when(mockLang4jClient.generate(anyString())).thenReturn(mockRefinedPrompt);

            // Act
            result = promptRefinementStep.refinePrompt(originalPrompt, inputText, null, null);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(mockRefinedPrompt, result, "Should handle null inputs");
            verify(mockLang4jClient, times(1)).generate(anyString());
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    // ==================== Integration Test ====================

    @Test
    @DisplayName("Integration: Consistency check triggers prompt refinement workflow")
    public void testIntegration_ConsistencyAndRefinement() {
        // Initialize all variables before try block
        RuleTree<NodeData> tree = null;
        NodeData rootData = null;
        RuleNode<NodeData> root = null;
        NodeData child1Data = null;
        String consistencyResponse = null;
        String originalPrompt = null;
        String refinedPrompt = null;
        Double score = null;
        String result = null;
        
        try {
            // Arrange
            consistencyResponse = "{\"similarity_score\": 0.65}"; // Below threshold
            when(mockLang4jClient.generate(anyString()))
                .thenReturn(consistencyResponse)
                .thenReturn("Improved prompt with better instructions");

            rootData = new NodeData("Original campaign text", "grok");
            root = new RuleNode<>(rootData);
            tree = new RuleTree<>();
            tree.setRoot(root);

            child1Data = new NodeData("Incomplete decomposition", "grok");
            root.addChild(new RuleNode<>(child1Data));

            // Act - Step 1: Check consistency
            consistencyStep.checkConsistency(tree);
            score = tree.getRoot().getData().getSimilarityScore();

            // Assert consistency check
            assertNotNull(score, "Score should be set");
            assertEquals(0.65, score, 0.01, "Score should be 0.65");
            assertTrue(score < 0.8, "Score should be below threshold, triggering refinement");

            // Act - Step 2: Refine prompt if score is low
            if (score < 0.8) {
                originalPrompt = "Decompose the text";
                refinedPrompt = promptRefinementStep.refinePrompt(
                    originalPrompt,
                    rootData.getInput(),
                    child1Data.getInput(),
                    "Consistency score below threshold: " + score
                );

                // Assert prompt refinement
                assertNotNull(refinedPrompt, "Refined prompt should not be null");
                assertNotEquals(originalPrompt, refinedPrompt, "Prompt should be refined");
                result = refinedPrompt;
            }

            // Verify both services were called
            verify(mockLang4jClient, times(2)).generate(anyString());
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
