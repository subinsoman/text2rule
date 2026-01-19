package com.sixdee.text2rule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ActionExtractionResult {
    @JsonProperty("ActionName")
    private String actionName;

    @JsonProperty("Channel")
    private String channel;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Message_ID")
    private String messageId;

    @JsonProperty("UserType")
    private String userType;

    @JsonProperty("TriggerType")
    private String triggerType;

    @JsonProperty("AdditionalAttributes")
    private Map<String, String> additionalAttributes;

    @JsonProperty("Conditions")
    private List<ActionCondition> conditions;

    // Getters and Setters
    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Map<String, String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, String> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public List<ActionCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ActionCondition> conditions) {
        this.conditions = conditions;
    }

    public static class ActionCondition {
        @JsonProperty("Condition")
        private String condition;

        @JsonProperty("Benefits")
        private List<Benefit> benefits;

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public List<Benefit> getBenefits() {
            return benefits;
        }

        public void setBenefits(List<Benefit> benefits) {
            this.benefits = benefits;
        }
    }

    public static class Benefit {
        @JsonProperty("Product")
        private String product;

        @JsonProperty("PRODUCT_TYPE")
        private String productType;

        @JsonProperty("PRODUCT_GROUP")
        private String productGroup;

        @JsonProperty("Validity")
        private String validity;

        @JsonProperty("Bonus")
        private String bonus;

        @JsonProperty("Discount")
        private String discount;

        @JsonProperty("OTT")
        private String ott;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getProductType() {
            return productType;
        }

        public void setProductType(String productType) {
            this.productType = productType;
        }

        public String getProductGroup() {
            return productGroup;
        }

        public void setProductGroup(String productGroup) {
            this.productGroup = productGroup;
        }

        public String getValidity() {
            return validity;
        }

        public void setValidity(String validity) {
            this.validity = validity;
        }

        public String getBonus() {
            return bonus;
        }

        public void setBonus(String bonus) {
            this.bonus = bonus;
        }

        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }

        public String getOtt() {
            return ott;
        }

        public void setOtt(String ott) {
            this.ott = ott;
        }
    }
}
