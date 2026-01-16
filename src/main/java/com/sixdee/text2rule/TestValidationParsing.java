package com.sixdee.text2rule;

import com.sixdee.text2rule.dto.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestValidationParsing {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Test with camelCase (what LLM sends)
        String camelCaseJson = "{\"isValid\":true,\"issuesDetected\":[],\"suggestion\":\"\",\"inputText\":\"test\",\"hasCondition\":true,\"hasAction\":true,\"hasBonus\":false,\"hasSampling\":false,\"hasPolicy\":false,\"hasSchedule\":true,\"hasValidFormat\":true,\"hasMessageIdWithAction\":true}";

        ValidationResult result = mapper.readValue(camelCaseJson, ValidationResult.class);

        System.out.println("Parsed isValid: " + result.isValid());
        System.out.println("Parsed issuesDetected: " + result.getIssuesDetected());
        System.out.println("Parsed hasCondition: " + result.isHasCondition());
        System.out.println("Parsed hasAction: " + result.isHasAction());

        if (result.isValid()) {
            System.out.println("SUCCESS: Validation parsing works correctly!");
        } else {
            System.out.println("FAILURE: isValid should be true but is false");
        }
    }
}
