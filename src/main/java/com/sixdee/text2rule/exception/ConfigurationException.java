package com.sixdee.text2rule.exception;

/**
 * Exception thrown when configuration loading or access fails.
 */
public class ConfigurationException extends Text2RuleException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
