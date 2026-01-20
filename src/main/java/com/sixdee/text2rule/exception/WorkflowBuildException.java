package com.sixdee.text2rule.exception;

/**
 * Exception thrown when workflow building or compilation fails.
 */
public class WorkflowBuildException extends Text2RuleException {

    public WorkflowBuildException(String message) {
        super(message);
    }

    public WorkflowBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
