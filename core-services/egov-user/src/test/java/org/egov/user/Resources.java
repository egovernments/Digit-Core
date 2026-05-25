package org.egov.user;

import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

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
                                           .getResourceAsStream(fileName), "UTF-8");

            ObjectMapper mapper = new ObjectMapper();
            List<Object> list = mapper.readValue(json, new TypeReference<List<Object>>() {});

            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(list);
            return jsonArray;


        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON array from " + fileName, e);
        }
    }
}
