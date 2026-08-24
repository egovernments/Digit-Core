package org.digit.services.mdms;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import org.digit.util.DigitJson;
import org.digit.config.ApiProperties;
import org.digit.exception.DigitClientException;
import org.digit.util.DigitContextHolder;
import org.digit.util.DigitRequestContext;
import org.digit.util.HeaderStore;
import org.digit.services.mdms.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.digit.services.mdms.model.Mdms;
import org.digit.services.mdms.model.MdmsResponseV2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;

/**
 * Service client for MDMS API operations.
 * Provides methods to interact with the MDMS service.
 */
@Slf4j
@Getter
@Setter
public class MdmsClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    /**
     * Constructor for MdmsClient.
     *
     * @param restTemplate the RestTemplate for HTTP operations
     * @param apiProperties the API configuration properties
     */
    public MdmsClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Validates whether all provided unique identifiers exist for the given schema code.
     * Returns true if all unique identifiers are found, false if any are missing.
     * Tenant ID is automatically propagated via X-Tenant-ID header.
     *
     * @param schemaCode the schema code to validate against
     * @param uniqueIdentifiers the set of unique identifiers to validate
     * @return true if all unique identifiers are valid (found), false otherwise
     * @throws DigitClientException if request fails or input is invalid
     */
    public boolean isMdmsDataValid(String schemaCode, Set<String> uniqueIdentifiers) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (uniqueIdentifiers == null || uniqueIdentifiers.isEmpty()) {
            throw new DigitClientException("Unique identifiers set cannot be null or empty");
        }

        try {
            log.debug("Validating MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers);

            // Build URL with query parameters
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(apiProperties.getMdmsServiceUrl() + "/mdms-v2/v2")
                    .queryParam("schemaCode", schemaCode);

            // Add each unique identifier as a separate query parameter
            for (String uniqueIdentifier : uniqueIdentifiers) {
                uriBuilder.queryParam("uniqueIdentifiers", uniqueIdentifier);
            }

            String url = uriBuilder.toUriString();
            log.debug("MDMS validation URL: {}", url);

            HttpEntity<Void> entity = new HttpEntity<>(mdmsHeaders());

            ResponseEntity<MdmsResponseV2> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, MdmsResponseV2.class);

            List<Mdms> mdmsList = response.getBody() != null ? response.getBody().getMdms() : null;
            int foundCount = mdmsList != null ? mdmsList.size() : 0;

            boolean allValid = foundCount == uniqueIdentifiers.size();
            log.debug("MDMS validation result: {} ({} out of {} found)", 
                      allValid ? "valid" : "invalid", foundCount, uniqueIdentifiers.size());

            if (log.isDebugEnabled() && mdmsList != null) {
                log.debug("Found MDMS entries:");
                mdmsList.forEach(mdms -> log.debug("  - ID: {}, UniqueIdentifier: {}", 
                                                  mdms.getId(), mdms.getUniqueIdentifier()));
            }

            return allValid;

        } catch (Exception e) {
            log.error("Failed to validate MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers, e);
            throw DigitClientException.wrap("Failed to validate MDMS data", e);
        }
    }

    /**
     * Searches MDMS data by schema code and unique identifiers.
     * Tenant ID is automatically propagated via X-Tenant-ID header.
     *
     * @param schemaCode the schema code
     * @param uniqueIdentifiers the set of unique identifiers to search for
     * @return the list of Mdms objects
     * @throws DigitClientException if search fails
     */
    public List<Mdms> searchMdmsData(String schemaCode, Set<String> uniqueIdentifiers) {
        if (schemaCode == null || schemaCode.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (uniqueIdentifiers == null || uniqueIdentifiers.isEmpty()) {
            throw new DigitClientException("Unique identifiers set cannot be null or empty");
        }

        try {
            log.debug("Searching MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers);

            // Build URL with query parameters
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(apiProperties.getMdmsServiceUrl() + "/mdms-v2/v2")
                    .queryParam("schemaCode", schemaCode);

            // Add each unique identifier as a separate query parameter
            for (String uniqueIdentifier : uniqueIdentifiers) {
                uriBuilder.queryParam("uniqueIdentifiers", uniqueIdentifier);
            }

            String url = uriBuilder.toUriString();

            HttpEntity<Void> entity = new HttpEntity<>(mdmsHeaders());

            ResponseEntity<MdmsResponseV2> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, MdmsResponseV2.class);

            List<Mdms> mdmsList = response.getBody() != null ? response.getBody().getMdms() : null;
            log.debug("Successfully retrieved {} MDMS entries", mdmsList != null ? mdmsList.size() : 0);

            return mdmsList;

        } catch (Exception e) {
            log.error("Failed to search MDMS data for schemaCode: {}, uniqueIdentifiers: {} (tenant from X-Tenant-ID header)", 
                     schemaCode, uniqueIdentifiers, e);
            throw DigitClientException.wrap("Failed to search MDMS data", e);
        }
    }

    /**
     * Fetches a schema definition by code.
     *
     * <p>Returns the raw JSON. MDMS has no implementation in this repository — it is deployed from a
     * prebuilt image — and its published contract contradicts itself on this very payload (the
     * schema body is named {@code definition} in the client known to work against a live server and
     * {@code schema} in the spec). Typed models would have to guess, so the caller decides.
     *
     * @return the response body, or null when the schema does not exist
     */
    public JsonNode searchSchema(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        String url = UriComponentsBuilder
                .fromUriString(apiProperties.getMdmsServiceUrl() + "/mdms-v2/v1/schema")
                .queryParam("code", code)
                .toUriString();
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<Void>(mdmsHeaders()), JsonNode.class);
            return response.getBody();
        }
        catch (DigitClientException e) {
            if (e.getHttpStatus() != null && e.getHttpStatus().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Registers a schema definition.
     *
     * <p>Wrapped as {@code {"SchemaDefinition": {...}}}, and the JSON Schema body travels under
     * {@code definition}. That naming follows the client with evidence of working against a live
     * server; the published spec disagrees with itself on this field, so it is not the authority.
     *
     * @param definition the JSON Schema for records of this code
     */
    public JsonNode createSchema(String code, String description, JsonNode definition, boolean isActive) {
        if (code == null || code.trim().isEmpty()) {
            throw new DigitClientException("Schema code cannot be null or empty");
        }
        if (definition == null) {
            throw new DigitClientException("Schema definition cannot be null");
        }
        ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.put("code", code);
        if (description != null) {
            schema.put("description", description);
        }
        schema.set("definition", definition);
        schema.put("isActive", isActive);
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.set("SchemaDefinition", schema);

        String url = apiProperties.getMdmsServiceUrl() + "/mdms-v2/v1/schema";
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, mdmsHeaders()), JsonNode.class);
        return response.getBody();
    }

    /**
     * Writes a master-data record.
     *
     * <p>Wrapped as {@code {"Mdms": {...}}} and posted to the same path the search reads from, the
     * two being told apart by method.
     */
    public JsonNode createMasterData(Mdms mdms) {
        if (mdms == null || mdms.getSchemaCode() == null || mdms.getSchemaCode().trim().isEmpty()) {
            throw new DigitClientException("schemaCode is required to write master data");
        }
        if (mdms.getData() == null) {
            throw new DigitClientException("data is required to write master data");
        }
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.set("Mdms", DigitJson.shared().valueToTree(mdms));

        String url = apiProperties.getMdmsServiceUrl() + "/mdms-v2/v2";
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, mdmsHeaders()), JsonNode.class);
        return response.getBody();
    }

    /**
     * Headers for an MDMS call.
     *
     * <p>MDMS is the only DIGIT service that requires {@code X-Client-ID}, and no gateway or filter
     * synthesizes it — the caller owns it end to end. Resolved from an explicit request context when
     * one is set, otherwise from the inbound request, and failed fast here because the alternative is
     * an opaque missing-header error from a service whose source we don't ship.
     *
     * <p>Tenant and authorization are left to the header interceptor, which applies them to every
     * outbound call.
     */
    private HttpHeaders mdmsHeaders() {
        DigitRequestContext context = DigitContextHolder.get();
        String clientId = context != null ? context.getClientId() : HeaderStore.extractClientId();
        if (clientId == null || clientId.isBlank()) {
            throw new DigitClientException(
                    "MDMS requires a client id: set it on a DigitRequestContext, or send X-Client-Id "
                            + "on the inbound request so it can be propagated");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Client-ID", clientId);
        return headers;
    }
}
