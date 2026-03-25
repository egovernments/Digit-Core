package org.egov.test;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
@Disabled
public class Resources {

    public String getFileContents(String fileName) {
        try {
            return IOUtils.toString(this.getClass().getClassLoader()
                    .getResourceAsStream(fileName), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

