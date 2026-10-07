package org.egov.user.domain.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.exception.sso.IdpPersistenceException;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.UserIdpDetails;
import org.egov.user.domain.model.UserIdpLink;
import org.egov.user.persistence.repository.UserIdpDetailsRepository;
import org.egov.user.persistence.repository.UserIdpLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing SSO user persistence operations.
 * 
 * <p>This service provides atomic operations for persisting user and IDP details during SSO authentication
 * flows.
 * 
 * <p>The service handles two main persistence scenarios:
 * <ol>
 *   <li>User creation or role changes requiring both user and IDP details updates</li>
 *   <li>Existing user login requiring only IDP session/MFA details updates</li>
 * </ol>
 * 
 * <p>All operations are transactional and include comprehensive input validation and error handling
 * to maintain data integrity and provide meaningful error messages for SSO integration scenarios.
 * 
 * @see org.egov.user.persistence.repository.UserIdpDetailsRepository
 */
@Service
public class SsoUserPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(SsoUserPersistenceService.class);

    private final UserService userService;
    private final UserIdpDetailsRepository userIdpDetailsRepository;
    private final UserIdpLinkRepository userIdpLinkRepository;

    public SsoUserPersistenceService(UserService userService,
                                     UserIdpDetailsRepository userIdpDetailsRepository,
                                     UserIdpLinkRepository userIdpLinkRepository) {
        this.userService = userService;
        this.userIdpDetailsRepository = userIdpDetailsRepository;
        this.userIdpLinkRepository = userIdpLinkRepository;
    }

    /**
     * Updates user record and upserts IDP details in a single atomic transaction.
     * 
     * <p>This method is used when both user information and IDP details must be persisted together,
     * typically during SSO login with role changes or new user creation scenarios.
     * 
     * <p>The transaction ensures that either both the user update and IDP details upsert succeed,
     * or neither does, maintaining data consistency.
     * 
     * @param user the user domain object to update (must contain valid user data)
     * @param idpDetails the IDP details to persist (tokenId, expiration, MFA data)
     * @param tenantId the tenant identifier for schema routing and data isolation
     * @param requestInfo the request context containing user information for audit trails
     * @param link optional IdP link to insert in the same transaction; skipped when null
     * @return the updated user domain object with latest state
     * @throws IdpPersistenceException if required input parameters are null or invalid
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     */
    @Transactional
    public User updateUserAndUpsertIdpDetails(User user, UserIdpDetails idpDetails,
                                              String tenantId, RequestInfo requestInfo, UserIdpLink link) {
        validateIdpPersistenceInput(idpDetails, tenantId);
        User updatedUser = userService.updateWithoutOtpValidation(user, requestInfo);
        userIdpDetailsRepository.upsert(idpDetails, tenantId);
        if (link != null) {
            userIdpLinkRepository.insert(link);
        }
        return updatedUser;
    }

    /**
     * Upserts IDP details only within a transaction, without modifying user record.
     * 
     * <p>This method is used when the user record remains unchanged but IDP session and MFA
     * details must be updated, typically during existing user SSO login scenarios without
     * role changes.
     * 
     * @param idpDetails the IDP details to persist (tokenId, expiration, MFA data)
     * @param tenantId the tenant identifier for schema routing and data isolation
     * @throws IdpPersistenceException if required input parameters are null or invalid
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     */
    @Transactional
    public void upsertIdpDetailsOnly(UserIdpDetails idpDetails, String tenantId) {
        validateIdpPersistenceInput(idpDetails, tenantId);
        userIdpDetailsRepository.upsert(idpDetails, tenantId);
    }

    /**
     * Validates input parameters for IDP persistence operations.
     * 
     * <p>This method ensures that all required parameters are present and valid before
     * attempting database operations. It provides detailed error logging for troubleshooting
     * and throws specific exceptions for each validation failure scenario.
     * 
     * @param details the IDP details object to validate (must not be null)
     * @param tenantId the tenant identifier to validate (must not be null)
     * @throws IdpPersistenceException if any required parameter is null or invalid
     */
    private void validateIdpPersistenceInput(UserIdpDetails details, String tenantId) {
        if (details == null) {
            LOG.error("IDP details persistence aborted: details is null for tenantId={}", tenantId);
            throw IdpPersistenceException.invalidInput("details is null");
        }
        if (details.getId() == null) {
            LOG.error("IDP details persistence aborted: details.id is null for tenantId={}", tenantId);
            throw IdpPersistenceException.invalidInput("details.id is null");
        }
        if (tenantId == null) {
            LOG.error("IDP details persistence aborted: tenantId is null for userId={}", details.getId());
            throw IdpPersistenceException.invalidInput("tenantId is null");
        }
    }
}
