package com.sixdee.text2rule.exception;

/**
 * Exception thrown when an agent execution fails.
 * Provides context about which agent failed and why.
 */
public class AgentExecutionException extends Text2RuleException {

    private final String agentName;

    public AgentExecutionException(String agentName, String message) {
        super(String.format("Agent '%s' execution failed: %s", agentName, message));
        this.agentName = agentName;
    }

    public AgentExecutionException(String agentName, String message, Throwable cause) {
        super(String.format("Agent '%s' execution failed: %s", agentName, message), cause);
        this.agentName = agentName;
    }

    public String getAgentName() {
        return agentName;
    }
}
