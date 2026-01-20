package com.sixdee.text2rule.parser;

import com.sixdee.text2rule.model.NodeData;
import com.sixdee.text2rule.model.RuleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for extracting and parsing condition nodes from RuleTree.
 * Follows Single Responsibility Principle - only handles condition parsing.
 */
public class ConditionParser {
    private static final Logger logger = LoggerFactory.getLogger(ConditionParser.class);

    /**
     * Extract all conditions from the tree starting at root.
     */
    public List<Map<String, Object>> extractConditions(RuleNode<NodeData> root) {
        List<Map<String, Object>> conditions = null;

        try {
            conditions = new ArrayList<>();
            extractConditionsRecursive(root, conditions, "0");
            logger.info("Extracted conditions [count={}]", conditions.size());
            return conditions;
        } catch (Exception e) {
            logger.error("Failed to extract conditions [error={}]", e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            // Cleanup handled by return or exception
            logger.debug("Condition extraction completed");
        }
    }

    /**
     * Recursively traverse tree to find IF_Condition nodes.
     */
    private void extractConditionsRecursive(RuleNode<NodeData> node, List<Map<String, Object>> conditions,
            String parentId) {
        if (node == null) {
            return;
        }

        String nodeType = null;
        String nodeInput = null;
        List<Map<String, Object>> parsedConditions = null;

        try {
            nodeType = node.getData().getType();
            nodeInput = node.getData().getInput();

            logger.debug("Traversing node [type={}, input_length={}]", nodeType,
                    nodeInput != null ? nodeInput.length() : 0);

            // Look for IF_Condition nodes
            if ("IF_Condition".equalsIgnoreCase(nodeType)) {
                logger.debug("Found IF_Condition node [input_preview={}]",
                        nodeInput != null && nodeInput.length() > 50 ? nodeInput.substring(0, 50) + "..." : nodeInput);

                if (nodeInput != null) {
                    parsedConditions = parseComplexIfCondition(nodeInput, parentId,
                            conditions.size());
                    logger.debug("Parsed conditions from IF_Condition [count={}]", parsedConditions.size());
                    conditions.addAll(parsedConditions);
                }
            }

            // Traverse children
            if (node.getChildren() != null) {
                for (RuleNode<NodeData> child : node.getChildren()) {
                    extractConditionsRecursive(child, conditions, parentId);
                }
            }
        } catch (Exception e) {
            logger.error("Error during condition extraction traversal [node_type={}, error={}]",
                    node.getData().getType(), e.getMessage(), e);
        } finally {
            // Cleanup resources
            nodeType = null;
            nodeInput = null;
            parsedConditions = null;
        }
    }

    /**
     * Parse complex IF condition that may contain multiple conditions with AND.
     */
    private List<Map<String, Object>> parseComplexIfCondition(String input, String parentId, int startIndex) {
        List<Map<String, Object>> conditions = null;
        String cleaned = null;
        String[] parts = null;
        String part = null;
        Map<String, Object> condition = null;

        try {
            conditions = new ArrayList<>();

            // Clean the condition
            cleaned = cleanConditionString(input);

            // Split by AND to get individual conditions
            parts = cleaned.split(" AND ");

            for (int i = 0; i < parts.length; i++) {
                part = parts[i].trim().replace("(", "").replace(")", "").trim();

                condition = parseSingleCondition(part, parentId, startIndex + i);
                if (condition != null) {
                    conditions.add(condition);
                }
            }

            logger.debug("Parsed complex condition [input_parts={}, output_conditions={}]", parts.length,
                    conditions.size());
            return conditions;
        } catch (Exception e) {
            logger.error("Failed to parse complex IF condition [input={}, error={}]", input, e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            // Cleanup resources
            cleaned = null;
            parts = null;
            part = null;
            condition = null;
        }
    }

    /**
     * Clean condition string by removing IF prefixes and extra parentheses.
     */
    private String cleanConditionString(String input) {
        String cleaned = null;

        try {
            cleaned = input.trim();

            if (cleaned.startsWith("if (")) {
                cleaned = cleaned.substring(4);
            }
            if (cleaned.startsWith("if(")) {
                cleaned = cleaned.substring(3);
            }

            while (cleaned.endsWith(")")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }

            return cleaned;
        } finally {
            // Cleanup handled by return
        }
    }

    /**
     * Parse a single condition into structured format.
     * Format: PROFILE_NAME OPERATOR VALUE
     */
    private Map<String, Object> parseSingleCondition(String conditionStr, String parentId, int index) {
        Map<String, Object> condition = null;
        String operator = null;
        String[] parts = null;
        String profileName = null;
        String value = null;
        Map<String, Object> profile = null;
        Map<String, Object> values = null;

        try {
            condition = new HashMap<>();
            condition.put("id", parentId + "_" + index);
            condition.put("pid", parentId);
            condition.put("type", "condition");

            // Parse condition: "PROFILE_NAME OPERATOR VALUE"
            operator = extractOperator(conditionStr);
            parts = null;

            if (operator != null) {
                parts = conditionStr.split(operator, 2);
            }

            if (parts != null && parts.length == 2) {
                profileName = parts[0].trim();
                value = parts[1].trim().replace("'", "").replace("\"", "");

                profile = new HashMap<>();
                profile.put("id", 1000 + index);
                profile.put("name", profileName);
                condition.put("profile", profile);
                condition.put("operator", operator);

                values = new HashMap<>();
                values.put("value", parseValue(value));
                condition.put("values", values);

                logger.debug("Parsed single condition [profile={}, operator={}, value={}]", profileName, operator,
                        value);
            }

            return condition;
        } catch (Exception e) {
            logger.error("Failed to parse single condition [input={}, error={}]", conditionStr, e.getMessage(), e);
            return null;
        } finally {
            // Cleanup resources
            operator = null;
            parts = null;
            profileName = null;
            value = null;
            profile = null;
            values = null;
        }
    }

    /**
     * Extract operator from condition string.
     */
    private String extractOperator(String conditionStr) {
        if (conditionStr.contains(">="))
            return ">=";
        if (conditionStr.contains("<="))
            return "<=";
        if (conditionStr.contains("!="))
            return "!=";
        if (conditionStr.contains(">"))
            return ">";
        if (conditionStr.contains("<"))
            return "<";
        if (conditionStr.contains("="))
            return "=";
        return null;
    }

    /**
     * Parse value as appropriate type (number or string).
     */
    private Object parseValue(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
