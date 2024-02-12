package org.egov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Resources {
    public String getFileContents(String fileName) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String content = reader.lines().collect(Collectors.joining());
            return content.replaceAll("\\s*\\{\\s*", "{")
                    .replaceAll("\\s*\\}\\s*", "}")
                    .replaceAll("\\s*:\\s*", ":")
                    .replace("\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
