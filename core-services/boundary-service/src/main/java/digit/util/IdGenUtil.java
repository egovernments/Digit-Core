package digit.util;

import digit.config.ApplicationProperties;
import digit.errors.ErrorCodes;
import digit.web.models.IdGenerationRequest;
import digit.web.models.IdRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.idgen.IdGenerationResponse;
import org.egov.common.contract.idgen.IdResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class IdGenUtil {

    // Everything before the trailing number, and the trailing number itself.
    private static final Pattern TRAILING_NUMBER = Pattern.compile("^(.*?)(\\d+)$");

    private final RestTemplate restTemplate;

    private final ApplicationProperties config;

    public IdGenUtil(RestTemplate restTemplate, ApplicationProperties config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * Generates count boundary identifierIds for the tenant from egov-idgen in a single call
     * @param requestInfo
     * @param tenantId
     * @param count
     * @return generated ids, exactly count of them
     */
    public List<String> getBoundaryIdentifierIds(RequestInfo requestInfo, String tenantId, int count) {

        IdRequest idRequest = IdRequest.builder()
                .idName(config.getBoundaryIdentifierIdGenName())
                .format(StringUtils.hasText(config.getBoundaryIdentifierIdGenFormat()) ? config.getBoundaryIdentifierIdGenFormat() : null)
                .tenantId(tenantId)
                .count(count)
                .build();

        IdGenerationRequest request = IdGenerationRequest.builder()
                .requestInfo(requestInfo)
                .idRequests(Collections.singletonList(idRequest))
                .build();

        IdGenerationResponse response;
        try {
            response = restTemplate.postForObject(config.getIdGenHost() + config.getIdGenPath(), request, IdGenerationResponse.class);
        } catch (RestClientException e) {
            log.error("egov-idgen call failed for idName {} and tenantId {}", idRequest.getIdName(), tenantId, e);
            throw new CustomException(ErrorCodes.IDGEN_ERROR_CODE, ErrorCodes.IDGEN_ERROR_MSG + e.getMessage());
        }

        List<IdResponse> idResponses = response == null ? null : response.getIdResponses();
        if (CollectionUtils.isEmpty(idResponses) || idResponses.size() != count) {
            throw new CustomException(ErrorCodes.IDGEN_ERROR_CODE, ErrorCodes.IDGEN_ERROR_MSG + "expected " + count + " ids but received "
                    + (idResponses == null ? 0 : idResponses.size()));
        }

        return idResponses.stream().map(IdResponse::getId).map(this::applySequenceMinDigits).collect(Collectors.toList());
    }

    /**
     * egov-idgen always zero-pads [SEQ_*] values to 6 digits. When a different minimum width is configured,
     * the trailing number of the id (the sequence, which must therefore be the last part of the format) is
     * re-padded to that width; it still grows beyond it once the width is used up, e.g. with 4: 0001 .. 9999, 10000.
     * @param id as returned by egov-idgen
     * @return id with the trailing sequence re-padded, or unchanged when no width is configured
     */
    private String applySequenceMinDigits(String id) {

        Integer minDigits = config.getBoundaryIdentifierSequenceMinDigits();
        if (minDigits == null || minDigits <= 0) {
            return id;
        }

        Matcher matcher = TRAILING_NUMBER.matcher(id);
        if (!matcher.matches()) {
            throw new CustomException(ErrorCodes.IDGEN_ERROR_CODE, ErrorCodes.IDGEN_ERROR_MSG + "id " + id
                    + " does not end with the sequence number, so it cannot be padded to " + minDigits + " digits");
        }
        return matcher.group(1) + String.format("%0" + minDigits + "d", Long.parseLong(matcher.group(2)));
    }
}
