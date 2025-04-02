package org.egov.encryption.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonFieldUtil {

    private final ObjectMapper objectMapper;

    public JsonFieldUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Recursively extracts all leaf fields (path and value) from the input.
     *
     * @param input The input object (Map, List, JsonNode, or primitive).
     * @return A list of Field objects representing each leaf.
     */
    public List<Field> extractFields(Object input) {
        List<Field> fields = new ArrayList<>();
        extractFieldsRecursive(input, new ArrayList<>(), fields);
        return fields;
    }

    private void extractFieldsRecursive(Object input, List<String> currentPath, List<Field> fields) {
        if (input == null) {
            fields.add(new Field(String.join(".", currentPath), "", false));
        } else if (input instanceof JsonNode) {
            JsonNode node = (JsonNode) input;
            if (node.isArray()) {
                int idx = 0;
                for (JsonNode element : node) {
                    currentPath.add(String.valueOf(idx));
                    extractFieldsRecursive(element, currentPath, fields);
                    currentPath.remove(currentPath.size() - 1);
                    idx++;
                }
            } else if (node.isObject()) {
                node.fields().forEachRemaining(entry -> {
                    currentPath.add(entry.getKey());
                    extractFieldsRecursive(entry.getValue(), currentPath, fields);
                    currentPath.remove(currentPath.size() - 1);
                });
            } else {
                fields.add(new Field(String.join(".", currentPath), node.asText(), false));
            }
        } else if (input instanceof java.util.Map<?, ?>) {
            ((java.util.Map<?, ?>) input).forEach((key, value) -> {
                currentPath.add(key.toString());
                extractFieldsRecursive(value, currentPath, fields);
                currentPath.remove(currentPath.size() - 1);
            });
        } else if (input instanceof java.util.Collection<?>) {
            int idx = 0;
            for (Object element : (java.util.Collection<?>) input) {
                currentPath.add(String.valueOf(idx));
                extractFieldsRecursive(element, currentPath, fields);
                currentPath.remove(currentPath.size() - 1);
                idx++;
            }
        } else if (input.getClass().isArray()) {
            int length = Array.getLength(input);
            for (int i = 0; i < length; i++) {
                currentPath.add(String.valueOf(i));
                extractFieldsRecursive(Array.get(input, i), currentPath, fields);
                currentPath.remove(currentPath.size() - 1);
            }
        } else {
            fields.add(new Field(String.join(".", currentPath), input.toString(), false));
        }
    }

    /**
     * Merges the values from the fieldsMap back into the original object,
     * based on the field paths.
     *
     * @param original  The original object.
     * @param fieldsMap Map of field path to new value.
     * @return The object with updated fields.
     */
    public Object mergeEncryptedFields(Object original, java.util.Map<String, String> fieldsMap) {
        for (java.util.Map.Entry<String, String> entry : fieldsMap.entrySet()) {
            String path = entry.getKey();
            String newValue = entry.getValue();
            String[] parts = path.split("\\.");
            updateValue(original, parts, 0, newValue);
        }
        return original;
    }

    @SuppressWarnings("unchecked")
    private void updateValue(Object current, String[] pathParts, int index, String newValue) {
        if (index >= pathParts.length) {
            return;
        }
        if (index == pathParts.length - 1) {
            if (current instanceof java.util.Map) {
                ((java.util.Map<String, Object>) current).put(pathParts[index], newValue);
            } else if (current instanceof java.util.List) {
                int idx = Integer.parseInt(pathParts[index]);
                ((java.util.List<Object>) current).set(idx, newValue);
            }
            return;
        }
        Object next = null;
        if (current instanceof java.util.Map) {
            next = ((java.util.Map<String, Object>) current).get(pathParts[index]);
        } else if (current instanceof java.util.List) {
            int idx = Integer.parseInt(pathParts[index]);
            next = ((java.util.List<Object>) current).get(idx);
        }
        if (next != null) {
            updateValue(next, pathParts, index + 1, newValue);
        }
    }

    /**
     * Creates a deep copy of the given input object using Jackson.
     *
     * @param input The input object.
     * @return A deep copy of the input.
     * @throws Exception if an error occurs during copy.
     */
    public Object deepCopy(Object input) throws Exception {
        String json = objectMapper.writeValueAsString(input);
        return objectMapper.readValue(json, Object.class);
    }

    // -------------------------------------------------------------------------
    // Field inner class representing a leaf field.
    // -------------------------------------------------------------------------
    public static class Field {
        private final String path;
        private final String value;
        private final boolean isMobile;  // Informational flag; not used for special processing.

        public Field(String path, String value, boolean isMobile) {
            this.path = path;
            this.value = value;
            this.isMobile = isMobile;
        }

        public String getPath() {
            return path;
        }

        public String getValue() {
            return value;
        }

        public boolean isMobile() {
            return isMobile;
        }

        @Override
        public String toString() {
            return "Field[path=" + path + ", value=" + value + ", isMobile=" + isMobile + "]";
        }
    }
}
