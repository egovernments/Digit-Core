package digit.util;

import static digit.constants.ErrorCodes.IO_ERROR_CODE;
import static digit.constants.ErrorCodes.IO_READ_ERROR_MESSAGE;
import static digit.constants.MDMSMigrationToolkitConstants.JSON_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FileReader {

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, JsonNode> readFiles(String directoryPath){
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();

        Map<String, JsonNode> schemaCodeVsSchemaDefinitionMap = new HashMap<>();

        Arrays.stream(listOfFiles).forEach(file -> {
            String schemaCode = file.getName().replace(JSON_EXTENSION, "");
            JsonNode schemaDefinition = null;
            try {
                schemaDefinition = objectMapper.readValue(file, JsonNode.class);
            } catch (IOException e) {
                throw new CustomException(IO_ERROR_CODE, IO_READ_ERROR_MESSAGE + file.getName());
            }
            schemaCodeVsSchemaDefinitionMap.put(schemaCode, schemaDefinition);
        });

        return schemaCodeVsSchemaDefinitionMap;

    }
}
