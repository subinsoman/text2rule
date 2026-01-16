package com.sixdee.text2rule.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.langchain4j.model.output.structured.Description;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DecompositionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("normal_statements")
    @JsonAlias({ "normalStatements" })
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    @Description("A complete, standalone rule statement that includes conditions, actions, bonus, policy, and sampling logic. Each statement must be logically isolated and self-contained.")
    private String normalStatements;

    @JsonProperty("schedule")
    @JsonAlias({ "schedule" })
    @Description("A single, separate scheduling instruction for when the campaign should run, extracted independently from the input statement. Leave empty if no schedule is specified.")
    private String schedule;

    @JsonProperty("input_text")
    @JsonAlias({ "inputText" })
    @Description("The original input text.")
    private String inputText;

    // Getters and Setters
    public String getNormalStatements() {
        return normalStatements;
    }

    public void setNormalStatements(String normalStatements) {
        this.normalStatements = normalStatements;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    @Override
    public String toString() {
        return "DecompositionResult{" +
                "normalStatements='" + normalStatements + '\'' +
                ", schedule='" + schedule + '\'' +
                ", inputText='" + inputText + '\'' +
                '}';
    }
}
