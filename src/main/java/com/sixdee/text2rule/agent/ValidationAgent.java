package com.sixdee.text2rule.agent;

import com.sixdee.text2rule.config.PromptRegistry;
import com.sixdee.text2rule.dto.ValidationResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;

public class ValidationAgent {
    private static final Logger logger = LoggerFactory.getLogger(ValidationAgent.class);

    private final ChatLanguageModel lang4jService;
    private final ObjectMapper objectMapper;
    private CompiledGraph<ValidationState> compiledGraph;

    public static class ValidationState extends AgentState {
        public ValidationState(Map<String, Object> initData) {
            super(new HashMap<>(initData));
        }

        public String getInput() {
            return (String) this.data().get("input");
        }

        public ValidationResult getValidationResult() {
            return (ValidationResult) this.data().get("validationResult");
        }

        public boolean isValid() {
            return "true".equalsIgnoreCase((String) this.data().get("valid"));
        }
    }

    public ValidationAgent(ChatLanguageModel lang4jService) {
        this.lang4jService = lang4jService;
        this.objectMapper = new ObjectMapper();
        // Configure to accept both camelCase and snake_case
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        compile();
    }

    private void compile() {
        try {
            StateGraph<ValidationState> graph = new StateGraph<>(ValidationState::new);
            graph.addNode("validate", this::validateNode);
            graph.addEdge(org.bsc.langgraph4j.StateGraph.START, "validate");
            graph.addEdge("validate", END);
            this.compiledGraph = graph.compile();
        } catch (Exception e) {
            logger.error("Failed to compile ValidationAgent graph", e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Map<String, Object>> validateNode(ValidationState state) {
        logger.info("ValidationAgent: Executing Validate Node...");
        String input = state.getInput();
        List<ChatMessage> messages = new ArrayList<>();

        String promptTemplate = PromptRegistry.getInstance().get("basic_validator_agent_prompt");
        // Fallback or use template
        if (promptTemplate == null)
            promptTemplate = "You are a validation agent. Validate the following rule: {{ $json.ruletext }}";

        // Append instructions to ensure strict JSON output
        String detailedInstructions = "\nValidate the rule and return a valid JSON object matching the format. Output ONLY the JSON.";

        messages.add(new SystemMessage(promptTemplate.replace("{{ $json.ruletext }}", input) + detailedInstructions));

        // Generate without tools
        Response<AiMessage> response = lang4jService.generate(messages);
        AiMessage aiMessage = response.content();

        ValidationResult result = null;
        String content = aiMessage.text();

        try {
            // cleaner JSON extraction
            int start = content.indexOf("{");
            int end = content.lastIndexOf("}");
            if (start >= 0 && end > start) {
                String json = content.substring(start, end + 1);
                result = objectMapper.readValue(json, ValidationResult.class);
                logger.info("ValidationAgent: Parsed result - isValid: {}, issuesDetected: {}",
                        result.isValid(), result.getIssuesDetected());
            } else {
                logger.warn("No JSON found in response: {}", content);
            }
        } catch (Exception e) {
            logger.error("Failed to parse validation result", e);
        }

        // Safety check if LLM didn't return proper structure
        if (result == null) {
            result = new ValidationResult();
            result.setValid(false);
            result.setIssuesDetected(
                    Collections
                            .singletonList("Validation failed: Could not parse JSON response. Response: " + content));
        }

        String isValid = String.valueOf(result.isValid());
        return CompletableFuture.completedFuture(Map.of("validationResult", result, "valid", isValid));
    }

    public CompletableFuture<ValidationState> execute(String input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return compiledGraph.invoke(Map.of("input", input)).orElse(null);
            } catch (Exception e) {
                logger.error("Error executing validation agent", e);
                throw new RuntimeException(e);
            }
        });
    }
}
