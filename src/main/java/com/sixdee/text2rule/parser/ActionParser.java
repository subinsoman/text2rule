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
 * Parser for extracting and parsing action nodes from RuleTree.
 * Follows Single Responsibility Principle - only handles action parsing.
 */
public class ActionParser {
    private static final Logger logger = LoggerFactory.getLogger(ActionParser.class);

    /**
     * Extract all actions from the tree starting at root.
     */
    public List<Map<String, Object>> extractActions(RuleNode<NodeData> root) {
        List<Map<String, Object>> actions = null;

        try {
            actions = new ArrayList<>();
            extractActionsRecursive(root, actions, "0");
            logger.info("Extracted actions [count={}]", actions.size());
            return actions;
        } catch (Exception e) {
            logger.error("Failed to extract actions [error={}]", e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            // Cleanup handled by return or exception
            logger.debug("Action extraction completed");
        }
    }

    /**
     * Recursively traverse tree to find Action nodes with ActionDetails.
     */
    private void extractActionsRecursive(RuleNode<NodeData> node, List<Map<String, Object>> actions, String parentId) {
        if (node == null) {
            return;
        }

        RuleNode<NodeData> actionDetailsNode = null;
        Map<String, Object> action = null;

        try {
            // Look for Action nodes with ActionDetails
            if ("Action".equalsIgnoreCase(node.getData().getType())) {
                actionDetailsNode = findDirectChild(node, "ActionDetails");
                if (actionDetailsNode != null && actionDetailsNode.getData().getInput() != null) {
                    action = buildActionObject(actionDetailsNode.getData().getInput(), parentId,
                            actions.size());
                    if (action != null) {
                        actions.add(action);
                        logger.debug("Extracted action [index={}, details_length={}]",
                                actions.size() - 1, actionDetailsNode.getData().getInput().length());
                    }
                }
            }

            // Traverse children
            if (node.getChildren() != null) {
                for (RuleNode<NodeData> child : node.getChildren()) {
                    extractActionsRecursive(child, actions, parentId);
                }
            }
        } catch (Exception e) {
            logger.error("Error during action extraction traversal [node_type={}, error={}]",
                    node.getData().getType(), e.getMessage(), e);
        } finally {
            // Cleanup resources
            actionDetailsNode = null;
            action = null;
        }
    }

    /**
     * Find direct child node with specified type.
     */
    private RuleNode<NodeData> findDirectChild(RuleNode<NodeData> parent, String type) {
        if (parent == null || parent.getChildren() == null) {
            return null;
        }

        for (RuleNode<NodeData> child : parent.getChildren()) {
            if (type.equalsIgnoreCase(child.getData().getType())) {
                return child;
            }
        }
        return null;
    }

    /**
     * Build action object from action details string.
     */
    private Map<String, Object> buildActionObject(String details, String parentId, int index) {
        Map<String, String> fields = null;
        Map<String, Object> action = null;
        Map<String, Object> actionInfo = null;
        List<Map<String, String>> fieldList = null;
        Map<String, Object> request = null;
        List<Map<String, String>> requestFields = null;

        try {
            fields = parseActionDetails(details);

            action = new HashMap<>();
            action.put("id", parentId + "_" + index);
            action.put("pid", parentId);
            action.put("type", "action");

            // Action info
            actionInfo = new HashMap<>();
            actionInfo.put("id", 5);
            actionInfo.put("name", fields.getOrDefault("Action", "Send Promotion"));
            action.put("action", actionInfo);

            // Action fields
            fieldList = new ArrayList<>();
            fieldList.add(Map.of("name", "ActionCall", "value", "EXTERNAL"));
            fieldList.add(Map.of("name", "ActionName", "value", "UPLOADER_MAIN"));
            fieldList.add(Map.of("name", "ActionURL", "value", "UPLOADER_CALL"));
            fieldList.add(Map.of("name", "ActionType", "value", "ASYNCH"));
            action.put("field", fieldList);

            // Request fields
            request = new HashMap<>();
            requestFields = new ArrayList<>();
            requestFields.add(Map.of("name", "ActionKey", "value", "campaign_action"));
            requestFields.add(Map.of("name", "CHANNEL", "value", fields.getOrDefault("Channel", "SMS")));
            requestFields.add(Map.of("name", "MESSAGE_ID", "value", fields.getOrDefault("Message_ID", "")));
            request.put("field", requestFields);
            action.put("request", request);

            logger.debug("Built action object [id={}, action_name={}]", action.get("id"), actionInfo.get("name"));
            return action;
        } catch (Exception e) {
            logger.error("Failed to build action object [details={}, error={}]", details, e.getMessage(), e);
            return null;
        } finally {
            // Cleanup resources
            fields = null;
            actionInfo = null;
            fieldList = null;
            request = null;
            requestFields = null;
        }
    }

    /**
     * Parse action details string into field map.
     * Format: "Key1: Value1, Key2: Value2, ..."
     */
    private Map<String, String> parseActionDetails(String details) {
        Map<String, String> fields = null;
        String[] parts = null;
        String part = null;
        String[] keyValue = null;
        String key = null;
        String value = null;

        try {
            fields = new HashMap<>();
            parts = details.split(",");

            for (int i = 0; i < parts.length; i++) {
                part = parts[i].trim();
                if (part.contains(":")) {
                    keyValue = part.split(":", 2);
                    key = keyValue[0].trim().replace(" ", "_");
                    value = keyValue[1].trim();
                    fields.put(key, value);
                }
            }
            logger.debug("Parsed action details [field_count={}]", fields.size());
            return fields;
        } catch (Exception e) {
            logger.error("Failed to parse action details [details={}, error={}]", details, e.getMessage(), e);
            return new HashMap<>();
        } finally {
            // Cleanup resources
            parts = null;
            part = null;
            keyValue = null;
            key = null;
            value = null;
        }
    }
}
