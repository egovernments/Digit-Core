package org.egov.encryption.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JSONBrowseUtilTest {

    ObjectMapper mapper;

    @BeforeEach
    public void init() {
        mapper = new ObjectMapper(new JsonFactory());
    }

    @Test
    public void test() throws IOException {
        JsonNode jsonNode = mapper.readTree("{\"asd\" : \"qwe\"}");
        jsonNode = JSONBrowseUtil.mapValues(jsonNode, v -> ((String) v).length());

        JsonNode expectedNode = mapper.readTree("{\"asd\" : \"3\"}");

        assertEquals(expectedNode, jsonNode);
    }

}