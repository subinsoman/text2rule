package com.sixdee.text2rule.exception;

/**
 * Base exception for all Text2Rule application errors.
 * Follows exception hierarchy best practices.
 */
public class Text2RuleException extends RuntimeException {

    public Text2RuleException(String message) {
        super(message);
    }

    public Text2RuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public Text2RuleException(Throwable cause) {
        super(cause);
    }
}
