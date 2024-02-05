package org.egov;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Resources {
    public String getFileContents(String fileName) {
        try {
            /*return IOUtils.toString(this.getClass().getClassLoader()
                            .getResourceAsStream(fileName), "UTF-8")
                    .replace(" ", "").replace("\n", "");*/
            String content;
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
                if (stream == null) {
                    throw new IllegalArgumentException("File not found: " + fileName);
                }
                content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            return content.replace(" ", "").replace("\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
