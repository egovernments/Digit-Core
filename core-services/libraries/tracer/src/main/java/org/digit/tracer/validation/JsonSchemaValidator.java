package org.digit.tracer.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.digit.tracer.model.CustomException;

/**
 * JSON schema validation utility mirroring the Go validation/validator.go.
 */
public class JsonSchemaValidator {

    private final ObjectMapper objectMapper;

    public JsonSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Validates that the input is well-formed JSON. Throws CustomException on failure.
     */
    public JsonNode validate(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new CustomException("INVALID_JSON", "Input is not valid JSON: " + ex.getMessage());
        }
    }

    /**
     * Returns true if the string is valid JSON, false otherwise.
     */
    public boolean isValid(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
