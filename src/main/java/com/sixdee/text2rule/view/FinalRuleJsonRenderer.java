package com.sixdee.text2rule.view;

import com.sixdee.text2rule.builder.RuleJsonBuilder;
import com.sixdee.text2rule.model.NodeData;
import com.sixdee.text2rule.model.RuleTree;
import com.sixdee.text2rule.parser.ActionParser;
import com.sixdee.text2rule.parser.ConditionParser;
import com.sixdee.text2rule.parser.ScheduleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Refactored FinalRuleJsonRenderer following SOLID principles.
 * Delegates parsing to specialized parser classes.
 * Uses Builder pattern for JSON construction.
 * Follows Single Responsibility Principle - only orchestrates rendering.
 */
public class FinalRuleJsonRenderer {
    private static final Logger logger = LoggerFactory.getLogger(FinalRuleJsonRenderer.class);

    private final ConditionParser conditionParser;
    private final ActionParser actionParser;
    private final ScheduleParser scheduleParser;
    private final RuleJsonBuilder jsonBuilder;

    /**
     * Constructor with dependency injection of parsers and builder.
     */
    public FinalRuleJsonRenderer() {
        try {
            this.conditionParser = new ConditionParser();
            this.actionParser = new ActionParser();
            this.scheduleParser = new ScheduleParser();
            this.jsonBuilder = new RuleJsonBuilder();
            logger.info("FinalRuleJsonRenderer initialized [parsers=3, builder=RuleJsonBuilder]");
        } catch (Exception e) {
            logger.error("Failed to initialize FinalRuleJsonRenderer [error={}]", e.getMessage(), e);
            throw new RuntimeException("Renderer initialization failed", e);
        }
    }

    /**
     * Render the tree into hierarchical rule JSON format.
     * Orchestrates parsing and building process.
     * 
     * @param tree The RuleTree to render
     * @return JSON string representation of the rule
     */
    public String render(RuleTree<NodeData> tree) {
        List<Map<String, Object>> conditions = null;
        List<Map<String, Object>> actions = null;
        Map<String, Object> schedule = null;
        String result = null;

        try {
            if (tree == null || tree.getRoot() == null) {
                logger.warn("Render called with null tree or root");
                return "[]";
            }

            logger.info("Starting rule JSON rendering [tree_has_root=true]");

            // Extract components using specialized parsers
            conditions = conditionParser.extractConditions(tree.getRoot());
            actions = actionParser.extractActions(tree.getRoot());
            schedule = scheduleParser.extractSchedule(tree.getRoot());

            // Build JSON using builder pattern
            result = jsonBuilder
                    .withConditions(conditions)
                    .withActions(actions)
                    .withSchedule(schedule)
                    .build();

            logger.info("Rule JSON rendering completed [output_size={}, conditions={}, actions={}, has_schedule={}]",
                    result.length(), conditions.size(), actions.size(), schedule != null);

            return result;
        } catch (Exception e) {
            logger.error("Failed to render rule JSON [error={}]", e.getMessage(), e);
            return "[]";
        } finally {
            // Cleanup resources
            conditions = null;
            actions = null;
            schedule = null;
            // Note: result is returned, so not nullified here
        }
    }
}
