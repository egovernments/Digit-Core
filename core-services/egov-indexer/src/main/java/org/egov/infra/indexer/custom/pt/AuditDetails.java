package org.egov.infra.indexer.custom.pt;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * Collection of audit related fields used by most models
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-11T14:12:44.497+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AuditDetails   {

        @Size(max=64)
        @JsonProperty("createdBy")
        private String createdBy;

        @Size(max=64)
        @JsonProperty("lastModifiedBy")
        private String lastModifiedBy;

        @JsonProperty("createdTime")
        private Long createdTime;

        @JsonProperty("lastModifiedTime")
        private Long lastModifiedTime;


}

