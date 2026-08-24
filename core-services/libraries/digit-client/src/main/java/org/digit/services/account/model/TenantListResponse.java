package org.digit.services.account.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A page of tenants. */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantListResponse {
    @JsonProperty("totalCount")
    private int totalCount;
    @JsonProperty("page")
    private int page;
    @JsonProperty("size")
    private int size;
    @JsonProperty("hasMore")
    private boolean hasMore;
    @JsonProperty("tenants")
    private List<Tenant> tenants;
}
