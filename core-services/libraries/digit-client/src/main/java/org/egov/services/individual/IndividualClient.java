package org.egov.services.individual;

import org.egov.config.ApiProperties;
import org.egov.services.individual.model.Individual;
import org.egov.services.individual.model.IndividualSearchResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;
import lombok.Getter;

@Getter
public class IndividualClient {
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    public IndividualClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public Individual createIndividual(Individual individual) {
        String url = this.apiProperties.getIndividualServiceUrl() + "/individuals/v3/individuals";
        ResponseEntity<Individual> response = this.restTemplate.postForEntity(url, individual, Individual.class, new Object[0]);
        return response.getBody();
    }

    public Individual getIndividualById(String individualId) {
        String url = this.apiProperties.getIndividualServiceUrl() + "/individuals/v3/individuals/" + individualId;
        ResponseEntity<Individual> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Individual.class, new Object[0]);
        return response.getBody();
    }

    public IndividualSearchResponse searchIndividualsByName(String individualName) {
        return this.searchIndividualsByName(individualName, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public IndividualSearchResponse searchIndividualsByName(String individualName, Integer page, Integer size) {
        int finalPage = page != null && page > 0 ? page : DEFAULT_PAGE;
        int finalSize = size != null && size > 0 ? size : DEFAULT_SIZE;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.apiProperties.getIndividualServiceUrl() + "/individuals/v3/individuals")
                .queryParam("page", new Object[]{finalPage})
                .queryParam("size", new Object[]{finalSize});
        if (individualName != null && !individualName.trim().isEmpty()) {
            builder.queryParam("givenName", new Object[]{individualName});
        }
        ResponseEntity<IndividualSearchResponse> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), IndividualSearchResponse.class, new Object[0]);
        return response.getBody();
    }

    public IndividualSearchResponse searchAllIndividuals() {
        return this.searchIndividualsByName(null, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public IndividualSearchResponse searchAllIndividuals(Integer page, Integer size) {
        return this.searchIndividualsByName(null, page, size);
    }

    public boolean isIndividualExist(String individualId) {
        return this.isIndividualExistsById(individualId, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public boolean isIndividualExistsById(String individualId, Integer page, Integer size) {
        try {
            String url = UriComponentsBuilder.fromUriString(this.apiProperties.getIndividualServiceUrl() + "/individuals/v3/individuals/exists")
                    .queryParam("id", new Object[]{individualId})
                    .toUriString();
            ResponseEntity<Map<String, Boolean>> response = this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<Map<String, Boolean>>(){}, new Object[0]);
            Map<String, Boolean> body = response.getBody();
            return body != null && Boolean.TRUE.equals(body.get("exists"));
        }
        catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
