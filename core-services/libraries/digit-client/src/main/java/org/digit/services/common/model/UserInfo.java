package org.digit.services.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Who a canonical (envelope) request is acting as.
 *
 * <p>Deliberately exposes {@code uuid} and no bare {@code id}. Registry identifies the acting user by
 * scanning the raw request body for the first key matching {@code uuid|userId|id|userName|username},
 * so a field called {@code id} here would be a second candidate competing with the real one.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"uuid", "userName", "tenantId", "type", "roles"})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("type")
    private String type;
    @JsonProperty("roles")
    private List<String> roles;
}
