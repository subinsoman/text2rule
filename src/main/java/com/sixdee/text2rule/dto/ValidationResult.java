package com.sixdee.text2rule.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import java.util.List;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationResult implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("is_valid")
    @JsonAlias({ "isValid" })
    @Description("Set to true if the rule is logically sound and contains necessary components (condition, action, etc.). False otherwise.")
    private boolean isValid;

    @JsonProperty("issues_detected")
    @JsonAlias({ "issuesDetected" })
    @Description("List of specific issues found in the rule logic, if any.")
    private List<String> issuesDetected;

    @Description("Suggestions for improving the rule or fixing issues.")
    private String suggestion;

    @JsonProperty("input_text")
    @JsonAlias({ "inputText" })
    @Description("The original input text.")
    private String inputText;

    @JsonProperty("has_condition")
    @JsonAlias({ "hasCondition" })
    private boolean hasCondition;

    @JsonProperty("has_action")
    @JsonAlias({ "hasAction" })
    private boolean hasAction;

    @JsonProperty("has_bonus")
    @JsonAlias({ "hasBonus" })
    private boolean hasBonus;

    @JsonProperty("has_sampling")
    @JsonAlias({ "hasSampling" })
    private boolean hasSampling;

    @JsonProperty("has_policy")
    @JsonAlias({ "hasPolicy" })
    private boolean hasPolicy;

    @JsonProperty("has_schedule")
    @JsonAlias({ "hasSchedule" })
    private boolean hasSchedule;

    @JsonProperty("has_valid_format")
    @JsonAlias({ "hasValidFormat" })
    private boolean hasValidFormat;

    @JsonProperty("has_message_id_with_action")
    @JsonAlias({ "hasMessageIdWithAction" })
    private boolean hasMessageIdWithAction;

    // Getters and Setters

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public List<String> getIssuesDetected() {
        return issuesDetected;
    }

    public void setIssuesDetected(List<String> issuesDetected) {
        this.issuesDetected = issuesDetected;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public boolean isHasCondition() {
        return hasCondition;
    }

    public void setHasCondition(boolean hasCondition) {
        this.hasCondition = hasCondition;
    }

    public boolean isHasAction() {
        return hasAction;
    }

    public void setHasAction(boolean hasAction) {
        this.hasAction = hasAction;
    }

    public boolean isHasBonus() {
        return hasBonus;
    }

    public void setHasBonus(boolean hasBonus) {
        this.hasBonus = hasBonus;
    }

    public boolean isHasSampling() {
        return hasSampling;
    }

    public void setHasSampling(boolean hasSampling) {
        this.hasSampling = hasSampling;
    }

    public boolean isHasPolicy() {
        return hasPolicy;
    }

    public void setHasPolicy(boolean hasPolicy) {
        this.hasPolicy = hasPolicy;
    }

    public boolean isHasSchedule() {
        return hasSchedule;
    }

    public void setHasSchedule(boolean hasSchedule) {
        this.hasSchedule = hasSchedule;
    }

    public boolean isHasValidFormat() {
        return hasValidFormat;
    }

    public void setHasValidFormat(boolean hasValidFormat) {
        this.hasValidFormat = hasValidFormat;
    }

    public boolean isHasMessageIdWithAction() {
        return hasMessageIdWithAction;
    }

    public void setHasMessageIdWithAction(boolean hasMessageIdWithAction) {
        this.hasMessageIdWithAction = hasMessageIdWithAction;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "isValid=" + isValid +
                ", issuesDetected=" + issuesDetected +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }
}
