package com.sixdee.text2rule.model;

/**
 * Data object to hold node information.
 */
import java.io.Serializable;
import java.util.Objects;

public class NodeData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String statementPrompt;
    private String systemPrompt;
    private String modelName;
    private String modelCredential;
    private String type;
    private String input;

    public NodeData(String type, String stmt, String sys, String model, String cred, String input) {
        this.type = type;
        this.statementPrompt = stmt;
        this.systemPrompt = sys;
        this.modelName = model;
        this.modelCredential = cred;
        this.input = input;
    }

    /**
     * Simplified constructor for convenience.
     * Defaults: type="Root", statementPrompt="", systemPrompt="", credential=""
     */
    public NodeData(String input, String modelName) {
        this("Root", "", "", modelName, "", input);
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatementPrompt() {
        return statementPrompt;
    }

    public void setStatementPrompt(String statementPrompt) {
        this.statementPrompt = statementPrompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelCredential() {
        return modelCredential;
    }

    public void setModelCredential(String modelCredential) {
        this.modelCredential = modelCredential;
    }

    @Override
    public String toString() {
        return ""+this.input;
    }

    // New field for parent context
    private String parentContext;

    public String getParentContext() {
        return parentContext;
    }

    public void setParentContext(String parentContext) {
        this.parentContext = parentContext;
    }

    // New field for similarity score
    private double similarityScore;

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
