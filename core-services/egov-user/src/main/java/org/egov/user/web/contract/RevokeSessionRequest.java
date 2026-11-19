package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.egov.common.contract.request.RequestInfo;
import org.egov.user.config.UserServiceConstants;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * "userUuid" accepts a string (single revoke) or a list of strings (bulk revoke).
 */
@Getter
@Setter
@ToString
public class RevokeSessionRequest {

    private static final int MAX_USER_UUID_LENGTH = 64;

    public static final int MAX_BULK_REVOKE_SIZE = 100;

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    // No @NotNull: null when "userUuid" is sent as a list. Presence is checked in isUserUuidValid().
    @Size(max = 64)
    @JsonProperty("userUuid")
    private String userUuid;

    // Set when "userUuid" is a list.
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private List<String> userUuidList;

    @NotNull(message = "tenantId is mandatory")
    @Pattern(regexp = UserServiceConstants.PATTERN_TENANT)
    @Size(max = 256)
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("userUuid")
    public void setUserUuid(Object value) {
        userUuid = null;
        userUuidList = null;
        if (value == null || value instanceof String) {
            userUuid = (String) value;
        } else if (value instanceof Collection) {
            userUuidList = new ArrayList<>();
            for (Object entry : (Collection<?>) value) {
                if (entry != null && !(entry instanceof String)) {
                    throw new IllegalArgumentException("userUuid list entries must be strings");
                }
                userUuidList.add((String) entry);
            }
        } else {
            throw new IllegalArgumentException("userUuid must be a string or a list of strings");
        }
    }

    /** True when "userUuid" was sent as a list (bulk revoke); false for the original single-string form. */
    @JsonIgnore
    public boolean isBulkRequest() {
        return userUuidList != null;
    }

    /** All requested user UUIDs (a single string becomes a one-item list), duplicates removed, in request order. */
    public List<String> getUserUuidList() {
        if (userUuid != null) {
            return Collections.singletonList(userUuid);
        }
        return userUuidList == null ? null : new ArrayList<>(new LinkedHashSet<>(userUuidList));
    }

    // Hibernate Validator 5 (Spring Boot 1.5) can't validate list entries via annotations.
    @AssertTrue(message = "userUuid must be a non-blank string or a non-empty list of non-blank strings")
    public boolean isUserUuidValid() {
        if (userUuid != null) {
            return !userUuid.trim().isEmpty();
        }
        if (userUuidList == null || userUuidList.isEmpty()) {
            return false;
        }
        for (String uuid : userUuidList) {
            if (uuid == null || uuid.trim().isEmpty() || uuid.length() > MAX_USER_UUID_LENGTH) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "userUuid list cannot contain more than " + MAX_BULK_REVOKE_SIZE + " entries")
    public boolean isUserUuidListWithinLimit() {
        return userUuidList == null || userUuidList.size() <= MAX_BULK_REVOKE_SIZE;
    }
}
