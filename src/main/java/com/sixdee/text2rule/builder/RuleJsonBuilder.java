package com.sixdee.text2rule.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for constructing rule JSON following Builder Pattern.
 * Provides fluent API for building complex JSON structures.
 * Follows Single Responsibility Principle - only handles JSON construction.
 */
public class RuleJsonBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RuleJsonBuilder.class);

    private final ObjectMapper objectMapper;
    private List<Map<String, Object>> conditions;
    private List<Map<String, Object>> actions;
    private Map<String, Object> schedule;

    public RuleJsonBuilder() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.conditions = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    /**
     * Set conditions for the rule.
     */
    public RuleJsonBuilder withConditions(List<Map<String, Object>> conditions) {
        try {
            this.conditions = conditions != null ? conditions : new ArrayList<>();
            logger.debug("Set conditions [count={}]", this.conditions.size());
            return this;
        } catch (Exception e) {
            logger.error("Failed to set conditions [error={}]", e.getMessage(), e);
            this.conditions = new ArrayList<>();
            return this;
        }
    }

    /**
     * Set actions for the rule.
     */
    public RuleJsonBuilder withActions(List<Map<String, Object>> actions) {
        try {
            this.actions = actions != null ? actions : new ArrayList<>();
            logger.debug("Set actions [count={}]", this.actions.size());
            return this;
        } catch (Exception e) {
            logger.error("Failed to set actions [error={}]", e.getMessage(), e);
            this.actions = new ArrayList<>();
            return this;
        }
    }

    /**
     * Set schedule for the rule.
     */
    public RuleJsonBuilder withSchedule(Map<String, Object> schedule) {
        try {
            this.schedule = schedule;
            logger.debug("Set schedule [has_data={}]", schedule != null);
            return this;
        } catch (Exception e) {
            logger.error("Failed to set schedule [error={}]", e.getMessage(), e);
            this.schedule = null;
            return this;
        }
    }

    /**
     * Build the final JSON string.
     */
    public String build() {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> detail = new HashMap<>();
            Map<String, Object> rules = new HashMap<>();

            rules.put("id", "0");
            rules.put("pid", "#");

            // Build conditions with their child actions
            List<Map<String, Object>> children = buildConditionsWithActions();
            rules.put("childrens", children);

            // Add schedule if present
            if (schedule != null) {
                rules.put("schedule", schedule);
            }

            detail.put("rules", rules);
            result.add(Map.of("detail", detail));

            String json = objectMapper.writeValueAsString(result);
            logger.info("Built rule JSON [output_size={}, conditions={}, actions={}]",
                    json.length(), conditions.size(), actions.size());
            return json;
        } catch (Exception e) {
            logger.error("Failed to build rule JSON [error={}]", e.getMessage(), e);
            return "[]";
        }
    }

    /**
     * Build conditions with actions nested under each condition.
     */
    private List<Map<String, Object>> buildConditionsWithActions() {
        try {
            List<Map<String, Object>> conditionsWithActions = new ArrayList<>();

            int conditionIndex = 0;
            for (Map<String, Object> condition : conditions) {
                // Update condition ID to be direct child of root
                condition.put("id", "0_" + conditionIndex);
                condition.put("pid", "0");

                // Create children list for this condition
                List<Map<String, Object>> conditionChildren = new ArrayList<>();

                // Add actions as children of this condition
                int actionIndex = 0;
                for (Map<String, Object> action : actions) {
                    // Clone the action and update its IDs
                    Map<String, Object> actionCopy = new HashMap<>(action);
                    actionCopy.put("id", "0_" + conditionIndex + "_" + actionIndex);
                    actionCopy.put("pid", "0_" + conditionIndex);
                    conditionChildren.add(actionCopy);
                    actionIndex++;
                }

                // Add children to condition if any exist
                if (!conditionChildren.isEmpty()) {
                    condition.put("childrens", conditionChildren);
                }

                conditionsWithActions.add(condition);
                conditionIndex++;
            }

            logger.debug("Built conditions with actions [condition_count={}, total_actions={}]",
                    conditionsWithActions.size(), actions.size() * conditionsWithActions.size());
            return conditionsWithActions;
        } catch (Exception e) {
            logger.error("Failed to build conditions with actions [error={}]", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Reset builder to initial state.
     */
    public RuleJsonBuilder reset() {
        this.conditions = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.schedule = null;
        logger.debug("Builder reset to initial state");
        return this;
    }
}
