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
 * Parser for extracting and parsing schedule information from RuleTree.
 * Follows Single Responsibility Principle - only handles schedule parsing.
 */
public class ScheduleParser {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleParser.class);

    /**
     * Extract schedule information from the tree.
     */
    public Map<String, Object> extractSchedule(RuleNode<NodeData> root) {
        try {
            RuleNode<NodeData> scheduleNode = findNodeByType(root, "Schedule");
            if (scheduleNode == null) {
                logger.debug("No Schedule node found in tree");
                return null;
            }

            RuleNode<NodeData> scheduleDetailsNode = findNodeByType(scheduleNode, "ScheduleDetails");
            if (scheduleDetailsNode == null || scheduleDetailsNode.getData().getInput() == null) {
                logger.debug("No ScheduleDetails node found or input is null");
                return null;
            }

            String details = scheduleDetailsNode.getData().getInput();
            Map<String, Object> schedule = buildScheduleObject(details);

            logger.info("Extracted schedule [has_data={}]", schedule != null);
            return schedule;
        } catch (Exception e) {
            logger.error("Failed to extract schedule [error={}]", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Find node by type recursively.
     */
    private RuleNode<NodeData> findNodeByType(RuleNode<NodeData> node, String type) {
        if (node == null) {
            return null;
        }

        if (type.equalsIgnoreCase(node.getData().getType())) {
            return node;
        }

        if (node.getChildren() != null) {
            for (RuleNode<NodeData> child : node.getChildren()) {
                RuleNode<NodeData> found = findNodeByType(child, type);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * Build schedule object from details string.
     */
    private Map<String, Object> buildScheduleObject(String details) {
        try {
            Map<String, Object> schedule = new HashMap<>();
            List<Map<String, String>> fields = new ArrayList<>();

            String scheduleType = extractValue(details, "Schedule Type:");
            // Note: days field could be extracted here if needed in future

            fields.add(Map.of("name", "ScheduleId", "value", ""));
            fields.add(Map.of("name", "ScheduleName", "value", scheduleType));
            fields.add(Map.of("name", "ScheduleType", "value", scheduleType));
            fields.add(Map.of("name", "StartDate", "value", "2024-11-01"));
            fields.add(Map.of("name", "ExpiryDate", "value", "2024-11-30"));
            fields.add(Map.of("name", "Repeat", "value", "Yes"));

            schedule.put("field", fields);

            logger.debug("Built schedule object [type={}, field_count={}]", scheduleType, fields.size());
            return schedule;
        } catch (Exception e) {
            logger.error("Failed to build schedule object [details={}, error={}]", details, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract value from text using prefix.
     */
    private String extractValue(String text, String prefix) {
        try {
            int startIdx = text.indexOf(prefix);
            if (startIdx == -1) {
                return "";
            }

            startIdx += prefix.length();
            int endIdx = text.indexOf(",", startIdx);
            if (endIdx == -1) {
                endIdx = text.length();
            }

            return text.substring(startIdx, endIdx).trim();
        } catch (Exception e) {
            logger.error("Failed to extract value [prefix={}, error={}]", prefix, e.getMessage(), e);
            return "";
        }
    }
}
