package com.sixdee.text2rule.exception;

/**
 * Exception thrown when consistency check fails or encounters errors.
 */
public class ConsistencyCheckException extends Text2RuleException {

    private final Double score;
    private final Double threshold;

    public ConsistencyCheckException(String message, Double score, Double threshold) {
        super(String.format("%s [score=%.2f, threshold=%.2f]", message, score, threshold));
        this.score = score;
        this.threshold = threshold;
    }

    public ConsistencyCheckException(String message, Throwable cause) {
        super(message, cause);
        this.score = null;
        this.threshold = null;
    }

    public Double getScore() {
        return score;
    }

    public Double getThreshold() {
        return threshold;
    }
}
