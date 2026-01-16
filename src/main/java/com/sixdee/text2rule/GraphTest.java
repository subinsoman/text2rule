package com.sixdee.text2rule;

import com.sixdee.text2rule.workflow.AgenticConversionWorkflow;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Simple test to print the workflow graph structure
 */
public class GraphTest {
    private static final Logger logger = LoggerFactory.getLogger(GraphTest.class);

    public static void main(String[] args) {
        try {
            ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                    .apiKey("dummy-key")
                    .baseUrl("https://api.groq.com/openai/v1")
                    .modelName("llama-3.3-70b-versatile")
                    .timeout(Duration.ofSeconds(60))
                    .build();

            AgenticConversionWorkflow graphBuilder = new AgenticConversionWorkflow(chatLanguageModel);
            graphBuilder.build();

            System.out.println("\n=== WORKFLOW GRAPH WITH RETRY LOGIC ===\n");
            System.out.println(graphBuilder.print());
            System.out.println("\n=== END OF GRAPH ===\n");

            logger.info("Graph structure printed successfully");
        } catch (Exception e) {
            logger.error("Error printing graph", e);
            System.exit(1);
        }
    }
}
