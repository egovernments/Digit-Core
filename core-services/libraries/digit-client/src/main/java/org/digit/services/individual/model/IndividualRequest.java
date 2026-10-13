package org.digit.services.individual.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualRequest {
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="dateOfBirth")
    private String dateOfBirth;
    @JsonProperty(value="gender")
    private String gender;
    @JsonProperty(value="mobileNumber")
    private String mobileNumber;
    @JsonProperty(value="email")
    private String email;
    @JsonProperty(value="address")
    private Address address;
    @JsonProperty(value="documents")
    private List<Document> documents;
    @JsonProperty(value="additionalFields")
    private Map<String, Object> additionalFields;
}