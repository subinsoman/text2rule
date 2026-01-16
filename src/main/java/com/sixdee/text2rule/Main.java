package com.sixdee.text2rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixdee.text2rule.workflow.AgenticConversionWorkflow;
import com.sixdee.text2rule.workflow.DecompositionWorkflow;
import com.sixdee.text2rule.workflow.WorkflowState;
import com.sixdee.text2rule.model.RuleTree;
import com.sixdee.text2rule.model.NodeData;
import com.sixdee.text2rule.view.TreeRenderer;
import com.sixdee.text2rule.view.AsciiRenderer;
import com.sixdee.text2rule.view.MermaidRenderer;
import com.sixdee.text2rule.dto.ValidationResult;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Demo application for Multi-RuleNode Tree with NodeData using LangGraph4j
 * Agent.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Initialize all variables before try block
        ChatLanguageModel chatLanguageModel = null;
        CompiledGraph<WorkflowState> app = null;
        DecompositionWorkflow graphBuilder = null;
        Map<String, Object> inputs = null;
        WorkflowState finalState = null;
        ValidationResult validationResponse = null;
        RuleTree<NodeData> tree = null;
        List<TreeRenderer> renderers = null;

        try {
            String apiKey = System.getenv("GROQ_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                apiKey = "demo-key";
                logger.warn("GROQ_API_KEY environment variable not set. Using dummy key.");
            }
            chatLanguageModel = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl("https://api.groq.com/openai/v1")
                    .modelName("llama-3.3-70b-versatile")
                    .timeout(Duration.ofSeconds(60))
                    .build();

            // SWITCHED TO DECOMPOSITION WORKFLOW
            graphBuilder = new DecompositionWorkflow(chatLanguageModel);
            app = graphBuilder.build();

            inputs = Map.of("input",
                    "Run this campaign weekly on Mondays and Tuesdays from 5 October 2024 to 5 October 2026, targeting subscribers based on their SMS revenue, preferred location, and recharge behavior. Subscribers whose SMS revenue in the last 30 days is exactly 15 RO, whose favorite location is Mumbai, and whose total recharge in the last 30 days is at least 200 RO should receive a promotional SMS with Message ID 24, while subscribers whose SMS revenue in the last 30 days is greater than 15 RO, whose favorite location is Bengaluru, and whose total recharge in the last 30 days is at least 150 RO should receive a promotional SMS with Message ID 25. Subscribers who do not meet either of these criteria should be excluded from the campaign.");

            logger.info("Invoking Decomposition Workflow (Partial)...");
            finalState = app.invoke(inputs)
                    .orElseThrow(() -> new RuntimeException("Graph execution failed to return state"));

            // Process validation results
            validationResponse = finalState.getValidationResponse();
            if (validationResponse != null) {
                if (!validationResponse.isValid()) {
                    ObjectMapper mapper = new ObjectMapper();
                    logger.error("Validation Failed. Details:\n{}",
                            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(validationResponse));
                } else {
                    logger.info("Validation Result: \nIs Valid: {}\nIssues: {}",
                            validationResponse.isValid(),
                            validationResponse.getIssuesDetected());
                }
            }

            // Check workflow failure status
            if (finalState.isWorkflowFailed()) {
                logger.error("Workflow failed: {}", finalState.getFailureReason());
            } else {
                // Process tree results
                tree = finalState.getTree();
                if (tree != null) {
                    logger.info("Tree generated successfully (Decomposition Only).");

                    // Define Renderers
                    renderers = Arrays.asList(
                            new AsciiRenderer(),
                            new MermaidRenderer());

                    // Render Tree
                    for (TreeRenderer renderer : renderers) {
                        renderer.render(tree);
                    }
                } else {
                    logger.warn("No tree was generated. Check validation errors.");
                }
            }

            // Display workflow graph using LangGraph native Mermaid generation
            System.out.println(graphBuilder.print());

            // Log Consistency Result
            Double consistencyScore = finalState.getConsistencyScore();
            String consistencyFeedback = finalState.getFeedback();

            if (consistencyScore != null) {
                logger.info("Consistency Agent Result:\nScore: {}\nFeedback: {}",
                        String.format("%.2f", consistencyScore),
                        consistencyFeedback != null ? consistencyFeedback.trim() : "None");
            } else {
                logger.warn("Consistency Agent Result: Not available (null score)");
            }

            logger.info("Decomposition Workflow completed successfully.");

        } catch (RuntimeException e) {
            logger.error("Runtime error occurred during execution", e);
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error occurred during execution", e);
            System.exit(1);
        } finally {
            // Cleanup resources
            logger.info("Cleaning up resources...");
            try {
                if (app != null) {
                    logger.debug("Workflow graph cleanup completed");
                }
            } catch (Exception e) {
                logger.warn("Error during resource cleanup", e);
            }

            // Set all variables to null
            chatLanguageModel = null;
            app = null;
            graphBuilder = null;
            inputs = null;
            finalState = null;
            validationResponse = null;
            tree = null;
            renderers = null;

            logger.info("Application shutdown complete.");
        }
    }
}
