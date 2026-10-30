package digit.util;

import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import org.egov.common.contract.idgen.IdGenerationResponse;
import org.egov.common.contract.idgen.IdResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdGenUtilTest {

    private RestTemplate restTemplate;
    private ApplicationProperties config;
    private IdGenUtil idGenUtil;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        config = new ApplicationProperties();
        config.setIdGenHost("http://idgen/");
        config.setIdGenPath("egov-idgen/id/_generate");
        config.setBoundaryIdentifierIdGenName("boundary.identifier");
        idGenUtil = new IdGenUtil(restTemplate, config);
    }

    private void idgenReturns(String... ids) {
        IdGenerationResponse response = IdGenerationResponse.builder()
                .idResponses(Arrays.stream(ids).map(id -> IdResponse.builder().id(id).build()).collect(Collectors.toList()))
                .build();
        when(restTemplate.postForObject(anyString(), any(), eq(IdGenerationResponse.class))).thenReturn(response);
    }

    @Test
    void repadsSequenceToConfiguredMinDigitsAndGrowsBeyondIt() {
        config.setBoundaryIdentifierSequenceMinDigits(4);
        idgenReturns("000001", "000123", "009999", "010000", "123456", "1000000");

        assertEquals(List.of("0001", "0123", "9999", "10000", "123456", "1000000"),
                idGenUtil.getBoundaryIdentifierIds(new RequestInfo(), "ba", 6));
    }

    @Test
    void keepsPrefixBeforeTheSequence() {
        config.setBoundaryIdentifierSequenceMinDigits(4);
        idgenReturns("BND-000042");

        assertEquals(List.of("BND-0042"), idGenUtil.getBoundaryIdentifierIds(new RequestInfo(), "ba", 1));
    }

    @Test
    void returnsIdsUnchangedWhenNoMinDigitsConfigured() {
        idgenReturns("000042");

        assertEquals(List.of("000042"), idGenUtil.getBoundaryIdentifierIds(new RequestInfo(), "ba", 1));
    }

    @Test
    void rejectsIdThatDoesNotEndWithTheSequence() {
        config.setBoundaryIdentifierSequenceMinDigits(4);
        idgenReturns("000042-BND");

        CustomException ex = assertThrows(CustomException.class, () -> idGenUtil.getBoundaryIdentifierIds(new RequestInfo(), "ba", 1));
        assertEquals(ErrorCodes.IDGEN_ERROR_CODE, ex.getCode());
    }

    @Test
    void rejectsWrongNumberOfIds() {
        idgenReturns("000001");

        CustomException ex = assertThrows(CustomException.class, () -> idGenUtil.getBoundaryIdentifierIds(new RequestInfo(), "ba", 2));
        assertEquals(ErrorCodes.IDGEN_ERROR_CODE, ex.getCode());
    }
}
