package com.sixdee.text2rule.agent;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;

public class ConsistencyAgent {
    private static final Logger logger = LoggerFactory.getLogger(ConsistencyAgent.class);
    private static final String PROMPT_KEY = "consistency_check_prompt";

    private final ChatLanguageModel lang4jService;
    private final ObjectMapper objectMapper;
    private CompiledGraph<ConsistencyState> compiledGraph;

    public static class ConsistencyState extends AgentState {
        public ConsistencyState(Map<String, Object> initData) {
            super(new HashMap<>(initData));
        }

        @SuppressWarnings("unchecked")
        public RuleTree<NodeData> getTree() {
            return (RuleTree<NodeData>) this.data().get("tree");
        }

        public String getCheckType() {
            return (String) this.data().getOrDefault("checkType", "root");
        }

        public Double getConsistencyScore() {
            return (Double) this.data().get("consistencyScore");
        }
    }

    public ConsistencyAgent(ChatLanguageModel lang4jService) {
        this.lang4jService = lang4jService;
        this.objectMapper = new ObjectMapper();
        compile();
    }

    private void compile() {
        try {
            StateGraph<ConsistencyState> graph = new StateGraph<>(ConsistencyState::new);
            graph.addNode("check_consistency", this::checkConsistencyNode);
            graph.addEdge(org.bsc.langgraph4j.StateGraph.START, "check_consistency");
            graph.addEdge("check_consistency", END);
            this.compiledGraph = graph.compile();
        } catch (Exception e) {
            logger.error("Failed to compile ConsistencyAgent graph", e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Map<String, Object>> checkConsistencyNode(ConsistencyState state) {
        logger.info("ConsistencyAgent: Executing Consistency Check...");
        RuleTree<NodeData> tree = state.getTree();
        String checkType = state.getCheckType();

        if (tree == null || tree.getRoot() == null) {
            logger.warn("Tree is empty. Skipping consistency check.");
            return CompletableFuture.completedFuture(Map.of("consistencyScore", 0.0));
        }

        try {
            if ("condition".equals(checkType)) {
                checkConditionConsistency(tree);
            } else {
                checkRootConsistency(tree);
            }

            Double score = tree.getRoot().getData().getSimilarityScore();
            if (score == null)
                score = 0.0;

            return CompletableFuture.completedFuture(Map.of("tree", tree, "consistencyScore", score));
        } catch (Exception e) {
            logger.error("Error during consistency check", e);
            return CompletableFuture.completedFuture(Map.of("consistencyScore", 0.0));
        }
    }

    private void checkRootConsistency(RuleTree<NodeData> tree) {
        RuleNode<NodeData> root = tree.getRoot();
        String originalText = root.getData().getInput();
        List<String> childrenTexts = collectChildrenTexts(root);
        String childrenCombined = String.join("\n", childrenTexts);

        Double score = calculateConsistencyScore(originalText, childrenCombined);

        if (score != null) {
            root.getData().setSimilarityScore(score);

            // Get threshold from config
            String thresholdStr = PromptRegistry.getInstance().getAttribute(PROMPT_KEY, "consistency_threshold");
            double threshold = (thresholdStr != null) ? Double.parseDouble(thresholdStr) : 0.8;

            if (score >= threshold) {
                logger.info("✓ Root Consistency Check: PASSED (score={})", score);
            } else {
                logger.warn("✗ Root Consistency Check: FAILED (score={}, threshold={})", score, threshold);
                logger.warn("  Original Text: {}", originalText);
                logger.warn("  Children Combined: {}", childrenCombined);
            }
        } else {
            logger.warn("Failed to parse similarity_score from response.");
        }
    }

    private void checkConditionConsistency(RuleTree<NodeData> tree) {
        if (tree == null || tree.getRoot() == null)
            return;
        checkConditionConsistencyRecursive(tree.getRoot());
    }

    private void checkConditionConsistencyRecursive(RuleNode<NodeData> node) {
        // Check NormalStatements nodes that have Condition children
        if ("NormalStatements".equalsIgnoreCase(node.getData().getType()) && !node.getChildren().isEmpty()) {
            logger.info("Checking consistency for Segments of NormalStatements node...");
            String originalText = node.getData().getInput();

            // Collect Condition texts
            List<String> segmentTexts = new ArrayList<>();
            for (RuleNode<NodeData> child : node.getChildren()) {
                if ("Segment".equalsIgnoreCase(child.getData().getType())) {
                    segmentTexts.add(child.getData().getInput());
                }
            }

            if (!segmentTexts.isEmpty()) {
                String childrenCombined = String.join("\n", segmentTexts);

                Double score = calculateConsistencyScore(originalText, childrenCombined);

                if (score != null) {
                    node.getData().setSimilarityScore(score);

                    // Get threshold from config
                    String thresholdStr = PromptRegistry.getInstance().getAttribute(PROMPT_KEY,
                            "consistency_threshold");
                    double threshold = (thresholdStr != null) ? Double.parseDouble(thresholdStr) : 0.8;

                    if (score >= threshold) {
                        logger.info("✓ Condition Consistency Check: PASSED (score={})", score);
                    } else {
                        logger.warn("✗ Condition Consistency Check: FAILED (score={}, threshold={})", score, threshold);
                        logger.warn("  Original Text: {}", originalText);
                        logger.warn("  Children Combined: {}", childrenCombined);
                    }
                }
            } else {
                logger.warn("No Condition children found for NormalStatements node");
            }
        }

        for (RuleNode<NodeData> child : node.getChildren()) {
            checkConditionConsistencyRecursive(child);
        }
    }

    private Double calculateConsistencyScore(String originalText, String childrenCombined) {
        logger.info("Calculating consistency score...");
        try {
            String promptTemplate = PromptRegistry.getInstance().get(PROMPT_KEY);
            if (promptTemplate == null) {
                logger.error("Consistency prompt not found.");
                return null;
            }

            String populatedPrompt = promptTemplate
                    .replace("{{ $json.original }}", originalText)
                    .replace("{{ $json.children }}", childrenCombined);

            // Rate limit protection: 12-second delay
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String responseJson = lang4jService.generate(populatedPrompt);

            // Robust JSON extraction
            int startIndex = responseJson.indexOf("{");
            int endIndex = responseJson.lastIndexOf("}");

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                responseJson = responseJson.substring(startIndex, endIndex + 1);
            } else {
                // Fallback
                if (responseJson.contains("```json")) {
                    responseJson = responseJson.substring(responseJson.indexOf("```json") + 7);
                    if (responseJson.contains("```")) {
                        responseJson = responseJson.substring(0, responseJson.indexOf("```"));
                    }
                } else if (responseJson.contains("```")) {
                    responseJson = responseJson.substring(responseJson.indexOf("```") + 3);
                    if (responseJson.contains("```")) {
                        responseJson = responseJson.substring(0, responseJson.indexOf("```"));
                    }
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseJson.trim(), Map.class);
            Object scoreObj = responseMap.get("similarity_score");
            if (scoreObj instanceof Number) {
                Double score = ((Number) scoreObj).doubleValue();
                logger.info("Calculated consistency score: {}", score);
                return score;
            }
        } catch (Exception e) {
            logger.error("Error calculating consistency score", e);
        }
        return null;
    }

    private List<String> collectChildrenTexts(RuleNode<NodeData> node) {
        List<String> texts = new ArrayList<>();
        if (node.getChildren().isEmpty()) {
            return texts;
        }

        for (RuleNode<NodeData> child : node.getChildren()) {
            texts.add(child.getData().getInput());
        }
        return texts;
    }

    public CompletableFuture<ConsistencyState> execute(RuleTree<NodeData> tree, String checkType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> input = new HashMap<>();
                input.put("tree", tree);
                input.put("checkType", checkType);
                return compiledGraph.invoke(input).orElse(null);
            } catch (Exception e) {
                logger.error("Error executing ConsistencyAgent", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<ConsistencyState> execute(RuleTree<NodeData> tree) {
        return execute(tree, "root");
    }
}
