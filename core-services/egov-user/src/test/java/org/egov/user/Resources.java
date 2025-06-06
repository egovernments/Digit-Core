package org.egov.user;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class Resources {
    public String getFileContents(String fileName) {
        try {
            return IOUtils.toString(this.getClass().getClassLoader()
                    .getResourceAsStream(fileName), "UTF-8")
                    .replace(" ", "").replace("\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONArray getFileJSONArrayContents(String fileName) {
        try {
            String json = IOUtils.toString(Resources.class.getClassLoader()
                            .getResourceAsStream(fileName), "UTF-8")
                    .replace(" ", "").replace("\n", "");

            // Parse JSON array using JsonPath
            Object result = JsonPath.read(json, "$");

            JSONArray jsonArray = new JSONArray();

            if (result instanceof java.util.List<?>) {
                jsonArray.addAll((java.util.List<?>) result);
            } else {
                throw new RuntimeException("Expected a List at root of JSON but found: " + result.getClass());
            }

            return jsonArray;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON array from " + fileName, e);
        }
    }
}
