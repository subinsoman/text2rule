package com.sixdee.text2rule.exception;

/**
 * Exception thrown when tree building or manipulation fails.
 */
public class TreeBuildingException extends Text2RuleException {

    public TreeBuildingException(String message) {
        super(message);
    }

    public TreeBuildingException(String message, Throwable cause) {
        super(message, cause);
    }
}
