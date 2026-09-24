package org.egov.user.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.UserSearchCriteria;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.utils.EncryptionDecryptionUtil;
import org.egov.user.persistence.repository.UserRepository;
import org.egov.user.persistence.repository.UserTenantMappingRepository;
import org.egov.user.web.contract.TenantMappingUpsertResponse.Result;
import org.egov.user.web.contract.TenantMappingUpsertResponse.Status;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates the tenant-mapping rows of users that already exist.
 *
 * <p>A mapping row is written when a user is created, so users created before that
 * behaviour existed have none and never show up in the shared-login tenant lookup.
 * Exchanging into a named tenant does not use the mapping, so this only affects
 * {@code POST /oauth/tenants}.</p>
 */
@Slf4j
@Service
public class UserTenantMappingService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserTenantMappingRepository userTenantMappingRepository;
    private final EncryptionDecryptionUtil encryptionDecryptionUtil;

    public UserTenantMappingService(UserService userService, UserRepository userRepository,
            UserTenantMappingRepository userTenantMappingRepository,
            EncryptionDecryptionUtil encryptionDecryptionUtil) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userTenantMappingRepository = userTenantMappingRepository;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
    }

    public List<Result> upsertMappings(String tenantId, UserType userType, List<String> userNames,
            List<Long> userIds, RequestInfo requestInfo) {
        UserType type = userType != null ? userType : UserType.EMPLOYEE;
        List<Result> results = new ArrayList<>();

        if (!CollectionUtils.isEmpty(userNames)) {
            for (String userName : userNames) {
                results.add(byUserName(userName, tenantId, type));
            }
        }
        if (!CollectionUtils.isEmpty(userIds)) {
            for (Long userId : userIds) {
                results.add(byUserId(userId, tenantId, type, requestInfo));
            }
        }
        return results;
    }

    private Result byUserName(String userName, String tenantId, UserType type) {
        User user;
        try {
            user = userService.getUniqueUser(userName, tenantId, type);
        } catch (UserNotFoundException e) {
            log.info("No {} named {} in tenant {}, nothing to map", type, userName, tenantId);
            return Result.builder().identifier(userName).status(Status.USER_NOT_FOUND).build();
        }
        // the looked-up user is still encrypted; the caller gave us the plaintext username
        return upsert(userName, user, tenantId, type, userName);
    }

    private Result byUserId(Long userId, String tenantId, UserType type, RequestInfo requestInfo) {
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .id(Collections.singletonList(userId))
                .tenantId(tenantId)
                .type(type)
                .build();
        List<User> users = userRepository.findAll(criteria);
        if (users.isEmpty()) {
            log.info("No {} with id {} in tenant {}, nothing to map", type, userId, tenantId);
            return Result.builder().identifier(String.valueOf(userId)).status(Status.USER_NOT_FOUND).build();
        }
        User user = users.get(0);
        // no plaintext username was supplied, so decrypt the row to derive the mapping key
        User decrypted = encryptionDecryptionUtil.decryptObject(user, "UserSelf", User.class, requestInfo);
        return upsert(String.valueOf(userId), user, tenantId, type, decrypted.getUsername());
    }

    private Result upsert(String identifier, User user, String tenantId, UserType type, String plainUserName) {
        String mappingKey = encryptionDecryptionUtil.tenantMappingKey(plainUserName);
        if (mappingKey == null) {
            log.warn("No mapping key derived for {} in tenant {}; is auth.oidc.shared-login.tenant-id set?",
                    identifier, tenantId);
            return Result.builder().identifier(identifier).userId(user.getId())
                    .status(Status.USER_NOT_FOUND).build();
        }
        boolean existed = userTenantMappingRepository.exists(user.getId(), type, tenantId);
        userTenantMappingRepository.upsert(user.getId(), type, tenantId, mappingKey, user.getUuid(),
                Boolean.TRUE.equals(user.getActive()));
        log.info("Tenant mapping {} for user {} in tenant {}", existed ? "refreshed" : "created", user.getId(),
                tenantId);
        return Result.builder().identifier(identifier).userId(user.getId())
                .status(existed ? Status.ALREADY_PRESENT : Status.CREATED).build();
    }
}
