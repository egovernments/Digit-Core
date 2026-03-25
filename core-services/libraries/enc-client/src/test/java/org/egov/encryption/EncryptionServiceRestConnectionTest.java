package org.egov.encryption;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.web.reactive.function.client.WebClient;

public class EncryptionServiceRestConnectionTest {

    @Mock
    private EncryptionServiceRestConnection encryptionServiceRestConnection;

    private ObjectMapper mapper;

    @BeforeEach
    public void initialize() {
        encryptionServiceRestConnection = new EncryptionServiceRestConnection(WebClient.builder());
        mapper = new ObjectMapper(new JsonFactory());
    }

}
