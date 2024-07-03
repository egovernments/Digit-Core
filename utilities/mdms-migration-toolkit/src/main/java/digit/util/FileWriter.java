package digit.util;

import static digit.constants.ErrorCodes.IO_ERROR_CODE;
import static digit.constants.ErrorCodes.IO_WRITE_ERROR_MESSAGE;
import static digit.constants.MDMSMigrationToolkitConstants.JSON_EXTENSION;

import java.io.File;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FileWriter {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${master.schema.files.dir}")
    private String masterSchemaFilesDirectory;

    public void writeJsonToFile(JsonNode content, String fileName) {
        try {
            objectMapper.writeValue(new File(masterSchemaFilesDirectory + fileName + JSON_EXTENSION), content);
        }catch (Exception e){
            throw new CustomException(IO_ERROR_CODE, IO_WRITE_ERROR_MESSAGE);
        }

    }

}
