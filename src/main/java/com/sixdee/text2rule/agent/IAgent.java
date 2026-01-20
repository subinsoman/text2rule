package com.sixdee.text2rule.agent;

import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all agents following Interface Segregation Principle.
 * Defines common contract for agent execution.
 * 
 * @param <T> The state type returned by the agent
 */
public interface IAgent<T> {

    /**
     * Execute the agent with the provided input.
     * 
     * @param input The input data for agent execution
     * @return CompletableFuture containing the agent state result
     */
    CompletableFuture<T> execute(Object input);

    /**
     * Get the agent name for logging and identification.
     * 
     * @return The agent name
     */
    String getAgentName();

    /**
     * Check if the agent execution is currently in progress.
     * 
     * @return true if executing, false otherwise
     */
    default boolean isExecuting() {
        return false;
    }
}
