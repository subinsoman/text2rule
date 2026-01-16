package com.sixdee.text2rule.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixdee.text2rule.config.PromptRegistry;
import com.sixdee.text2rule.model.NodeData;
import com.sixdee.text2rule.model.RuleNode;
import com.sixdee.text2rule.model.RuleTree;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * ActionExtractionAgent extracts action details from Segment nodes.
 * Simplified version - no internal retry logic (handled by workflow).
 */
public class ActionExtractionAgent {
    private static final Logger logger = LoggerFactory.getLogger(ActionExtractionAgent.class);
    private static final String DEFAULT_PROMPT_KEY = "action_extraction_prompt";

    private final ChatLanguageModel lang4jService;
    private final ObjectMapper objectMapper;
    private CompiledGraph<ActionState> compiledGraph;

    public static class ActionState extends AgentState {
        public ActionState(Map<String, Object> initData) {
            super(new HashMap<>(initData));
        }

        @SuppressWarnings("unchecked")
        public RuleTree<NodeData> getTree() {
            return (RuleTree<NodeData>) this.data().get("tree");
        }

        public boolean isFailed() {
            return (boolean) this.data().getOrDefault("failed", false);
        }
    }

    public ActionExtractionAgent(ChatLanguageModel lang4jService) {
        this.lang4jService = lang4jService;
        this.objectMapper = new ObjectMapper();
        // Configure ObjectMapper to handle flexible JSON parsing
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        compile();
    }

    private void compile() {
        try {
            StateGraph<ActionState> graph = new StateGraph<>(ActionState::new);
            graph.addNode("extract_action", this::extractNode);
            graph.addEdge(START, "extract_action");
            graph.addEdge("extract_action", END);
            this.compiledGraph = graph.compile();
        } catch (Exception e) {
            logger.error("Failed to compile ActionExtractionAgent", e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Map<String, Object>> extractNode(ActionState state) {
        logger.info("ActionExtractionAgent: Executing Extract Node...");
        RuleTree<NodeData> tree = state.getTree();
        if (tree == null)
            return CompletableFuture.completedFuture(Map.of("failed", true));

        String customPromptKey = (String) state.data().get("customPromptKey");

        try {
            extractActions(tree, customPromptKey);
        } catch (Exception e) {
            logger.error("Error extracting actions", e);
            return CompletableFuture.completedFuture(Map.of("failed", true));
        }

        return CompletableFuture.completedFuture(Map.of("tree", tree));
    }

    private void extractActions(RuleTree<NodeData> tree, String customPromptKey) {
        if (tree == null || tree.getRoot() == null)
            return;
        extractActionsRecursive(tree.getRoot(), customPromptKey);
    }

    private void extractActionsRecursive(RuleNode<NodeData> node, String customPromptKey) {
        // Only process "Segment" nodes
        if ("Segment".equalsIgnoreCase(node.getData().getType())) {
            // Clear existing children to support retry mechanism
            node.getChildren().clear();

            String segmentInput = node.getData().getInput();
            // Expected format: "Condition: ... -> Action: ..."
            // Extract the Action part
            String actionText = "";
            if (segmentInput.contains("-> Action:")) {
                actionText = segmentInput.substring(segmentInput.indexOf("-> Action:") + 10).trim();
            } else {
                logger.warn("Segment node does not contain '-> Action:' delimiter: {}", segmentInput);
                actionText = segmentInput; // Fallback to full text
            }

            if (!actionText.isEmpty()) {
                logger.info("Extracting details for Action: {}", actionText);
                try {
                    // Use custom prompt key if provided, otherwise use default
                    String promptKey = (customPromptKey != null && !customPromptKey.trim().isEmpty())
                            ? customPromptKey
                            : DEFAULT_PROMPT_KEY;

                    String promptTemplate = PromptRegistry.getInstance().get(promptKey);
                    String prompt = promptTemplate.replace("{{ $json.action_text }}", actionText);

                    String jsonResponse = lang4jService.generate(prompt);

                    // Robust JSON extraction: Find first '{' and last '}'
                    int startIndex = jsonResponse.indexOf("{");
                    int endIndex = jsonResponse.lastIndexOf("}");

                    if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                        jsonResponse = jsonResponse.substring(startIndex, endIndex + 1);
                    } else {
                        // Fallback cleanup if braces not found (unlikely for valid JSON)
                        if (jsonResponse.startsWith("```json")) {
                            jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();
                        }
                    }

                    logger.info("Parsed JSON String: {}", jsonResponse);

                    Map<String, String> actionDetails = objectMapper.readValue(jsonResponse,
                            new TypeReference<Map<String, String>>() {
                            });

                    String actionType = actionDetails.getOrDefault("action_type", "Action");
                    String details = actionDetails.getOrDefault("details", "");
                    String channel = actionDetails.getOrDefault("channel", "");

                    String formattedAction = String.format("Type: %s, Channel: %s, Details: %s", actionType, channel,
                            details);

                    NodeData actionNode = new NodeData("Action", "", "", node.getData().getModelName(), "",
                            formattedAction);
                    node.addChild(new RuleNode<>(actionNode));
                    logger.info("Added Action child node: {}", formattedAction);

                } catch (Exception e) {
                    logger.error("Error extracting action details. Using fallback.", e);
                    // Fallback: Create a generic action node with the raw text
                    String fallbackAction = "Type: Unknown, Channel: Unknown, Details: " + actionText;
                    NodeData actionNode = new NodeData("Action", "", "", node.getData().getModelName(), "",
                            fallbackAction);
                    node.addChild(new RuleNode<>(actionNode));
                    logger.info("Added Fallback Action child node: {}", fallbackAction);
                }
            }
        }

        if (node.getChildren() != null) {
            for (RuleNode<NodeData> child : node.getChildren()) {
                extractActionsRecursive(child, customPromptKey);
            }
        }
    }

    public CompletableFuture<ActionState> execute(RuleTree<NodeData> tree) {
        return execute(tree, null);
    }

    public CompletableFuture<ActionState> execute(RuleTree<NodeData> tree, String customPromptKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> input = new HashMap<>();
                input.put("tree", tree);
                if (customPromptKey != null && !customPromptKey.trim().isEmpty()) {
                    input.put("customPromptKey", customPromptKey);
                    logger.info("Using custom prompt key for action extraction");
                }
                return compiledGraph.invoke(input).orElse(null);
            } catch (Exception e) {
                logger.error("Error executing ActionExtractionAgent", e);
                throw new RuntimeException(e);
            }
        });
    }
}
