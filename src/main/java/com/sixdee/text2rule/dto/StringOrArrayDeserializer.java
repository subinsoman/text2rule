package com.sixdee.text2rule.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer that accepts either a String or an Array of Strings.
 * If an array is provided, joins all elements with ", otherwise " separator.
 */
public class StringOrArrayDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();

        if (token == JsonToken.VALUE_STRING) {
            // Already a string, return as-is
            return p.getText();
        } else if (token == JsonToken.START_ARRAY) {
            // It's an array, read all elements and join them
            List<String> elements = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                elements.add(p.getText());
            }

            if (elements.isEmpty()) {
                return "";
            } else if (elements.size() == 1) {
                return elements.get(0);
            } else {
                // Join multiple statements with ", otherwise " to maintain logical flow
                return String.join(", otherwise ", elements);
            }
        }

        // Fallback
        return null;
    }
}
