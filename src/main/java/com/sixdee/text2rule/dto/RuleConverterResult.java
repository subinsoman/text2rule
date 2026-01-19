package com.sixdee.text2rule.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConverterResult implements Serializable {
    @JsonProperty("segments")
    private List<String> segments;

    @JsonProperty("actions")
    private String actions;

    @JsonProperty("input_text")
    private String inputText;

    @JsonProperty("sampling")
    private String sampling;

    @JsonProperty("policy")
    private String policy;

    @JsonProperty("schedule")
    private String schedule;

    public List<String> getSegments() {
        return segments;
    }

    public void setSegments(List<String> segments) {
        this.segments = segments;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getSampling() {
        return sampling;
    }

    public void setSampling(String sampling) {
        this.sampling = sampling;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return "RuleConverterResult{" +
                "segments=" + segments +
                ", actions='" + actions + '\'' +
                ", inputText='" + inputText + '\'' +
                ", sampling='" + sampling + '\'' +
                ", policy='" + policy + '\'' +
                ", schedule='" + schedule + '\'' +
                '}';
    }
}
